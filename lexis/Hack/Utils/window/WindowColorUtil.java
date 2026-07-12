package lexis.Hack.Utils.window;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import java.nio.IntBuffer;

public class WindowColorUtil {
   private static final int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;
   private static final int DWMWA_CAPTION_COLOR = 35;
   private static final int DWMWA_TEXT_COLOR = 36;
   private static final int DWMWA_BORDER_COLOR = 34;

   private static WinDef.HWND getHwnd() {
      return WindowHandle.getMinecraftHwnd();
   }

   private static void setAttribute(int attr, int value) {
      WinDef.HWND hwnd = getHwnd();
      if (hwnd != null) {
         IntBuffer buf = IntBuffer.allocate(1);
         buf.put(0, value);
         WindowColorUtil.Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, attr, buf, 4);
      }
   }

   public static void setCaptionColor(int rgb) {
      int bgr = (rgb & 255) << 16 | rgb & '\uff00' | rgb >> 16 & 255;
      setAttribute(35, bgr);
   }

   public static void setTextColor(int rgb) {
      int bgr = (rgb & 255) << 16 | rgb & '\uff00' | rgb >> 16 & 255;
      setAttribute(36, bgr);
   }

   public static void setBorderColor(int rgb) {
      int bgr = (rgb & 255) << 16 | rgb & '\uff00' | rgb >> 16 & 255;
      setAttribute(34, bgr);
   }

   public static void enableDarkMode(boolean dark) {
      setAttribute(20, dark ? 1 : 0);
   }

   public static void applyLexisTheme() {
      enableDarkMode(true);
      setCaptionColor(1703987);
      setTextColor(13408767);
      setBorderColor(6684876);
   }

   public interface Dwmapi extends StdCallLibrary {
      Dwmapi INSTANCE = (Dwmapi)Native.load("dwmapi", Dwmapi.class);

      int DwmSetWindowAttribute(WinDef.HWND hwnd, int attr, IntBuffer value, int size);
   }
}
