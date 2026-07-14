package fku.org.example.fku.features.displaymodel; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
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
 * - ClientTickEvent 驱动的有序状态机，每 tick 仅处理一个阶段
 * - 给刷怪蛋 EntityTag 追加 Pos NBT，实体可在远离玩家的坐标生成
 * - 支持多行指令队列，执行完一行自动进入下一行
 * - 物品仅在全部放置完成或中止时统一返还
 *
 * 状态机流程（每 tick 一步）：
 *   SYNC_EGG ──(同步蛋)──> PLACE_ENTITY ──(放置)──> SYNC_EGG ...
 *   循环直至全部放完 → RESTORE_ITEM
 *
 * 延迟控制：
 * - syncWaitTicks:  蛋写入后等待的 tick 数（由 placeDelayMs 换算）
 * - interDelayTicks: 两实体间的间隔 tick 数（由 generationDelayMs 换算）
 * - 最小 2 ticks/实体（sync + place），不受延迟配置低于 50ms 的影响
 */
public class DisplayModelManager {

    private static final DisplayModelManager INSTANCE = new DisplayModelManager();
    private boolean eventsRegistered = false;

    private enum Phase {
        IDLE,
        SYNC_EGG,
        WAIT_SYNC,
        PLACE_ENTITY,
        RETRY_PLACE,
        INTER_DELAY,
        RESTORE_ITEM
    }

    // ============ 状态 ============
    private Phase phase = Phase.IDLE;
    private int tickCounter = 0;

    // ============ 多行指令队列 ============
    /** 当前行索引 */
    private int commandIndex = 0;
    /** 待执行的多行指令列表 */
    private List<CommandEntry> commandQueue;

    // ============ 当前行放置数据 ============
    private List<CompoundTag> passengers;
    private Vec3 offset;
    private int currentIndex = 0;          // 当前行已放置个数
    private int totalCount = 0;            // 当前行总数
    /** 当前实体放置重试次数 */
    private int placeRetries = 0;
    private static final int MAX_PLACE_RETRIES = 3;
    private ItemStack originalItem;
    private int selectedSlot;

    // ============ 全局放置参数 ============
    private BlockPos fixedPos;             // 放置基准坐标
    private double entitySpacing = 0.5;
    /** 可视距离（0=默认），对应 Display Entity 的 view_range NBT */
    private double viewRange = 0.0;

    // ============ 延迟参数（tick数） ============
    private int syncWaitTicks = 0;
    private int interDelayTicks = 0;

    // ============ 状态信息 ============
    private String statusMessage = "";
    private boolean running = false;
    private Runnable onStatusUpdate;

    // ====================================================================
    //  内部类：一行指令及其解析结果
    // ====================================================================
    public static class CommandEntry {
        public final String rawCommand;
        public List<CompoundTag> passengers;
        public Vec3 offset;

        public CommandEntry(String cmd) {
            this.rawCommand = cmd;
        }
    }

    private DisplayModelManager() {}

    public static DisplayModelManager getInstance() {
        return INSTANCE;
    }

    public static void registerEventHandlers() {
        if (!INSTANCE.eventsRegistered) {
            MinecraftForge.EVENT_BUS.register(INSTANCE);
            INSTANCE.eventsRegistered = true;
            Fku.LOGGER.info("[DisplayModel] 事件处理器已注册");
        }
    }

    public void setOnStatusUpdate(Runnable callback) {
        this.onStatusUpdate = callback;
    }

    // ====================================================================
    //  start — 启动多行指令队列
    // ====================================================================
    public void start(List<CommandEntry> queue, int generationDelayMs, int placeDelayMs,
                      double spacing, BlockPos fixedPos, double viewRange) {
        if (running) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (!player.isCreative()) {
            setStatus("§c需要创造模式");
            return;
        }

        if (queue == null || queue.isEmpty()) {
            setStatus("§c指令队列为空");
            return;
        }

        // 解析所有指令
        for (CommandEntry entry : queue) {
            try {
                entry.offset = ModelParser.extractOffset(entry.rawCommand);
                entry.passengers = ModelParser.extractPassengers(entry.rawCommand);
            } catch (Exception e) {
                setStatus("§c指令格式错误: " + entry.rawCommand + " - " + e.getMessage());
                return;
            }
            if (entry.passengers.isEmpty()) {
                setStatus("§c指令中没有 Passengers: " + entry.rawCommand);
                return;
            }
        }

        this.commandQueue = queue;
        this.commandIndex = 0;
        this.fixedPos = fixedPos;
        this.entitySpacing = Math.max(0, spacing);
        this.viewRange = Math.max(0, viewRange);
        this.syncWaitTicks = Math.max(0, placeDelayMs / 50);
        this.interDelayTicks = Math.max(0, generationDelayMs / 50);
        this.originalItem = player.getItemInHand(InteractionHand.MAIN_HAND).copy();
        this.selectedSlot = player.getInventory().selected;
        this.tickCounter = 0;

        // 加载第一行
        loadCurrentCommand();

        this.running = true;
        this.phase = Phase.SYNC_EGG;

        setStatus("§a开始放置第 " + (commandIndex + 1) + " 行，共 " + totalCount + " 个...");
        Fku.LOGGER.info("[DisplayModel] 开始放置，共{}行，syncWait={}tick, interDelay={}tick",
                queue.size(), syncWaitTicks, interDelayTicks);
    }

    /** 加载当前行的乘客和偏移 */
    private void loadCurrentCommand() {
        CommandEntry entry = commandQueue.get(commandIndex);
        this.passengers = entry.passengers;
        this.offset = entry.offset;
        this.currentIndex = 0;
        this.totalCount = passengers.size();
        this.placeRetries = 0;
    }

    // ====================================================================
    //  stop — 中止（恢复物品）
    // ====================================================================
    public void stop() {
        if (!running) return;
        doRestoreItem();
        running = false;
        phase = Phase.IDLE;
        commandQueue = null;
        setStatus("§c已中止");
        Fku.LOGGER.info("[DisplayModel] 放置已中止");
    }

    // ============ 状态查询 ============

    public boolean isRunning() { return running; }
    public int getCurrentIndex() { return currentIndex; }
    public int getTotalCount() { return totalCount; }
    public String getStatusMessage() { return statusMessage; }

    // ====================================================================
    //  onClientTick — 事件入口
    // ====================================================================
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (this.running) {
            this.tick();
        }
    }

    // ====================================================================
    //  tick — 核心状态机（每 tick 仅处理一步）
    //
    //  设计思想（实践论）：
    //  - 移除旧的 while 循环，避免单 tick 多发包导致服务端处理异常
    //  - 每个实体至少 2 ticks（SYNC + PLACE），保证有序可靠
    //  - 延迟为 0 时，SYNC_WAIT 和 INTER_DELAY 阶段直接跳过
    //    但仍保持每 tick 一步的节奏
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

        switch (phase) {
            case SYNC_EGG -> {
                doSyncEgg();
                if (syncWaitTicks <= 0) {
                    phase = Phase.PLACE_ENTITY;      // 跳过等待，下 tick 直接放置
                } else {
                    phase = Phase.WAIT_SYNC;
                    tickCounter = 0;
                }
                fireStatusUpdate();
            }

            case WAIT_SYNC -> {
                tickCounter++;
                if (tickCounter >= syncWaitTicks) {
                    phase = Phase.PLACE_ENTITY;
                    tickCounter = 0;
                }
                fireStatusUpdate();
            }

            case PLACE_ENTITY -> {
                boolean success = doPlaceEntity();
                if (!success) {
                    // 放置失败 → 重试
                    if (placeRetries < MAX_PLACE_RETRIES) {
                        placeRetries++;
                        phase = Phase.RETRY_PLACE;
                        setStatus("§e放置重试 " + placeRetries + "/" + MAX_PLACE_RETRIES + " (index=" + currentIndex + ")");
                        Fku.LOGGER.warn("[DisplayModel] 放置失败，准备重试 {}/{}, index={}", 
                                placeRetries, MAX_PLACE_RETRIES, currentIndex);
                    } else {
                        // 超过最大重试次数，跳过该实体
                        setStatus("§c放置失败，跳过实体 " + (currentIndex + 1) + "/" + totalCount);
                        Fku.LOGGER.warn("[DisplayModel] 放置失败超过{}次，跳过实体 index={}", MAX_PLACE_RETRIES, currentIndex);
                        advanceToNext();
                    }
                } else {
                    placeRetries = 0;
                    advanceToNext();
                }
                fireStatusUpdate();
            }

            case RETRY_PLACE -> {
                // 等待 1 tick 后重试
                phase = Phase.PLACE_ENTITY;
                fireStatusUpdate();
            }

            case INTER_DELAY -> {
                tickCounter++;
                if (tickCounter >= interDelayTicks) {
                    phase = Phase.SYNC_EGG;
                    tickCounter = 0;
                }
                fireStatusUpdate();
            }

            case RESTORE_ITEM -> {
                doRestoreItem();
                running = false;
                phase = Phase.IDLE;
                commandQueue = null;
                setStatus("§a全部完成！共 " + (commandQueue == null ? 0 : commandQueue.size()) + " 行");
                Fku.LOGGER.info("[DisplayModel] 放置完成");
                fireStatusUpdate();
            }

            default -> {
                running = false;
                phase = Phase.IDLE;
                fireStatusUpdate();
            }
        }
    }

    // ====================================================================
    //  doSyncEgg — 创建刷怪蛋（含 Pos NBT）+ 同步
    //
    //  ★ 为每个乘客的 EntityTag 附加 Pos NBT，使实体在指定坐标生成
    //    不受玩家距离限制（无需服务端处理，NBT 在 Entity.load 时读取）
    // ====================================================================
    private void doSyncEgg() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || currentIndex >= totalCount) return;

        // 计算放置坐标
        double targetX = fixedPos.getX() + 0.5 + offset.x;
        double targetY = fixedPos.getY() + offset.y + currentIndex * entitySpacing;
        double targetZ = fixedPos.getZ() + 0.5 + offset.z;

        CompoundTag passengerTag = passengers.get(currentIndex).copy();

        // ── 加入 Pos NBT ──
        // 实体加载时会读取 EntityTag.Pos 覆盖默认位置
        ListTag posList = new ListTag();
        posList.add(DoubleTag.valueOf(targetX));
        posList.add(DoubleTag.valueOf(targetY));
        posList.add(DoubleTag.valueOf(targetZ));
        passengerTag.put("Pos", posList);

        ensureRotationTag(passengerTag);

        ItemStack spawnEgg = createSpawnEgg(passengerTag);

        // 替换客户端热栏槽位
        player.getInventory().items.set(selectedSlot, spawnEgg.copy());
        player.getInventory().setChanged();

        // 同步到服务端
        syncItemToServer(selectedSlot, spawnEgg.copy());

        // 设置客户端手部视觉
        player.setItemInHand(InteractionHand.MAIN_HAND, spawnEgg.copy());

        Fku.LOGGER.debug("[DisplayModel] 同步刷怪蛋到槽位 {} pos=({}, {}, {})",
                selectedSlot, String.format("%.1f", targetX), String.format("%.1f", targetY), String.format("%.1f", targetZ));
    }

    // ====================================================================
    //  doPlaceEntity — 放置实体（返回是否放置成功）
    //
    //  ★ EntityTag 已含 Pos NBT，实体在指定坐标生成，不受玩家距离限制
    //  ★ 失败时返回 false，由调用方决定重试或跳过
    // ====================================================================
    private boolean doPlaceEntity() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return false;

        // 计算放置坐标（与 doSyncEgg 保持一致）
        double targetX = fixedPos.getX() + 0.5 + offset.x;
        double targetY = fixedPos.getY() + offset.y + currentIndex * entitySpacing;
        double targetZ = fixedPos.getZ() + 0.5 + offset.z;

        // 确保客户端和服务端手部有刷怪蛋（含 Pos NBT）
        CompoundTag passengerTag = passengers.get(currentIndex).copy();
        ListTag posList = new ListTag();
        posList.add(DoubleTag.valueOf(targetX));
        posList.add(DoubleTag.valueOf(targetY));
        posList.add(DoubleTag.valueOf(targetZ));
        passengerTag.put("Pos", posList);
        ensureRotationTag(passengerTag);
        ItemStack egg = createSpawnEgg(passengerTag);

        // ★ 必须在 useItemOn 之前将物品同步到服务端，否则多人模式下服务端认不出这个蛋
        player.getInventory().items.set(selectedSlot, egg.copy());
        syncItemToServer(selectedSlot, egg.copy());

        // 使用玩家脚下方块作为交互目标
        BlockPos playerBlock = player.blockPosition();
        BlockHitResult hitResult = new BlockHitResult(
                new Vec3(playerBlock.getX() + 0.5, playerBlock.getY(), playerBlock.getZ() + 0.5),
                Direction.UP,
                playerBlock,
                false
        );

        try {
            InteractionResult result = mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hitResult);
            if (result == InteractionResult.FAIL) {
                Fku.LOGGER.warn("[DisplayModel] useItemOn 失败 (index={})", currentIndex);
                return false;
            }
            player.swing(InteractionHand.MAIN_HAND);
            return true;
        } catch (Exception e) {
            Fku.LOGGER.error("[DisplayModel] useItemOn 异常: {}", e.getMessage(), e);
            return false;
        }
    }

    // ====================================================================
    //  syncItemToServer — 将物品同步到服务端玩家热栏
    //
    //  使用 handleCreativeModeItemAdd 发送 ServerboundSetCreativeModeSlotPacket，
    //  这是多人模式下唯一可靠的客户端→服务端物品同步方式。
    //  ★ 槽位索引必须使用 36+selected（热栏在 PlayerInventory 中的实际槽位）
    // ====================================================================
    private static void syncItemToServer(int slotIndex, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) return;

        // 热栏槽位在 PlayerInventory 中是 36-44，传入 36+slotIndex
        mc.gameMode.handleCreativeModeItemAdd(stack.copy(), 36 + slotIndex);

        // 同时更新本地 hand 同步
        mc.player.setItemInHand(InteractionHand.MAIN_HAND, stack.copy());
    }

    // ====================================================================
    //  doRestoreItem — 恢复原物品（仅一次，全部完成或中止时）
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
    private ItemStack createSpawnEgg(CompoundTag passengerTag) {
        ItemStack egg = new ItemStack(Items.BAT_SPAWN_EGG, 1);
        CompoundTag tag = new CompoundTag();

        // 添加附魔光效（显示为发光物品）
        CompoundTag enchantment = new CompoundTag();
        enchantment.putString("id", "minecraft:sharpness");
        enchantment.putInt("lvl", 1);
        ListTag enchantments = new ListTag();
        enchantments.add(enchantment);
        tag.put("Enchantments", enchantments);

        // ── 可选：可视距离 view_range ──
        // 若用户配置了 >0 的可视距离，直接写入 EntityTag
        // 显示实体默认可视距离为 1.0 格
        if (viewRange > 0) {
            passengerTag.putFloat("view_range", (float) viewRange);
        }

        // 设置实体数据（含 Pos + Rotation + Passengers + view_range）
        tag.put("EntityTag", passengerTag);

        // 设置物品显示名称
        CompoundTag display = new CompoundTag();
        display.putString("Name", "{\"translate\":\"entity.minecraft.block_display\"}");
        tag.put("display", display);

        egg.setTag(tag);
        return egg;
    }

    /**
     * 推进到下一个实体/行
     * 由 doPlaceEntity 成功后调用，确保每个蛋放置后才处理下一个
     */
    private void advanceToNext() {
        currentIndex++;

        if (currentIndex >= totalCount) {
            // 当前行完成 → 检查是否有下一行
            if (commandIndex + 1 < commandQueue.size()) {
                commandIndex++;
                loadCurrentCommand();
                phase = Phase.SYNC_EGG;
                setStatus("§a切换到第 " + (commandIndex + 1) + " 行，共 " + totalCount + " 个");
            } else {
                phase = Phase.RESTORE_ITEM;
                setStatus("§a全部放置完成！");
            }
        } else if (interDelayTicks <= 0) {
            phase = Phase.SYNC_EGG;
            setStatus("§a已放置 " + currentIndex + "/" + totalCount);
        } else {
            phase = Phase.INTER_DELAY;
            tickCounter = 0;
            setStatus("§a已放置 " + currentIndex + "/" + totalCount);
        }
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