package fku.org.example.fku.features.worldedit;

import fku.org.example.fku.features.worldedit.BlockSnapshot;
import fku.org.example.fku.features.worldedit.TaskQueue;
import fku.org.example.fku.features.worldedit.WorldEditConfig;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HistoryManager {
    private static final HistoryManager INSTANCE = new HistoryManager();
    private final Deque<List<BlockSnapshot>> undoStack = new ArrayDeque<List<BlockSnapshot>>();
    private final Deque<List<BlockSnapshot>> redoStack = new ArrayDeque<List<BlockSnapshot>>();

    public static HistoryManager getInstance() {
        return INSTANCE;
    }

    private HistoryManager() {
    }

    public void pushSnapshot(List<BlockSnapshot> snapshots) {
        if (snapshots.isEmpty()) {
            return;
        }
        WorldEditConfig cfg = WorldEditConfig.getInstance();
        this.undoStack.push(snapshots);
        this.redoStack.clear();
        while (this.undoStack.size() > cfg.maxUndoSteps) {
            this.undoStack.removeLast();
        }
    }

    public boolean undo() {
        if (this.undoStack.isEmpty()) {
            this.sendStatus("\u00a7c\u6ca1\u6709\u53ef\u64a4\u9500\u7684\u64cd\u4f5c");
            return false;
        }
        List<BlockSnapshot> snapshots = this.undoStack.pop();
        ArrayList<BlockSnapshot> redoSnapshots = new ArrayList<BlockSnapshot>();
        for (BlockSnapshot snapshot : snapshots) {
            BlockEntity be;
            CompoundTag tag;
            if (HistoryManager.mc().level == null) continue;
            BlockPos pos = snapshot.pos;
            BlockState currentState = HistoryManager.mc().level.getBlockState(pos);
            redoSnapshots.add(new BlockSnapshot(pos, currentState, snapshot.blockEntityData));
            HistoryManager.mc().level.setBlock(pos, snapshot.oldState, 3);
            Object object = snapshot.blockEntityData;
            if (object instanceof CompoundTag && !(tag = (CompoundTag)object).isEmpty() && (be = HistoryManager.mc().level.getBlockEntity(pos)) != null) {
                be.load(tag);
            }
            this.sendBlockPacket(pos, snapshot.oldState);
        }
        this.redoStack.push(redoSnapshots);
        this.sendStatus("\u00a7a\u64a4\u9500 \u00a77(" + snapshots.size() + " \u4e2a\u65b9\u5757)");
        return true;
    }

    public boolean redo() {
        if (this.redoStack.isEmpty()) {
            this.sendStatus("\u00a7c\u6ca1\u6709\u53ef\u91cd\u505a\u7684\u64cd\u4f5c");
            return false;
        }
        List<BlockSnapshot> snapshots = this.redoStack.pop();
        ArrayList<BlockSnapshot> undoSnapshots = new ArrayList<BlockSnapshot>();
        for (BlockSnapshot snapshot : snapshots) {
            if (HistoryManager.mc().level == null) continue;
            BlockPos pos = snapshot.pos;
            BlockState currentState = HistoryManager.mc().level.getBlockState(pos);
            undoSnapshots.add(new BlockSnapshot(pos, currentState, snapshot.blockEntityData));
            HistoryManager.mc().level.setBlock(pos, snapshot.oldState, 3);
            this.sendBlockPacket(pos, snapshot.oldState);
        }
        this.undoStack.push(undoSnapshots);
        this.sendStatus("\u00a7a\u91cd\u505a \u00a77(" + snapshots.size() + " \u4e2a\u65b9\u5757)");
        return true;
    }

    private void sendBlockPacket(BlockPos pos, BlockState state) {
        if (HistoryManager.mc().player == null || HistoryManager.mc().player.connection == null) {
            return;
        }
        ArrayList<BlockPos> posList = new ArrayList<BlockPos>();
        posList.add(pos);
        ArrayList<BlockState> stateList = new ArrayList<BlockState>();
        stateList.add(state);
        TaskQueue.getInstance().submitPaste(posList, stateList, new ArrayList<Object>(), "\u5386\u53f2\u6062\u590d");
    }

    private void sendStatus(String msg) {
        if (HistoryManager.mc().player != null) {
            HistoryManager.mc().player.displayClientMessage(Component.literal((String)("\u00a77[WorldEdit] " + msg)), true);
        }
    }

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public int getUndoCount() {
        return this.undoStack.size();
    }

    public int getRedoCount() {
        return this.redoStack.size();
    }
}

