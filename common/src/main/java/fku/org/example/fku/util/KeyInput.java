package fku.org.example.fku.util; /* water */

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

/**
 * 安全的按键状态查询封装。
 * <p>
 * 直接调用 {@code GLFW.glfwGetKey(window, key)} 容易踩到 GLFW 上下文尚未就绪
 * 或已被破坏（窗口句柄为 0 / 失效）的坑。这里改用 Minecraft 官方封装的
 * {@link InputConstants#isKeyDown(long, int)}，并在调用前先安全地获取并校验
 * 窗口句柄：上下文未就绪或窗口无效时直接返回 false，而非把非法句柄传给 GLFW。
 */
public final class KeyInput {
    private KeyInput() {}

    public static boolean isKeyDown(int key) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) return false;
        long window = mc.getWindow().getWindow();
        if (window == 0L) return false;
        return InputConstants.isKeyDown(window, key);
    }
}
