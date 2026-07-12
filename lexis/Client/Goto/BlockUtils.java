package lexis.Client.Goto;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockUtils {
   private static final Minecraft MC = Minecraft.m_91087_();

   public static BlockState getState(BlockPos pos) {
      return MC.f_91073_ == null ? null : MC.f_91073_.m_8055_(pos);
   }

   public static Block getBlock(BlockPos pos) {
      BlockState state = getState(pos);
      return state != null ? state.m_60734_() : null;
   }

   public static boolean canBeClicked(BlockPos pos) {
      BlockState state = getState(pos);
      return state != null && state.m_60808_(MC.f_91073_, pos) != Shapes.m_83040_();
   }

   public static AABB getBoundingBox(BlockPos pos) {
      BlockState state = getState(pos);
      if (state == null) {
         return new AABB(pos);
      } else {
         VoxelShape shape = state.m_60808_(MC.f_91073_, pos);
         return shape.m_83281_() ? new AABB(pos) : shape.m_83215_().m_82338_(pos);
      }
   }

   public static boolean isLiquid(BlockPos pos) {
      Block block = getBlock(pos);
      return block instanceof LiquidBlock;
   }

   public static boolean isAir(BlockPos pos) {
      BlockState state = getState(pos);
      return state != null && state.m_60795_();
   }

   public static boolean isSolid(BlockPos pos) {
      BlockState state = getState(pos);
      return state != null && state.m_280296_();
   }

   public static boolean isReplaceable(BlockPos pos) {
      BlockState state = getState(pos);
      return state != null && state.m_247087_();
   }
}
