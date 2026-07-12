package moze_intel.projecte.gameObjs.blocks;

import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransmutationStone extends DirectionalBlock implements SimpleWaterloggedBlock {
   private static final VoxelShape UP_SHAPE = Block.m_49796_(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);
   private static final VoxelShape DOWN_SHAPE = Block.m_49796_(0.0, 12.0, 0.0, 16.0, 16.0, 16.0);
   private static final VoxelShape NORTH_SHAPE = Block.m_49796_(0.0, 0.0, 12.0, 16.0, 16.0, 16.0);
   private static final VoxelShape SOUTH_SHAPE = Block.m_49796_(0.0, 0.0, 0.0, 16.0, 16.0, 4.0);
   private static final VoxelShape WEST_SHAPE = Block.m_49796_(12.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   private static final VoxelShape EAST_SHAPE = Block.m_49796_(0.0, 0.0, 0.0, 4.0, 16.0, 16.0);

   public TransmutationStone(BlockBehaviour.Properties props) {
      super(props);
      this.m_49959_((BlockState)((BlockState)((BlockState)this.m_49965_().m_61090_()).m_61124_(f_52588_, Direction.UP)).m_61124_(BlockStateProperties.f_61362_, false));
   }

   protected void m_7926_(@NotNull StateDefinition.@NotNull Builder props) {
      super.m_7926_(props);
      props.m_61104_(new Property[]{f_52588_, BlockStateProperties.f_61362_});
   }

   /** @deprecated */
   @Deprecated
   public boolean m_7357_(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull PathComputationType type) {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public @NotNull VoxelShape m_5940_(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
      Direction facing = (Direction)state.m_61143_(f_52588_);
      VoxelShape var10000;
      switch (facing) {
         case DOWN:
            var10000 = DOWN_SHAPE;
            break;
         case NORTH:
            var10000 = NORTH_SHAPE;
            break;
         case SOUTH:
            var10000 = SOUTH_SHAPE;
            break;
         case WEST:
            var10000 = WEST_SHAPE;
            break;
         case EAST:
            var10000 = EAST_SHAPE;
            break;
         case UP:
            var10000 = UP_SHAPE;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   /** @deprecated */
   @Deprecated
   public @NotNull InteractionResult m_6227_(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult rtr) {
      if (!level.f_46443_) {
         NetworkHooks.openScreen((ServerPlayer)player, new ContainerProvider(), (b) -> {
            b.writeBoolean(false);
         });
      }

      return InteractionResult.m_19078_(level.f_46443_);
   }

   public @Nullable BlockState m_5573_(@NotNull BlockPlaceContext context) {
      BlockState state = super.m_5573_(context);
      return state == null ? null : (BlockState)((BlockState)state.m_61124_(f_52588_, context.m_43719_())).m_61124_(BlockStateProperties.f_61362_, context.m_43725_().m_6425_(context.m_8083_()).m_76152_() == Fluids.f_76193_);
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

   /** @deprecated */
   @Deprecated
   public @NotNull BlockState m_6843_(BlockState state, Rotation rot) {
      return (BlockState)state.m_61124_(f_52588_, rot.m_55954_((Direction)state.m_61143_(f_52588_)));
   }

   /** @deprecated */
   @Deprecated
   public @NotNull BlockState m_6943_(BlockState state, Mirror mirrorIn) {
      return state.m_60717_(mirrorIn.m_54846_((Direction)state.m_61143_(f_52588_)));
   }

   private static class ContainerProvider implements MenuProvider {
      public AbstractContainerMenu m_7208_(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
         return new TransmutationContainer(windowId, playerInventory);
      }

      public @NotNull Component m_5446_() {
         return PELang.TRANSMUTATION_TRANSMUTE.translate(new Object[0]);
      }
   }
}
