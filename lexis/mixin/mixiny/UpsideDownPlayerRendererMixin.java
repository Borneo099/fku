package lexis.mixin.mixiny;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Fun.UpsideDownHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PlayerRenderer.class})
public class UpsideDownPlayerRendererMixin {
   @Unique
   private static boolean lexis$isUpsideDown = false;

   @Inject(
      method = {"render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"},
      at = {@At("HEAD")}
   )
   private void onRenderHead(AbstractClientPlayer p_117788_, float p_117789_, float p_117790_, PoseStack p_117791_, MultiBufferSource p_117792_, int p_117793_, CallbackInfo ci) {
      if (p_117788_ == Minecraft.m_91087_().f_91074_ && this.isHackEnabled()) {
         lexis$isUpsideDown = true;
         p_117791_.m_85836_();
         p_117791_.m_252781_(Axis.f_252529_.m_252977_(180.0F));
         p_117791_.m_85837_(0.0, -1.8, 0.0);
      }

   }

   @Inject(
      method = {"render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"},
      at = {@At("RETURN")}
   )
   private void onRenderReturn(AbstractClientPlayer p_117788_, float p_117789_, float p_117790_, PoseStack p_117791_, MultiBufferSource p_117792_, int p_117793_, CallbackInfo ci) {
      if (lexis$isUpsideDown) {
         lexis$isUpsideDown = false;
         p_117791_.m_85849_();
      }

   }

   @Unique
   private boolean isHackEnabled() {
      Iterator var1 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var1.hasNext()) {
            return false;
         }

         hack = (Hack)var1.next();
      } while(!(hack instanceof UpsideDownHack) || !hack.isEnabled());

      return true;
   }
}
