package lexis.mixin.mixinb;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.CameraDistanceHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Camera.class})
public class CameraMixin {
   private float currentDistance = 4.0F;
   private long lastTime = System.currentTimeMillis();

   @Inject(
      method = {"getMaxZoom"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetMaxZoom(double desiredCameraDistance, CallbackInfoReturnable cir) {
      Iterator var4 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var4.hasNext()) {
            this.currentDistance = 4.0F;
            return;
         }

         hack = (Hack)var4.next();
      } while(!(hack instanceof CameraDistanceHack) || !hack.isEnabled());

      CameraDistanceHack cameraHack = (CameraDistanceHack)hack;
      long currentTime = System.currentTimeMillis();
      float deltaTime = (float)(currentTime - this.lastTime) / 1000.0F;
      this.lastTime = currentTime;
      float targetDistance = cameraHack.getTargetDistance();
      float speed = cameraHack.getAnimationSpeed();
      float smoothing = Math.min(1.0F, speed * deltaTime * 20.0F);
      this.currentDistance += (targetDistance - this.currentDistance) * smoothing;
      cir.setReturnValue((double)this.currentDistance);
   }
}
