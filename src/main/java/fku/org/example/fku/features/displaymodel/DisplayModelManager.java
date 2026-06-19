package fku.org.example.fku.features.displaymodel;

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * 实体模型放置管理器（单例）
 *
 * 职责：
 * - 独立于 Screen 运行的状态机（ClientTickEvent 驱动）
 * - 生物蛋创建、服务端同步、useItemOn 放置
 * - 关闭 Screen 后仍可继续放置
 *
 * ★ 事件注册改为实例注册（不在类上用 @Mod.EventBusSubscriber），
 *   由 ClientSetup 显式调用 registerEventHandlers() 完成注册
 *
 * 状态机流程：
 *   IDLE → SYNC_EGG（同步蛋到客户端+服务端）→ WAIT_SYNC（等待同步生效）
 *   → PLACE_ENTITY（useItemOn 放置）→ WAIT_PLACE（等待生成）
 *   → RESTORE_ITEM（恢复原物品）→ INTER_DELAY（实体间间隔）
 *   → DONE 或 回到 SYNC_EGG
 *
 * 设计思想（矛盾论）：
 * - 主要矛盾 ①：客户端物品修改不能自动同步到服务端
 *   → 方案：单机直写 ServerPlayer.inventory；远程用 handleCreativeModeItemAdd
 * - 主要矛盾 ②：实体无 Rotation 标签时使用玩家朝向作为默认旋转
 *   → 方案：强制写入 Rotation:[0.0f, 0.0f]
 * - 主要矛盾 ③：Screen 关闭后状态机停止（类级事件注册可能失效）
 *   → 方案：改为实例级注册，从 ClientSetup 显式注册
 *
 * 参考：
 * - 用户提供的不会随机旋转的刷怪蛋 NBT 格式（带 display 物品名段）
 */
public class DisplayModelManager {

    private static final DisplayModelManager INSTANCE = new DisplayModelManager();
    private boolean eventsRegistered = false;

    private enum Phase {
        IDLE,          // 空闲
        SYNC_EGG,      // 同步生物蛋到客户端+服务端
        WAIT_SYNC,     // 等待同步生效
        PLACE_ENTITY,  // useItemOn 放置
        WAIT_PLACE,    // 等待放置生效
        RESTORE_ITEM,  // 恢复原物品
        INTER_DELAY,   // 实体间延迟
        DONE           // 完成
    }

    // ============ 状态 ============
    private Phase phase = Phase.IDLE;
    private int tickCounter = 0;

    // ============ 放置数据 ============
    private List<CompoundTag> passengers;
    private Vec3 offset;
    private BlockPos fixedPos;
    private int currentIndex = 0;
    private ItemStack originalItem;
    private int selectedSlot;

    // ============ 延迟参数（tick数） ============
    private int syncWaitTicks = 3;
    private int placeWaitTicks = 3;
    private int interDelayTicks = 20;

    // ============ 状态信息 ============
    private String statusMessage = "";
    private boolean running = false;
    private int totalCount = 0;
    private Runnable onStatusUpdate;

    private DisplayModelManager() {}

    public static DisplayModelManager getInstance() {
        return INSTANCE;
    }

    /**
     * 注册事件处理器（在 ClientSetup 中调用一次即可）
     */
    public static void registerEventHandlers() {
        if (!INSTANCE.eventsRegistered) {
            MinecraftForge.EVENT_BUS.register(INSTANCE);
            INSTANCE.eventsRegistered = true;
            Fku.LOGGER.info("[DisplayModel] 事件处理器已注册");
        }
    }

    /**
     * 设置状态更新回调（用于 Screen 刷新显示）
     */
    public void setOnStatusUpdate(Runnable callback) {
        this.onStatusUpdate = callback;
    }

    /**
     * 开始放置实体
     */
    public void start(List<CompoundTag> passengers, Vec3 offset, BlockPos playerPos,
                      double entitySpacing, int generationDelayMs, int placeDelayMs) {
        if (running) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (!player.isCreative()) {
            setStatus("§c需要创造模式");
            return;
        }

        this.passengers = passengers;
        this.offset = offset;
        this.fixedPos = playerPos;
        this.currentIndex = 0;
        this.totalCount = passengers.size();
        this.originalItem = player.getItemInHand(InteractionHand.MAIN_HAND).copy();
        this.selectedSlot = player.getInventory().selected;

        // ★ 直接使用传入的参数（不读 config 单例）
        this.syncWaitTicks = Math.max(0, placeDelayMs / 50);
        this.placeWaitTicks = Math.max(0, placeDelayMs / 50);
        this.interDelayTicks = Math.max(0, generationDelayMs / 50);
        this.tickCounter = 0;

        this.running = true;
        this.phase = Phase.SYNC_EGG;

        setStatus("§a开始放置实体，共 " + totalCount + " 个...");
        Fku.LOGGER.info("[DisplayModel] 开始放置，共{}个，位置={}，偏移={}，syncWait={}tick",
                totalCount, fixedPos, offset, syncWaitTicks);
    }

    /**
     * 中止当前放置并恢复物品
     */
    public void stop() {
        if (!running) return;
        doRestoreItem();
        running = false;
        phase = Phase.IDLE;
        setStatus("§c已中止");
        Fku.LOGGER.info("[DisplayModel] 放置已中止");
    }

    public boolean isRunning() { return running; }
    public int getCurrentIndex() { return currentIndex; }
    public int getTotalCount() { return totalCount; }
    public String getStatusMessage() { return statusMessage; }

    // ====================================================================
    //  onClientTick — 由 MinecraftForge.EVENT_BUS 实例级注册触发
    // ====================================================================
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (this.running) {
            this.tick();
        }
    }

    private void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            stop();
            return;
        }

        if (!player.isCreative()) {
            stop();
            setStatus("§c创造模式被取消");
            return;
        }

        switch (phase) {
            case SYNC_EGG -> {
                doSyncEgg();
                phase = Phase.WAIT_SYNC;
                tickCounter = 0;
            }
            case WAIT_SYNC -> {
                tickCounter++;
                if (tickCounter >= syncWaitTicks) {
                    phase = Phase.PLACE_ENTITY;
                    tickCounter = 0;
                }
            }
            case PLACE_ENTITY -> {
                doPlaceEntity();
                phase = Phase.WAIT_PLACE;
                tickCounter = 0;
            }
            case WAIT_PLACE -> {
                tickCounter++;
                if (tickCounter >= placeWaitTicks) {
                    doRestoreItem();
                    currentIndex++;
                    if (currentIndex >= totalCount) {
                        phase = Phase.DONE;
                        tickCounter = 0;
                    } else {
                        phase = Phase.INTER_DELAY;
                        tickCounter = 0;
                    }
                }
            }
            case INTER_DELAY -> {
                tickCounter++;
                if (tickCounter >= interDelayTicks) {
                    phase = Phase.SYNC_EGG;
                    tickCounter = 0;
                    setStatus("§a放置实体 " + (currentIndex + 1) + "/" + totalCount);
                }
            }
            case DONE -> {
                doRestoreItem();
                running = false;
                phase = Phase.IDLE;
                setStatus("§a放置完成！共生成 " + totalCount + " 个实体");
                Fku.LOGGER.info("[DisplayModel] 放置完成");
                fireStatusUpdate();
                return;
            }
        }

        fireStatusUpdate();
    }

    // ====================================================================
    //  doSyncEgg — 创建生物蛋并同步到服务端 + 客户端
    //
    //  ★ 采用用户提供的已验证 NBT 格式 ★
    //  {Count:1,id:"minecraft:bat_spawn_egg",tag:{Enchantments:[{id:"sharpness",lvl:1}],
    //   EntityTag:{...,Rotation:[90.0f,0.0f],...},
    //   display:{Name:'{"translate":"entity.minecraft.block_display"}'}}}
    // ====================================================================
    private void doSyncEgg() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || currentIndex >= totalCount) return;

        CompoundTag passengerTag = passengers.get(currentIndex).copy();
        ensureRotationTag(passengerTag);

        ItemStack spawnEgg = createSpawnEgg(passengerTag);

        // ★ 直接替换客户端热栏槽位，这是最可靠的客户端替换方式
        player.getInventory().items.set(selectedSlot, spawnEgg.copy());
        player.getInventory().setChanged();

        // 同步到服务端
        syncItemToServer(selectedSlot, spawnEgg.copy());

        // 设置客户端手部视觉
        player.setItemInHand(InteractionHand.MAIN_HAND, spawnEgg.copy());

        Fku.LOGGER.debug("[DisplayModel] 同步刷怪蛋到槽位 {}", selectedSlot);
    }

    // ====================================================================
    //  doPlaceEntity — useItemOn 放置
    //
    //  目标位置公式：
    //    targetX = fixedPos.x + 0.5 + offset.x
    //    targetY = fixedPos.y + offset.y + currentIndex * entitySpacing
    //    targetZ = fixedPos.z + 0.5 + offset.z
    //
    //  点击位置为 targetBlock（targetY-1 处的方块），方向 UP
    //
    //  设计思想（矛盾论）：
    //  - SYNC_EGG 阶段已同步到服务端（远程 10 tick 前），此处不再 re-sync
    //  - 仅确保客户端手部有刷怪蛋（用于视觉反馈）
    //  - 避免 useItemOn 包先于 sync 包到达服务端导致旧物品被使用
    // ====================================================================
    private void doPlaceEntity() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        double spacing = DisplayModelConfig.getInstance().entitySpacing;
        double targetX = fixedPos.getX() + 0.5 + offset.x;
        double targetY = fixedPos.getY() + offset.y + currentIndex * spacing;
        double targetZ = fixedPos.getZ() + 0.5 + offset.z;

        BlockPos targetBlock = BlockPos.containing(targetX, targetY - 1, targetZ);
        BlockHitResult hitResult = new BlockHitResult(
                new Vec3(targetX, targetY, targetZ),
                Direction.UP,
                targetBlock,
                false
        );

        // ★ 仅设置客户端手部视觉（服务端已在 SYNC_EGG 阶段同步，且已等待 WAIT_SYNC）
        CompoundTag passengerTag = passengers.get(currentIndex).copy();
        ensureRotationTag(passengerTag);
        ItemStack egg = createSpawnEgg(passengerTag);

        player.getInventory().items.set(selectedSlot, egg.copy());
        player.setItemInHand(InteractionHand.MAIN_HAND, egg.copy());
        // 单机场景下同步是即时的，不影响时序
        IntegratedServer server = mc.getSingleplayerServer();
        if (server != null) {
            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(player.getUUID());
            if (serverPlayer != null) {
                serverPlayer.getInventory().setItem(selectedSlot, egg.copy());
            }
        }

        try {
            Fku.LOGGER.debug("[DisplayModel] useItemOn target=({}, {}, {}) idx={}",
                    targetX, targetY, targetZ, currentIndex);
            InteractionResult result = mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hitResult);
            if (result == InteractionResult.FAIL) {
                Fku.LOGGER.warn("[DisplayModel] useItemOn 失败 (index={})", currentIndex);
            } else {
                Fku.LOGGER.info("[DisplayModel] useItemOn 成功 (index={})", currentIndex);
            }
            player.swing(InteractionHand.MAIN_HAND);
        } catch (Exception e) {
            Fku.LOGGER.error("[DisplayModel] useItemOn 异常: {}", e.getMessage(), e);
        }
    }

    // ====================================================================
    //  syncItemToServer — 将物品同步到服务端玩家热栏
    //
    //  策略：
    //   1. 单机/局域网（IntegratedServer）：直写 ServerPlayer.inventory
    //   2. 远程服务器：handleCreativeModeItemAdd(slotIndex)
    // ====================================================================
    private static void syncItemToServer(int slotIndex, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        IntegratedServer server = mc.getSingleplayerServer();
        if (server != null) {
            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (serverPlayer != null) {
                serverPlayer.getInventory().setItem(slotIndex, stack.copy());
                return;
            }
        }

        mc.gameMode.handleCreativeModeItemAdd(stack.copy(), slotIndex);
    }

    // ====================================================================
    //  doRestoreItem — 恢复玩家原物品
    // ====================================================================
    private void doRestoreItem() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || originalItem == null) return;

        player.getInventory().items.set(selectedSlot, originalItem.copy());
        player.setItemInHand(InteractionHand.MAIN_HAND, originalItem.copy());
        syncItemToServer(selectedSlot, originalItem.copy());
    }

    // ====================================================================
    //  工具方法
    // ====================================================================

    /**
     * 确保实体 NBT 包含 Rotation 标签
     * 仅当不存在时写入默认值 [0.0f, 0.0f]
     * 若已有 Rotation（如用户指令中指定），保留原值
     */
    private static void ensureRotationTag(CompoundTag tag) {
        if (!tag.contains("Rotation")) {
            ListTag rotation = new ListTag();
            rotation.add(FloatTag.valueOf(0.0f));
            rotation.add(FloatTag.valueOf(0.0f));
            tag.put("Rotation", rotation);
        }
    }

    /**
     * 创建刷怪蛋 ItemStack
     *
     * 格式对照用户已验证的 NBT：
     * {Count:1,id:"minecraft:bat_spawn_egg",
     *  tag:{Enchantments:[{id:"sharpness",lvl:1}],
     *       EntityTag:{...,Rotation:[90.0f,0.0f],...},
     *       display:{Name:'{"translate":"entity.minecraft.block_display"}'}}}
     */
    private static ItemStack createSpawnEgg(CompoundTag passengerTag) {
        ItemStack egg = new ItemStack(Items.BAT_SPAWN_EGG);
        CompoundTag eggTag = egg.getOrCreateTag();

        // 附魔发光效果
        CompoundTag enchantment = new CompoundTag();
        enchantment.putString("id", "minecraft:sharpness");
        enchantment.putInt("lvl", 1);
        ListTag enchantments = new ListTag();
        enchantments.add(enchantment);
        eggTag.put("Enchantments", enchantments);

        // EntityTag = 乘客完整 NBT
        eggTag.put("EntityTag", passengerTag);

        // ★ display 段：设置物品显示名（与用户提供的格式一致）
        CompoundTag displayTag = new CompoundTag();
        displayTag.putString("Name", "{\"translate\":\"entity.minecraft.block_display\"}");
        eggTag.put("display", displayTag);

        return egg;
    }

    // ====================================================================
    //  状态更新
    // ====================================================================
    private void setStatus(String msg) {
        this.statusMessage = msg;
    }

    private void fireStatusUpdate() {
        if (onStatusUpdate != null) {
            onStatusUpdate.run();
        }
    }
}