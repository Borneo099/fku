package lexis.mixin.mixinc;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Movement.BoatFrictionHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({Boat.class})
public class MixinBoat {
   @Shadow
   private float f_38264_;

   @ModifyArg(
      method = {"floatBoat"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/world/entity/vehicle/Boat;setDeltaMovement(DDD)V",
   ordinal = 0
),
      index = 0
   )
   private double modifyFloatBoatX(double dx) {
      BoatFrictionHack hack = this.getActiveHack();
      return hack != null ? dx / (double)this.f_38264_ * hack.getFriction() : dx;
   }

   @ModifyArg(
      method = {"floatBoat"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/world/entity/vehicle/Boat;setDeltaMovement(DDD)V",
   ordinal = 0
),
      index = 2
   )
   private double modifyFloatBoatZ(double dz) {
      BoatFrictionHack hack = this.getActiveHack();
      return hack != null ? dz / (double)this.f_38264_ * hack.getFriction() : dz;
   }

   private BoatFrictionHack getActiveHack() {
      Iterator var1 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var1.hasNext()) {
            return null;
         }

         hack = (Hack)var1.next();
      } while(!(hack instanceof BoatFrictionHack) || !hack.isEnabled());

      return (BoatFrictionHack)hack;
   }
}
