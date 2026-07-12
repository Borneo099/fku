package lexis.mixin;

import lexis.Hack.Hacks.World.NoGhostBlocksHack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientLevel.class})
public class NoGhostBlocksMixin {
   @Shadow
   @Final
   private BlockStatePredictionHandler f_233599_;

   @Inject(
      method = {"setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void lexis$noGhostPrediction(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable cir) {
      NoGhostBlocksHack hack = NoGhostBlocksHack.get();
      if (hack != null && hack.isEnabled()) {
         if (!Minecraft.m_91087_().m_257720_()) {
            if (this.f_233599_.m_233872_()) {
               boolean isBreak = state.m_60795_();
               if (isBreak) {
                  if (!hack.isAntiBreak()) {
                     return;
                  }
               } else if (!hack.isAntiPlace()) {
                  return;
               }

               cir.setReturnValue(false);
            }
         }
      }
   }
}
