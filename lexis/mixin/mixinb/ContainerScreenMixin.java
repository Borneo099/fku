package lexis.mixin.mixinb;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import lexis.Gui.GuiAnimator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({AbstractContainerScreen.class})
public class ContainerScreenMixin {
   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      Screen screen = (Screen)this;
      float scale = GuiAnimator.getScale();
      float alpha = GuiAnimator.getAlpha();
      float offsetX = GuiAnimator.getOffsetX();
      float offsetY = GuiAnimator.getOffsetY();
      float rotation = GuiAnimator.getRotation();
      if (scale != 1.0F || alpha != 1.0F || offsetX != 0.0F || offsetY != 0.0F || rotation != 0.0F) {
         int width = screen.f_96543_;
         int height = screen.f_96544_;
         PoseStack pose = guiGraphics.m_280168_();
         pose.m_85836_();
         pose.m_252880_((float)width / 2.0F + offsetX, (float)height / 2.0F + offsetY, 0.0F);
         if (rotation != 0.0F) {
            pose.m_252781_(Axis.f_252403_.m_252977_(rotation));
         }

         pose.m_85841_(scale, scale, 1.0F);
         pose.m_252880_((float)(-width) / 2.0F, (float)(-height) / 2.0F, 0.0F);
         if (alpha != 1.0F) {
            guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, alpha);
         }
      }

   }

   @Inject(
      method = {"render"},
      at = {@At("RETURN")}
   )
   private void onRenderEnd(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      Screen screen = (Screen)this;
      float scale = GuiAnimator.getScale();
      float alpha = GuiAnimator.getAlpha();
      float offsetX = GuiAnimator.getOffsetX();
      float offsetY = GuiAnimator.getOffsetY();
      float rotation = GuiAnimator.getRotation();
      if (scale != 1.0F || alpha != 1.0F || offsetX != 0.0F || offsetY != 0.0F || rotation != 0.0F) {
         guiGraphics.m_280168_().m_85849_();
         if (alpha != 1.0F) {
            guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, 1.0F);
         }
      }

   }
}
