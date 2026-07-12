package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.World.WorldBorderBypassHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.shapes.Shapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({WorldBorder.class})
public class WorldBorderMixin {
   @Inject(
      method = {"isWithinBounds*"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onIsWithinBounds(BlockPos pos, CallbackInfoReturnable cir) {
      Iterator var3 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var3.hasNext()) {
            return;
         }

         hack = (Hack)var3.next();
      } while(!(hack instanceof WorldBorderBypassHack) || !hack.isEnabled());

      cir.setReturnValue(true);
   }

   @Inject(
      method = {"getCollisionShape"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetCollisionShape(CallbackInfoReturnable cir) {
      Iterator var2 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var2.hasNext()) {
            return;
         }

         hack = (Hack)var2.next();
      } while(!(hack instanceof WorldBorderBypassHack) || !hack.isEnabled());

      cir.setReturnValue(Shapes.m_83040_());
   }
}
