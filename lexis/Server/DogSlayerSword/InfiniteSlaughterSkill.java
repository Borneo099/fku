package lexis.Server.DogSlayerSword;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;

public class InfiniteSlaughterSkill {
   public static void execute(ServerPlayer player) {
      ServerLevel level = (ServerLevel)player.m_9236_();
      BlockPos playerPos = player.m_20183_();
      CommandSourceStack silentSource = level.m_7654_().m_129893_().m_81324_();
      List targets = new ArrayList();
      level.m_260813_(EntityTypeTest.m_156916_(LivingEntity.class), (entity) -> {
         if (entity == player) {
            return false;
         } else if (entity instanceof Player) {
            return false;
         } else {
            return !(entity.m_20275_((double)playerPos.m_123341_(), (double)playerPos.m_123342_(), (double)playerPos.m_123343_()) > 16384.0);
         }
      }, targets);
      Iterator var5 = targets.iterator();

      while(var5.hasNext()) {
         Entity target = (Entity)var5.next();
         BlockPos targetPos = target.m_20183_();

         for(int i = 0; i < 6; ++i) {
            double offsetX = (Math.random() - 0.5) * 2.0;
            double offsetZ = (Math.random() - 0.5) * 2.0;
            String cmd = String.format("summon lightning_bolt %d %d %d", targetPos.m_123341_() + (int)offsetX, targetPos.m_123342_(), targetPos.m_123343_() + (int)offsetZ);
            level.m_7654_().m_129892_().m_230957_(silentSource, cmd);
         }

         target.m_6074_();
         target.m_142687_(RemovalReason.KILLED);
      }

   }
}
