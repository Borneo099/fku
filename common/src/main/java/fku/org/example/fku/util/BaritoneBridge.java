package fku.org.example.fku.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BaritoneBridge {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"BaritoneBridge");
    private static Boolean available = null;
    private static boolean suppressNextGotoMessage = false;
    private static boolean suppressNextSetMessage = false;

    private BaritoneBridge() {
    }

    public static boolean isAvailable() {
        if (available == null) {
            try {
                Class.forName("baritone.api.BaritoneAPI");
                available = true;
            }
            catch (Throwable e) {
                available = false;
            }
        }
        return available;
    }

    public static boolean isActive() {
        return BaritoneBridge.reflectBool("isActive");
    }

    public static boolean isMining() {
        return BaritoneBridge.reflectBool("isMining");
    }

    public static boolean isElytraActive() {
        return BaritoneBridge.reflectBool("isElytraActive");
    }

    public static void forceStopElytra() {
        BaritoneBridge.reflectVoid("forceStopElytra");
    }

    public static void gotoCoord(int x, int y, int z) {
        BaritoneBridge.reflectVoid("gotoCoord", x, y, z);
    }

    public static void setGoalOnly(int x, int y, int z) {
        BaritoneBridge.exec("goal " + x + " " + y + " " + z);
    }

    public static void clearGoal() {
        BaritoneBridge.exec("goal clear");
    }

    public static void pause() {
        BaritoneBridge.reflectVoid("pause");
    }

    public static void resume() {
        BaritoneBridge.reflectVoid("resume");
    }

    public static void stop() {
        BaritoneBridge.reflectVoid("stop");
    }

    public static void executeCommand(String command) {
        BaritoneBridge.reflectVoid("executeCommand", command);
    }

    public static void gotoCoordSilent(int x, int y, int z) {
        suppressNextGotoMessage = true;
        BaritoneBridge.gotoCoord(x, y, z);
    }

    public static boolean consumeSuppressFlag() {
        if (suppressNextGotoMessage) {
            suppressNextGotoMessage = false;
            return true;
        }
        return false;
    }

    public static void suppressNextSetMessage() {
        suppressNextSetMessage = true;
    }

    public static boolean consumeSetSuppressFlag() {
        if (suppressNextSetMessage) {
            suppressNextSetMessage = false;
            return true;
        }
        return false;
    }

    public static Boolean readBooleanSetting(String fieldName) {
        if (!BaritoneBridge.isAvailable()) {
            return null;
        }
        try {
            Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");
            Method getProvider = apiClass.getMethod("getProvider", new Class[0]);
            Object provider = getProvider.invoke(null, new Object[0]);
            Method getPrimary = provider.getClass().getMethod("getPrimaryBaritone", new Class[0]);
            Object baritone = getPrimary.invoke(provider, new Object[0]);
            Method getSettings = baritone.getClass().getMethod("getSettings", new Class[0]);
            Object settings = getSettings.invoke(baritone, new Object[0]);
            Field field = settings.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object setting = field.get(settings);
            Field valueField = setting.getClass().getDeclaredField("value");
            valueField.setAccessible(true);
            return (Boolean)valueField.get(setting);
        }
        catch (Throwable t) {
            LOGGER.debug("readBooleanSetting({}) failed: {}", fieldName, t.getMessage());
            return null;
        }
    }

    public static void writeBooleanSetting(String fieldName, boolean value) {
        BaritoneBridge.executeCommand("set " + fieldName + " " + value);
    }

    private static Object getPrimaryBaritone() {
        try {
            Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");
            Method getProvider = apiClass.getMethod("getProvider", new Class[0]);
            Object provider = getProvider.invoke(null, new Object[0]);
            Method getPrimary = provider.getClass().getMethod("getPrimaryBaritone", new Class[0]);
            return getPrimary.invoke(provider, new Object[0]);
        }
        catch (Exception e) {
            return null;
        }
    }

    private static Object getCommandManager(Object baritone) {
        try {
            return baritone.getClass().getMethod("getCommandManager", new Class[0]).invoke(baritone, new Object[0]);
        }
        catch (Exception e) {
            return null;
        }
    }

    private static void reflectVoid(String method) {
        if (!BaritoneBridge.isAvailable()) {
            return;
        }
        try {
            switch (method) {
                case "stop": {
                    BaritoneBridge.exec("stop");
                    return;
                }
                case "pause": {
                    BaritoneBridge.exec("pause");
                    return;
                }
                case "resume": {
                    BaritoneBridge.exec("resume");
                    return;
                }
                case "forceStopElytra": {
                    Object b = BaritoneBridge.getPrimaryBaritone();
                    if (b == null) {
                        return;
                    }
                    Object elytra = b.getClass().getMethod("getElytraProcess", new Class[0]).invoke(b, new Object[0]);
                    elytra.getClass().getMethod("onLostControl", new Class[0]).invoke(elytra, new Object[0]);
                    return;
                }
                case "isActive": {
                    return;
                }
            }
        }
        catch (Exception exception) {
            // ignored
        }
    }

    private static void reflectVoid(String method, int x, int y, int z) {
        BaritoneBridge.exec("goto " + x + " " + y + " " + z);
    }

    private static void reflectVoid(String method, String cmd) {
        BaritoneBridge.exec(cmd);
    }

    private static void exec(String cmd) {
        Object b = BaritoneBridge.getPrimaryBaritone();
        if (b == null) {
            return;
        }
        Object mgr = BaritoneBridge.getCommandManager(b);
        if (mgr == null) {
            return;
        }
        try {
            mgr.getClass().getMethod("execute", String.class).invoke(mgr, cmd);
        }
        catch (Exception exception) {
            // ignored
        }
    }

    private static boolean reflectBool(String method) {
        if (!BaritoneBridge.isAvailable()) {
            return false;
        }
        try {
            Object b = BaritoneBridge.getPrimaryBaritone();
            if (b == null) {
                return false;
            }
            switch (method) {
                case "isActive": {
                    Object pb = b.getClass().getMethod("getPathingBehavior", new Class[0]).invoke(b, new Object[0]);
                    boolean pathing = (Boolean)pb.getClass().getMethod("isPathing", new Class[0]).invoke(pb, new Object[0]);
                    if (pathing) {
                        return true;
                    }
                    Object pcm = b.getClass().getMethod("getPathingControlManager", new Class[0]).invoke(b, new Object[0]);
                    Object proc = pcm.getClass().getMethod("mostRecentInControl", new Class[0]).invoke(pcm, new Object[0]);
                    if (proc == null) {
                        return false;
                    }
                    Object p = proc.getClass().getMethod("orElse", Object.class).invoke(proc, new Object[]{null});
                    if (p == null) {
                        return false;
                    }
                    return (Boolean)p.getClass().getMethod("isActive", new Class[0]).invoke(p, new Object[0]);
                }
                case "isMining": {
                    Object mine = b.getClass().getMethod("getMineProcess", new Class[0]).invoke(b, new Object[0]);
                    return (Boolean)mine.getClass().getMethod("isActive", new Class[0]).invoke(mine, new Object[0]);
                }
                case "isElytraActive": {
                    Object elytra = b.getClass().getMethod("getElytraProcess", new Class[0]).invoke(b, new Object[0]);
                    return (Boolean)elytra.getClass().getMethod("isActive", new Class[0]).invoke(elytra, new Object[0]);
                }
            }
        }
        catch (Exception exception) {
            // ignored
        }
        return false;
    }
}

