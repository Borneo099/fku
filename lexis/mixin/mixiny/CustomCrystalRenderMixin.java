package lexis.mixin.mixiny;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import lexis.Hack.Hacks.World.CustomCrystalSpinHack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EndCrystalRenderer.class})
public class CustomCrystalRenderMixin {
   private long lastGameTime = 0L;
   private float lastPartialTicks = 0.0F;

   @Inject(
      method = {"render*"},
      at = {@At("HEAD")}
   )
   private void onRender(EndCrystal entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
      if (CustomCrystalSpinHack.isFeatureEnabled()) {
         boolean syncWorld = CustomCrystalSpinHack.isSyncWithWorldTime();
         long gameTick = syncWorld ? entity.f_19853_.m_46467_() : 0L;
         float timeSec;
         if (syncWorld) {
            if (gameTick != this.lastGameTime) {
               this.lastGameTime = gameTick;
            }

            timeSec = (float)gameTick / 20.0F + partialTicks / 20.0F;
         } else {
            timeSec = (float)System.currentTimeMillis() / 1000.0F;
         }

         float verticalOffset = (float)Math.sin((double)(timeSec * CustomCrystalSpinHack.getVerticalSpeed())) * CustomCrystalSpinHack.getVerticalAmplitude();
         matrixStack.m_252880_(0.0F, verticalOffset, 0.0F);
         float yaw = timeSec * CustomCrystalSpinHack.getRotationSpeed() % 360.0F;
         matrixStack.m_252781_(Axis.f_252436_.m_252977_(yaw));
      }
   }
}
