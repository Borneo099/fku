package moze_intel.projecte.gameObjs.blocks;

import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PEEntityBlock extends EntityBlock {
   @Nullable BlockEntityTypeRegistryObject getType();

   default @Nullable BlockEntity m_142194_(@NotNull BlockPos pos, @NotNull BlockState state) {
      BlockEntityTypeRegistryObject type = this.getType();
      return type == null ? null : ((BlockEntityType)type.get()).m_155264_(pos, state);
   }

   default @Nullable BlockEntityTicker m_142354_(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType blockEntityType) {
      BlockEntityTypeRegistryObject type = this.getType();
      return type != null && blockEntityType == type.get() ? type.getTicker(level.f_46443_) : null;
   }

   default boolean triggerBlockEntityEvent(@NotNull BlockState state, Level level, BlockPos pos, int id, int param) {
      BlockEntity blockEntity = WorldHelper.getBlockEntity(level, pos);
      return blockEntity != null && blockEntity.m_7531_(id, param);
   }
}
