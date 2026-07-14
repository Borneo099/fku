package fku.org.example.fku.features.worldedit; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * 任务队列 — 分帧执行批量方块操作
 *
 * 设计思想：
 * - 每 tick 处理 maxPacketsPerTick 个操作
 * - 可暂停、取消、显示进度
 * - 记录操作前的方块状态到 HistoryManager
 */
public class TaskQueue {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final TaskQueue INSTANCE = new TaskQueue();

    private final Queue<BlockOperation> queue = new ArrayDeque<>();
    private boolean running = false;
    private boolean paused = false;
    private int totalTasks = 0;
    private int completedTasks = 0;
    private String currentCommand = "";
    private Consumer<Boolean> onComplete;

    public static TaskQueue getInstance() { return INSTANCE; }

    private TaskQueue() {}

    /**
     * 添加批量设置任务
     * targetState=air → 改为 BREAK 操作（破坏方块）
     */
    public void submitSet(List<BlockPos> positions, BlockState targetState, String commandName) {
        List<BlockSnapshot> snapshots = new ArrayList<>();
        boolean isAir = targetState.isAir();

        for (BlockPos pos : positions) {
            if (mc.level == null) continue;
            BlockState oldState = mc.level.getBlockState(pos);
            snapshots.add(new BlockSnapshot(pos, oldState, null));
            queue.add(new BlockOperation(pos, targetState, null, isAir ? BlockOperation.Type.BREAK : BlockOperation.Type.SET));
        }

        HistoryManager.getInstance().pushSnapshot(snapshots);

        // ★ 如果是放置操作，确保物品在快捷栏（自动 give）
        if (!isAir) {
            ItemStack item = findItemForBlock(targetState);
            if (!item.isEmpty()) {
                int slot = ensureInHotbarSlot(item.getItem());
                if (slot >= 0 && originalSlot < 0) {
                    originalSlot = mc.player != null ? mc.player.getInventory().selected : -1;
                }
            }
        }

        startQueue(commandName);
    }

    /**
     * 添加替换任务
     */
    public void submitReplace(List<BlockPos> positions, BlockState targetState, BlockState fromState, String commandName) {
        List<BlockSnapshot> snapshots = new ArrayList<>();

        for (BlockPos pos : positions) {
            if (mc.level == null) continue;
            BlockState oldState = mc.level.getBlockState(pos);
            if (fromState != null && !matchesBlock(oldState, fromState)) continue;
            snapshots.add(new BlockSnapshot(pos, oldState, null));
            queue.add(new BlockOperation(pos, targetState, null, BlockOperation.Type.REPLACE));
        }

        if (snapshots.isEmpty()) {
            sendStatus("§e没有匹配的方块");
            return;
        }

        HistoryManager.getInstance().pushSnapshot(snapshots);
        startQueue(commandName);
    }

    /**
     * 添加粘贴任务
     */
    public void submitPaste(List<BlockPos> positions, List<BlockState> states, List<Object> blockEntityData, String commandName) {
        if (positions.size() != states.size()) return;
        List<BlockSnapshot> snapshots = new ArrayList<>();

        for (int i = 0; i < positions.size(); i++) {
            BlockPos pos = positions.get(i);
            if (mc.level == null) continue;
            BlockState oldState = mc.level.getBlockState(pos);
            snapshots.add(new BlockSnapshot(pos, oldState, null));
            queue.add(new BlockOperation(pos, states.get(i), i < blockEntityData.size() ? blockEntityData.get(i) : null, BlockOperation.Type.PASTE));
        }

        HistoryManager.getInstance().pushSnapshot(snapshots);
        startQueue(commandName);
    }

    private void startQueue(String commandName) {
        running = true;
        paused = false;
        totalTasks = queue.size();
        completedTasks = 0;
        currentCommand = commandName;
        WorldEditConfig.getInstance().taskRunning = true;
        WorldEditConfig.getInstance().taskStatus = "§a运行中: " + commandName + " (0/" + totalTasks + ")";
        sendStatus("§a开始 " + commandName + " §7(" + totalTasks + " 个方块)");
    }

    /**
     * 每 tick 调用 — 处理一批操作
     */
    public void tick() {
        if (!running || paused || queue.isEmpty()) {
            if (queue.isEmpty() && running) {
                finishQueue(true);
            }
            return;
        }

        WorldEditConfig cfg = WorldEditConfig.getInstance();
        int batchSize = Math.min(cfg.maxPacketsPerTick, queue.size());

        for (int i = 0; i < batchSize && !queue.isEmpty(); i++) {
            BlockOperation op = queue.poll();
            executeOperation(op);
            completedTasks++;
        }

        // 更新进度
        if (totalTasks > 0) {
            int percent = completedTasks * 100 / totalTasks;
            cfg.taskStatus = "§a运行中: " + currentCommand + " (" + percent + "%, " + completedTasks + "/" + totalTasks + ")";
        }

        if (queue.isEmpty()) {
            finishQueue(true);
        }
    }

    private void executeOperation(BlockOperation op) {
        if (mc.player == null || mc.level == null) return;

        boolean isCreative = mc.player.getAbilities().instabuild;

        switch (op.type) {
            case SET:
            case REPLACE:
                // 先破坏已有方块，再放置目标方块
                breakBlockPacket(op.pos, isCreative);
                if (op.targetState != null && !op.targetState.isAir()) {
                    placeBlockPacket(op.pos, op.targetState, false);
                }
                break;
            case PASTE:
                // 粘贴：先破坏，再带朝向放置
                breakBlockPacket(op.pos, isCreative);
                if (op.targetState != null && !op.targetState.isAir()) {
                    placeBlockPacket(op.pos, op.targetState, true);
                }
                // 恢复 BlockEntity NBT（容器内容等）
                if (op.blockEntityData instanceof CompoundTag && !((CompoundTag)op.blockEntityData).isEmpty()) {
                    restoreBlockEntity(op.pos, (CompoundTag) op.blockEntityData);
                }
                break;
            case BREAK:
                breakBlockPacket(op.pos, isCreative);
                break;
        }
    }

    /** 当前操作的原始槽位（用于恢复） */
    private int originalSlot = -1;

    /**
     * 发送放置方块包 — 支持朝向控制
     *
     * @param orient true=使用 BlockState 的朝向属性（用于粘贴），false=自动朝向玩家
     * 包序列：Swing → FakeRot → SetCarriedItem → PRESS_SHIFT → UseItemOn → RELEASE_SHIFT
     */
    private void placeBlockPacket(BlockPos pos, BlockState state, boolean orient) {
        if (mc.player == null || mc.player.connection == null) return;

        ItemStack item = findItemForBlock(state);
        if (item.isEmpty()) return;

        int targetSlot = ensureInHotbarSlot(item.getItem());
        if (targetSlot < 0) return;

        if (originalSlot < 0) {
            originalSlot = mc.player.getInventory().selected;
        }

        int seq = getSequence();

        // ① 挥动手
        mc.player.connection.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));

        // ② 假旋转
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        Vec3 blockCenter = Vec3.atCenterOf(pos);
        Vec3 dir = blockCenter.subtract(eyePos).normalize();
        float yaw, pitch;
        Direction placeFace;

        if (orient) {
            // 使用 BlockState 的朝向属性
            float[] orientation = ClipboardManager.getPlacementYawPitch(state);
            yaw = orientation[0];
            pitch = orientation[1];
            // 计算点击面
            placeFace = ClipboardManager.getPlacementFace(state);
            if (placeFace.getAxis() == Direction.Axis.Y && !Float.isNaN(yaw)) {
                // 水平方块用它原来的朝向决定点击面
            }
        } else {
            yaw = (float) (Math.atan2(-dir.x, dir.z) * 180.0 / Math.PI);
            pitch = (float) (-Math.asin(dir.y) * 180.0 / Math.PI);
            placeFace = Direction.getNearest(dir.x, dir.y, dir.z).getOpposite();
        }

        // 发送假旋转包
        if (!Float.isNaN(yaw) && !Float.isNaN(pitch)) {
            mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, mc.player.onGround()));
        }

        // ③ 切物品
        mc.player.connection.send(new ServerboundSetCarriedItemPacket(targetSlot));

        // ④ 放置
        if (!orient || placeFace == null) {
            placeFace = Direction.getNearest(dir.x, dir.y, dir.z).getOpposite();
        }
        Vec3 clickPos = blockCenter.add(Vec3.atLowerCornerOf(placeFace.getNormal()).scale(-0.5));
        BlockHitResult hitResult = new BlockHitResult(clickPos, placeFace, pos, false);

        mc.player.connection.send(new ServerboundPlayerCommandPacket(
                mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
        mc.player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hitResult, seq));
        mc.player.connection.send(new ServerboundPlayerCommandPacket(
                mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
    }

    /**
     * 恢复 BlockEntity NBT（容器内容、木牌文字等）
     */
    private void restoreBlockEntity(BlockPos pos, CompoundTag tag) {
        if (mc.player == null || mc.level == null) return;
        // 客户端预测：直接设置 BlockEntity 数据
        if (mc.level.getBlockEntity(pos) != null) {
            try {
                mc.level.getBlockEntity(pos).load(tag);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * 发送破坏方块包
     *
     * 创造模式：仅 START_DESTROY（服务端即时破坏）
     * 生存模式：START_DESTROY + STOP_DESTROY（需完整挖掘序列）
     */
    private void breakBlockPacket(BlockPos pos, boolean isCreative) {
        if (mc.player == null || mc.player.connection == null) return;

        int seq = getSequence();

        // ① 挥动手
        mc.player.connection.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));

        // ② 假旋转
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        Vec3 blockCenter = Vec3.atCenterOf(pos);
        Vec3 dir = blockCenter.subtract(eyePos).normalize();
        float yaw = (float) (Math.atan2(-dir.x, dir.z) * 180.0 / Math.PI);
        float pitch = (float) (-Math.asin(dir.y) * 180.0 / Math.PI);
        mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, mc.player.onGround()));

        // ③ 破坏包
        mc.player.connection.send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, Direction.DOWN, seq));

        if (!isCreative) {
            // 非创造模式需要 STOP_DESTROY（同序列号）
            mc.player.connection.send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN, seq));
        }
    }

    private int getSequence() {
        if (mc.level == null) return 0;
        var handler = ((fku.org.example.fku.mixin.ClientLevelAccessor) mc.level).getBlockStatePredictionHandler_CU();
        handler.startPredicting();
        int num = handler.currentSequence();
        handler.close();
        return num;
    }

    /**
     * 确保目标物品在当前主手槽位
     * 1. 主手已是目标 → 直接返回
     * 2. 快捷栏有 → 切过去
     * 3. 背包有 → 交换到主手
     * 4. 创造模式 → 自动 give 到主手
     * 不限制9格槽位，适用于大量不同方块的粘贴
     */
    private int ensureInHotbarSlot(net.minecraft.world.item.Item item) {
        if (mc.player == null) return -1;
        var inv = mc.player.getInventory();
        int curr = inv.selected;

        // 主手已是目标物品
        if (inv.getItem(curr).is(item)) return curr;

        // 快捷栏搜索
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i).is(item)) {
                inv.selected = i;
                mc.player.connection.send(new ServerboundSetCarriedItemPacket(i));
                return i;
            }
        }

        // 背包搜索 → 交换到主手槽
        for (int i = 9; i < 36; i++) {
            if (inv.getItem(i).is(item)) {
                var temp = inv.getItem(curr).copy();
                inv.setItem(curr, inv.getItem(i).copy());
                inv.setItem(i, temp);
                mc.player.connection.send(new ServerboundSetCarriedItemPacket(curr));
                return curr;
            }
        }

        // 创造模式自动 give
        if (mc.player.getAbilities().instabuild) {
            var stack = new net.minecraft.world.item.ItemStack(item, 64);
            inv.setItem(curr, stack);
            mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket(curr + 36, stack));
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(curr));
            return curr;
        }

        return curr; // 回退：可能无物品也无法给
    }

    /**
     * 为指定方块状态找对应的物品
     */
    private ItemStack findItemForBlock(BlockState state) {
        Block block = state.getBlock();
        ItemStack stack = new ItemStack(block.asItem(), 1);
        if (!stack.isEmpty()) return stack;

        // 兜底：遍历背包找第一个同种方块
        if (mc.player == null) return ItemStack.EMPTY;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = mc.player.getInventory().getItem(i);
            if (invStack.getItem() instanceof BlockItem bi) {
                if (bi.getBlock() == block) {
                    return invStack.copyWithCount(1);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 在快捷栏找指定物品
     */
    private int findHotbarSlot(ItemStack stack) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).getItem() == stack.getItem()) {
                return i;
            }
        }
        return -1;
    }

    private boolean matchesBlock(BlockState state, BlockState match) {
        return state.getBlock() == match.getBlock();
    }

    private void finishQueue(boolean success) {
        running = false;
        paused = false;
        queue.clear();
        WorldEditConfig cfg = WorldEditConfig.getInstance();
        cfg.taskRunning = false;

        // 恢复原始快捷栏槽位
        if (originalSlot >= 0 && mc.player != null) {
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(originalSlot));
            originalSlot = -1;
        }

        if (success) {
            cfg.taskStatus = "§a完成: " + currentCommand + " (" + completedTasks + " 个方块)";
            sendStatus("§a✔ " + currentCommand + " 完成 §7(" + completedTasks + " 个方块)");
        } else {
            cfg.taskStatus = "§c已取消: " + currentCommand;
            sendStatus("§c✘ " + currentCommand + " 已取消");
        }

        if (onComplete != null) {
            onComplete.accept(success);
            onComplete = null;
        }
    }

    private void sendStatus(String msg) {
        if (mc.player != null) {
            mc.player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§7[WorldEdit] " + msg), true);
        }
    }

    public boolean isRunning() { return running; }
    public boolean isPaused() { return paused; }
    public void setPaused(boolean p) { this.paused = p; }
    public void cancel() { finishQueue(false); }
    public void setOnComplete(Consumer<Boolean> callback) { this.onComplete = callback; }
    public int getProgress() { return totalTasks > 0 ? completedTasks * 100 / totalTasks : 0; }
    public String getStatusText() { return currentCommand + " " + completedTasks + "/" + totalTasks; }

    /**
     * 单个方块操作
     */
    static class BlockOperation {
        final BlockPos pos;
        final BlockState targetState;
        final Object blockEntityData;
        final Type type;

        enum Type { SET, REPLACE, BREAK, PASTE }

        BlockOperation(BlockPos pos, BlockState targetState, Object blockEntityData, Type type) {
            this.pos = pos;
            this.targetState = targetState;
            this.blockEntityData = blockEntityData;
            this.type = type;
        }
    }
}
