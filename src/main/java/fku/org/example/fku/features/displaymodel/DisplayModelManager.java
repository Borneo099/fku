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
 * - 支持暴发模式：延迟设为 0 时，单个 tick 内可放置多个实体
 *
 * ★ 事件注册改为实例注册（不在类上用 @Mod.EventBusSubscriber），
 *   由 ClientSetup 显式调用 registerEventHandlers() 完成注册
 *
 * 状态机流程：
 *   SYNC_EGG → [WAIT_SYNC(可选)] → PLACE_ENTITY → [INTER_DELAY(可选)] → 回到 SYNC_EGG
 *   全部放完 → RESTORE_ITEM（一次性返还物品）
 *
 * 优化说明：
 * - 去掉 WAIT_PLACE 阶段：放完无需等待返还，物品在 RESTORE_ITEM 统一返还
 * - 单 tick 循环：当延迟 == 0 时，一个 tick 可处理多个实体（上限 10）
 */
public class DisplayModelManager {

    private static final DisplayModelManager INSTANCE = new DisplayModelManager();
    private boolean eventsRegistered = false;

    private enum Phase {
        IDLE,
        SYNC_EGG,
        WAIT_SYNC,
        PLACE_ENTITY,
        INTER_DELAY,
        RESTORE_ITEM
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
    private int syncWaitTicks = 0;
    private int interDelayTicks = 0;

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
        //   整数除法：1ms → 0tick（暴发模式），50ms → 1tick
        this.syncWaitTicks = Math.max(0, placeDelayMs / 50);
        this.interDelayTicks = Math.max(0, generationDelayMs / 50);
        this.tickCounter = 0;

        this.running = true;
        this.phase = Phase.SYNC_EGG;

        setStatus("§a开始放置实体，共 " + totalCount + " 个...");
        Fku.LOGGER.info("[DisplayModel] 开始放置，共{}个，syncWait={}tick, interDelay={}tick",
                totalCount, syncWaitTicks, interDelayTicks);
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

    // ====================================================================
    //  tick — 核心状态机
    //
    //  当延迟 == 0 时，单个 tick 内循环处理多个实体（上限 10），
    //  实现近似"瞬间放置"的效果。
    // ====================================================================
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

        int processedThisTick = 0;
        final int MAX_PER_TICK = 10;

        while (running && processedThisTick < MAX_PER_TICK) {
            switch (phase) {
                case SYNC_EGG -> {
                    doSyncEgg();
                    if (syncWaitTicks <= 0) {
                        phase = Phase.PLACE_ENTITY;      // 0tick → 直接放置
                    } else {
                        phase = Phase.WAIT_SYNC;
                        tickCounter = 0;
                        fireStatusUpdate();
                        return;                           // 等待一小段时间
                    }
                }

                case WAIT_SYNC -> {
                    tickCounter++;
                    if (tickCounter >= syncWaitTicks) {
                        phase = Phase.PLACE_ENTITY;
                        tickCounter = 0;
                    }
                    fireStatusUpdate();
                    return;                               // WAIT 阶段至少 1 tick
                }

                case PLACE_ENTITY -> {
                    doPlaceEntity();
                    currentIndex++;
                    processedThisTick++;

                    if (currentIndex >= totalCount) {
                        phase = Phase.RESTORE_ITEM;       // 全部放完 → 统一返还
                    } else if (interDelayTicks <= 0) {
                        phase = Phase.SYNC_EGG;           // 0tick → 继续下一个
                        setStatus("§a放置实体 " + (currentIndex + 1) + "/" + totalCount);
                    } else {
                        phase = Phase.INTER_DELAY;        // 需要等待
                        tickCounter = 0;
                        setStatus("§a放置实体 " + (currentIndex + 1) + "/" + totalCount);
                        fireStatusUpdate();
                        return;
                    }
                }

                case INTER_DELAY -> {
                    tickCounter++;
                    if (tickCounter >= interDelayTicks) {
                        phase = Phase.SYNC_EGG;
                        tickCounter = 0;
                    }
                    fireStatusUpdate();
                    return;
                }

                case RESTORE_ITEM -> {
                    doRestoreItem();
                    running = false;
                    phase = Phase.IDLE;
                    setStatus("§a放置完成！共生成 " + totalCount + " 个实体");
                    Fku.LOGGER.info("[DisplayModel] 放置完成");
                    fireStatusUpdate();
                    return;
                }

                default -> {
                    // IDLE — 不应该发生
                    running = false;
                    phase = Phase.IDLE;
                    fireStatusUpdate();
                    return;
                }
            }
        }

        fireStatusUpdate();
    }

    // ====================================================================
    //  doSyncEgg — 创建生物蛋并同步到服务端 + 客户端
    // ====================================================================
    private void doSyncEgg() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || currentIndex >= totalCount) return;

        CompoundTag passengerTag = passengers.get(currentIndex).copy();
        ensureRotationTag(passengerTag);

        ItemStack spawnEgg = createSpawnEgg(passengerTag);

        // 直接替换客户端热栏槽位
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

        // 确保客户端和服务端手部有刷怪蛋
        CompoundTag passengerTag = passengers.get(currentIndex).copy();
        ensureRotationTag(passengerTag);
        ItemStack egg = createSpawnEgg(passengerTag);

        player.getInventory().items.set(selectedSlot, egg.copy());
        player.setItemInHand(InteractionHand.MAIN_HAND, egg.copy());
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
    //  doRestoreItem — 恢复玩家原物品（仅在全部放完或中止时调用）
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
     * ★ 重要：不可直接复制原版刷怪蛋再修改，需要新建 ItemStack 并设置 NBT
     *   否则使用原版蛋的 EntityTag 会被服务端忽略
     * 
     * @param passengerTag 实体 NBT（含 Passengers）
     * @return 刷怪蛋 ItemStack
     */
    private static ItemStack createSpawnEgg(CompoundTag passengerTag) {
        ItemStack egg = new ItemStack(Items.BAT_SPAWN_EGG, 1);
        CompoundTag tag = new CompoundTag();

        // 添加附魔光效（显示为发光物品）
        CompoundTag enchantment = new CompoundTag();
        enchantment.putString("id", "minecraft:sharpness");
        enchantment.putInt("lvl", 1);
        ListTag enchantments = new ListTag();
        enchantments.add(enchantment);
        tag.put("Enchantments", enchantments);

        // 设置实体数据
        tag.put("EntityTag", passengerTag);

        // 设置物品显示名称
        CompoundTag display = new CompoundTag();
        display.putString("Name", "{\"translate\":\"entity.minecraft.block_display\"}");
        tag.put("display", display);

        egg.setTag(tag);
        return egg;
    }

    private void setStatus(String msg) {
        this.statusMessage = msg;
    }

    private void fireStatusUpdate() {
        if (onStatusUpdate != null) {
            onStatusUpdate.run();
        }
    }
}