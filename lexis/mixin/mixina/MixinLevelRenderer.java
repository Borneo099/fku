package lexis.mixin.mixina;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.NoRenderHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LevelRenderer.class})
abstract class MixinLevelRenderer {
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
      method = {"renderSnowAndRain"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderWeather(LightTexture p_109704_, float p_109705_, double p_109706_, double p_109707_, double p_109708_, CallbackInfo ci) {
      NoRenderHack noRender = this.getNoRender();
      if (noRender != null && noRender.noWeather()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"renderClouds"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderClouds(PoseStack p_254145_, Matrix4f p_254537_, float p_254364_, double p_253843_, double p_253663_, double p_253795_, CallbackInfo ci) {
      NoRenderHack noRender = this.getNoRender();
      if (noRender != null && noRender.noWeather()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"renderWorldBorder"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderWorldBorder(Camera camera, CallbackInfo ci) {
      NoRenderHack noRender = this.getNoRender();
      if (noRender != null && noRender.noWorldBorder()) {
         ci.cancel();
      }

   }
}
