package fku.org.example.fku.features.skija;

/**
 * 安全加载 Skija 的后门：
 * - 公开 API 类（SkijaText / SkijaRoundRect / SkijaColorWheel）不得静态引用任何 io.github.humbleui.skija.*
 *   类型，否则在 Skija 不在模块 classpath 上的运行配置中会于「类校验」阶段抛 NoClassDefFoundError 直接崩游戏。
 * - 真正的 Skija 代码放在 *Backend 类里，仅当 isLoadable() 确认 Native 类可加载时才用反射加载并执行。
 */
public class SkijaReflect {
    private static Boolean loadable;

    public static boolean isLoadable() {
        if (loadable == null) {
            try {
                Class.forName("io.github.humbleui.skija.impl.Native");
                loadable = true;
            } catch (Throwable t) {
                loadable = false;
            }
        }
        return loadable;
    }

    /** 反射调用某个 Skija*Backend 的静态方法；任何失败都上抛，由调用方回退 */
    public static Object call(String backendClass, String method, Class<?>[] ptypes, Object... args) throws Throwable {
        return Class.forName(backendClass).getMethod(method, ptypes).invoke(null, args);
    }
}
