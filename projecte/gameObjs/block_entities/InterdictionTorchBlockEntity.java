package moze_intel.projecte.gameObjs.block_entities;

import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class InterdictionTorchBlockEntity extends BlockEntity {
   public InterdictionTorchBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)PEBlockEntityTypes.INTERDICTION_TORCH.get(), pos, state);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, InterdictionTorchBlockEntity torch) {
      WorldHelper.repelEntitiesInterdiction(level, new AABB(pos.m_7918_(-8, -8, -8), pos.m_7918_(8, 8, 8)), (double)pos.m_123341_() + 0.5, (double)pos.m_123342_() + 0.5, (double)pos.m_123343_() + 0.5);
   }
}
