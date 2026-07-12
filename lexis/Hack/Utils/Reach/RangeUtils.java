package lexis.Hack.Utils.Reach;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class RangeUtils {
   public static double get() {
      return Minecraft.m_91087_().f_91074_.getEntityReach();
   }

   public static double getBlockReach() {
      return Minecraft.m_91087_().f_91074_.getBlockReach();
   }

   public static boolean canHit(Entity entity) {
      return Minecraft.m_91087_().f_91074_.canReach(entity, 0.0);
   }

   public static boolean canReachBlock(BlockPos pos) {
      Vec3 eyePos = Minecraft.m_91087_().f_91074_.m_146892_();
      Vec3 blockCenter = Vec3.m_82512_(pos);
      double maxDist = getBlockReach();
      return eyePos.m_82557_(blockCenter) <= maxDist * maxDist;
   }
}
