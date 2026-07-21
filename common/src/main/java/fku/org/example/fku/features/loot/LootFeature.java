package fku.org.example.fku.features.loot;

import fku.org.example.fku.features.loot.LootConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value={Dist.CLIENT})
public class LootFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"FKU-Loot");
    private static State state = State.IDLE;
    private static final Queue<BlockPos> containerQueue = new ArrayDeque<BlockPos>();
    private static BlockPos currentContainer = null;
    private static int tickCounter = 0;
    private static int currentSlotIndex = 0;
    private static int containerSlotCount = 0;
    private static long lastClickTime = 0L;
    private static long lastCloseTime = 0L;
    private static String statusMessage = "";
    private static int overflowSlot = -1;
    private static final int OVERFLOW_PICKUP_DONE = -2;
    private static final Set<BlockPos> visitedContainers = new HashSet<BlockPos>();
    private static boolean overflowNotified = false;
    private static int scanTimer = 0;
    private static boolean waitingKeyBind = false;
    private static Runnable onKeyBoundCallback = null;

    public static void start() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.f_91073_ == null) {
            LootFeature.reset("\u542f\u52a8\u5931\u8d25\uff1a\u73a9\u5bb6\u6216\u4e16\u754c\u4e3a\u7a7a");
            return;
        }
        LootFeature.refreshContainerQueue(mc);
        overflowNotified = false;
        scanTimer = 0;
        if (!containerQueue.isEmpty()) {
            currentContainer = containerQueue.poll();
            state = State.OPEN;
            tickCounter = 0;
            statusMessage = "\u4e00\u952e\u53d6\u7269\uff1a\u53d1\u73b0 " + (containerQueue.size() + 1) + " \u4e2a\u5bb9\u5668\uff0c\u5f00\u59cb\u53d6\u7269";
        } else {
            state = State.IDLE;
            statusMessage = "\u4e00\u952e\u53d6\u7269\uff1a\u9644\u8fd1\u65e0\u5bb9\u5668\uff0c\u6301\u7eed\u626b\u63cf\u4e2d.";
        }
        LOGGER.info("\u4e00\u952e\u53d6\u7269\u542f\u52a8");
    }

    public static void stop() {
        visitedContainers.clear();
        overflowNotified = false;
        LootFeature.reset("\u7528\u6237\u5173\u95ed");
    }

    public static boolean isRunning() {
        return state != State.IDLE;
    }

    public static String getStatus() {
        return statusMessage;
    }

    public static void clearVisitedMarkers() {
        visitedContainers.clear();
    }

    public static void startKeyBind(Runnable onBound) {
        waitingKeyBind = true;
        onKeyBoundCallback = onBound;
    }

    public static void cancelKeyBind() {
        waitingKeyBind = false;
        onKeyBoundCallback = null;
    }

    public static boolean isWaitingKeyBind() {
        return waitingKeyBind;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        LootConfig cfg = LootConfig.getInstance();
        if (!cfg.enabled) {
            if (state != State.IDLE) {
                visitedContainers.clear();
                LootFeature.reset("\u529f\u80fd\u5df2\u5173\u95ed");
            }
            return;
        }
        if (++scanTimer >= cfg.scanRefreshInterval) {
            scanTimer = 0;
            LootFeature.refreshContainerQueue(mc);
        }
        if (state == State.IDLE && !containerQueue.isEmpty()) {
            currentContainer = containerQueue.poll();
            state = State.OPEN;
            tickCounter = 0;
            statusMessage = "\u4e00\u952e\u53d6\u7269\uff1a\u68c0\u6d4b\u5230\u65b0\u5bb9\u5668\uff0c\u5f00\u59cb\u53d6\u7269";
        }
        switch (state) {
            case IDLE: {
                break;
            }
            case OPEN: {
                LootFeature.handleOpen(mc);
                break;
            }
            case WAIT_OPEN: {
                LootFeature.handleWaitOpen(mc);
                break;
            }
            case LOOT: {
                LootFeature.handleLoot(mc);
                break;
            }
            case CLOSE: {
                LootFeature.handleClose(mc);
                break;
            }
            case WAIT_CLOSE: {
                LootFeature.handleWaitClose(mc);
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        LootConfig cfg = LootConfig.getInstance();
        if (waitingKeyBind) {
            if (event.getAction() != 1) {
                return;
            }
            if (event.getKey() == 256) {
                LootFeature.cancelKeyBind();
                mc.player.m_5661_(Component.literal((String)"\u00a76[\u4e00\u952e\u53d6\u7269] \u00a7c\u70ed\u952e\u7ed1\u5b9a\u5df2\u53d6\u6d88"), false);
                return;
            }
            cfg.setHotkeyKey(event.getKey());
            String keyName = GLFW.glfwGetKeyName(event.getKey(), event.getScanCode());
            if (keyName == null || keyName.isEmpty()) {
                keyName = switch (event.getKey()) {
                    case 340 -> "LSHIFT";
                    case 344 -> "RSHIFT";
                    case 341 -> "LCTRL";
                    case 345 -> "RCTRL";
                    case 342 -> "LALT";
                    case 346 -> "RALT";
                    case 32 -> "SPACE";
                    case 258 -> "TAB";
                    case 257 -> "ENTER";
                    case 280 -> "CAPS";
                    default -> "KEY_" + event.getKey();
                };
            } else {
                keyName = keyName.toUpperCase();
            }
            cfg.setHotkeyName(keyName);
            waitingKeyBind = false;
            mc.player.m_5661_(Component.literal((String)("\u00a76[\u4e00\u952e\u53d6\u7269] \u00a7a\u70ed\u952e\u5df2\u7ed1\u5b9a: \u00a7e" + keyName)), false);
            if (onKeyBoundCallback != null) {
                onKeyBoundCallback.run();
                onKeyBoundCallback = null;
            }
            return;
        }
    }

    private static void refreshContainerQueue(Minecraft mc) {
        ClientLevel level = mc.f_91073_;
        if (level == null || mc.player == null) {
            return;
        }
        LootConfig config = LootConfig.getInstance();
        BlockPos center = mc.player.m_20183_();
        int radius = config.radius;
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -radius; y <= radius; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    BlockPos immutable;
                    BlockPos pos = center.m_7918_(x, y, z);
                    BlockEntity be = level.m_7702_(pos);
                    if (!(be instanceof Container) || be instanceof EnderChestBlockEntity || visitedContainers.contains(immutable = pos.m_7949_()) || containerQueue.contains(immutable)) continue;
                    containerQueue.add(immutable);
                }
            }
        }
    }

    private static void handleOpen(Minecraft mc) {
        if (currentContainer == null) {
            LootFeature.reset("\u5f53\u524d\u5bb9\u5668\u4e3a\u7a7a");
            return;
        }
        ClientLevel level = mc.f_91073_;
        if (level == null) {
            LootFeature.reset("\u4e16\u754c\u4e3a\u7a7a");
            return;
        }
        BlockPos pos = currentContainer;
        if (mc.player.m_20238_(Vec3.m_82512_((Vec3i)pos)) > 36.0) {
            statusMessage = "\u4e00\u952e\u53d6\u7269\uff1a\u8df3\u8fc7\u8ddd\u79bb\u8fc7\u8fdc\u7684\u5bb9\u5668";
            LootFeature.moveToNextContainer();
            return;
        }
        Vec3 vec = Vec3.m_82512_((Vec3i)pos);
        BlockHitResult hit = new BlockHitResult(vec, Direction.UP, pos, false);
        mc.player.f_108617_.m_104955_((Packet)new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hit, 0));
        tickCounter = 0;
        state = State.WAIT_OPEN;
        statusMessage = "\u4e00\u952e\u53d6\u7269\uff1a\u6253\u5f00\u5bb9\u5668 " + pos.m_123344_();
    }

    private static void handleWaitOpen(Minecraft mc) {
        if (++tickCounter < 3) {
            return;
        }
        AbstractContainerMenu menu = mc.player.f_36096_;
        if (menu == null || menu.f_38840_ == 0) {
            statusMessage = "\u4e00\u952e\u53d6\u7269\uff1a\u5bb9\u5668\u6253\u5f00\u5931\u8d25\uff0c\u8df3\u8fc7";
            LootFeature.moveToNextContainer();
            return;
        }
        containerSlotCount = menu.f_38839_.size() - 36;
        if (containerSlotCount <= 0) {
            statusMessage = "\u4e00\u952e\u53d6\u7269\uff1a\u65e0\u6548\u5bb9\u5668\uff0c\u8df3\u8fc7";
            LootFeature.moveToNextContainer();
            return;
        }
        overflowNotified = false;
        currentSlotIndex = 0;
        lastClickTime = 0L;
        state = State.LOOT;
        statusMessage = "\u4e00\u952e\u53d6\u7269\uff1a\u6b63\u5728\u53d6\u51fa\u7269\u54c1.";
    }

    private static void handleLoot(Minecraft mc) {
        Slot slot;
        AbstractContainerMenu menu = mc.player.f_36096_;
        if (menu == null || menu.f_38840_ == 0) {
            statusMessage = "\u4e00\u952e\u53d6\u7269\uff1a\u5bb9\u5668\u5df2\u5173\u95ed";
            LootFeature.moveToNextContainer();
            return;
        }
        LootConfig config = LootConfig.getInstance();
        long now = System.currentTimeMillis();
        if (now - lastClickTime < config.clickDelay) {
            return;
        }
        if (overflowSlot == -2) {
            mc.player.f_108617_.m_104955_((Packet)new ServerboundContainerClickPacket(menu.f_38840_, menu.m_182424_(), -999, 0, ClickType.PICKUP, menu.m_142621_(), (Int2ObjectMap)new Int2ObjectOpenHashMap()));
            overflowSlot = -1;
            ++currentSlotIndex;
            lastClickTime = System.currentTimeMillis();
            if (config.dropOverflow && !overflowNotified) {
                overflowNotified = true;
                mc.player.m_5661_(Component.literal((String)"\u00a76[\u4e00\u952e\u53d6\u7269] \u00a7e\u80cc\u5305\u5df2\u6ee1\uff0c\u6b63\u5728\u4e22\u5f03\u591a\u4f59\u7269\u54c1"), false);
            }
            statusMessage = "\u4e00\u952e\u53d6\u7269\uff1a\u4e22\u5f03\u6ea2\u51fa\u7269\u54c1";
            return;
        }
        if (overflowSlot >= 0) {
            slot = menu.m_38853_(overflowSlot);
            if (slot != null && slot.m_6657_()) {
                if (config.dropOverflow) {
                    mc.player.f_108617_.m_104955_((Packet)new ServerboundContainerClickPacket(menu.f_38840_, menu.m_182424_(), overflowSlot, 0, ClickType.PICKUP, ItemStack.f_41583_, (Int2ObjectMap)new Int2ObjectOpenHashMap()));
                    overflowSlot = -2;
                    lastClickTime = System.currentTimeMillis();
                    if (!overflowNotified) {
                        overflowNotified = true;
                        mc.player.m_5661_(Component.literal((String)"\u00a76[\u4e00\u952e\u53d6\u7269] \u00a7e\u80cc\u5305\u5df2\u6ee1\uff0c\u6b63\u5728\u4e22\u5f03\u591a\u4f59\u7269\u54c1"), false);
                    }
                    statusMessage = "\u4e00\u952e\u53d6\u7269\uff1a\u62fe\u53d6\u6ea2\u51fa\u7269\u54c1";
                    return;
                }
                if (!overflowNotified) {
                    overflowNotified = true;
                    mc.player.m_5661_(Component.literal((String)"\u00a76[\u4e00\u952e\u53d6\u7269] \u00a7c\u80cc\u5305\u5df2\u6ee1\uff01"), false);
                }
                overflowSlot = -1;
                currentSlotIndex = containerSlotCount;
                state = State.CLOSE;
                statusMessage = "\u4e00\u952e\u53d6\u7269\uff1a\u80cc\u5305\u6ee1\uff0c\u5173\u95ed\u5bb9\u5668";
                return;
            }
            overflowSlot = -1;
        }
        while (!(currentSlotIndex >= containerSlotCount || (slot = menu.m_38853_(currentSlotIndex)) != null && slot.m_6657_())) {
            ++currentSlotIndex;
        }
        if (currentSlotIndex >= containerSlotCount) {
            state = State.CLOSE;
            statusMessage = "\u4e00\u952e\u53d6\u7269\uff1a\u5173\u95ed\u5bb9\u5668";
            overflowSlot = -1;
            return;
        }
        mc.player.f_108617_.m_104955_((Packet)new ServerboundContainerClickPacket(menu.f_38840_, menu.m_182424_(), currentSlotIndex, 0, ClickType.QUICK_MOVE, menu.m_142621_(), (Int2ObjectMap)new Int2ObjectOpenHashMap()));
        lastClickTime = System.currentTimeMillis();
        statusMessage = "\u4e00\u952e\u53d6\u7269\uff1a\u5904\u7406\u7b2c " + (currentSlotIndex + 1) + "/" + containerSlotCount + " \u683c";
        if (config.dropOverflow) {
            overflowSlot = currentSlotIndex;
        } else {
            ++currentSlotIndex;
        }
    }

    private static void handleClose(Minecraft mc) {
        LootConfig config = LootConfig.getInstance();
        AbstractContainerMenu menu = mc.player.f_36096_;
        if (menu != null && menu.f_38840_ != 0) {
            mc.player.f_108617_.m_104955_((Packet)new ServerboundContainerClosePacket(menu.f_38840_));
        }
        if (currentContainer != null) {
            visitedContainers.add(currentContainer);
        }
        if (config.autoCloseGUI && mc.screen != null) {
            mc.setScreen(null);
        }
        lastCloseTime = System.currentTimeMillis();
        tickCounter = 0;
        state = State.WAIT_CLOSE;
        statusMessage = "\u4e00\u952e\u53d6\u7269\uff1a\u7b49\u5f85\u5bb9\u5668\u5173\u95ed.";
    }

    private static void handleWaitClose(Minecraft mc) {
        LootConfig config = LootConfig.getInstance();
        if (++tickCounter < 2 || System.currentTimeMillis() - lastCloseTime < config.containerDelay) {
            return;
        }
        if (!containerQueue.isEmpty()) {
            currentContainer = containerQueue.poll();
            state = State.OPEN;
            tickCounter = 0;
            statusMessage = "\u4e00\u952e\u53d6\u7269\uff1a\u5904\u7406\u4e0b\u4e00\u4e2a\u5bb9\u5668. (" + containerQueue.size() + " \u5269\u4f59)";
        } else {
            statusMessage = "\u4e00\u952e\u53d6\u7269\uff1a\u672c\u8f6e\u5b8c\u6210\uff0c\u7b49\u5f85\u65b0\u5bb9\u5668.";
            state = State.IDLE;
            tickCounter = 0;
            LOGGER.info("\u4e00\u952e\u53d6\u7269\u672c\u8f6e\u5b8c\u6210\uff0c\u7b49\u5f85\u5b9a\u65f6\u5668\u626b\u63cf\u65b0\u5bb9\u5668");
        }
    }

    private static void moveToNextContainer() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.f_36096_ != null && mc.player.f_36096_.f_38840_ != 0) {
            mc.player.f_108617_.m_104955_((Packet)new ServerboundContainerClosePacket(mc.player.f_36096_.f_38840_));
        }
        if (!containerQueue.isEmpty()) {
            currentContainer = containerQueue.poll();
            state = State.OPEN;
            tickCounter = 0;
        } else {
            state = State.IDLE;
            statusMessage = "\u4e00\u952e\u53d6\u7269\uff1a\u7b49\u5f85\u65b0\u5bb9\u5668.";
        }
    }

    private static void reset(String reason) {
        state = State.IDLE;
        containerQueue.clear();
        currentContainer = null;
        currentSlotIndex = 0;
        containerSlotCount = 0;
        overflowSlot = -1;
        tickCounter = 0;
        statusMessage = "";
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.f_36096_ != null && mc.player.f_36096_.f_38840_ != 0) {
                mc.player.f_108617_.m_104955_((Packet)new ServerboundContainerClosePacket(mc.player.f_36096_.f_38840_));
            }
        }
        catch (Exception exception) {
            // ignored
        }
        LOGGER.info("\u4e00\u952e\u53d6\u7269\u4e2d\u6b62\uff1a{}", reason);
    }

    private static enum State {
        IDLE,
        OPEN,
        WAIT_OPEN,
        LOOT,
        CLOSE,
        WAIT_CLOSE;

    }
}

