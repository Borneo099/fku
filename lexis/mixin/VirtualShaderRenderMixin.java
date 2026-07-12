package lexis.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import lexis.Hack.Hacks.Render.VirtualShaderHack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameRenderer.class})
public class VirtualShaderRenderMixin {
   @Inject(
      method = {"renderLevel"},
      at = {@At("TAIL")}
   )
   private void onRenderLevelTail(float partialTick, long nanoTime, PoseStack poseStack, CallbackInfo ci) {
      if (VirtualShaderHack.INSTANCE != null) {
         VirtualShaderHack.INSTANCE.renderShaders(partialTick);
      }

   }
}
