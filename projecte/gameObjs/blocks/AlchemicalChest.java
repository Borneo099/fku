package moze_intel.projecte.gameObjs.blocks;

import moze_intel.projecte.gameObjs.block_entities.EmcChestBlockEntity;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlchemicalChest extends BlockDirection implements SimpleWaterloggedBlock, PEEntityBlock {
   private static final VoxelShape SHAPE = Block.m_49796_(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

   public AlchemicalChest(BlockBehaviour.Properties props) {
      super(props);
      this.m_49959_((BlockState)((BlockState)((BlockState)this.m_49965_().m_61090_()).m_61124_(FACING, Direction.NORTH)).m_61124_(BlockStateProperties.f_61362_, false));
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

   /** @deprecated */
   @Deprecated
   public @NotNull RenderShape m_7514_(@NotNull BlockState state) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }

   /** @deprecated */
   @Deprecated
   public @NotNull InteractionResult m_6227_(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult rtr) {
      if (level.f_46443_) {
         return InteractionResult.SUCCESS;
      } else {
         EmcChestBlockEntity chest = (EmcChestBlockEntity)WorldHelper.getBlockEntity(EmcChestBlockEntity.class, level, pos, true);
         if (chest != null) {
            NetworkHooks.openScreen((ServerPlayer)player, chest, pos);
            player.m_36220_(Stats.f_12968_);
            PiglinAi.m_34873_(player, true);
         }

         return InteractionResult.CONSUME;
      }
   }

   public @Nullable BlockEntityTypeRegistryObject getType() {
      return PEBlockEntityTypes.ALCHEMICAL_CHEST;
   }

   /** @deprecated */
   @Deprecated
   public boolean m_8133_(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
      super.m_8133_(state, level, pos, id, param);
      return this.triggerBlockEntityEvent(state, level, pos, id, param);
   }

   /** @deprecated */
   @Deprecated
   public void m_213897_(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
      EmcChestBlockEntity chest = (EmcChestBlockEntity)WorldHelper.getBlockEntity(EmcChestBlockEntity.class, level, pos);
      if (chest != null) {
         chest.recheckOpen();
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
      BlockEntity blockEntity = WorldHelper.getBlockEntity(level, pos);
      return blockEntity != null ? (Integer)blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).map(ItemHandlerHelper::calcRedstoneFromInventory).orElse(0) : 0;
   }

   public @NotNull BlockState m_5573_(BlockPlaceContext context) {
      return (BlockState)super.m_5573_(context).m_61124_(BlockStateProperties.f_61362_, context.m_43725_().m_6425_(context.m_8083_()).m_76152_() == Fluids.f_76193_);
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
}
