package lexis.mixin;

import lexis.Gui.GuiAnimator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
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
      if (screen instanceof AbstractContainerScreen && !(screen instanceof TitleScreen)) {
         float scale = GuiAnimator.getScale();
         if (scale != 1.0F) {
            int width = screen.f_96543_;
            int height = screen.f_96544_;
            guiGraphics.m_280168_().m_85836_();
            guiGraphics.m_280168_().m_252880_((float)width / 2.0F, (float)height / 2.0F, 0.0F);
            guiGraphics.m_280168_().m_85841_(scale, scale, 1.0F);
            guiGraphics.m_280168_().m_252880_((float)(-width) / 2.0F, (float)(-height) / 2.0F, 0.0F);
         }
      }

   }

   @Inject(
      method = {"render"},
      at = {@At("RETURN")}
   )
   private void onRenderEnd(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      Screen screen = (Screen)this;
      if (screen instanceof AbstractContainerScreen && !(screen instanceof TitleScreen)) {
         float scale = GuiAnimator.getScale();
         if (scale != 1.0F) {
            guiGraphics.m_280168_().m_85849_();
         }
      }

   }
}
