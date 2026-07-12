package lexis.Server.Tab;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class TabListManager {
   private static int entityCount = 0;
   private static int MAX_ENTITIES = 1000;
   private static boolean clearing = false;
   private static long serverStartTime = System.currentTimeMillis();

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase != Phase.END) {
         if (event.getServer().m_129921_() % 2 == 0) {
            entityCount = 0;

            AtomicInteger count;
            for(Iterator var1 = event.getServer().m_129785_().iterator(); var1.hasNext(); entityCount += count.get()) {
               ServerLevel level = (ServerLevel)var1.next();
               count = new AtomicInteger(0);
               level.m_8583_().forEach((entity) -> {
                  count.incrementAndGet();
               });
            }

            if (entityCount > MAX_ENTITIES && !clearing) {
               clearAllEntities(event.getServer().m_129785_());
            }
         }

         updateAllTabLists(event.getServer().m_6846_().m_11314_());
      }
   }

   private static void updateAllTabLists(List players) {
      String header = getHeader();
      Iterator var2 = players.iterator();

      while(var2.hasNext()) {
         ServerPlayer player = (ServerPlayer)var2.next();
         String footer = getFooter(player);
         player.f_8906_.m_9829_(new ClientboundTabListPacket(Component.m_237113_(header), Component.m_237113_(footer)));
      }

   }

   private static String getHeader() {
      return "§6§lLexis Server §7-X- §dCherry ";
   }

   private static String getFooter(ServerPlayer player) {
      long uptimeMillis = System.currentTimeMillis() - serverStartTime;
      long uptimeSeconds = uptimeMillis / 1000L;
      long days = uptimeSeconds / 86400L;
      long hours = uptimeSeconds % 86400L / 3600L;
      long minutes = uptimeSeconds % 3600L / 60L;
      long seconds = uptimeSeconds % 60L;
      double tps = Math.min(20.0, 1000.0 / Math.max((double)player.m_20194_().m_129903_(), 1.0));
      String tpsColor = tps > 18.0 ? "§a" : (tps > 15.0 ? "§e" : "§c");
      int ping = player.f_8906_.f_9743_.f_8943_;
      String pingColor = ping < 50 ? "§a" : (ping < 100 ? "§e" : "§c");
      StringBuilder footer = new StringBuilder();
      footer.append("§7════════════════════════════════════════════════════════\n");
      footer.append(String.format("§f在线玩家: §a%d\n", player.m_20194_().m_6846_().m_11309_()));
      footer.append(String.format("§f实体数量: %s%d§7/§c%d\n", entityCount > MAX_ENTITIES ? "§c" : "§a", entityCount, MAX_ENTITIES));
      footer.append(String.format("§f你的延迟: %s%dms\n", pingColor, ping));
      footer.append(String.format("§f服务器TPS: %s%.2f\n", tpsColor, tps));
      footer.append(String.format("§f运行时间: §a%d天 %d时 %d分 %d秒\n", days, hours, minutes, seconds));
      footer.append("§7════════════════════════════════════════════════════════");
      return footer.toString();
   }

   private static void clearAllEntities(Iterable levels) {
      clearing = true;
      int cleared = 0;
      Iterator var2 = levels.iterator();

      while(var2.hasNext()) {
         ServerLevel level = (ServerLevel)var2.next();
         List toRemove = new CopyOnWriteArrayList();
         level.m_8583_().forEach((entityx) -> {
            if (!(entityx instanceof ServerPlayer)) {
               toRemove.add(entityx);
            }

         });

         for(Iterator var5 = toRemove.iterator(); var5.hasNext(); ++cleared) {
            Entity entity = (Entity)var5.next();
            entity.m_142687_(RemovalReason.DISCARDED);
         }
      }

      clearing = false;
   }

   public static void setMaxEntities(int max) {
      MAX_ENTITIES = max;
   }

   public static int getMaxEntities() {
      return MAX_ENTITIES;
   }
}
