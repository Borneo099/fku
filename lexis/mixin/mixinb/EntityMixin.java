package lexis.mixin.mixinb;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.FreeCamHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class})
public class EntityMixin {
   @Unique
   private FreeCamHack lexis$getActiveFreeCam() {
      Entity self = (Entity)this;
      if (self != Minecraft.m_91087_().f_91074_) {
         return null;
      } else {
         Iterator var2 = HackManager.getInstance().getHacks().iterator();

         while(var2.hasNext()) {
            Hack hack = (Hack)var2.next();
            if (hack instanceof FreeCamHack) {
               FreeCamHack freeCam = (FreeCamHack)hack;
               if (freeCam.isActive()) {
                  return freeCam;
               }
            }
         }

         return null;
      }
   }

   @Inject(
      method = {"turn"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onTurn(double yawDelta, double pitchDelta, CallbackInfo ci) {
      Entity self = (Entity)this;
      if (self instanceof Player) {
         Iterator var7 = HackManager.getInstance().getHacks().iterator();

         while(var7.hasNext()) {
            Hack hack = (Hack)var7.next();
            if (hack instanceof FreeCamHack) {
               FreeCamHack freeCam = (FreeCamHack)hack;
               if (freeCam.isActive() && !freeCam.isCameraLocked() && !freeCam.isEyeLocked()) {
                  freeCam.onMouseTurn(yawDelta, pitchDelta);
                  ci.cancel();
                  return;
               }
            }
         }

      }
   }

   @Inject(
      method = {"getEyePosition(F)Lnet/minecraft/world/phys/Vec3;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void freecamEyePosition(float partialTick, CallbackInfoReturnable cir) {
      FreeCamHack freeCam = this.lexis$getActiveFreeCam();
      if (freeCam != null) {
         cir.setReturnValue(new Vec3(freeCam.getX(), freeCam.getY(), freeCam.getZ()));
      }
   }

   @Inject(
      method = {"getEyePosition()Lnet/minecraft/world/phys/Vec3;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void freecamEyePositionNoArg(CallbackInfoReturnable cir) {
      FreeCamHack freeCam = this.lexis$getActiveFreeCam();
      if (freeCam != null) {
         cir.setReturnValue(new Vec3(freeCam.getX(), freeCam.getY(), freeCam.getZ()));
      }
   }

   @Inject(
      method = {"getViewVector"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void freecamViewVector(float partialTick, CallbackInfoReturnable cir) {
      FreeCamHack freeCam = this.lexis$getActiveFreeCam();
      if (freeCam != null) {
         cir.setReturnValue(Vec3.m_82498_(freeCam.getXRot(), freeCam.getYRot()));
      }
   }
}
