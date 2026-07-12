package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.NoRenderHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Gui.class})
abstract class MixinInGameHud {
   @Shadow
   @Final
   private Minecraft f_92986_;

   private NoRenderHack getNoRender() {
      Iterator var1 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var1.hasNext()) {
            return null;
         }

         hack = (Hack)var1.next();
      } while(!(hack instanceof NoRenderHack) || !hack.isEnabled());

      return (NoRenderHack)hack;
   }

   @Inject(
      method = {"renderEffects"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderStatusEffectOverlay(GuiGraphics guiGraphics, CallbackInfo ci) {
      NoRenderHack noRender = this.getNoRender();
      if (noRender != null && noRender.noPotionIcons()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"renderPortalOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderPortalOverlay(GuiGraphics guiGraphics, float nauseaStrength, CallbackInfo ci) {
      NoRenderHack noRender = this.getNoRender();
      if (noRender != null && noRender.noPortalOverlay()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"displayScoreboardSidebar"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderScoreboardSidebar(GuiGraphics guiGraphics, Objective objective, CallbackInfo ci) {
      NoRenderHack noRender = this.getNoRender();
      if (noRender != null && noRender.noScoreboard()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"renderSpyglassOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderSpyglassOverlay(GuiGraphics guiGraphics, float scale, CallbackInfo ci) {
      NoRenderHack noRender = this.getNoRender();
      if (noRender != null && noRender.noSpyglassOverlay()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"renderCrosshair"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
      NoRenderHack noRender = this.getNoRender();
      if (noRender != null && noRender.noCrosshair()) {
         ci.cancel();
      }

   }
}
