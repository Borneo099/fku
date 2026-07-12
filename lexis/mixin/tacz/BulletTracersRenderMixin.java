package lexis.mixin.tacz;

import com.mojang.blaze3d.vertex.PoseStack;
import lexis.Hack.Hacks.TaCZ.BulletTracersHack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LevelRenderer.class})
public class BulletTracersRenderMixin {
   @Inject(
      method = {"renderLevel"},
      at = {@At("RETURN")}
   )
   private void onRenderLevel(PoseStack poseStack, float partialTick, long finishNano, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
      BulletTracersHack hack = BulletTracersHack.getInstance();
      if (hack != null && hack.isEnabled() && BulletTracersHack.tracersActive) {
         hack.onRender(poseStack, partialTick);
      }

   }
}
