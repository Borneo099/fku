package lexis.mixin.mixina;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.NoRenderHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({HumanoidArmorLayer.class})
abstract class MixinArmorLayer {
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
      method = {"render*"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRender(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
      NoRenderHack noRender = this.getNoRender();
      if (noRender != null && noRender.noArmor()) {
         ci.cancel();
      }

   }
}
