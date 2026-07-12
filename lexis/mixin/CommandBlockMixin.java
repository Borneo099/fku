package lexis.mixin;

import lexis.Server.Commandsavailabletoplayers.NoCommandsBlockCommand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({CommandBlock.class})
public class CommandBlockMixin {
   @Inject(
      method = {"neighborChanged"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onNeighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving, CallbackInfo ci) {
      if (NoCommandsBlockCommand.isEnabled()) {
         ci.cancel();
      }

   }
}
