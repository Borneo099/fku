package moze_intel.projecte.gameObjs.blocks;

import moze_intel.projecte.gameObjs.items.PhilosophersStone;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

public abstract class BlockDirection extends Block {
   public static final DirectionProperty FACING;

   public BlockDirection(BlockBehaviour.Properties props) {
      super(props);
   }

   protected void m_7926_(@NotNull StateDefinition.@NotNull Builder props) {
      super.m_7926_(props);
      props.m_61104_(new Property[]{FACING});
   }

   public @NotNull BlockState m_5573_(BlockPlaceContext ctx) {
      return ctx.m_43723_() != null ? (BlockState)this.m_49966_().m_61124_(FACING, ctx.m_43723_().m_6350_().m_122424_()) : this.m_49966_();
   }

   /** @deprecated */
   @Deprecated
   public void m_6810_(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
      if (state.m_60734_() != newState.m_60734_()) {
         BlockEntity blockEntity = WorldHelper.getBlockEntity(level, pos);
         if (blockEntity != null) {
            blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent((inv) -> {
               WorldHelper.dropInventory(inv, level, pos);
            });
         }

         super.m_6810_(state, level, pos, newState, isMoving);
      }

   }

   /** @deprecated */
   @Deprecated
   public void m_6256_(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player) {
      if (!level.f_46443_) {
         ItemStack stack = player.m_21205_();
         if (!stack.m_41619_() && stack.m_41720_() instanceof PhilosophersStone) {
            level.m_46597_(pos, (BlockState)level.m_8055_(pos).m_61124_(FACING, player.m_6350_().m_122424_()));
         }
      }

   }

   /** @deprecated */
   @Deprecated
   public @NotNull BlockState m_6843_(BlockState state, Rotation rot) {
      return (BlockState)state.m_61124_(FACING, rot.m_55954_((Direction)state.m_61143_(FACING)));
   }

   /** @deprecated */
   @Deprecated
   public @NotNull BlockState m_6943_(BlockState state, Mirror mirrorIn) {
      return state.m_60717_(mirrorIn.m_54846_((Direction)state.m_61143_(FACING)));
   }

   static {
      FACING = HorizontalDirectionalBlock.f_54117_;
   }
}
