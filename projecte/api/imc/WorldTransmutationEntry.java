package moze_intel.projecte.api.imc;

import net.minecraft.world.level.block.state.BlockState;

public record WorldTransmutationEntry(BlockState origin, BlockState result, BlockState altResult) {
   public WorldTransmutationEntry(BlockState origin, BlockState result, BlockState altResult) {
      altResult = altResult == null ? result : altResult;
      this.origin = origin;
      this.result = result;
      this.altResult = altResult;
   }

   public BlockState origin() {
      return this.origin;
   }

   public BlockState result() {
      return this.result;
   }

   public BlockState altResult() {
      return this.altResult;
   }
}
