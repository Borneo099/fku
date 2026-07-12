package moze_intel.projecte.gameObjs.blocks;

import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface InterdictionTorchEntityBlock extends PEEntityBlock {
   default @Nullable BlockEntityTypeRegistryObject getType() {
      return PEBlockEntityTypes.INTERDICTION_TORCH;
   }

   public static class InterdictionTorchWall extends WallTorchBlock implements InterdictionTorchEntityBlock {
      public InterdictionTorchWall(BlockBehaviour.Properties props) {
         super(props, ParticleTypes.f_123745_);
      }

      /** @deprecated */
      @Deprecated
      public boolean m_8133_(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
         super.m_8133_(state, level, pos, id, param);
         return this.triggerBlockEntityEvent(state, level, pos, id, param);
      }
   }

   public static class InterdictionTorch extends TorchBlock implements InterdictionTorchEntityBlock {
      public InterdictionTorch(BlockBehaviour.Properties props) {
         super(props, ParticleTypes.f_123745_);
      }

      /** @deprecated */
      @Deprecated
      public boolean m_8133_(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
         super.m_8133_(state, level, pos, id, param);
         return this.triggerBlockEntityEvent(state, level, pos, id, param);
      }
   }
}
