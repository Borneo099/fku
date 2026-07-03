package fku.org.example.fku.features.worldedit; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
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
     */
    public void submitSet(List<BlockPos> positions, BlockState targetState, String commandName) {
        List<BlockSnapshot> snapshots = new ArrayList<>();

        for (BlockPos pos : positions) {
            if (mc.level == null) continue;
            BlockState oldState = mc.level.getBlockState(pos);
            snapshots.add(new BlockSnapshot(pos, oldState, null));
            queue.add(new BlockOperation(pos, targetState, null, BlockOperation.Type.SET));
        }

        HistoryManager.getInstance().pushSnapshot(snapshots);
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

        switch (op.type) {
            case SET:
            case REPLACE:
            case PASTE:
                placeBlockPacket(op.pos, op.targetState);
                break;
            case BREAK:
                breakBlockPacket(op.pos);
                break;
        }

        // 客户端预测性更新
        if (op.targetState != null && op.type != BlockOperation.Type.BREAK) {
            mc.level.setBlock(op.pos, op.targetState, 3);
        } else if (op.type == BlockOperation.Type.BREAK) {
            mc.level.removeBlock(op.pos, false);
        }
    }

    /**
     * 发送放置方块包
     */
    private void placeBlockPacket(BlockPos pos, BlockState state) {
        if (mc.player == null || mc.player.connection == null) return;

        // 找到合适的物品
        ItemStack item = findItemForBlock(state);
        if (item.isEmpty()) return;

        int slot = findHotbarSlot(item);
        if (slot < 0) return;

        // 切换物品
        mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(slot));

        // 计算点击位置 — 从玩家位置往目标方向射线的命中面
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        Vec3 blockCenter = Vec3.atCenterOf(pos);
        Vec3 hitDir = blockCenter.subtract(eyePos).normalize();

        // 找到最佳点击面
        Direction bestFace = Direction.getNearest(hitDir.x, hitDir.y, hitDir.z).getOpposite();
        Vec3 clickPos = blockCenter.add(Vec3.atLowerCornerOf(bestFace.getNormal()).scale(-0.5));

        BlockHitResult hitResult = new BlockHitResult(clickPos, bestFace, pos, false);
        mc.player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hitResult, getSequence()));
    }

    /**
     * 发送破坏方块包
     */
    private void breakBlockPacket(BlockPos pos) {
        if (mc.player == null || mc.player.connection == null) return;
        int seq = getSequence();
        mc.player.connection.send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, Direction.DOWN, seq));
        mc.player.connection.send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN, seq));
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
