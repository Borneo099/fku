package lexis.mixin.mixinc;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.ButtonBeautifyHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({AbstractButton.class})
public class ButtonBeautifyMixin {
   private static final int[] GRADIENT_COLORS = new int[]{-2461482, -2252579, -1146130, -18751, -38476};

   @Inject(
      method = {"renderWidget"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      AbstractButton button = (AbstractButton)this;
      if (button instanceof Button) {
         boolean enabled = false;
         Iterator var8 = HackManager.getInstance().getHacks().iterator();

         while(var8.hasNext()) {
            Hack hack = (Hack)var8.next();
            if (hack instanceof ButtonBeautifyHack && hack.isEnabled()) {
               enabled = true;
               break;
            }
         }

         if (enabled) {
            int x = button.m_252754_();
            int y = button.m_252907_();
            int width = button.m_5711_();
            int height = button.m_93694_();
            String message = button.m_6035_().getString();
            boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
            int bgColor = hovered ? -1426063361 : -1442840576;
            gui.m_280509_(x, y, x + width, y + height, bgColor);
            this.drawFlowingGradientBorder(gui, x, y, width, height);
            Font font = Minecraft.m_91087_().f_91062_;
            int textWidth = font.m_92895_(message);
            int textX = x + (width - textWidth) / 2;
            int textY = y + (height - 8) / 2;
            gui.m_280488_(font, message, textX, textY, 16777215);
            ci.cancel();
         }
      }
   }

   private void drawFlowingGradientBorder(GuiGraphics gui, int x, int y, int width, int height) {
      long time = System.currentTimeMillis();
      float offset = (float)(time % 3000L) / 3000.0F;

      int i;
      float progress;
      int color;
      for(i = 0; i < width; ++i) {
         progress = ((float)i / (float)width + offset) % 1.0F;
         color = this.interpolateGradient(progress);
         gui.m_280509_(x + i, y, x + i + 1, y + 1, color);
      }

      for(i = 0; i < width; ++i) {
         progress = (1.0F - (float)i / (float)width + offset) % 1.0F;
         color = this.interpolateGradient(progress);
         gui.m_280509_(x + i, y + height - 1, x + i + 1, y + height, color);
      }

      for(i = 0; i < height; ++i) {
         progress = ((float)i / (float)height + offset) % 1.0F;
         color = this.interpolateGradient(progress);
         gui.m_280509_(x, y + i, x + 1, y + i + 1, color);
      }

      for(i = 0; i < height; ++i) {
         progress = (1.0F - (float)i / (float)height + offset) % 1.0F;
         color = this.interpolateGradient(progress);
         gui.m_280509_(x + width - 1, y + i, x + width, y + i + 1, color);
      }

   }

   private int interpolateGradient(float progress) {
      int index = (int)(progress * (float)(GRADIENT_COLORS.length - 1));
      float blend = progress * (float)(GRADIENT_COLORS.length - 1) - (float)index;
      if (index >= GRADIENT_COLORS.length - 1) {
         return GRADIENT_COLORS[GRADIENT_COLORS.length - 1];
      } else {
         int c1 = GRADIENT_COLORS[index];
         int c2 = GRADIENT_COLORS[index + 1];
         return this.blendColors(c1, c2, blend);
      }
   }

   private int blendColors(int c1, int c2, float ratio) {
      int r1 = c1 >> 16 & 255;
      int g1 = c1 >> 8 & 255;
      int b1 = c1 & 255;
      int r2 = c2 >> 16 & 255;
      int g2 = c2 >> 8 & 255;
      int b2 = c2 & 255;
      int r = (int)((float)r1 + (float)(r2 - r1) * ratio);
      int g = (int)((float)g1 + (float)(g2 - g1) * ratio);
      int b = (int)((float)b1 + (float)(b2 - b1) * ratio);
      return -16777216 | r << 16 | g << 8 | b;
   }
}
