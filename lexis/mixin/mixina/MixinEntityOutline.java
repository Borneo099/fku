package lexis.mixin.mixina;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lexis.Hack.Hacks.Lexis.BetterVanillaGlowHack;
import lexis.Hack.Utils.Render.OutlineShaderRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({LevelRenderer.class})
public class MixinEntityOutline {
   @Redirect(
      method = {"renderLevel"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/renderer/PostChain;process(F)V",
   ordinal = 0
)
   )
   private void lexis$skipVanillaOutline(PostChain chain, float partialTick) {
      if (!BetterVanillaGlowHack.isShaderActive()) {
         chain.m_110023_(partialTick);
      }
   }

   @Redirect(
      method = {"doEntityOutline"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen(IIZ)V"
)
   )
   private void lexis$customOutlineBlit(RenderTarget entityTarget, int width, int height, boolean disableBlend) {
      if (BetterVanillaGlowHack.isShaderActive()) {
         BetterVanillaGlowHack hk = BetterVanillaGlowHack.INSTANCE;
         OutlineShaderRenderer.get().render(entityTarget.m_83975_(), entityTarget.f_83915_, entityTarget.f_83916_, hk.getWidth(), hk.getShapeMode(), hk.isEspColor(), hk.getFillColor(), hk.getOutlineColor());
      } else {
         entityTarget.m_83957_(width, height, disableBlend);
      }

   }
}
