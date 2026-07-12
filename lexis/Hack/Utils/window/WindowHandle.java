package lexis.Hack.Utils.window;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFWNativeWin32;

public class WindowHandle {
   public static WinDef.HWND getMinecraftHwnd() {
      long glfwWindow = Minecraft.m_91087_().m_91268_().m_85439_();
      long hwndLong = GLFWNativeWin32.glfwGetWin32Window(glfwWindow);
      return hwndLong == 0L ? null : new WinDef.HWND(new Pointer(hwndLong));
   }
}
