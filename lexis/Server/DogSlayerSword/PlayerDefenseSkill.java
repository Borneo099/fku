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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class PlayerDefenseSkill {
   private static final String PREFIX = "§c[§6Lexis-Server§c] §f";
   private static final Map defenseEffects = new ConcurrentHashMap();
   private static final Map playerEffects = new ConcurrentHashMap();

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase != Phase.END) {
         Player var2 = event.player;
         if (var2 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)var2;
            ItemStack mainHand = player.m_21205_();
            ItemStack offHand = player.m_21206_();
            boolean hasSword = DogSlayerSword.isDogSlayerSword(mainHand) || DogSlayerSword.isDogSlayerSword(offHand);
            UUID playerId = player.m_20148_();
            if (hasSword) {
               if (!playerEffects.containsKey(playerId)) {
                  playerEffects.put(playerId, new PlayerEffect(playerId));
               }

               PlayerEffect effect = (PlayerEffect)playerEffects.get(playerId);
               updatePlayerEffect(player, effect);
               ++effect.ticks;
            } else {
               playerEffects.remove(playerId);
            }

         }
      }
   }

   private static void updatePlayerEffect(ServerPlayer player, PlayerEffect effect) {
      if (!DogSlayerSword.isInFastMove(player.m_20148_())) {
         ServerLevel level = (ServerLevel)player.m_9236_();
         Vec3 pos = player.m_20182_();
         double time = (double)effect.ticks * 0.05;
         double speed = 0.02;
         int starPoints = 5;
         int pointsPerEdge = 10;

         int i;
         double angle;
         double radius;
         double x;
         double z;
         for(i = 0; i < starPoints; ++i) {
            angle = (double)i * Math.PI * 2.0 / (double)starPoints;
            radius = (double)(i + 1) * Math.PI * 2.0 / (double)starPoints;
            x = pos.f_82479_ + Math.cos(angle) * 1.8;
            z = pos.f_82481_ + Math.sin(angle) * 1.8;
            double x2 = pos.f_82479_ + Math.cos(angle + Math.PI / (double)starPoints) * 0.8;
            double z2 = pos.f_82481_ + Math.sin(angle + Math.PI / (double)starPoints) * 0.8;

            int nextIdx;
            double nextAngle;
            double x3;
            double z3;
            for(nextIdx = 0; nextIdx <= pointsPerEdge; ++nextIdx) {
               nextAngle = (double)nextIdx / (double)pointsPerEdge;
               x3 = x + (x2 - x) * nextAngle;
               z3 = z + (z2 - z) * nextAngle;
               double rotAngle = (double)effect.ticks * speed;
               double rx = pos.f_82479_ + (x3 - pos.f_82479_) * Math.cos(rotAngle) - (z3 - pos.f_82481_) * Math.sin(rotAngle);
               double rz = pos.f_82481_ + (x3 - pos.f_82479_) * Math.sin(rotAngle) + (z3 - pos.f_82481_) * Math.cos(rotAngle);
               float hue = (float)((time + (double)i * 0.2 + (double)nextIdx * 0.05) % 1.0);
               int rgb = Color.HSBtoRGB(hue, 0.9F, 1.0F);
               float r = (float)(rgb >> 16 & 255) / 255.0F;
               float g = (float)(rgb >> 8 & 255) / 255.0F;
               float b = (float)(rgb & 255) / 255.0F;
               String dustCommand = String.format("particle dust %.2f %.2f %.2f 0.27 %f %f %f 0 0 0 0 1 force", r, g, b, rx, pos.f_82480_ + 0.1, rz);
               level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), dustCommand);
            }

            nextIdx = (i + 1) % starPoints;
            nextAngle = (double)nextIdx * Math.PI * 2.0 / (double)starPoints;
            x3 = pos.f_82479_ + Math.cos(nextAngle) * 1.8;
            z3 = pos.f_82481_ + Math.sin(nextAngle) * 1.8;

            for(int j = 0; j <= pointsPerEdge; ++j) {
               double t = (double)j / (double)pointsPerEdge;
               double x = x2 + (x3 - x2) * t;
               double z = z2 + (z3 - z2) * t;
               double rotAngle = (double)effect.ticks * speed;
               double rx = pos.f_82479_ + (x - pos.f_82479_) * Math.cos(rotAngle) - (z - pos.f_82481_) * Math.sin(rotAngle);
               double rz = pos.f_82481_ + (x - pos.f_82479_) * Math.sin(rotAngle) + (z - pos.f_82481_) * Math.cos(rotAngle);
               float hue = (float)((time + (double)i * 0.2 + (double)j * 0.05 + 0.5) % 1.0);
               int rgb = Color.HSBtoRGB(hue, 0.9F, 1.0F);
               float r = (float)(rgb >> 16 & 255) / 255.0F;
               float g = (float)(rgb >> 8 & 255) / 255.0F;
               float b = (float)(rgb & 255) / 255.0F;
               String dustCommand = String.format("particle dust %.2f %.2f %.2f 0.27 %f %f %f 0 0 0 0 1 force", r, g, b, rx, pos.f_82480_ + 0.15, rz);
               level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), dustCommand);
            }
         }

         int rgb;
         float g;
         float hue;
         float r;
         float b;
         String dustCommand;
         for(i = 0; i < 32; ++i) {
            angle = (double)effect.ticks * speed * 0.5 + (double)i * Math.PI * 2.0 / 32.0;
            radius = 2.2;
            x = pos.f_82479_ + Math.cos(angle) * radius;
            z = pos.f_82481_ + Math.sin(angle) * radius;
            hue = (float)((time + (double)i * 0.03) % 1.0);
            rgb = Color.HSBtoRGB(hue, 0.8F, 1.0F);
            r = (float)(rgb >> 16 & 255) / 255.0F;
            g = (float)(rgb >> 8 & 255) / 255.0F;
            b = (float)(rgb & 255) / 255.0F;
            dustCommand = String.format("particle dust %.2f %.2f %.2f 0.4 %f %f %f 0 0 0 0 1 force", r, g, b, x, pos.f_82480_ + 0.05, z);
            level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), dustCommand);
         }

         for(i = 0; i < 5; ++i) {
            angle = (double)effect.ticks * speed * 1.5 + (double)i * Math.PI * 2.0 / 5.0;
            radius = 1.0;
            x = pos.f_82479_ + Math.cos(angle) * radius;
            z = pos.f_82481_ + Math.sin(angle) * radius;
            hue = (float)((time + (double)i * 0.2 + 0.3) % 1.0);
            rgb = Color.HSBtoRGB(hue, 1.0F, 1.0F);
            r = (float)(rgb >> 16 & 255) / 255.0F;
            g = (float)(rgb >> 8 & 255) / 255.0F;
            b = (float)(rgb & 255) / 255.0F;
            dustCommand = String.format("particle dust %.2f %.2f %.2f 0.6 %f %f %f 0 0 0 0 1 force", r, g, b, x, pos.f_82480_ + 0.2, z);
            level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), dustCommand);
         }

      }
   }

   @SubscribeEvent
   public static void onPlayerAttack(LivingAttackEvent event) {
      if (!event.getEntity().m_9236_().f_46443_) {
         LivingEntity var2 = event.getEntity();
         if (var2 instanceof ServerPlayer) {
            ServerPlayer defender = (ServerPlayer)var2;
            Entity var3 = event.getSource().m_7639_();
            if (var3 instanceof ServerPlayer) {
               ServerPlayer attacker = (ServerPlayer)var3;
               ItemStack mainHand = defender.m_21205_();
               ItemStack offHand = defender.m_21206_();
               if (DogSlayerSword.isDogSlayerSword(mainHand) || DogSlayerSword.isDogSlayerSword(offHand)) {
                  if (!defenseEffects.containsKey(attacker.m_20148_())) {
                     event.setCanceled(true);
                     startDefenseEffect(attacker, defender);
                  }
               }
            }
         }
      }
   }

   private static void startDefenseEffect(ServerPlayer attacker, ServerPlayer defender) {
      DefenseEffect effect = new DefenseEffect(attacker.m_20148_(), defender.m_20148_());
      defenseEffects.put(attacker.m_20148_(), effect);
      attacker.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§6你在试图攻击 词汇大帝 你已被锁定！在32秒后你会被击杀"));
      startDefenseEffectLoop(attacker, defender, effect);
   }

   private static void startDefenseEffectLoop(final ServerPlayer attacker, final ServerPlayer defender, final DefenseEffect effect) {
      final ServerLevel level = (ServerLevel)attacker.m_9236_();
      effect.timer = new Timer();
      effect.timer.scheduleAtFixedRate(new TimerTask() {
         public void run() {
            if (effect.ticks < 640 && attacker.m_6084_() && defender.m_6084_()) {
               level.m_7654_().execute(() -> {
                  PlayerDefenseSkill.updateDefenseEffect(attacker, defender, effect);
               });
               ++effect.ticks;
            } else {
               level.m_7654_().execute(() -> {
                  if (attacker.m_6084_() && defender.m_6084_()) {
                     PlayerDefenseSkill.executePunish(attacker, defender, effect);
                  }

                  PlayerDefenseSkill.cleanup(effect);
               });
               effect.timer.cancel();
            }
         }
      }, 0L, 50L);
   }

   private static void updateDefenseEffect(ServerPlayer attacker, ServerPlayer defender, DefenseEffect effect) {
      if (attacker.m_6084_() && defender.m_6084_()) {
         ServerLevel level = (ServerLevel)attacker.m_9236_();
         Vec3 attackerPos = attacker.m_20182_();
         Vec3 defenderPos = defender.m_20182_();
         if (!effect.heartSpawned) {
            double heartY = attackerPos.f_82480_ + 2.5;
            String heartCommand = String.format("summon minecraft:snowball %f %f %f {Glowing:1b,Item:{Count:1b,id:\"minecraft:nether_star\"},NoGravity:true,Tags:[\"%s\"]}", attackerPos.f_82479_, heartY, attackerPos.f_82481_, effect.heartTag);
            level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), heartCommand);
            effect.heartSpawned = true;
         }

         String tpCommand = String.format("tp @e[type=minecraft:snowball,tag=%s,limit=1] %f %f %f", effect.heartTag, attackerPos.f_82479_, attackerPos.f_82480_ + 2.5, attackerPos.f_82481_);
         level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), tpCommand);
         float hue = (float)effect.ticks * 0.01F % 1.0F;
         int rgb = Color.HSBtoRGB(hue, 0.9F, 1.0F);
         float r = (float)(rgb >> 16 & 255) / 255.0F;
         float g = (float)(rgb >> 8 & 255) / 255.0F;
         float b = (float)(rgb & 255) / 255.0F;
         double dx = defenderPos.f_82479_ - attackerPos.f_82479_;
         double dy = defenderPos.f_82480_ + 1.0 - (attackerPos.f_82480_ + 2.0);
         double dz = defenderPos.f_82481_ - attackerPos.f_82481_;
         double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
         int steps = (int)(distance * 6.0);

         int i;
         double fx;
         double fy;
         double fz;
         double dx2;
         double dy2;
         for(i = 1; i <= steps; ++i) {
            fx = (double)i / (double)steps;
            fy = Math.sin((double)effect.ticks * 0.2 + (double)i * 0.5) * 0.3;
            fz = attackerPos.f_82479_ + dx * fx + fy * (dz / distance);
            dx2 = attackerPos.f_82481_ + dz * fx + fy * (dx / distance);
            dy2 = attackerPos.f_82480_ + 2.0 + dy * fx;
            String dustCommand = String.format("particle dust %.2f %.2f %.2f 1.0 %f %f %f 0 0 0 0 1 force", r, g, b, fz, dy2, dx2);
            level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), dustCommand);
         }

         for(i = 0; i < 3; ++i) {
            fx = attackerPos.f_82479_ + (Math.random() - 0.5) * 2.0;
            fy = attackerPos.f_82480_ + 1.5 + Math.random() * 2.0;
            fz = attackerPos.f_82481_ + (Math.random() - 0.5) * 2.0;
            dx2 = defenderPos.f_82479_ - fx;
            dy2 = defenderPos.f_82480_ + 1.0 - fy;
            double dz2 = defenderPos.f_82481_ - fz;

            for(int j = 1; j <= 8; ++j) {
               double t = (double)j / 8.0;
               double px = fx + dx2 * t;
               double py = fy + dy2 * t;
               double pz = fz + dz2 * t;
               String dustCommand = String.format("particle dust %.2f %.2f %.2f 0.8 %f %f %f 0 0 0 0 1 force", r, g, b, px, py, pz);
               level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), dustCommand);
            }
         }

         if (effect.ticks % 20 == 0) {
            i = (640 - effect.ticks) / 20;
         }

      } else {
         cleanup(effect);
      }
   }

   private static void executePunish(ServerPlayer attacker, ServerPlayer defender, DefenseEffect effect) {
      ServerLevel level = (ServerLevel)attacker.m_9236_();
      Vec3 pos = attacker.m_20182_();
      String clearHeart = String.format("kill @e[type=minecraft:snowball,tag=%s]", effect.heartTag);
      level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), clearHeart);

      for(int i = 0; i < 16; ++i) {
         double offsetX = (Math.random() - 0.5) * 10.0;
         double offsetZ = (Math.random() - 0.5) * 10.0;
         String lightningCommand = String.format("summon minecraft:lightning_bolt %f %f %f", pos.f_82479_ + offsetX, pos.f_82480_, pos.f_82481_ + offsetZ);
         level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), lightningCommand);
      }

      String killCommand = String.format("kill %s", attacker.m_20148_());
      level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), killCommand);
   }

   private static void cleanup(DefenseEffect effect) {
      defenseEffects.remove(effect.attackerId);
      if (effect.timer != null) {
         effect.timer.cancel();
      }

   }

   private static class PlayerEffect {
      final UUID playerId;
      int ticks = 0;
      float hue = 0.0F;

      PlayerEffect(UUID playerId) {
         this.playerId = playerId;
      }
   }

   private static class DefenseEffect {
      final UUID attackerId;
      final UUID defenderId;
      final long startTime;
      final String heartTag;
      int ticks = 0;
      Timer timer;
      boolean heartSpawned = false;

      DefenseEffect(UUID attackerId, UUID defenderId) {
         this.attackerId = attackerId;
         this.defenderId = defenderId;
         this.startTime = System.currentTimeMillis();
         String var10001 = UUID.randomUUID().toString();
         this.heartTag = "lexis_heart_" + var10001.substring(0, 8);
      }
   }
}
