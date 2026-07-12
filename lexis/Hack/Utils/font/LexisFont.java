package lexis.Hack.Utils.font;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public final class LexisFont {
   public static final ResourceLocation FONT = new ResourceLocation("lexis", "hud");
   private static final Style STYLE;

   private LexisFont() {
   }

   private static Font font() {
      return Minecraft.m_91087_().f_91062_;
   }

   public static Style style() {
      return STYLE;
   }

   public static Component component(String text) {
      return Component.m_237113_(text).m_130948_(STYLE);
   }

   public static int width(String text) {
      return font().m_92852_(component(text));
   }

   public static int lineHeight() {
      Objects.requireNonNull(font());
      return 9;
   }

   public static int draw(GuiGraphics gfx, String text, float x, float y, int color) {
      return gfx.m_280614_(font(), component(text), (int)x, (int)y, color, false);
   }

   public static int drawShadow(GuiGraphics gfx, String text, float x, float y, int color) {
      return gfx.m_280614_(font(), component(text), (int)x, (int)y, color, true);
   }

   public static int draw(GuiGraphics gfx, Component component, float x, float y, int color, boolean shadow) {
      return gfx.m_280614_(font(), component, (int)x, (int)y, color, shadow);
   }

   public static void drawCentered(GuiGraphics gfx, String text, float centerX, float y, int color) {
      draw(gfx, text, centerX - (float)width(text) / 2.0F, y, color);
   }

   public static void drawCenteredShadow(GuiGraphics gfx, String text, float centerX, float y, int color) {
      drawShadow(gfx, text, centerX - (float)width(text) / 2.0F, y, color);
   }

   public static void drawRight(GuiGraphics gfx, String text, float rightX, float y, int color) {
      draw(gfx, text, rightX - (float)width(text), y, color);
   }

   public static void drawRightShadow(GuiGraphics gfx, String text, float rightX, float y, int color) {
      drawShadow(gfx, text, rightX - (float)width(text), y, color);
   }

   static {
      STYLE = Style.f_131099_.m_131150_(FONT);
   }
}
