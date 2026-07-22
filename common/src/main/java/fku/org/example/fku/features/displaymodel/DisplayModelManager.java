package fku.org.example.fku.features.displaymodel;

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.displaymodel.ModelParser;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DisplayModelManager {
    private static final DisplayModelManager INSTANCE = new DisplayModelManager();
    private boolean eventsRegistered = false;
    private Phase phase = Phase.IDLE;
    private int tickCounter = 0;
    private int commandIndex = 0;
    private List<CommandEntry> commandQueue;
    private List<CompoundTag> passengers;
    private Vec3 offset;
    private int currentIndex = 0;
    private int totalCount = 0;
    private int placeRetries = 0;
    private static final int MAX_PLACE_RETRIES = 3;
    private ItemStack originalItem;
    private int selectedSlot;
    private BlockPos fixedPos;
    private double entitySpacing = 0.5;
    private double viewRange = 0.0;
    private int syncWaitTicks = 0;
    private int interDelayTicks = 0;
    private String statusMessage = "";
    private boolean running = false;
    private Runnable onStatusUpdate;

    private DisplayModelManager() {
    }

    public static DisplayModelManager getInstance() {
        return INSTANCE;
    }

    public static void registerEventHandlers() {
        if (!DisplayModelManager.INSTANCE.eventsRegistered) {
            MinecraftForge.EVENT_BUS.register(INSTANCE);
            DisplayModelManager.INSTANCE.eventsRegistered = true;
            Fku.LOGGER.info("[DisplayModel] \u4e8b\u4ef6\u5904\u7406\u5668\u5df2\u6ce8\u518c");
        }
    }

    public void setOnStatusUpdate(Runnable callback) {
        this.onStatusUpdate = callback;
    }

    public void start(List<CommandEntry> queue, int generationDelayMs, int placeDelayMs, double spacing, BlockPos fixedPos, double viewRange) {
        if (this.running) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        if (!player.isCreative()) {
            this.setStatus("\u00a7c\u9700\u8981\u521b\u9020\u6a21\u5f0f");
            return;
        }
        if (queue == null || queue.isEmpty()) {
            this.setStatus("\u00a7c\u6307\u4ee4\u961f\u5217\u4e3a\u7a7a");
            return;
        }
        for (CommandEntry entry : queue) {
            try {
                entry.offset = ModelParser.extractOffset(entry.rawCommand);
                entry.passengers = ModelParser.extractPassengers(entry.rawCommand);
            }
            catch (Exception e) {
                this.setStatus("\u00a7c\u6307\u4ee4\u683c\u5f0f\u9519\u8bef: " + entry.rawCommand + " - " + e.getMessage());
                return;
            }
            if (!entry.passengers.isEmpty()) continue;
            this.setStatus("\u00a7c\u6307\u4ee4\u4e2d\u6ca1\u6709 Passengers: " + entry.rawCommand);
            return;
        }
        this.commandQueue = queue;
        this.commandIndex = 0;
        this.fixedPos = fixedPos;
        this.entitySpacing = Math.max(0.0, spacing);
        this.viewRange = Math.max(0.0, viewRange);
        this.syncWaitTicks = Math.max(0, placeDelayMs / 50);
        this.interDelayTicks = Math.max(0, generationDelayMs / 50);
        this.originalItem = player.getItemInHand(InteractionHand.MAIN_HAND).copy();
        this.selectedSlot = player.getInventory().selected;
        this.tickCounter = 0;
        this.loadCurrentCommand();
        this.running = true;
        this.phase = Phase.SYNC_EGG;
        this.setStatus("\u00a7a\u5f00\u59cb\u653e\u7f6e\u7b2c " + (this.commandIndex + 1) + " \u884c\uff0c\u5171 " + this.totalCount + " \u4e2a.");
        Fku.LOGGER.info("[DisplayModel] \u5f00\u59cb\u653e\u7f6e\uff0c\u5171{}\u884c\uff0csyncWait={}tick, interDelay={}tick", new Object[]{queue.size(), this.syncWaitTicks, this.interDelayTicks});
    }

    private void loadCurrentCommand() {
        CommandEntry entry = this.commandQueue.get(this.commandIndex);
        this.passengers = entry.passengers;
        this.offset = entry.offset;
        this.currentIndex = 0;
        this.totalCount = this.passengers.size();
        this.placeRetries = 0;
    }

    public void stop() {
        if (!this.running) {
            return;
        }
        this.doRestoreItem();
        this.running = false;
        this.phase = Phase.IDLE;
        this.commandQueue = null;
        this.setStatus("\u00a7c\u5df2\u4e2d\u6b62");
        Fku.LOGGER.info("[DisplayModel] \u653e\u7f6e\u5df2\u4e2d\u6b62");
    }

    public boolean isRunning() {
        return this.running;
    }

    public int getCurrentIndex() {
        return this.currentIndex;
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    public String getStatusMessage() {
        return this.statusMessage;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        if (this.running) {
            this.tick();
        }
    }

    private void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            this.stop();
            return;
        }
        if (!player.isCreative()) {
            this.stop();
            this.setStatus("\u00a7c\u521b\u9020\u6a21\u5f0f\u88ab\u53d6\u6d88");
            return;
        }
        switch (this.phase) {
            case SYNC_EGG: {
                this.doSyncEgg();
                if (this.syncWaitTicks <= 0) {
                    this.phase = Phase.PLACE_ENTITY;
                } else {
                    this.phase = Phase.WAIT_SYNC;
                    this.tickCounter = 0;
                }
                this.fireStatusUpdate();
                break;
            }
            case WAIT_SYNC: {
                ++this.tickCounter;
                if (this.tickCounter >= this.syncWaitTicks) {
                    this.phase = Phase.PLACE_ENTITY;
                    this.tickCounter = 0;
                }
                this.fireStatusUpdate();
                break;
            }
            case PLACE_ENTITY: {
                boolean success = this.doPlaceEntity();
                if (!success) {
                    if (this.placeRetries < 3) {
                        ++this.placeRetries;
                        this.phase = Phase.RETRY_PLACE;
                        this.setStatus("\u00a7e\u653e\u7f6e\u91cd\u8bd5 " + this.placeRetries + "/3 (index=" + this.currentIndex + ")");
                        Fku.LOGGER.warn("[DisplayModel] \u653e\u7f6e\u5931\u8d25\uff0c\u51c6\u5907\u91cd\u8bd5 {}/{}, index={}", new Object[]{this.placeRetries, 3, this.currentIndex});
                    } else {
                        this.setStatus("\u00a7c\u653e\u7f6e\u5931\u8d25\uff0c\u8df3\u8fc7\u5b9e\u4f53 " + (this.currentIndex + 1) + "/" + this.totalCount);
                        Fku.LOGGER.warn("[DisplayModel] \u653e\u7f6e\u5931\u8d25\u8d85\u8fc7{}\u6b21\uff0c\u8df3\u8fc7\u5b9e\u4f53 index={}", 3, this.currentIndex);
                        this.advanceToNext();
                    }
                } else {
                    this.placeRetries = 0;
                    this.advanceToNext();
                }
                this.fireStatusUpdate();
                break;
            }
            case RETRY_PLACE: {
                this.phase = Phase.PLACE_ENTITY;
                this.fireStatusUpdate();
                break;
            }
            case INTER_DELAY: {
                ++this.tickCounter;
                if (this.tickCounter >= this.interDelayTicks) {
                    this.phase = Phase.SYNC_EGG;
                    this.tickCounter = 0;
                }
                this.fireStatusUpdate();
                break;
            }
            case RESTORE_ITEM: {
                this.doRestoreItem();
                this.running = false;
                this.phase = Phase.IDLE;
                this.commandQueue = null;
                this.setStatus("\u00a7a\u5168\u90e8\u5b8c\u6210\uff01\u5171 " + (this.commandQueue == null ? 0 : this.commandQueue.size()) + " \u884c");
                Fku.LOGGER.info("[DisplayModel] \u653e\u7f6e\u5b8c\u6210");
                this.fireStatusUpdate();
                break;
            }
            default: {
                this.running = false;
                this.phase = Phase.IDLE;
                this.fireStatusUpdate();
            }
        }
    }

    private void doSyncEgg() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || this.currentIndex >= this.totalCount) {
            return;
        }
        double targetX = this.fixedPos.getX() + 0.5 + this.offset.x;
        double targetY = this.fixedPos.getY() + this.offset.y;
        double targetZ = this.fixedPos.getZ() + 0.5 + this.offset.z;
        CompoundTag passengerTag = this.passengers.get(this.currentIndex).copy();
        ListTag posList = new ListTag();
        posList.add(DoubleTag.valueOf(targetX));
        posList.add(DoubleTag.valueOf(targetY));
        posList.add(DoubleTag.valueOf(targetZ));
        passengerTag.put("Pos", (Tag)posList);
        DisplayModelManager.ensureRotationTag(passengerTag);
        ItemStack spawnEgg = this.createSpawnEgg(passengerTag);
        player.getInventory().items.set(this.selectedSlot, spawnEgg.copy());
        player.getInventory().setChanged();
        DisplayModelManager.syncItemToServer(this.selectedSlot, spawnEgg.copy());
        player.setItemInHand(InteractionHand.MAIN_HAND, spawnEgg.copy());
        Fku.LOGGER.debug("[DisplayModel] \u540c\u6b65\u5237\u602a\u86cb\u5230\u69fd\u4f4d {} pos=({}, {}, {})", new Object[]{this.selectedSlot, String.format("%.1f", targetX), String.format("%.1f", targetY), String.format("%.1f", targetZ)});
    }

    private boolean doPlaceEntity() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return false;
        }
        double targetX = this.fixedPos.getX() + 0.5 + this.offset.x;
        double targetY = this.fixedPos.getY() + this.offset.y;
        double targetZ = this.fixedPos.getZ() + 0.5 + this.offset.z;
        CompoundTag passengerTag = this.passengers.get(this.currentIndex).copy();
        ListTag posList = new ListTag();
        posList.add(DoubleTag.valueOf(targetX));
        posList.add(DoubleTag.valueOf(targetY));
        posList.add(DoubleTag.valueOf(targetZ));
        passengerTag.put("Pos", (Tag)posList);
        DisplayModelManager.ensureRotationTag(passengerTag);
        ItemStack egg = this.createSpawnEgg(passengerTag);
        player.getInventory().items.set(this.selectedSlot, egg.copy());
        DisplayModelManager.syncItemToServer(this.selectedSlot, egg.copy());
        BlockPos playerBlock = player.blockPosition();
        BlockHitResult hitResult = new BlockHitResult(new Vec3(playerBlock.getX() + 0.5, playerBlock.getY(), playerBlock.getZ() + 0.5), Direction.UP, playerBlock, false);
        try {
            InteractionResult result = mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hitResult);
            if (result == InteractionResult.FAIL) {
                Fku.LOGGER.warn("[DisplayModel] useItemOn \u5931\u8d25 (index={})", this.currentIndex);
                return false;
            }
            player.swing(InteractionHand.MAIN_HAND);
            return true;
        }
        catch (Exception e) {
            Fku.LOGGER.error("[DisplayModel] useItemOn \u5f02\u5e38: {}", e.getMessage(), e);
            return false;
        }
    }

    private static void syncItemToServer(int slotIndex, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) {
            return;
        }
        mc.gameMode.handleCreativeModeItemAdd(stack.copy(), 36 + slotIndex);
        mc.player.setItemInHand(InteractionHand.MAIN_HAND, stack.copy());
    }

    private void doRestoreItem() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || this.originalItem == null) {
            return;
        }
        player.getInventory().items.set(this.selectedSlot, this.originalItem.copy());
        player.setItemInHand(InteractionHand.MAIN_HAND, this.originalItem.copy());
        DisplayModelManager.syncItemToServer(this.selectedSlot, this.originalItem.copy());
    }

    private static void ensureRotationTag(CompoundTag tag) {
        if (!tag.contains("Rotation")) {
            ListTag rotation = new ListTag();
            rotation.add(FloatTag.valueOf(0.0f));
            rotation.add(FloatTag.valueOf(0.0f));
            tag.put("Rotation", (Tag)rotation);
        }
    }

    private ItemStack createSpawnEgg(CompoundTag passengerTag) {
        ItemStack egg = new ItemStack((ItemLike)Items.BAT_SPAWN_EGG, 1);
        CompoundTag tag = new CompoundTag();
        CompoundTag enchantment = new CompoundTag();
        enchantment.putString("id", "minecraft:sharpness");
        enchantment.putInt("lvl", 1);
        ListTag enchantments = new ListTag();
        enchantments.add(enchantment);
        tag.put("Enchantments", (Tag)enchantments);
        if (this.viewRange > 0.0) {
            passengerTag.putFloat("view_range", (float)this.viewRange);
        }
        tag.put("EntityTag", (Tag)passengerTag);
        CompoundTag display = new CompoundTag();
        display.putString("Name", "{\"translate\":\"entity.minecraft.block_display\"}");
        tag.put("display", (Tag)display);
        egg.setTag(tag);
        return egg;
    }

    private void advanceToNext() {
        ++this.currentIndex;
        if (this.currentIndex >= this.totalCount) {
            if (this.commandIndex + 1 < this.commandQueue.size()) {
                ++this.commandIndex;
                this.loadCurrentCommand();
                this.phase = Phase.SYNC_EGG;
                this.setStatus("\u00a7a\u5207\u6362\u5230\u7b2c " + (this.commandIndex + 1) + " \u884c\uff0c\u5171 " + this.totalCount + " \u4e2a");
            } else {
                this.phase = Phase.RESTORE_ITEM;
                this.setStatus("\u00a7a\u5168\u90e8\u653e\u7f6e\u5b8c\u6210\uff01");
            }
        } else if (this.interDelayTicks <= 0) {
            this.phase = Phase.SYNC_EGG;
            this.setStatus("\u00a7a\u5df2\u653e\u7f6e " + this.currentIndex + "/" + this.totalCount);
        } else {
            this.phase = Phase.INTER_DELAY;
            this.tickCounter = 0;
            this.setStatus("\u00a7a\u5df2\u653e\u7f6e " + this.currentIndex + "/" + this.totalCount);
        }
    }

    private void setStatus(String msg) {
        this.statusMessage = msg;
    }

    private void fireStatusUpdate() {
        if (this.onStatusUpdate != null) {
            this.onStatusUpdate.run();
        }
    }

    private static enum Phase {
        IDLE,
        SYNC_EGG,
        WAIT_SYNC,
        PLACE_ENTITY,
        RETRY_PLACE,
        INTER_DELAY,
        RESTORE_ITEM;

    }

    public static class CommandEntry {
        public final String rawCommand;
        public List<CompoundTag> passengers;
        public Vec3 offset;

        public CommandEntry(String cmd) {
            this.rawCommand = cmd;
        }
    }
}

