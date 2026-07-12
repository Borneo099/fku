package lexis.Hack.Utils.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CollisionHelper {
   private final Level level;
   private static final double FAT_WIDTH = 0.85;
   private static final double PLAYER_HEIGHT = 1.8;

   public CollisionHelper(Level level) {
      this.level = level;
   }

   public boolean canRaycast(Vec3 start, Vec3 end) {
      return this.canSweep(start, end);
   }

   public boolean isSafe(Vec3 pos) {
      return this.isSafeBox(pos.f_82479_, pos.f_82480_, pos.f_82481_);
   }

   public double getFloorHeight(BlockPos pos) {
      BlockState state = this.level.m_8055_(pos);
      VoxelShape shape = state.m_60812_(this.level, pos);
      return shape.m_83281_() ? 0.0 : shape.m_83297_(Axis.Y);
   }

   private boolean isSafeBox(double x, double y, double z) {
      AABB box = new AABB(x - 0.425, y + 0.01, z - 0.425, x + 0.425, y + 1.8, z + 0.425);
      return !this.level.m_186434_((Entity)null, box).iterator().hasNext();
   }

   public boolean canSweep(Vec3 start, Vec3 end) {
      double dist = start.m_82554_(end);
      if (dist < 0.001) {
         return true;
      } else {
         int steps = (int)Math.ceil(dist / 0.05);
         Vec3 dir = end.m_82546_(start).m_82490_(1.0 / (double)steps);
         Vec3 current = start;

         for(int i = 1; i <= steps; ++i) {
            current = current.m_82549_(dir);
            if (!this.isSafe(current)) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean isStrictDiagonalSafe(BlockPos start, BlockPos end) {
      BlockPos c1 = new BlockPos(start.m_123341_(), start.m_123342_(), end.m_123343_());
      BlockPos c2 = new BlockPos(end.m_123341_(), start.m_123342_(), start.m_123343_());
      return !this.isBlockSolid(c1) && !this.isBlockSolid(c2);
   }

   private boolean isBlockSolid(BlockPos pos) {
      return !this.level.m_8055_(pos).m_60812_(this.level, pos).m_83281_();
   }
}
