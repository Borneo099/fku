package lexis.mixin.mixinb;

import com.mojang.blaze3d.vertex.PoseStack;
import lexis.Hack.Hacks.Render.ItemOutlineHack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameRenderer.class})
public class ItemHandOutlineMixin {
   @Inject(
      method = {"renderItemInHand"},
      at = {@At("HEAD")}
   )
   private void lexis$itemOutlineBegin(PoseStack poseStack, Camera camera, float partialTick, CallbackInfo ci) {
      if (ItemOutlineHack.isShaderActive()) {
         ItemOutlineHack.INSTANCE.beginCapture();
      }

   }

   @Inject(
      method = {"renderItemInHand"},
      at = {@At("RETURN")}
   )
   private void lexis$itemOutlineEnd(PoseStack poseStack, Camera camera, float partialTick, CallbackInfo ci) {
      if (ItemOutlineHack.isShaderActive()) {
         ItemOutlineHack.INSTANCE.endCaptureAndComposite();
      }

   }
}
