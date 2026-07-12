package lexis.Hack.Utils;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockUtils {
   private static final Minecraft mc = Minecraft.m_91087_();

   public static BlockState getState(BlockPos pos) {
      return mc.f_91073_.m_8055_(pos);
   }

   public static Block getBlock(BlockPos pos) {
      return getState(pos).m_60734_();
   }

   public static String getName(BlockPos pos) {
      return getName(getBlock(pos));
   }

   public static String getName(Block block) {
      return BuiltInRegistries.f_256975_.m_7981_(block).toString();
   }

   public static List getSphere(Vec3 center, double radius) {
      List list = new ArrayList();
      int radiusInt = (int)Math.ceil(radius);
      BlockPos centerPos = BlockPos.m_274446_(center);

      for(int x = -radiusInt; x <= radiusInt; ++x) {
         for(int y = -radiusInt; y <= radiusInt; ++y) {
            for(int z = -radiusInt; z <= radiusInt; ++z) {
               BlockPos pos = centerPos.m_7918_(x, y, z);
               if (center.m_82557_(Vec3.m_82512_(pos)) <= radius * radius) {
                  list.add(pos);
               }
            }
         }
      }

      return list;
   }

   public static Direction getClickSide(BlockPos pos) {
      Direction best = null;
      double bestDist = Double.MAX_VALUE;
      Vec3 eye = mc.f_91074_.m_146892_();
      Direction[] var5 = Direction.values();
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         Direction dir = var5[var7];
         Vec3 hitVec = Vec3.m_82512_(pos).m_82520_((double)dir.m_122429_() * 0.5, (double)dir.m_122430_() * 0.5, (double)dir.m_122431_() * 0.5);
         if (eye.m_82557_(hitVec) < bestDist) {
            HitResult result = mc.f_91073_.m_45547_(new ClipContext(eye, hitVec, net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.NONE, mc.f_91074_));
            if (result.m_6662_() == Type.MISS || result.m_82450_().m_82509_(hitVec, 0.01)) {
               bestDist = eye.m_82557_(hitVec);
               best = dir;
            }
         }
      }

      return best;
   }

   public static Block getBlockFromName(String name) {
      try {
         return (Block)BuiltInRegistries.f_256975_.m_7745_(new ResourceLocation(name));
      } catch (Exception var2) {
         return Blocks.f_50016_;
      }
   }

   public static float getHardness(BlockPos pos) {
      return getState(pos).m_60800_(mc.f_91073_, pos);
   }

   private static VoxelShape getOutlineShape(BlockPos pos) {
      return getState(pos).m_60808_(mc.f_91073_, pos);
   }

   public static AABB getBoundingBox(BlockPos pos) {
      return getOutlineShape(pos).m_83215_().m_82338_(pos);
   }

   public static boolean canBeClicked(BlockPos pos) {
      return getOutlineShape(pos) != Shapes.m_83040_();
   }

   public static ArrayList getAllInBox(BlockPos from, BlockPos to) {
      ArrayList blocks = new ArrayList();
      BlockPos min = new BlockPos(Math.min(from.m_123341_(), to.m_123341_()), Math.min(from.m_123342_(), to.m_123342_()), Math.min(from.m_123343_(), to.m_123343_()));
      BlockPos max = new BlockPos(Math.max(from.m_123341_(), to.m_123341_()), Math.max(from.m_123342_(), to.m_123342_()), Math.max(from.m_123343_(), to.m_123343_()));

      for(int x = min.m_123341_(); x <= max.m_123341_(); ++x) {
         for(int y = min.m_123342_(); y <= max.m_123342_(); ++y) {
            for(int z = min.m_123343_(); z <= max.m_123343_(); ++z) {
               blocks.add(new BlockPos(x, y, z));
            }
         }
      }

      return blocks;
   }

   public static ArrayList getAllInBox(BlockPos center, int range) {
      return getAllInBox(center.m_7918_(-range, -range, -range), center.m_7918_(range, range, range));
   }
}
