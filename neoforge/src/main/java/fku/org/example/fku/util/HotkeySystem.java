package fku.org.example.fku.util;

import fku.org.example.fku.Fku;
import fku.org.example.fku.util.FeatureHotkeyManager.IHotkeyInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.tick.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局热键系统 — 参考 lexis: GLFW 轮询 + 持久 pressedKeys 边缘检测
 * <p>
 * 每个 hack 的按键状态持久跟踪，按下瞬间触发一次，释放不触发。
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HotkeySystem {

    private static final Minecraft mc = Minecraft.getInstance();

    // ── 绑定模式 ──
    private static String waitingFeature = null;
    private static Runnable waitingCallback = null;

    // ── 触发模式（参考 lexis LexisClient） ──
    private static final Map<String, Runnable> triggers = new HashMap<>();
    /** 每个按键的上一次状态：keyCode → 上一 tick 是否按下（独立跟踪，避免跨功能干扰） */
    private static final Map<Integer, Boolean> prevKeyState = new HashMap<>();

    // ═══════ 公开 API ═══════

    public static void registerFeature(String featureName, Runnable action) {
        if (featureName != null && action != null)
            triggers.put(featureName, action);
    }

    public static boolean startBinding(String featureName, Runnable onComplete) {
        if (featureName == null) return false;
        if (featureName.equals(waitingFeature)) return false;
        if (waitingFeature != null) finishBind();
        waitingFeature = featureName;
        waitingCallback = onComplete;
        if (mc.player != null)
            mc.player.displayClientMessage(Component.literal("§e[热键] 按任意键绑定 " + featureName + "，Delete 删除，Esc 取消"), false);
        return true;
    }

    /** ClickGuiScreen 调用：ESC 取消绑定 */
    public static void cancelBinding() {
        if (waitingFeature == null) return;
        if (mc.player != null)
            mc.player.displayClientMessage(Component.literal("§7[热键] 已取消"), false);
        finishBind();
    }

    public static boolean isWaitingFor(String featureName) {
        return featureName != null && featureName.equals(waitingFeature);
    }

    public static boolean isWaiting() { return waitingFeature != null; }

    // ═══════ GLFW 轮询（参考 lexis: 持续跟踪 pressedKeys） ═══════

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.getWindow() == null) return;
        long window = mc.getWindow().getWindow();

        // ── 绑定模式：捕获下一个有效按键 ──
        if (waitingFeature != null) {
            for (int key = 32; key < 512; key++) {
                if (GLFW.glfwGetKey(window, key) != GLFW.GLFW_PRESS) continue;

                if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE) {
                    IHotkeyInterface hk = FeatureHotkeyManager.getInstance().getHotkey(waitingFeature);
                    hk.setHotkeyKey(-1);
                    hk.setHotkeyName("");
                    hk.saveConfig();
                    if (mc.player != null)
                        mc.player.displayClientMessage(Component.literal("§e[热键] " + waitingFeature + " 热键已删除"), false);
                    finishBind();
                    return;
                }
                // ESC → 由 ClickGuiScreen.keyPressed 处理
                if (key == GLFW.GLFW_KEY_ESCAPE) return;
                if (isModifier(key)) continue;

                String name = GLFW.glfwGetKeyName(key, 0);
                if (name == null || name.isEmpty()) name = "Key#" + key;
                IHotkeyInterface hk = FeatureHotkeyManager.getInstance().getHotkey(waitingFeature);
                hk.setHotkeyKey(key);
                hk.setHotkeyName(name);
                hk.saveConfig();
                if (mc.player != null)
                    mc.player.displayClientMessage(Component.literal("§a[热键] " + waitingFeature + " 已绑定: " + name), false);
                finishBind();
                return;
            }
            return;
        }

        // ── 触发模式：有 GUI 打开时不触发（防误触，如打开箱子按 R 整理） ──
        if (mc.screen != null) {
            // 但保留按键状态跟踪，避免恢复后 key 状态错乱
            for (var entry : triggers.entrySet()) {
                IHotkeyInterface hk = FeatureHotkeyManager.getInstance().getHotkey(entry.getKey());
                int keyCode = hk.getHotkeyKey();
                if (keyCode < 0) continue;
                boolean isDown = GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS;
                prevKeyState.put(keyCode, isDown);
            }
            return;
        }

        for (var entry : triggers.entrySet()) {
            IHotkeyInterface hk = FeatureHotkeyManager.getInstance().getHotkey(entry.getKey());
            int keyCode = hk.getHotkeyKey();
            if (keyCode < 0) continue;

            boolean isDown = GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS;
            boolean wasDown = prevKeyState.getOrDefault(keyCode, false);

            // 按下瞬间触发一次（边缘触发）：当前按下 + 上一 tick 未按下
            if (isDown && !wasDown) {
                try { entry.getValue().run(); }
                catch (Exception ignored) {}
            }

            // 持久跟踪状态
            prevKeyState.put(keyCode, isDown);
        }
    }

    private static boolean isModifier(int key) {
        return key == GLFW.GLFW_KEY_LEFT_SHIFT || key == GLFW.GLFW_KEY_RIGHT_SHIFT ||
               key == GLFW.GLFW_KEY_LEFT_CONTROL || key == GLFW.GLFW_KEY_RIGHT_CONTROL ||
               key == GLFW.GLFW_KEY_LEFT_ALT || key == GLFW.GLFW_KEY_RIGHT_ALT ||
               key == GLFW.GLFW_KEY_LEFT_SUPER || key == GLFW.GLFW_KEY_RIGHT_SUPER ||
               key == GLFW.GLFW_KEY_CAPS_LOCK || key == GLFW.GLFW_KEY_NUM_LOCK ||
               key == GLFW.GLFW_KEY_SCROLL_LOCK;
    }

    private static void finishBind() {
        waitingFeature = null;
        if (waitingCallback != null) {
            Runnable cb = waitingCallback;
            waitingCallback = null;
            cb.run();
        }
    }
}
