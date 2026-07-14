package fku.org.example.fku.util;

import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Baritone 桥接工具类
 * <p>
 * 通过反射与 Baritone API 通信，避免编译期依赖。（参考 lexis Hack.Utils.BaritoneBridge）
 * <p>
 * 如果未安装 Baritone，所有方法安全返回 false / 无操作。
 */
public final class BaritoneBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger("BaritoneBridge");
    private static Boolean available = null;


    private BaritoneBridge() {
    }

    // ──────── 可用性检测 ────────

    /** 检查 Baritone 是否可用（通过反射检测 API 类，不用 ModList，Baritone 可能非 Forge 模组） */
    public static boolean isAvailable() {
        if (available == null) {
            try {
                Class.forName("baritone.api.BaritoneAPI");
                available = true;
            } catch (Throwable e) {
                available = false;
            }
        }
        return available;
    }

    // ──────── 状态查询 ────────

    /** Baritone 是否正在执行路径任务 */
    public static boolean isActive() {
        return reflectBool("isActive");
    }

    /** Baritone 挖矿进程是否活跃 */
    public static boolean isMining() {
        return reflectBool("isMining");
    }

    /** Baritone 鞘翅飞行进程是否活跃 */
    public static boolean isElytraActive() {
        return reflectBool("isElytraActive");
    }

    // ──────── 控制 ────────

    /** 强制停止鞘翅进程 */
    public static void forceStopElytra() {
        reflectVoid("forceStopElytra");
    }

    /** goto 坐标 (x y z) */
    public static void gotoCoord(int x, int y, int z) {
        reflectVoid("gotoCoord", x, y, z);
    }

    /** 仅标记坐标（设置 Baritone 目标但不寻路），用 #goal 命令 */
    public static void setGoalOnly(int x, int y, int z) {
        exec("goal " + x + " " + y + " " + z);
    }

    /** 清除 Baritone 目标 */
    public static void clearGoal() {
        exec("cancel");
    }

    /** 暂停 */
    public static void pause() {
        reflectVoid("pause");
    }

    /** 恢复 */
    public static void resume() {
        reflectVoid("resume");
    }

    /** 停止 */
    public static void stop() {
        reflectVoid("stop");
    }

    /** 执行任意 Baritone 命令 */
    public static void executeCommand(String command) {
        reflectVoid("executeCommand", command);
    }

    // ──────── 静默 goto（抑制聊天消息） ────────

    private static boolean suppressNextGotoMessage = false;
    private static boolean suppressNextSetMessage = false;

    public static void gotoCoordSilent(int x, int y, int z) {
        suppressNextGotoMessage = true;
        gotoCoord(x, y, z);
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

    // ──────── 读取/写入 Baritone 设置（反射） ────────

    /**
     * 读取 Baritone 指定的 Boolean 设置项（如 allowBreak, allowSprint ...）
     *
     * @param fieldName 设置字段名（如 "allowBreak"）
     * @return 当前值；失败返回 null
     */
    public static Boolean readBooleanSetting(String fieldName) {
        if (!isAvailable()) return null;
        try {
            Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");
            Method getProvider = apiClass.getMethod("getProvider");
            Object provider = getProvider.invoke(null);
            Method getPrimary = provider.getClass().getMethod("getPrimaryBaritone");
            Object baritone = getPrimary.invoke(provider);
            Method getSettings = baritone.getClass().getMethod("getSettings");
            Object settings = getSettings.invoke(baritone);
            Field field = settings.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object setting = field.get(settings);
            Field valueField = setting.getClass().getDeclaredField("value");
            valueField.setAccessible(true);
            return (Boolean) valueField.get(setting);
        } catch (Throwable t) {
            LOGGER.debug("readBooleanSetting({}) failed: {}", fieldName, t.getMessage());
            return null;
        }
    }

    /**
     * 设置 Baritone Boolean 设置项
     */
    public static void writeBooleanSetting(String fieldName, boolean value) {
        executeCommand("set " + fieldName + " " + value);
    }

    // ============================================================
    //  反射工具方法
    // ============================================================

    private static Object getPrimaryBaritone() {
        try {
            Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");
            Method getProvider = apiClass.getMethod("getProvider");
            Object provider = getProvider.invoke(null);
            Method getPrimary = provider.getClass().getMethod("getPrimaryBaritone");
            return getPrimary.invoke(provider);
        } catch (Exception e) {
            return null;
        }
    }

    private static Object getCommandManager(Object baritone) {
        try {
            return baritone.getClass().getMethod("getCommandManager").invoke(baritone);
        } catch (Exception e) {
            return null;
        }
    }

    private static void reflectVoid(String method) {
        if (!isAvailable()) return;
        try {
            switch (method) {
                case "stop":
                    exec("stop");
                    return;
                case "pause":
                    exec("pause");
                    return;
                case "resume":
                    exec("resume");
                    return;
                case "forceStopElytra": {
                    Object b = getPrimaryBaritone();
                    if (b == null) return;
                    Object elytra = b.getClass().getMethod("getElytraProcess").invoke(b);
                    elytra.getClass().getMethod("onLostControl").invoke(elytra);
                    return;
                }
                case "isActive": {
                    // handled by reflectBool
                    return;
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void reflectVoid(String method, int x, int y, int z) {
        // gotoCoord
        exec("goto " + x + " " + y + " " + z);
    }

    private static void reflectVoid(String method, String cmd) {
        exec(cmd);
    }

    private static void exec(String cmd) {
        Object b = getPrimaryBaritone();
        if (b == null) return;
        Object mgr = getCommandManager(b);
        if (mgr == null) return;
        try {
            mgr.getClass().getMethod("execute", String.class).invoke(mgr, cmd);
        } catch (Exception ignored) {
        }
    }

    private static boolean reflectBool(String method) {
        if (!isAvailable()) return false;
        try {
            Object b = getPrimaryBaritone();
            if (b == null) return false;
            switch (method) {
                case "isActive": {
                    Object pb = b.getClass().getMethod("getPathingBehavior").invoke(b);
                    boolean pathing = (Boolean) pb.getClass().getMethod("isPathing").invoke(pb);
                    if (pathing) return true;
                    Object pcm = b.getClass().getMethod("getPathingControlManager").invoke(b);
                    Object proc = pcm.getClass().getMethod("mostRecentInControl").invoke(pcm);
                    if (proc == null) return false;
                    Object p = proc.getClass().getMethod("orElse", Object.class).invoke(proc, (Object) null);
                    if (p == null) return false;
                    return (Boolean) p.getClass().getMethod("isActive").invoke(p);
                }
                case "isMining": {
                    Object mine = b.getClass().getMethod("getMineProcess").invoke(b);
                    return (Boolean) mine.getClass().getMethod("isActive").invoke(mine);
                }
                case "isElytraActive": {
                    Object elytra = b.getClass().getMethod("getElytraProcess").invoke(b);
                    return (Boolean) elytra.getClass().getMethod("isActive").invoke(elytra);
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
