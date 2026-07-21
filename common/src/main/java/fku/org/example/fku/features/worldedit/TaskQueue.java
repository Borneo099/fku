package fku.org.example.fku.features.worldedit;

import fku.org.example.fku.features.worldedit.BlockSnapshot;
import fku.org.example.fku.features.worldedit.ClipboardManager;
import fku.org.example.fku.features.worldedit.HistoryManager;
import fku.org.example.fku.features.worldedit.WorldEditConfig;
import fku.org.example.fku.mixin.ClientLevelAccessor;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class TaskQueue {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final TaskQueue INSTANCE = new TaskQueue();
    private final Queue<BlockOperation> queue = new ArrayDeque<BlockOperation>();
    private boolean running = false;
    private boolean paused = false;
    private int totalTasks = 0;
    private int completedTasks = 0;
    private String currentCommand = "";
    private Consumer<Boolean> onComplete;
    private int originalSlot = -1;

    public static TaskQueue getInstance() {
        return INSTANCE;
    }

    private TaskQueue() {
    }

    public void submitSet(List<BlockPos> positions, BlockState targetState, String commandName) {
        int slot;
        ItemStack item;
        ArrayList<BlockSnapshot> snapshots = new ArrayList<BlockSnapshot>();
        boolean isAir = targetState.m_60795_();
        for (BlockPos pos : positions) {
            if (TaskQueue.mc.f_91073_ == null) continue;
            BlockState oldState = TaskQueue.mc.f_91073_.m_8055_(pos);
            snapshots.add(new BlockSnapshot(pos, oldState, null));
            this.queue.add(new BlockOperation(pos, targetState, null, isAir ? BlockOperation.Type.BREAK : BlockOperation.Type.SET));
        }
        HistoryManager.getInstance().pushSnapshot(snapshots);
        if (!isAir && !(item = this.findItemForBlock(targetState)).m_41619_() && (slot = this.ensureInHotbarSlot(item.m_41720_())) >= 0 && this.originalSlot < 0) {
            this.originalSlot = TaskQueue.mc.player != null ? TaskQueue.mc.player.m_150109_().f_35977_ : -1;
        }
        this.startQueue(commandName);
    }

    public void submitReplace(List<BlockPos> positions, BlockState targetState, BlockState fromState, String commandName) {
        ArrayList<BlockSnapshot> snapshots = new ArrayList<BlockSnapshot>();
        for (BlockPos pos : positions) {
            if (TaskQueue.mc.f_91073_ == null) continue;
            BlockState oldState = TaskQueue.mc.f_91073_.m_8055_(pos);
            if (fromState != null && !this.matchesBlock(oldState, fromState)) continue;
            snapshots.add(new BlockSnapshot(pos, oldState, null));
            this.queue.add(new BlockOperation(pos, targetState, null, BlockOperation.Type.REPLACE));
        }
        if (snapshots.isEmpty()) {
            this.sendStatus("\u00a7e\u6ca1\u6709\u5339\u914d\u7684\u65b9\u5757");
            return;
        }
        HistoryManager.getInstance().pushSnapshot(snapshots);
        this.startQueue(commandName);
    }

    public void submitPaste(List<BlockPos> positions, List<BlockState> states, List<Object> blockEntityData, String commandName) {
        if (positions.size() != states.size()) {
            return;
        }
        ArrayList<BlockSnapshot> snapshots = new ArrayList<BlockSnapshot>();
        for (int i = 0; i < positions.size(); ++i) {
            BlockPos pos = positions.get(i);
            if (TaskQueue.mc.f_91073_ == null) continue;
            BlockState oldState = TaskQueue.mc.f_91073_.m_8055_(pos);
            snapshots.add(new BlockSnapshot(pos, oldState, null));
            this.queue.add(new BlockOperation(pos, states.get(i), i < blockEntityData.size() ? blockEntityData.get(i) : null, BlockOperation.Type.PASTE));
        }
        HistoryManager.getInstance().pushSnapshot(snapshots);
        this.startQueue(commandName);
    }

    private void startQueue(String commandName) {
        this.running = true;
        this.paused = false;
        this.totalTasks = this.queue.size();
        this.completedTasks = 0;
        this.currentCommand = commandName;
        WorldEditConfig.getInstance().taskRunning = true;
        WorldEditConfig.getInstance().taskStatus = "\u00a7a\u8fd0\u884c\u4e2d: " + commandName + " (0/" + this.totalTasks + ")";
        this.sendStatus("\u00a7a\u5f00\u59cb " + commandName + " \u00a77(" + this.totalTasks + " \u4e2a\u65b9\u5757)");
    }

    public void tick() {
        if (!this.running || this.paused || this.queue.isEmpty()) {
            if (this.queue.isEmpty() && this.running) {
                this.finishQueue(true);
            }
            return;
        }
        WorldEditConfig cfg = WorldEditConfig.getInstance();
        int batchSize = Math.min(cfg.maxPacketsPerTick, this.queue.size());
        for (int i = 0; i < batchSize && !this.queue.isEmpty(); ++i) {
            BlockOperation op = this.queue.poll();
            this.executeOperation(op);
            ++this.completedTasks;
        }
        if (this.totalTasks > 0) {
            int percent = this.completedTasks * 100 / this.totalTasks;
            cfg.taskStatus = "\u00a7a\u8fd0\u884c\u4e2d: " + this.currentCommand + " (" + percent + "%, " + this.completedTasks + "/" + this.totalTasks + ")";
        }
        if (this.queue.isEmpty()) {
            this.finishQueue(true);
        }
    }

    private void executeOperation(BlockOperation op) {
        if (TaskQueue.mc.player == null || TaskQueue.mc.f_91073_ == null) {
            return;
        }
        boolean isCreative = TaskQueue.mc.player.m_150110_().f_35937_;
        switch (op.type) {
            case SET: 
            case REPLACE: {
                this.breakBlockPacket(op.pos, isCreative);
                if (op.targetState == null || op.targetState.m_60795_()) break;
                this.placeBlockPacket(op.pos, op.targetState, false);
                break;
            }
            case PASTE: {
                this.breakBlockPacket(op.pos, isCreative);
                if (op.targetState != null && !op.targetState.m_60795_()) {
                    this.placeBlockPacket(op.pos, op.targetState, true);
                }
                if (!(op.blockEntityData instanceof CompoundTag) || ((CompoundTag)op.blockEntityData).m_128456_()) break;
                this.restoreBlockEntity(op.pos, (CompoundTag)op.blockEntityData);
                break;
            }
            case BREAK: {
                this.breakBlockPacket(op.pos, isCreative);
            }
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    private void placeBlockPacket(BlockPos pos, BlockState state, boolean orient) {
        Direction placeFace;
        float pitch;
        float yaw;
        if (TaskQueue.mc.player == null || TaskQueue.mc.player.f_108617_ == null) {
            return;
        }
        ItemStack item = this.findItemForBlock(state);
        if (item.m_41619_()) {
            return;
        }
        int targetSlot = this.ensureInHotbarSlot(item.m_41720_());
        if (targetSlot < 0) {
            return;
        }
        if (this.originalSlot < 0) {
            this.originalSlot = TaskQueue.mc.player.m_150109_().f_35977_;
        }
        int seq = this.getSequence();
        TaskQueue.mc.player.f_108617_.m_104955_((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        Vec3 eyePos = TaskQueue.mc.player.m_20299_(1.0f);
        Vec3 blockCenter = Vec3.m_82512_((Vec3i)pos);
        Vec3 dir = blockCenter.subtract(eyePos).normalize();
        if (orient) {
            float[] orientation = ClipboardManager.getPlacementYawPitch(state);
            yaw = orientation[0];
            pitch = orientation[1];
            placeFace = ClipboardManager.getPlacementFace(state);
            if (placeFace.m_122434_() == Direction.Axis.Y && Float.isNaN(yaw)) {
                // empty if block
            }
        } else {
            yaw = (Math.atan2(-dir.x, dir.z) * 180.0 / Math.PI);
            pitch = (-Math.asin(dir.y) * 180.0 / Math.PI);
            placeFace = Direction.m_122366_(dir.x, dir.y, dir.z).m_122424_();
        }
        if (!Float.isNaN(yaw) && !Float.isNaN(pitch)) {
            TaskQueue.mc.player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(yaw, pitch, TaskQueue.mc.player.m_20096_()));
        }
        TaskQueue.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(targetSlot));
        if (!orient || placeFace == null) {
            placeFace = Direction.m_122366_(dir.x, dir.y, dir.z).m_122424_();
        }
        Vec3 clickPos = blockCenter.add(Vec3.m_82528_((Vec3i)placeFace.m_122436_()).scale(-0.5));
        BlockHitResult hitResult = new BlockHitResult(clickPos, placeFace, pos, false);
        TaskQueue.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerCommandPacket((Entity)TaskQueue.mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
        TaskQueue.mc.player.f_108617_.m_104955_((Packet)new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hitResult, seq));
        TaskQueue.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerCommandPacket((Entity)TaskQueue.mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
    }

    private void restoreBlockEntity(BlockPos pos, CompoundTag tag) {
        if (TaskQueue.mc.player == null || TaskQueue.mc.f_91073_ == null) {
            return;
        }
        if (TaskQueue.mc.f_91073_.m_7702_(pos) != null) {
            try {
                TaskQueue.mc.f_91073_.m_7702_(pos).m_142466_(tag);
            }
            catch (Exception exception) {
                // ignored
            }
        }
    }

    private void breakBlockPacket(BlockPos pos, boolean isCreative) {
        if (TaskQueue.mc.player == null || TaskQueue.mc.player.f_108617_ == null) {
            return;
        }
        int seq = this.getSequence();
        TaskQueue.mc.player.f_108617_.m_104955_((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        Vec3 eyePos = TaskQueue.mc.player.m_20299_(1.0f);
        Vec3 blockCenter = Vec3.m_82512_((Vec3i)pos);
        Vec3 dir = blockCenter.subtract(eyePos).normalize();
        float yaw = (Math.atan2(-dir.x, dir.z) * 180.0 / Math.PI);
        float pitch = (-Math.asin(dir.y) * 180.0 / Math.PI);
        TaskQueue.mc.player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(yaw, pitch, TaskQueue.mc.player.m_20096_()));
        TaskQueue.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, Direction.DOWN, seq));
        if (!isCreative) {
            TaskQueue.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN, seq));
        }
    }

    private int getSequence() {
        if (TaskQueue.mc.f_91073_ == null) {
            return 0;
        }
        BlockStatePredictionHandler handler = ((ClientLevelAccessor)TaskQueue.mc.f_91073_).getBlockStatePredictionHandler_CU();
        handler.m_233855_();
        int num = handler.m_233871_();
        handler.close();
        return num;
    }

    private int ensureInHotbarSlot(Item item) {
        int i;
        int curr;
        if (TaskQueue.mc.player == null) {
            return -1;
        }
        Inventory inv = TaskQueue.mc.player.m_150109_();
        if (inv.m_8020_(curr = inv.f_35977_).m_150930_(item)) {
            return curr;
        }
        for (i = 0; i < 9; ++i) {
            if (!inv.m_8020_(i).m_150930_(item)) continue;
            inv.f_35977_ = i;
            TaskQueue.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(i));
            return i;
        }
        for (i = 9; i < 36; ++i) {
            if (!inv.m_8020_(i).m_150930_(item)) continue;
            ItemStack temp = inv.m_8020_(curr).m_41777_();
            inv.m_6836_(curr, inv.m_8020_(i).m_41777_());
            inv.m_6836_(i, temp);
            TaskQueue.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(curr));
            return curr;
        }
        if (TaskQueue.mc.player.m_150110_().f_35937_) {
            ItemStack stack = new ItemStack((ItemLike)item, 64);
            inv.m_6836_(curr, stack);
            TaskQueue.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCreativeModeSlotPacket(curr + 36, stack));
            TaskQueue.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(curr));
            return curr;
        }
        return curr;
    }

    private ItemStack findItemForBlock(BlockState state) {
        Block block = state.m_60734_();
        ItemStack stack = new ItemStack((ItemLike)block.m_5456_(), 1);
        if (!stack.m_41619_()) {
            return stack;
        }
        if (TaskQueue.mc.player == null) {
            return ItemStack.f_41583_;
        }
        for (int i = 0; i < TaskQueue.mc.player.m_150109_().m_6643_(); ++i) {
            BlockItem bi;
            ItemStack invStack = TaskQueue.mc.player.m_150109_().m_8020_(i);
            Item item = invStack.m_41720_();
            if (!(item instanceof BlockItem) || (bi = (BlockItem)item).m_40614_() != block) continue;
            return invStack.m_255036_(1);
        }
        return ItemStack.f_41583_;
    }

    private int findHotbarSlot(ItemStack stack) {
        if (TaskQueue.mc.player == null) {
            return -1;
        }
        for (int i = 0; i < 9; ++i) {
            if (TaskQueue.mc.player.m_150109_().m_8020_(i).m_41720_() != stack.m_41720_()) continue;
            return i;
        }
        return -1;
    }

    private boolean matchesBlock(BlockState state, BlockState match) {
        return state.m_60734_() == match.m_60734_();
    }

    private void finishQueue(boolean success) {
        this.running = false;
        this.paused = false;
        this.queue.clear();
        WorldEditConfig cfg = WorldEditConfig.getInstance();
        cfg.taskRunning = false;
        if (this.originalSlot >= 0 && TaskQueue.mc.player != null) {
            TaskQueue.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(this.originalSlot));
            this.originalSlot = -1;
        }
        if (success) {
            cfg.taskStatus = "\u00a7a\u5b8c\u6210: " + this.currentCommand + " (" + this.completedTasks + " \u4e2a\u65b9\u5757)";
            this.sendStatus("\u00a7a\u2714 " + this.currentCommand + " \u5b8c\u6210 \u00a77(" + this.completedTasks + " \u4e2a\u65b9\u5757)");
        } else {
            cfg.taskStatus = "\u00a7c\u5df2\u53d6\u6d88: " + this.currentCommand;
            this.sendStatus("\u00a7c\u2718 " + this.currentCommand + " \u5df2\u53d6\u6d88");
        }
        if (this.onComplete != null) {
            this.onComplete.accept(success);
            this.onComplete = null;
        }
    }

    private void sendStatus(String msg) {
        if (TaskQueue.mc.player != null) {
            TaskQueue.mc.player.m_5661_(Component.literal((String)("\u00a77[WorldEdit] " + msg)), true);
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void setPaused(boolean p) {
        this.paused = p;
    }

    public void cancel() {
        this.finishQueue(false);
    }

    public void setOnComplete(Consumer<Boolean> callback) {
        this.onComplete = callback;
    }

    public int getProgress() {
        return this.totalTasks > 0 ? this.completedTasks * 100 / this.totalTasks : 0;
    }

    public String getStatusText() {
        return this.currentCommand + " " + this.completedTasks + "/" + this.totalTasks;
    }

    static class BlockOperation {
        final BlockPos pos;
        final BlockState targetState;
        final Object blockEntityData;
        final Type type;

        BlockOperation(BlockPos pos, BlockState targetState, Object blockEntityData, Type type) {
            this.pos = pos;
            this.targetState = targetState;
            this.blockEntityData = blockEntityData;
            this.type = type;
        }

        static enum Type {
            SET,
            REPLACE,
            BREAK,
            PASTE;

        }
    }
}

