package lexis.Hack.Utils;

import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockBreaker {
   private static final Minecraft mc = Minecraft.m_91087_();

   public static boolean breakOneBlock(BlockPos pos, boolean swing) {
      BlockBreakingParams params = getBlockBreakingParams(pos);
      if (params == null) {
         return false;
      } else {
         mc.f_91074_.f_108617_.m_104955_(new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, pos, params.side));
         if (swing) {
            mc.f_91074_.f_108617_.m_104955_(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
         }

         mc.f_91074_.f_108617_.m_104955_(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, pos, params.side));
         return true;
      }
   }

   public static BlockBreakingParams getBlockBreakingParams(BlockPos pos) {
      BlockState state = BlockUtils.getState(pos);
      VoxelShape shape = state.m_60808_(mc.f_91073_, pos);
      if (shape.m_83281_()) {
         return null;
      } else {
         Vec3 eyesPos = mc.f_91074_.m_146892_();
         Vec3 center = Vec3.m_82512_(pos);
         Direction bestSide = null;
         Vec3 bestHitVec = null;
         double bestDistSq = Double.MAX_VALUE;
         Direction[] var9 = Direction.values();
         int var10 = var9.length;

         for(int var11 = 0; var11 < var10; ++var11) {
            Direction side = var9[var11];
            Vec3 hitVec = center.m_82520_((double)side.m_122429_() * 0.5, (double)side.m_122430_() * 0.5, (double)side.m_122431_() * 0.5);
            double distSq = eyesPos.m_82557_(hitVec);
            if (distSq < bestDistSq) {
               bestDistSq = distSq;
               bestSide = side;
               bestHitVec = hitVec;
            }
         }

         return new BlockBreakingParams(bestSide, bestHitVec, bestDistSq);
      }
   }

   public static void breakBlocksWithPacketSpam(Iterable blocks) {
      Vec3 eyesPos = mc.f_91074_.m_146892_();
      Iterator var2 = blocks.iterator();

      while(true) {
         while(var2.hasNext()) {
            BlockPos pos = (BlockPos)var2.next();
            Vec3 posVec = Vec3.m_82512_(pos);
            Direction[] var5 = Direction.values();
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               Direction side = var5[var7];
               Vec3 hitVec = posVec.m_82520_((double)side.m_122429_() * 0.5, (double)side.m_122430_() * 0.5, (double)side.m_122431_() * 0.5);
               if (eyesPos.m_82557_(hitVec) < eyesPos.m_82557_(posVec)) {
                  mc.f_91074_.f_108617_.m_104955_(new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, pos, side));
                  mc.f_91074_.f_108617_.m_104955_(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, pos, side));
                  break;
               }
            }
         }

         return;
      }
   }

   public static record BlockBreakingParams(Direction side, Vec3 hitVec, double distanceSq) {
      public BlockBreakingParams(Direction side, Vec3 hitVec, double distanceSq) {
         this.side = side;
         this.hitVec = hitVec;
         this.distanceSq = distanceSq;
      }

      public Direction side() {
         return this.side;
      }

      public Vec3 hitVec() {
         return this.hitVec;
      }

      public double distanceSq() {
         return this.distanceSq;
      }
   }
}
