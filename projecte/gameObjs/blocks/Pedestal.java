package moze_intel.projecte.gameObjs.blocks;

import java.util.List;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.block_entities.DMPedestalBlockEntity;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Pedestal extends Block implements SimpleWaterloggedBlock, PEEntityBlock, IMatterBlock {
   private static final VoxelShape SHAPE = Shapes.m_83110_(Block.m_49796_(3.0, 0.0, 3.0, 13.0, 2.0, 13.0), Shapes.m_83110_(Block.m_49796_(6.0, 2.0, 6.0, 10.0, 9.0, 10.0), Block.m_49796_(5.0, 9.0, 5.0, 11.0, 10.0, 11.0)));

   public Pedestal(BlockBehaviour.Properties props) {
      super(props);
      this.m_49959_((BlockState)((BlockState)this.m_49965_().m_61090_()).m_61124_(BlockStateProperties.f_61362_, false));
   }

   protected void m_7926_(@NotNull StateDefinition.@NotNull Builder props) {
      super.m_7926_(props);
      props.m_61104_(new Property[]{BlockStateProperties.f_61362_});
   }

   /** @deprecated */
   @Deprecated
   public boolean m_7357_(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull PathComputationType type) {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public @NotNull VoxelShape m_5940_(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
      return SHAPE;
   }

   private boolean dropItem(Level level, BlockPos pos) {
      DMPedestalBlockEntity pedestal = (DMPedestalBlockEntity)WorldHelper.getBlockEntity(DMPedestalBlockEntity.class, level, pos);
      if (pedestal != null) {
         ItemStack stack = pedestal.getInventory().getStackInSlot(0);
         if (!stack.m_41619_()) {
            pedestal.getInventory().setStackInSlot(0, ItemStack.f_41583_);
            level.m_7967_(new ItemEntity(level, (double)pos.m_123341_(), (double)pos.m_123342_() + 0.8, (double)pos.m_123343_(), stack));
            return true;
         }
      }

      return false;
   }

   /** @deprecated */
   @Deprecated
   public void m_6810_(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
      if (state.m_60734_() != newState.m_60734_()) {
         this.dropItem(level, pos);
         super.m_6810_(state, level, pos, newState, isMoving);
      }

   }

   /** @deprecated */
   @Deprecated
   public void m_6256_(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player) {
      if (!level.f_46443_) {
         this.dropItem(level, pos);
      }

   }

   public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
      if (player.m_7500_() && this.dropItem(level, pos)) {
         level.m_7260_(pos, state, state, 8);
         return false;
      } else {
         return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
      }
   }

   /** @deprecated */
   @Deprecated
   public @NotNull InteractionResult m_6227_(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult rtr) {
      if (!level.f_46443_) {
         DMPedestalBlockEntity pedestal = (DMPedestalBlockEntity)WorldHelper.getBlockEntity(DMPedestalBlockEntity.class, level, pos, true);
         if (pedestal == null) {
            return InteractionResult.FAIL;
         }

         ItemStack item = pedestal.getInventory().getStackInSlot(0);
         ItemStack stack = player.m_21120_(hand);
         if (stack.m_41619_() && !item.m_41619_()) {
            item.getCapability(PECapabilities.PEDESTAL_ITEM_CAPABILITY).ifPresent((pedestalItem) -> {
               pedestal.setActive(!pedestal.getActive());
               level.m_7260_(pos, state, state, 8);
            });
         } else if (!stack.m_41619_() && item.m_41619_()) {
            pedestal.getInventory().setStackInSlot(0, stack.m_41620_(1));
            if (stack.m_41613_() <= 0) {
               player.m_21008_(hand, ItemStack.f_41583_);
            }
         }
      }

      return InteractionResult.m_19078_(level.f_46443_);
   }

   /** @deprecated */
   @Deprecated
   public void m_6861_(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Block neighbor, @NotNull BlockPos neighborPos, boolean isMoving) {
      boolean hasSignal = level.m_276867_(pos);
      DMPedestalBlockEntity ped = (DMPedestalBlockEntity)WorldHelper.getBlockEntity(DMPedestalBlockEntity.class, level, pos);
      if (ped != null && ped.previousRedstoneState != hasSignal) {
         if (hasSignal) {
            ItemStack stack = ped.getInventory().getStackInSlot(0);
            if (!stack.m_41619_() && stack.getCapability(PECapabilities.PEDESTAL_ITEM_CAPABILITY).isPresent()) {
               ped.setActive(!ped.getActive());
               level.m_7260_(pos, state, state, 11);
            }
         }

         ped.previousRedstoneState = hasSignal;
         ped.markDirty(false);
      }

   }

   /** @deprecated */
   @Deprecated
   public boolean m_7278_(@NotNull BlockState state) {
      return true;
   }

   /** @deprecated */
   @Deprecated
   public int m_6782_(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
      DMPedestalBlockEntity pedestal = (DMPedestalBlockEntity)WorldHelper.getBlockEntity(DMPedestalBlockEntity.class, level, pos);
      if (pedestal != null) {
         ItemStack stack = pedestal.getInventory().getStackInSlot(0);
         if (!stack.m_41619_()) {
            if (stack.getCapability(PECapabilities.PEDESTAL_ITEM_CAPABILITY).isPresent()) {
               return pedestal.getActive() ? 15 : 10;
            }

            return 5;
         }
      }

      return 0;
   }

   public @Nullable BlockEntityTypeRegistryObject getType() {
      return PEBlockEntityTypes.DARK_MATTER_PEDESTAL;
   }

   /** @deprecated */
   @Deprecated
   public boolean m_8133_(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
      super.m_8133_(state, level, pos, id, param);
      return this.triggerBlockEntityEvent(state, level, pos, id, param);
   }

   public void m_5871_(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List tooltip, @NotNull TooltipFlag flags) {
      super.m_5871_(stack, level, tooltip, flags);
      tooltip.add(PELang.PEDESTAL_TOOLTIP1.translate(new Object[0]));
      tooltip.add(PELang.PEDESTAL_TOOLTIP2.translate(new Object[0]));
   }

   public @Nullable BlockState m_5573_(@NotNull BlockPlaceContext context) {
      BlockState state = super.m_5573_(context);
      return state == null ? null : (BlockState)state.m_61124_(BlockStateProperties.f_61362_, context.m_43725_().m_6425_(context.m_8083_()).m_76152_() == Fluids.f_76193_);
   }

   /** @deprecated */
   @Deprecated
   public @NotNull FluidState m_5888_(BlockState state) {
      return (Boolean)state.m_61143_(BlockStateProperties.f_61362_) ? Fluids.f_76193_.m_76068_(false) : super.m_5888_(state);
   }

   /** @deprecated */
   @Deprecated
   public @NotNull BlockState m_7417_(@NotNull BlockState state, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor level, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
      if ((Boolean)state.m_61143_(BlockStateProperties.f_61362_)) {
         level.m_186469_(currentPos, Fluids.f_76193_, Fluids.f_76193_.m_6718_(level));
      }

      return super.m_7417_(state, facing, facingState, level, currentPos, facingPos);
   }

   public EnumMatterType getMatterType() {
      return EnumMatterType.DARK_MATTER;
   }
}
