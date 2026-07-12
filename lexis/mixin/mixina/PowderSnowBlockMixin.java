package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Movement.SnowShoeHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PowderSnowBlock.class})
public class PowderSnowBlockMixin {
   @Inject(
      method = {"canEntityWalkOnPowderSnow"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void onCanEntityWalkOnPowderSnow(Entity entity, CallbackInfoReturnable cir) {
      Iterator var2 = HackManager.getInstance().getHacks().iterator();

      while(var2.hasNext()) {
         Hack hack = (Hack)var2.next();
         if (hack instanceof SnowShoeHack && hack.isEnabled()) {
            if (entity == Minecraft.m_91087_().f_91074_) {
               cir.setReturnValue(true);
            }
            break;
         }
      }

   }
}
