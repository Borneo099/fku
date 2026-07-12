package lexis.mixin.mixiny;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.FreeCamHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.StartBreakingBlockEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({MultiPlayerGameMode.class})
public class MultiPlayerGameModeMixin {
   @Unique
   private boolean lexis$isCameraInsideBlock(BlockPos targetPos) {
      Iterator var2 = HackManager.getInstance().getHacks().iterator();

      while(var2.hasNext()) {
         Hack hack = (Hack)var2.next();
         if (hack instanceof FreeCamHack fc) {
            if (fc.isActive()) {
               BlockPos camPos = BlockPos.m_274561_(fc.getX(), fc.getY(), fc.getZ());
               return targetPos.equals(camPos);
            }
         }
      }

      return false;
   }

   @Inject(
      method = {"startDestroyBlock"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onStartDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable cir) {
      if (this.lexis$isCameraInsideBlock(pos)) {
         cir.setReturnValue(false);
      } else {
         StartBreakingBlockEvent event = StartBreakingBlockEvent.get(pos, direction);
         EventManager.fire(event);
         if (event.isCancelled()) {
            cir.setReturnValue(false);
         }

      }
   }

   @Inject(
      method = {"continueDestroyBlock"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onContinueDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable cir) {
      if (this.lexis$isCameraInsideBlock(pos)) {
         cir.setReturnValue(false);
      }

   }
}
