package lexis.Client.Goto;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ChatUtils {
   private static final Minecraft MC = Minecraft.m_91087_();
   public static final String PREFIX = "§d[§6Lexis§d] §f";

   public static void message(String message) {
      if (MC.f_91074_ != null) {
         MC.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f" + message), false);
      }

   }

   public static void error(String message) {
      message("§c" + message);
   }

   public static void warning(String message) {
      message("§6" + message);
   }
}
