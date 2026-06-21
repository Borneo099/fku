package fku.org.example.fku.features.loot; /* water */

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * 一键取物（Loot Nearby Containers）核心逻辑 —— 状态机
 *
 * ★ 设计思想（矛盾论）：
 *   主要矛盾是「容器扫描的广度 × 取物交互的精确性」。
 *   采用状态机驱动 ClientTickEvent，每 tick 推进一个步骤，
 *   在「不阻塞渲染」的前提下完成多容器序列化交互。
 *
 * ★ v2.0 关键变更（实践论）：
 *   - 热键改为开关模式：按一次开启持续自动取物，再按关闭
 *   - 已取容器标记：visitedContainers 避免循环
 *   - 背包满提示：首次溢出时客户端消息通知
 *   - 连续扫描：取完一轮后若功能仍启用则自动重扫
 *
 * ★ 工作流程：
 *   IDLE -> SCAN -> OPEN -> WAIT_OPEN -> LOOT ->
 *   CLOSE -> WAIT_CLOSE -> SCAN(loop) -> IDLE
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class LootFeature {

    private static final Logger LOGGER = LoggerFactory.getLogger("FKU-Loot");

    // ════════ 状态枚举 ════════
    private enum State {
        IDLE,
        SCAN,
        OPEN,
        WAIT_OPEN,
        LOOT,
        CLOSE,
        WAIT_CLOSE
    }

    // ════════ 状态机字段 ════════
    private static State state = State.IDLE;
    private static final Queue<BlockPos> containerQueue = new ArrayDeque<>();
    private static BlockPos currentContainer = null;
    private static int tickCounter = 0;
    private static int currentSlotIndex = 0;
    private static int containerSlotCount = 0;
    private static long lastClickTime = 0;
    private static long lastCloseTime = 0;
    private static String statusMessage = "";

    // ════════ 溢出（dropOverflow）字段 ════════
    private static int overflowSlot = -1;
    private static final int OVERFLOW_PICKUP_DONE = -2;

    // ════════ 已取容器标记（避免循环） ════════
    private static final Set<BlockPos> visitedContainers = new HashSet<>();

    // ════════ 背包满通知（每轮只提示一次） ════════
    private static boolean overflowNotified = false;

    // ════════ 热键绑定字段 ════════
    private static boolean waitingKeyBind = false;
    private static Runnable onKeyBoundCallback = null;

    // ════════ 外部触发接口 ════════

    /** 开启自动取物 */
    public static void start() {
        if (Minecraft.getInstance().player == null) return;
        state = State.SCAN;
        overflowNotified = false;
        statusMessage = "一键取物：开始扫描...";
        LOGGER.info("一键取物启动");
    }

    /** 关闭自动取物，清空已取标记 */
    public static void stop() {
        visitedContainers.clear();
        overflowNotified = false;
        reset("用户关闭");
    }

    public static boolean isRunning() {
        return state != State.IDLE;
    }

    public static String getStatus() {
        return statusMessage;
    }

    /** 清空已取容器标记（热键关闭时自动调用） */
    public static void clearVisitedMarkers() {
        visitedContainers.clear();
    }

    /** 设置热键绑定模式 */
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

    // ════════ 事件处理器 ════════

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!LootConfig.getInstance().enabled) {
            if (state != State.IDLE) {
                visitedContainers.clear();
                reset("功能已关闭");
            }
            return;
        }

        switch (state) {
            case IDLE -> {}
            case SCAN -> handleScan(mc);
            case OPEN -> handleOpen(mc);
            case WAIT_OPEN -> handleWaitOpen(mc);
            case LOOT -> handleLoot(mc);
            case CLOSE -> handleClose(mc);
            case WAIT_CLOSE -> handleWaitClose(mc);
        }
    }

    /**
     * 热键处理：开关模式
     *   ★ 按一次开启 -> 自动持续取物
     *   ★ 再按一次关闭 -> 清空标记
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        LootConfig cfg = LootConfig.getInstance();

        // ── 热键绑定模式 ──
        if (waitingKeyBind) {
            if (event.getAction() != GLFW.GLFW_PRESS) return;
            if (event.getKey() == GLFW.GLFW_KEY_ESCAPE) {
                cancelKeyBind();
                mc.player.displayClientMessage(
                    Component.literal("§6[一键取物] §c热键绑定已取消"), false);
                return;
            }
            cfg.setHotkeyKey(event.getKey());
            String keyName = GLFW.glfwGetKeyName(event.getKey(), event.getScanCode());
            if (keyName == null || keyName.isEmpty()) {
                keyName = switch (event.getKey()) {
                    case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
                    case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
                    case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
                    case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
                    case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
                    case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
                    case GLFW.GLFW_KEY_SPACE -> "SPACE";
                    case GLFW.GLFW_KEY_TAB -> "TAB";
                    case GLFW.GLFW_KEY_ENTER -> "ENTER";
                    case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS";
                    default -> "KEY_" + event.getKey();
                };
            } else {
                keyName = keyName.toUpperCase();
            }
            cfg.setHotkeyName(keyName);
            waitingKeyBind = false;
            mc.player.displayClientMessage(
                Component.literal("§6[一键取物] §a热键已绑定: §e" + keyName), false);
            if (onKeyBoundCallback != null) {
                onKeyBoundCallback.run();
                onKeyBoundCallback = null;
            }
            return;
        }

        // ── 开关模式：热键 toggle ──
        if (cfg.hotkeyKey < 0) return;
        if (event.getKey() != cfg.hotkeyKey) return;
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        if (mc.screen != null) return;

        if (cfg.enabled) {
            cfg.setEnabled(false);
            visitedContainers.clear();
            overflowNotified = false;
            if (mc.player != null) {
                mc.player.displayClientMessage(
                    Component.literal("§6[一键取物] §c已关闭，清空容器标记"), false);
            }
        } else {
            cfg.setEnabled(true);
            if (mc.player != null) {
                mc.player.displayClientMessage(
                    Component.literal("§6[一键取物] §a已开启，开始自动取物"), false);
            }
            start();
        }
    }

    // ════════ 各状态 handler ════════

    /**
     * SCAN：扫描周围容器，跳过已取过的
     */
    private static void handleScan(Minecraft mc) {
        Level level = mc.level;
        if (level == null) { reset("世界为空"); return; }

        LootConfig config = LootConfig.getInstance();
        BlockPos center = mc.player.blockPosition();
        int radius = config.radius;
        containerQueue.clear();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof Container && !(be instanceof EnderChestBlockEntity)) {
                        BlockPos immutable = pos.immutable();
                        if (visitedContainers.contains(immutable)) continue;
                        containerQueue.add(immutable);
                    }
                }
            }
        }

        if (containerQueue.isEmpty()) {
            statusMessage = "一键取物：附近无未取容器";
            state = State.IDLE;
            return;
        }

        statusMessage = "一键取物：发现 " + containerQueue.size() + " 个容器";
        state = State.OPEN;
        currentContainer = containerQueue.poll();
        tickCounter = 0;
    }

    private static void handleOpen(Minecraft mc) {
        if (currentContainer == null) { reset("当前容器为空"); return; }

        Level level = mc.level;
        //noinspection IfStatementWithIdenticalBranches 保持清晰语义：如果一个方向不行也没关系 break fallback
        if (level == null) { reset("世界为空"); return; }

        BlockPos pos = currentContainer;
        // 距离 > 6 块跳过
        if (mc.player.distanceToSqr(Vec3.atCenterOf(pos)) > 36) {
            statusMessage = "一键取物：跳过距离过远的容器";
            moveToNextContainer();
            return;
        }

        Vec3 vec = Vec3.atCenterOf(pos);
        BlockHitResult hit = new BlockHitResult(vec, Direction.UP, pos, false);
        mc.player.connection.send(new ServerboundUseItemOnPacket(
                InteractionHand.MAIN_HAND, hit, 0));

        tickCounter = 0;
        state = State.WAIT_OPEN;
        statusMessage = "一键取物：打开容器 " + pos.toShortString();
    }

    private static void handleWaitOpen(Minecraft mc) {
        tickCounter++;
        if (tickCounter < 3) return;

        AbstractContainerMenu menu = mc.player.containerMenu;
        if (menu == null || menu.containerId == 0) {
            statusMessage = "一键取物：容器打开失败，跳过";
            moveToNextContainer();
            return;
        }

        containerSlotCount = menu.slots.size() - 36;
        if (containerSlotCount <= 0) {
            statusMessage = "一键取物：无效容器，跳过";
            moveToNextContainer();
            return;
        }

        overflowNotified = false;
        currentSlotIndex = 0;
        lastClickTime = 0;
        state = State.LOOT;
        statusMessage = "一键取物：正在取出物品...";
    }

    /**
     * LOOT：每 tick 处理一格
     *   ★ dropOverflow 溢出时 PICKUP + THROW
     *   ★ 背包满首次提示
     */
    private static void handleLoot(Minecraft mc) {
        AbstractContainerMenu menu = mc.player.containerMenu;
        if (menu == null || menu.containerId == 0) {
            statusMessage = "一键取物：容器已关闭";
            moveToNextContainer();
            return;
        }

        LootConfig config = LootConfig.getInstance();
        long now = System.currentTimeMillis();
        if (now - lastClickTime < config.clickDelay) return;

        // ── 阶段1：THROW ──
        if (overflowSlot == OVERFLOW_PICKUP_DONE) {
            mc.player.connection.send(new ServerboundContainerClickPacket(
                    menu.containerId, menu.getStateId(), -999, 0,
                    ClickType.PICKUP, menu.getCarried(), new Int2ObjectOpenHashMap<>()));
            overflowSlot = -1;
            currentSlotIndex++;
            lastClickTime = System.currentTimeMillis();
            if (config.dropOverflow && !overflowNotified) {
                overflowNotified = true;
                mc.player.displayClientMessage(
                    Component.literal("§6[一键取物] §e背包已满，正在丢弃多余物品"), false);
            }
            statusMessage = "一键取物：丢弃溢出物品";
            return;
        }

        // ── 阶段2：验证上一 QUICK_MOVE ──
        if (overflowSlot >= 0) {
            Slot slot = menu.getSlot(overflowSlot);
            if (slot != null && slot.hasItem()) {
                if (config.dropOverflow) {
                    mc.player.connection.send(new ServerboundContainerClickPacket(
                            menu.containerId, menu.getStateId(), overflowSlot, 0,
                            ClickType.PICKUP, ItemStack.EMPTY, new Int2ObjectOpenHashMap<>()));
                    overflowSlot = OVERFLOW_PICKUP_DONE;
                    lastClickTime = System.currentTimeMillis();
                    if (!overflowNotified) {
                        overflowNotified = true;
                        mc.player.displayClientMessage(
                            Component.literal("§6[一键取物] §e背包已满，正在丢弃多余物品"), false);
                    }
                    statusMessage = "一键取物：拾取溢出物品";
                    return;
                } else {
                    if (!overflowNotified) {
                        overflowNotified = true;
                        mc.player.displayClientMessage(
                            Component.literal("§6[一键取物] §c背包已满！"), false);
                    }
                    overflowSlot = -1;
                    currentSlotIndex = containerSlotCount;
                    state = State.CLOSE;
                    statusMessage = "一键取物：背包满，关闭容器";
                    return;
                }
            }
            overflowSlot = -1;
        }

        // ── 阶段3：跳过空格 ──
        while (currentSlotIndex < containerSlotCount) {
            Slot slot = menu.getSlot(currentSlotIndex);
            if (slot != null && slot.hasItem()) break;
            currentSlotIndex++;
        }

        if (currentSlotIndex >= containerSlotCount) {
            state = State.CLOSE;
            statusMessage = "一键取物：关闭容器";
            overflowSlot = -1;
            return;
        }

        // ── 阶段4：QUICK_MOVE ──
        mc.player.connection.send(new ServerboundContainerClickPacket(
                menu.containerId, menu.getStateId(), currentSlotIndex, 0,
                ClickType.QUICK_MOVE, menu.getCarried(), new Int2ObjectOpenHashMap<>()));
        lastClickTime = System.currentTimeMillis();
        statusMessage = "一键取物：处理第 " + (currentSlotIndex + 1) + "/" + containerSlotCount + " 格";

        if (config.dropOverflow) {
            overflowSlot = currentSlotIndex;
        } else {
            currentSlotIndex++;
        }
    }

    /**
     * CLOSE：关闭 + 标记已取
     */
    private static void handleClose(Minecraft mc) {
        LootConfig config = LootConfig.getInstance();

        AbstractContainerMenu menu = mc.player.containerMenu;
        if (menu != null && menu.containerId != 0) {
            mc.player.connection.send(new ServerboundContainerClosePacket(menu.containerId));
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
        statusMessage = "一键取物：等待容器关闭...";
    }

    /**
     * WAIT_CLOSE：等待后处理下一容器或重扫
     */
    private static void handleWaitClose(Minecraft mc) {
        LootConfig config = LootConfig.getInstance();

        tickCounter++;
        if (tickCounter < 2 || System.currentTimeMillis() - lastCloseTime < config.containerDelay) return;

        if (!containerQueue.isEmpty()) {
            currentContainer = containerQueue.poll();
            state = State.OPEN;
            tickCounter = 0;
            statusMessage = "一键取物：处理下一个容器... (" + containerQueue.size() + " 剩余)";
        } else {
            if (config.enabled) {
                statusMessage = "一键取物：本轮完成，重新扫描...";
                state = State.SCAN;
                tickCounter = 0;
                LOGGER.info("一键取物本轮完成，继续循环扫描");
            } else {
                statusMessage = "一键取物：全部完成";
                state = State.IDLE;
                LOGGER.info("一键取物完成");
                mc.player.displayClientMessage(
                    Component.literal("§6[一键取物] §a全部完成"), false);
            }
        }
    }

    // ════════ 辅助方法 ════════

    private static void moveToNextContainer() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.containerMenu != null && mc.player.containerMenu.containerId != 0) {
            mc.player.connection.send(new ServerboundContainerClosePacket(
                    mc.player.containerMenu.containerId));
        }
        if (!containerQueue.isEmpty()) {
            currentContainer = containerQueue.poll();
            state = State.OPEN;
            tickCounter = 0;
        } else {
            if (LootConfig.getInstance().enabled) {
                state = State.SCAN;
                statusMessage = "一键取物：队列为空，重扫";
            } else {
                statusMessage = "一键取物：全部完成";
                state = State.IDLE;
            }
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
            if (mc.player != null && mc.player.containerMenu != null
                    && mc.player.containerMenu.containerId != 0) {
                mc.player.connection.send(new ServerboundContainerClosePacket(
                        mc.player.containerMenu.containerId));
            }
        } catch (Exception ignored) {}
        LOGGER.info("一键取物中止：{}", reason);
    }
}