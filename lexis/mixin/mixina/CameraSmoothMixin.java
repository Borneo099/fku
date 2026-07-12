package lexis.mixin.mixina;

import lexis.Hack.Utils.CameraSmooth;
import lexis.mixin.accessor.CameraAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Camera.class})
public abstract class CameraSmoothMixin {
   @Shadow
   private Vec3 f_90552_;
   private static int lastCameraType = -1;

   @Inject(
      method = {"setup"},
      at = {@At("RETURN")}
   )
   private void onSetup(CallbackInfo ci) {
      if (CameraSmooth.isEnabled()) {
         int cameraType = Minecraft.m_91087_().f_91066_.m_92176_().ordinal();
         if (cameraType != lastCameraType) {
            CameraSmooth.reset();
            lastCameraType = cameraType;
         }

         if (cameraType != 0) {
            CameraAccessor accessor = (CameraAccessor)this;
            Vec3 original = accessor.getPosition();
            Vec3 smoothed = CameraSmooth.applySmooth(original);
            accessor.setPosition(smoothed);
         }
      }
   }
}
