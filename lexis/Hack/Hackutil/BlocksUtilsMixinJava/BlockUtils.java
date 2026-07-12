package lexis.Hack.Hackutil.BlocksUtilsMixinJava;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

public class BlockUtils {
   public static boolean isPlayerInPortal(Player player) {
      if (player != null && player.m_9236_() != null) {
         BlockPos pos = player.m_20183_();
         int range = 2;

         for(int x = -range; x <= range; ++x) {
            for(int y = -range; y <= range; ++y) {
               for(int z = -range; z <= range; ++z) {
                  BlockPos checkPos = pos.m_7918_(x, y, z);
                  if (player.m_9236_().m_8055_(checkPos).m_60713_(Blocks.f_50142_)) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
