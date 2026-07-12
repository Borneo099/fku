package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.FreeCamHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.mixin.accessor.CameraAccessor;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Camera.class})
public abstract class CameraMixin {
   @Shadow
   protected abstract void m_90572_(float yRot, float xRot);

   @Shadow
   public abstract void m_90581_(Vec3 pos);

   @Inject(
      method = {"setup"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/Camera;setRotation(FF)V",
   ordinal = 0
)},
      cancellable = true
   )
   private void onSetup(BlockGetter level, Entity entity, boolean detached, boolean mirrored, float partialTick, CallbackInfo ci) {
      Iterator var7 = HackManager.getInstance().getHacks().iterator();

      while(var7.hasNext()) {
         Hack hack = (Hack)var7.next();
         if (hack instanceof FreeCamHack freeCam) {
            if (freeCam.isActive() && !freeCam.isCameraLocked() && !freeCam.isEyeLocked()) {
               double x = Mth.m_14139_((double)partialTick, freeCam.getPrevX(), freeCam.getX());
               double y = Mth.m_14139_((double)partialTick, freeCam.getPrevY(), freeCam.getY());
               double z = Mth.m_14139_((double)partialTick, freeCam.getPrevZ(), freeCam.getZ());
               CameraAccessor acc = (CameraAccessor)this;
               acc.setYRot(freeCam.getYRot() % 360.0F);
               acc.setXRot(Mth.m_14036_(freeCam.getXRot(), -90.0F, 90.0F));
               acc.setPosition(new Vec3(x, y, z));
               ci.cancel();
               return;
            }
         }
      }

   }
}
