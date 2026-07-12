package lexis.Hack.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockPlacer {
   private static final Minecraft mc = Minecraft.m_91087_();

   public static BlockHitResult getBlockPlacingParams(BlockPos pos) {
      if (mc.f_91073_.m_8055_(pos).m_60795_()) {
         Direction[] var8 = Direction.values();
         int var9 = var8.length;

         for(int var10 = 0; var10 < var9; ++var10) {
            Direction side = var8[var10];
            BlockPos neighbor = pos.m_121945_(side);
            BlockState neighborState = mc.f_91073_.m_8055_(neighbor);
            if (!neighborState.m_60795_() && neighborState.m_280296_()) {
               Vec3 hitVec = Vec3.m_82512_(neighbor).m_82520_((double)side.m_122424_().m_122429_() * 0.5, (double)side.m_122424_().m_122430_() * 0.5, (double)side.m_122424_().m_122431_() * 0.5);
               return new BlockHitResult(hitVec, side.m_122424_(), neighbor, false);
            }
         }

         return null;
      } else {
         BlockState state = mc.f_91073_.m_8055_(pos);
         VoxelShape shape = state.m_60808_(mc.f_91073_, pos);
         if (!shape.m_83281_()) {
            AABB box = shape.m_83215_();
            Vec3 center = Vec3.m_82512_(pos).m_82520_(box.f_82288_, box.f_82289_, box.f_82290_);
            Vec3 hitVec = new Vec3(center.f_82479_, center.f_82480_ + box.m_82376_() / 2.0, center.f_82481_);
            return new BlockHitResult(hitVec, Direction.UP, pos, false);
         } else {
            return null;
         }
      }
   }
}
