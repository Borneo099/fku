package lexis.Server.Commandsavailabletoplayers.Store;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lexis.Server.Commandsavailabletoplayers.Menu.NoMenuCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CoinRewardsListener {
   private static final Map playerStats = new ConcurrentHashMap();

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase != Phase.END) {
         if (!NoMenuCommand.isNoMenuEnabled()) {
            if (event.getServer().m_129921_() % 1200 == 0) {
               Iterator var1 = event.getServer().m_6846_().m_11314_().iterator();

               while(var1.hasNext()) {
                  ServerPlayer player = (ServerPlayer)var1.next();
                  UUID playerId = player.m_20148_();
                  StoreConfig.getInstance().addPlayerMoney(playerId, 1);
                  StoreConfig.getInstance().addPlayerOnlineTime(playerId, 60L);
                  player.m_213846_(Component.m_237113_("§a[奖励] §7在线1分钟 +1 金币"));
               }
            }

         }
      }
   }

   @SubscribeEvent
   public static void onPlayerKill(LivingDeathEvent event) {
      if (!NoMenuCommand.isNoMenuEnabled()) {
         Entity var2 = event.getSource().m_7639_();
         if (var2 instanceof ServerPlayer) {
            ServerPlayer killer = (ServerPlayer)var2;
            LivingEntity var3 = event.getEntity();
            if (var3 instanceof ServerPlayer) {
               ServerPlayer victim = (ServerPlayer)var3;
               UUID killerId = killer.m_20148_();
               PlayerStats stats = (PlayerStats)playerStats.computeIfAbsent(killerId, (k) -> {
                  return new PlayerStats();
               });
               ++stats.killCount;
               if (stats.killCount >= 5) {
                  StoreConfig.getInstance().addPlayerMoney(killerId, 35);
                  killer.m_213846_(Component.m_237113_("§a[奖励] §7击杀5个玩家 +35 金币"));
                  stats.killCount = 0;
               }
            }
         }

      }
   }

   @SubscribeEvent
   public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
      if (!NoMenuCommand.isNoMenuEnabled()) {
         Entity var2 = event.getEntity();
         if (var2 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)var2;
            if (event.getPlacedBlock().m_60734_() == Blocks.f_50272_ || event.getPlacedBlock().m_60734_() == Blocks.f_50448_ || event.getPlacedBlock().m_60734_() == Blocks.f_50447_) {
               return;
            }

            UUID playerId = player.m_20148_();
            PlayerStats stats = (PlayerStats)playerStats.computeIfAbsent(playerId, (k) -> {
               return new PlayerStats();
            });
            ++stats.blockPlaceCount;
            if (stats.blockPlaceCount >= 256) {
               StoreConfig.getInstance().addPlayerMoney(playerId, 120);
               player.m_213846_(Component.m_237113_("§a[奖励] §7放置256个方块 +120 金币"));
               stats.blockPlaceCount = 0;
            }
         }

      }
   }

   private static class PlayerStats {
      int killCount = 0;
      int blockPlaceCount = 0;
   }
}
