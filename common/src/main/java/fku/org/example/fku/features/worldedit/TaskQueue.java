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
        boolean isAir = targetState.isAir();
        for (BlockPos pos : positions) {
            if (TaskQueue.mc.level == null) continue;
            BlockState oldState = TaskQueue.mc.level.getBlockState(pos);
            snapshots.add(new BlockSnapshot(pos, oldState, null));
            this.queue.add(new BlockOperation(pos, targetState, null, isAir ? BlockOperation.Type.BREAK : BlockOperation.Type.SET));
        }
        HistoryManager.getInstance().pushSnapshot(snapshots);
        if (!isAir && !(item = this.findItemForBlock(targetState)).isEmpty() && (slot = this.ensureInHotbarSlot(item.getItem())) >= 0 && this.originalSlot < 0) {
            this.originalSlot = TaskQueue.mc.player != null ? TaskQueue.mc.player.getInventory().selected : -1;
        }
        this.startQueue(commandName);
    }

    public void submitReplace(List<BlockPos> positions, BlockState targetState, BlockState fromState, String commandName) {
        ArrayList<BlockSnapshot> snapshots = new ArrayList<BlockSnapshot>();
        for (BlockPos pos : positions) {
            if (TaskQueue.mc.level == null) continue;
            BlockState oldState = TaskQueue.mc.level.getBlockState(pos);
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
            if (TaskQueue.mc.level == null) continue;
            BlockState oldState = TaskQueue.mc.level.getBlockState(pos);
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
        if (TaskQueue.mc.player == null || TaskQueue.mc.level == null) {
            return;
        }
        boolean isCreative = TaskQueue.mc.player.getAbilities().instabuild;
        switch (op.type) {
            case SET: 
            case REPLACE: {
                this.breakBlockPacket(op.pos, isCreative);
                if (op.targetState == null || op.targetState.isAir()) break;
                this.placeBlockPacket(op.pos, op.targetState, false);
                break;
            }
            case PASTE: {
                this.breakBlockPacket(op.pos, isCreative);
                if (op.targetState != null && !op.targetState.isAir()) {
                    this.placeBlockPacket(op.pos, op.targetState, true);
                }
                if (!(op.blockEntityData instanceof CompoundTag) || ((CompoundTag)op.blockEntityData).isEmpty()) break;
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
        if (TaskQueue.mc.player == null || TaskQueue.mc.player.connection == null) {
            return;
        }
        ItemStack item = this.findItemForBlock(state);
        if (item.isEmpty()) {
            return;
        }
        int targetSlot = this.ensureInHotbarSlot(item.getItem());
        if (targetSlot < 0) {
            return;
        }
        if (this.originalSlot < 0) {
            this.originalSlot = TaskQueue.mc.player.getInventory().selected;
        }
        int seq = this.getSequence();
        TaskQueue.mc.player.connection.send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        Vec3 eyePos = TaskQueue.mc.player.getEyePosition(1.0f);
        Vec3 blockCenter = Vec3.atCenterOf((Vec3i)pos);
        Vec3 dir = blockCenter.subtract(eyePos).normalize();
        if (orient) {
            float[] orientation = ClipboardManager.getPlacementYawPitch(state);
            yaw = orientation[0];
            pitch = orientation[1];
            placeFace = ClipboardManager.getPlacementFace(state);
            if (placeFace.getAxis() == Direction.Axis.Y && Float.isNaN(yaw)) {
                // empty if block
            }
        } else {
            yaw = (float)(Math.atan2(-dir.x, dir.z) * 180.0 / Math.PI);
            pitch = (float)(-Math.asin(dir.y) * 180.0 / Math.PI);
            placeFace = Direction.getNearest(dir.x, dir.y, dir.z).getOpposite();
        }
        if (!Float.isNaN(yaw) && !Float.isNaN(pitch)) {
            TaskQueue.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Rot(yaw, pitch, TaskQueue.mc.player.onGround()));
        }
        TaskQueue.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(targetSlot));
        if (!orient || placeFace == null) {
            placeFace = Direction.getNearest(dir.x, dir.y, dir.z).getOpposite();
        }
        Vec3 clickPos = blockCenter.add(Vec3.atLowerCornerOf((Vec3i)placeFace.getNormal()).scale(-0.5));
        BlockHitResult hitResult = new BlockHitResult(clickPos, placeFace, pos, false);
        TaskQueue.mc.player.connection.send((Packet)new ServerboundPlayerCommandPacket((Entity)TaskQueue.mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
        TaskQueue.mc.player.connection.send((Packet)new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hitResult, seq));
        TaskQueue.mc.player.connection.send((Packet)new ServerboundPlayerCommandPacket((Entity)TaskQueue.mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
    }

    private void restoreBlockEntity(BlockPos pos, CompoundTag tag) {
        if (TaskQueue.mc.player == null || TaskQueue.mc.level == null) {
            return;
        }
        if (TaskQueue.mc.level.getBlockEntity(pos) != null) {
            try {
                TaskQueue.mc.level.getBlockEntity(pos).load(tag);
            }
            catch (Exception exception) {
                // ignored
            }
        }
    }

    private void breakBlockPacket(BlockPos pos, boolean isCreative) {
        if (TaskQueue.mc.player == null || TaskQueue.mc.player.connection == null) {
            return;
        }
        int seq = this.getSequence();
        TaskQueue.mc.player.connection.send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        Vec3 eyePos = TaskQueue.mc.player.getEyePosition(1.0f);
        Vec3 blockCenter = Vec3.atCenterOf((Vec3i)pos);
        Vec3 dir = blockCenter.subtract(eyePos).normalize();
        float yaw = (float)(Math.atan2(-dir.x, dir.z) * 180.0 / Math.PI);
        float pitch = (float)(-Math.asin(dir.y) * 180.0 / Math.PI);
        TaskQueue.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Rot(yaw, pitch, TaskQueue.mc.player.onGround()));
        TaskQueue.mc.player.connection.send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, Direction.DOWN, seq));
        if (!isCreative) {
            TaskQueue.mc.player.connection.send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN, seq));
        }
    }

    private int getSequence() {
        if (TaskQueue.mc.level == null) {
            return 0;
        }
        BlockStatePredictionHandler handler = ((ClientLevelAccessor)TaskQueue.mc.level).getBlockStatePredictionHandler_CU();
        handler.startPredicting();
        int num = handler.currentSequence();
        handler.close();
        return num;
    }

    private int ensureInHotbarSlot(Item item) {
        int i;
        int curr;
        if (TaskQueue.mc.player == null) {
            return -1;
        }
        Inventory inv = TaskQueue.mc.player.getInventory();
        if (inv.getItem(curr = inv.selected).is(item)) {
            return curr;
        }
        for (i = 0; i < 9; ++i) {
            if (!inv.getItem(i).is(item)) continue;
            inv.selected = i;
            TaskQueue.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(i));
            return i;
        }
        for (i = 9; i < 36; ++i) {
            if (!inv.getItem(i).is(item)) continue;
            ItemStack temp = inv.getItem(curr).copy();
            inv.setItem(curr, inv.getItem(i).copy());
            inv.setItem(i, temp);
            TaskQueue.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(curr));
            return curr;
        }
        if (TaskQueue.mc.player.getAbilities().instabuild) {
            ItemStack stack = new ItemStack((ItemLike)item, 64);
            inv.setItem(curr, stack);
            TaskQueue.mc.player.connection.send((Packet)new ServerboundSetCreativeModeSlotPacket(curr + 36, stack));
            TaskQueue.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(curr));
            return curr;
        }
        return curr;
    }

    private ItemStack findItemForBlock(BlockState state) {
        Block block = state.getBlock();
        ItemStack stack = new ItemStack((ItemLike)block.asItem(), 1);
        if (!stack.isEmpty()) {
            return stack;
        }
        if (TaskQueue.mc.player == null) {
            return ItemStack.EMPTY;
        }
        for (int i = 0; i < TaskQueue.mc.player.getInventory().getContainerSize(); ++i) {
            BlockItem bi;
            ItemStack invStack = TaskQueue.mc.player.getInventory().getItem(i);
            Item item = invStack.getItem();
            if (!(item instanceof BlockItem) || (bi = (BlockItem)item).getBlock() != block) continue;
            return invStack.copyWithCount(1);
        }
        return ItemStack.EMPTY;
    }

    private int findHotbarSlot(ItemStack stack) {
        if (TaskQueue.mc.player == null) {
            return -1;
        }
        for (int i = 0; i < 9; ++i) {
            if (TaskQueue.mc.player.getInventory().getItem(i).getItem() != stack.getItem()) continue;
            return i;
        }
        return -1;
    }

    private boolean matchesBlock(BlockState state, BlockState match) {
        return state.getBlock() == match.getBlock();
    }

    private void finishQueue(boolean success) {
        this.running = false;
        this.paused = false;
        this.queue.clear();
        WorldEditConfig cfg = WorldEditConfig.getInstance();
        cfg.taskRunning = false;
        if (this.originalSlot >= 0 && TaskQueue.mc.player != null) {
            TaskQueue.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(this.originalSlot));
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
            TaskQueue.mc.player.displayClientMessage(Component.literal((String)("\u00a77[WorldEdit] " + msg)), true);
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

