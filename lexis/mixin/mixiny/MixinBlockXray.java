package lexis.mixin.mixiny;

import lexis.Hack.Hacks.Render.XrayHack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Block.class})
public class MixinBlockXray {
   @Inject(
      method = {"shouldRenderFace"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void lexis$xray(BlockState state, BlockGetter level, BlockPos pos, Direction face, BlockPos neighborPos, CallbackInfoReturnable cir) {
      if (XrayHack.enabled) {
         cir.setReturnValue(XrayHack.isVisible(state.m_60734_()));
      }
   }
}
