package lexis.Server.DogSlayerSword;

import java.awt.Color;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class FastMoveSkill {
   private static final String PREFIX = "§c[§6Lexis-Server§c] §f";
   private static final Map fastMovePlayers = new ConcurrentHashMap();

   public static void startFastMove(ServerPlayer player) {
      DogSlayerSword.setFastMoveState(player.m_20148_(), true);
      int originalMode = player.f_8941_.m_9290_().m_46392_();
      player.m_143403_(GameType.SPECTATOR);
      FastMoveData data = new FastMoveData(player.m_20148_(), originalMode);
      fastMovePlayers.put(player.m_20148_(), data);
      startFastMoveEffect(player, data);
   }

   private static void startFastMoveEffect(final ServerPlayer player, final FastMoveData data) {
      final ServerLevel level = (ServerLevel)player.m_9236_();
      String startEffect = String.format("particle minecraft:end_rod %f %f %f 0 0 0 0.3 129 force", player.m_20185_(), player.m_20186_() + 1.0, player.m_20189_());
      level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), startEffect);

      for(int i = 0; i < 9; ++i) {
         double offsetX = (Math.random() - 0.5) * 2.0;
         double offsetY = (Math.random() - 0.5) * 2.0;
         double offsetZ = (Math.random() - 0.5) * 2.0;
         String flashCommand = String.format("particle minecraft:flash %f %f %f 0 0 0 0 1 force", player.m_20185_() + offsetX, player.m_20186_() + 1.0 + offsetY, player.m_20189_() + offsetZ);
         level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), flashCommand);
      }

      data.timer = new Timer();
      data.timer.scheduleAtFixedRate(new TimerTask() {
         public void run() {
            if (data.ticks < 100 && player.m_6084_()) {
               level.m_7654_().execute(() -> {
                  FastMoveSkill.updateFastMove(player, data);
               });
               ++data.ticks;
            } else {
               level.m_7654_().execute(() -> {
                  FastMoveSkill.endFastMove(player, data);
               });
               data.timer.cancel();
            }
         }
      }, 0L, 50L);
   }

   private static void updateFastMove(ServerPlayer player, FastMoveData data) {
      ServerLevel level = (ServerLevel)player.m_9236_();

      String endRodCommand;
      for(int i = 0; i < 9; ++i) {
         endRodCommand = String.format("execute as %s at %s run tp %s ^ ^ ^0.7", player.m_7755_().getString(), player.m_7755_().getString(), player.m_7755_().getString());
         level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), endRodCommand);
      }

      data.hue += 0.05F;
      if (data.hue > 1.0F) {
         data.hue = 0.0F;
      }

      Vec3 pos = player.m_20182_();
      endRodCommand = String.format("particle minecraft:end_rod %f %f %f 0 0 0 0.07 29 force", pos.f_82479_, pos.f_82480_ + 0.5, pos.f_82481_);
      level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), endRodCommand);

      int remaining;
      float r;
      float b;
      for(remaining = 0; remaining < 20; ++remaining) {
         double t = (double)remaining / 20.0;
         r = (float)(((double)data.hue + t * 0.30000001192092896) % 1.0);
         int rgb = Color.HSBtoRGB(r, 0.9F, 1.0F);
         b = (float)(rgb >> 16 & 255) / 255.0F;
         float g = (float)(rgb >> 8 & 255) / 255.0F;
         float b = (float)(rgb & 255) / 255.0F;
         double wave = Math.sin((double)data.ticks * 0.4 + (double)remaining * 0.6) * 0.6;
         String dustCommand = String.format("particle dust %.2f %.2f %.2f 0.8 %f %f %f 0 0 0 0 2 force", b, g, b, pos.f_82479_ - player.m_20154_().f_82479_ * t * 3.0 + wave * player.m_20154_().f_82481_, pos.f_82480_ + 0.5 + Math.sin((double)data.ticks * 0.25 + (double)remaining) * 0.4, pos.f_82481_ - player.m_20154_().f_82481_ * t * 3.0 - wave * player.m_20154_().f_82479_);
         level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), dustCommand);
      }

      for(remaining = 0; remaining < 8; ++remaining) {
         float hue = (data.hue + (float)remaining * 0.1F) % 1.0F;
         int rgb = Color.HSBtoRGB(hue, 1.0F, 1.0F);
         r = (float)(rgb >> 16 & 255) / 255.0F;
         float g = (float)(rgb >> 8 & 255) / 255.0F;
         b = (float)(rgb & 255) / 255.0F;
         String speedLine = String.format("particle dust %.2f %.2f %.2f 1.0 %f %f %f 0 0 0 0 1 force", r, g, b, pos.f_82479_ - player.m_20154_().f_82479_ * (3.0 + Math.random() * 4.0), pos.f_82480_ + 0.5 + Math.random() * 1.5, pos.f_82481_ - player.m_20154_().f_82481_ * (3.0 + Math.random() * 4.0));
         level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), speedLine);
      }

      if (data.ticks % 20 == 0) {
         remaining = (100 - data.ticks) / 20;
         player.m_5661_(Component.m_237113_("快速移动，剩余: " + remaining + "秒"), true);
      }

   }

   private static void endFastMove(ServerPlayer player, FastMoveData data) {
      DogSlayerSword.setFastMoveState(player.m_20148_(), false);
      ServerLevel level = (ServerLevel)player.m_9236_();
      Vec3 pos = player.m_20182_();
      GameType originalType = GameType.m_46393_(data.originalGameMode);
      player.m_143403_(originalType);

      for(int radius = 1; radius <= 5; ++radius) {
         int steps = radius * 8;

         for(int i = 0; i < steps; ++i) {
            double angle = (double)i * Math.PI * 2.0 / (double)steps;
            double dx = Math.cos(angle) * (double)radius;
            double dz = Math.sin(angle) * (double)radius;
            float hue = (float)Math.random();
            int rgb = Color.HSBtoRGB(hue, 0.9F, 1.0F);
            float r = (float)(rgb >> 16 & 255) / 255.0F;
            float g = (float)(rgb >> 8 & 255) / 255.0F;
            float b = (float)(rgb & 255) / 255.0F;
            String dustCommand = String.format("particle dust %.2f %.2f %.2f 1.0 %f %f %f 0 0 0 0 1 force", r, g, b, pos.f_82479_ + dx, pos.f_82480_ + 0.5 + Math.random(), pos.f_82481_ + dz);
            level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), dustCommand);
         }
      }

      fastMovePlayers.remove(player.m_20148_());
   }

   private static class FastMoveData {
      final UUID playerId;
      final int originalGameMode;
      int ticks = 0;
      Timer timer;
      float hue = 0.0F;

      FastMoveData(UUID playerId, int originalGameMode) {
         this.playerId = playerId;
         this.originalGameMode = originalGameMode;
      }
   }
}
