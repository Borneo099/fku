package lexis.mixin.mixins;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.NoPumpkinHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Gui.class})
public class NoPumpkinOverlayMixin {
   @Inject(
      method = {"renderTextureOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderTextureOverlay(GuiGraphics guiGraphics, ResourceLocation texture, float alpha, CallbackInfo ci) {
      if (texture != null && "textures/misc/pumpkinblur.png".equals(texture.m_135815_())) {
         Iterator var5 = HackManager.getInstance().getHacks().iterator();

         while(var5.hasNext()) {
            Hack hack = (Hack)var5.next();
            if (hack instanceof NoPumpkinHack && hack.isEnabled()) {
               ci.cancel();
               return;
            }
         }
      }

   }
}
