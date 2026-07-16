package fku.org.example.fku.features.worldedit; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 历史管理器 — 支持撤销/重做
 *
 * 设计思想：
 * - 每次批量操作前记录所有受影响的方块快照
 * - 撤销时恢复快照状态
 * - 重做时再次应用操作
 * - 可配置最大撤销步数
 */
public class HistoryManager {

    private static final HistoryManager INSTANCE = new HistoryManager();
    private final Deque<List<BlockSnapshot>> undoStack = new ArrayDeque<>();
    private final Deque<List<BlockSnapshot>> redoStack = new ArrayDeque<>();

    public static HistoryManager getInstance() { return INSTANCE; }

    private HistoryManager() {}

    /**
     * 推入一次操作的历史快照
     */
    public void pushSnapshot(List<BlockSnapshot> snapshots) {
        if (snapshots.isEmpty()) return;

        WorldEditConfig cfg = WorldEditConfig.getInstance();
        undoStack.push(snapshots);
        redoStack.clear(); // 新操作清空重做栈

        // 限制栈大小
        while (undoStack.size() > cfg.maxUndoSteps) {
            undoStack.removeLast();
        }
    }

    /**
     * 撤销 — 恢复上一个操作前的状态
     */
    public boolean undo() {
        if (undoStack.isEmpty()) {
            sendStatus("§c没有可撤销的操作");
            return false;
        }

        List<BlockSnapshot> snapshots = undoStack.pop();
        List<BlockSnapshot> redoSnapshots = new ArrayList<>();

        // 恢复方块状态，同时记录当前状态供重做
        for (BlockSnapshot snapshot : snapshots) {
            if (mc().level == null) continue;
            BlockPos pos = snapshot.pos;
            BlockState currentState = mc().level.getBlockState(pos);
            redoSnapshots.add(new BlockSnapshot(pos, currentState, snapshot.blockEntityData));

            mc().level.setBlock(pos, snapshot.oldState, 3);
            if (snapshot.blockEntityData instanceof CompoundTag tag && !tag.isEmpty()) {
                BlockEntity be = mc().level.getBlockEntity(pos);
                if (be != null) {
                    be.load(tag);
                }
            }
            // 发包同步
            sendBlockPacket(pos, snapshot.oldState);
        }

        redoStack.push(redoSnapshots);
        sendStatus("§a撤销 §7(" + snapshots.size() + " 个方块)");
        return true;
    }

    /**
     * 重做
     */
    public boolean redo() {
        if (redoStack.isEmpty()) {
            sendStatus("§c没有可重做的操作");
            return false;
        }

        List<BlockSnapshot> snapshots = redoStack.pop();
        List<BlockSnapshot> undoSnapshots = new ArrayList<>();

        for (BlockSnapshot snapshot : snapshots) {
            if (mc().level == null) continue;
            BlockPos pos = snapshot.pos;
            BlockState currentState = mc().level.getBlockState(pos);
            // 注意：snapshot.oldState 在 undo 时记录的是当前状态（即操作后的状态）
            undoSnapshots.add(new BlockSnapshot(pos, currentState, snapshot.blockEntityData));

            mc().level.setBlock(pos, snapshot.oldState, 3);
            sendBlockPacket(pos, snapshot.oldState);
        }

        undoStack.push(undoSnapshots);
        sendStatus("§a重做 §7(" + snapshots.size() + " 个方块)");
        return true;
    }

    private void sendBlockPacket(BlockPos pos, BlockState state) {
        if (mc().player == null || mc().player.connection == null) return;
        // 使用任务队列重新放置这些方块
        List<BlockPos> posList = new ArrayList<>();
        posList.add(pos);
        List<BlockState> stateList = new ArrayList<>();
        stateList.add(state);
        TaskQueue.getInstance().submitPaste(posList, stateList, new ArrayList<>(), "历史恢复");
    }

    private void sendStatus(String msg) {
        if (mc().player != null) {
            mc().player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§7[WorldEdit] " + msg), true);
        }
    }

    private static net.minecraft.client.Minecraft mc() {
        return net.minecraft.client.Minecraft.getInstance();
    }

    public int getUndoCount() { return undoStack.size(); }
    public int getRedoCount() { return redoStack.size(); }
}

/**
 * 方块快照 — 记录方块操作前的状态
 */
class BlockSnapshot {
    final BlockPos pos;
    final BlockState oldState;
    final Object blockEntityData; // CompoundTag or null

    BlockSnapshot(BlockPos pos, BlockState oldState, Object blockEntityData) {
        this.pos = pos;
        this.oldState = oldState;
        this.blockEntityData = blockEntityData;
    }
}
