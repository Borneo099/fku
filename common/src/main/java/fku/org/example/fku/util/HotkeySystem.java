package fku.org.example.fku.util;

import fku.org.example.fku.util.FeatureHotkeyManager;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class HotkeySystem {
    private static String waitingFeature = null;
    private static Runnable waitingCallback = null;
    private static final Map<String, Runnable> triggers = new HashMap<String, Runnable>();
    private static final Map<Integer, Boolean> prevKeyState = new HashMap<Integer, Boolean>();

    private static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    public static void registerFeature(String featureName, Runnable action) {
        if (featureName != null && action != null) {
            triggers.put(featureName, action);
        }
    }

    public static boolean startBinding(String featureName, Runnable onComplete) {
        if (featureName == null) {
            return false;
        }
        if (featureName.equals(waitingFeature)) {
            return false;
        }
        if (waitingFeature != null) {
            HotkeySystem.finishBind();
        }
        waitingFeature = featureName;
        waitingCallback = onComplete;
        Minecraft mc = HotkeySystem.getMc();
        if (mc != null && mc.player != null) {
            mc.player.m_5661_(Component.literal((String)("\u00a7e[\u70ed\u952e] \u6309\u4efb\u610f\u952e\u7ed1\u5b9a " + featureName + "\uff0cDelete \u5220\u9664\uff0cEsc \u53d6\u6d88")), false);
        }
        return true;
    }

    public static void cancelBinding() {
        if (waitingFeature == null) {
            return;
        }
        Minecraft mc = HotkeySystem.getMc();
        if (mc != null && mc.player != null) {
            mc.player.m_5661_(Component.literal((String)"\u00a77[\u70ed\u952e] \u5df2\u53d6\u6d88"), false);
        }
        HotkeySystem.finishBind();
    }

    public static boolean isWaitingFor(String featureName) {
        return featureName != null && featureName.equals(waitingFeature);
    }

    public static boolean isWaiting() {
        return waitingFeature != null;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = HotkeySystem.getMc();
        if (mc == null || mc.getWindow() == null) {
            return;
        }
        long window = mc.getWindow().m_85439_();
        if (waitingFeature != null) {
            for (int key = 32; key < 512; ++key) {
                if (GLFW.glfwGetKey(window, key) != 1) continue;
                if (key == 261 || key == 259) {
                    FeatureHotkeyManager.IHotkeyInterface hk = FeatureHotkeyManager.getInstance().getHotkey(waitingFeature);
                    hk.setHotkeyKey(-1);
                    hk.setHotkeyName("");
                    hk.saveConfig();
                    if (mc.player != null) {
                        mc.player.m_5661_(Component.literal((String)("\u00a7e[\u70ed\u952e] " + waitingFeature + " \u70ed\u952e\u5df2\u5220\u9664")), false);
                    }
                    HotkeySystem.finishBind();
                    return;
                }
                if (key == 256) {
                    return;
                }
                if (HotkeySystem.isModifier(key)) continue;
                Object name = GLFW.glfwGetKeyName(key, 0);
                if (name == null || ((String)name).isEmpty()) {
                    name = "Key#" + key;
                }
                FeatureHotkeyManager.IHotkeyInterface hk = FeatureHotkeyManager.getInstance().getHotkey(waitingFeature);
                hk.setHotkeyKey(key);
                hk.setHotkeyName((String)name);
                hk.saveConfig();
                if (mc.player != null) {
                    mc.player.m_5661_(Component.literal((String)("\u00a7a[\u70ed\u952e] " + waitingFeature + " \u5df2\u7ed1\u5b9a: " + (String)name)), false);
                }
                HotkeySystem.finishBind();
                return;
            }
            return;
        }
        if (mc.screen != null) {
            for (Map.Entry<String, Runnable> entry : triggers.entrySet()) {
                FeatureHotkeyManager.IHotkeyInterface hk = FeatureHotkeyManager.getInstance().getHotkey(entry.getKey());
                int keyCode = hk.getHotkeyKey();
                if (keyCode < 0) continue;
                boolean isDown = GLFW.glfwGetKey(window, keyCode) == 1;
                prevKeyState.put(keyCode, isDown);
            }
            return;
        }
        for (Map.Entry<String, Runnable> entry : triggers.entrySet()) {
            FeatureHotkeyManager.IHotkeyInterface hk = FeatureHotkeyManager.getInstance().getHotkey(entry.getKey());
            int keyCode = hk.getHotkeyKey();
            if (keyCode < 0) continue;
            boolean isDown = GLFW.glfwGetKey(window, keyCode) == 1;
            boolean wasDown = prevKeyState.getOrDefault(keyCode, false);
            if (isDown && !wasDown) {
                try {
                    entry.getValue().run();
                }
                catch (Exception exception) {
                    // ignored
                }
            }
            prevKeyState.put(keyCode, isDown);
        }
    }

    private static boolean isModifier(int key) {
        return key == 340 || key == 344 || key == 341 || key == 345 || key == 342 || key == 346 || key == 343 || key == 347 || key == 280 || key == 282 || key == 281;
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

