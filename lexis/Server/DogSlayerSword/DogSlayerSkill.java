package lexis.Server.DogSlayerSword;

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
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DogSlayerSkill {
   private static final String PREFIX = "§c[§6Lexis-Server§c] §f";
   private static final Map lockEffects = new ConcurrentHashMap();
   private static final Map cooldownMap = new ConcurrentHashMap();
   private static final Map skillModeMap = new ConcurrentHashMap();
   private static final long COOLDOWN_TIME = 5000L;

   public static int getCurrentMode(UUID playerId) {
      return (Integer)skillModeMap.getOrDefault(playerId, 0);
   }

   @SubscribeEvent
   public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
      Player player = event.getEntity();
      if (player instanceof ServerPlayer serverPlayer) {
         ItemStack mainHand = player.m_21205_();
         boolean hasSword = DogSlayerSword.isDogSlayerSword(mainHand);
         boolean hasBow = LexisBowSkill.isLexisBow(mainHand);
         if (hasSword || hasBow) {
            UUID playerId = serverPlayer.m_20148_();
            int currentMode;
            if (serverPlayer.m_6144_()) {
               currentMode = (Integer)skillModeMap.getOrDefault(playerId, 0);
               int newMode = (currentMode + 1) % 4;
               skillModeMap.put(playerId, newMode);
               String modeName = newMode == 0 ? "§b标记击杀" : (newMode == 1 ? "§d快速移动" : (newMode == 2 ? "§c无尽绝杀" : "§a词汇仙弓"));
               serverPlayer.m_5661_(Component.m_237113_("已切换到 " + modeName + " §e模式"), true);
               if (newMode == 3) {
                  if (hasSword) {
                     replaceSwordWithBow(serverPlayer);
                  }
               } else if (hasBow) {
                  replaceBowWithSword(serverPlayer);
               }

               event.setCanceled(true);
            } else {
               currentMode = (Integer)skillModeMap.getOrDefault(playerId, 0);
               if (currentMode == 0) {
                  shootMarker(serverPlayer);
               } else if (currentMode == 1) {
                  FastMoveSkill.startFastMove(serverPlayer);
               } else if (currentMode == 2) {
                  InfiniteSlaughterSkill.execute(serverPlayer);
               } else if (currentMode == 3) {
               }

            }
         }
      }
   }

   private static void replaceSwordWithBow(ServerPlayer player) {
      for(int i = 0; i < player.m_150109_().m_6643_(); ++i) {
         ItemStack stack = player.m_150109_().m_8020_(i);
         if (DogSlayerSword.isDogSlayerSword(stack)) {
            ItemStack bow = LexisBowSkill.createLexisBow();
            player.m_150109_().m_6836_(i, bow);
            break;
         }
      }

   }

   private static void replaceBowWithSword(ServerPlayer player) {
      for(int i = 0; i < player.m_150109_().m_6643_(); ++i) {
         ItemStack stack = player.m_150109_().m_8020_(i);
         if (LexisBowSkill.isLexisBow(stack)) {
            ItemStack sword = DogSlayerSword.createLexisSword();
            player.m_150109_().m_6836_(i, sword);
            break;
         }
      }

   }

   private static void shootMarker(final ServerPlayer player) {
      ServerLevel level = (ServerLevel)player.m_9236_();
      SmallFireball fireball = new SmallFireball(level, player, player.m_20154_().f_82479_ * 2.0, player.m_20154_().f_82480_ * 2.0, player.m_20154_().f_82481_ * 2.0) {
         protected void m_5790_(EntityHitResult result) {
            if (!this.f_19853_.f_46443_) {
               final Entity target = result.m_82443_();
               if (target instanceof LivingEntity) {
                  final Vec3 hitPos = target.m_20182_();
                  (new Timer()).schedule(new TimerTask() {
                     public void run() {
                        f_19853_.m_7654_().execute(() -> {
                           DogSlayerSkill.executeKill(target, player, hitPos);
                        });
                     }
                  }, 500L);
                  this.m_146870_();
               }
            }

         }
      };
      fireball.m_6034_(player.m_20185_(), player.m_20186_() + 1.5, player.m_20189_());
      fireball.m_20256_(player.m_20154_().m_82490_(2.0));
      fireball.m_5602_(player);
      fireball.m_20049_("lexis_marker");
      level.m_7967_(fireball);
      spawnFlameTrail(level, fireball);
   }

   private static void spawnFlameTrail(final ServerLevel level, final Entity entity) {
      (new Timer()).scheduleAtFixedRate(new TimerTask() {
         int count = 0;

         public void run() {
            if (this.count < 40 && entity.m_6084_()) {
               level.m_7654_().execute(() -> {
                  Vec3 pos = entity.m_20182_();
                  String command = String.format("particle minecraft:flame %f %f %f 0.1 0.1 0.1 0.05 8 force", pos.f_82479_, pos.f_82480_ + 0.3, pos.f_82481_);
                  level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), command);
               });
               ++this.count;
            } else {
               this.cancel();
            }
         }
      }, 0L, 50L);
   }

   private static void executeKill(Entity target, ServerPlayer player, Vec3 hitPos) {
      ServerLevel level = (ServerLevel)target.m_9236_();

      int radius;
      double theta;
      for(radius = 0; radius < 16; ++radius) {
         double offsetX = (Math.random() - 0.5) * 8.0;
         theta = (Math.random() - 0.5) * 8.0;
         String lightningCommand = String.format("summon minecraft:lightning_bolt %f %f %f", hitPos.f_82479_ + offsetX, hitPos.f_82480_, hitPos.f_82481_ + theta);
         level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), lightningCommand);
      }

      for(radius = 1; radius <= 10; ++radius) {
         int steps = radius * 12;

         for(int i = 0; i < steps; ++i) {
            theta = Math.random() * Math.PI * 2.0;
            double phi = Math.acos(2.0 * Math.random() - 1.0);
            double x = hitPos.f_82479_ + Math.sin(phi) * Math.cos(theta) * (double)radius;
            double y = hitPos.f_82480_ + 0.5 + Math.sin(phi) * Math.sin(theta) * (double)radius;
            double z = hitPos.f_82481_ + Math.cos(phi) * (double)radius;
            String flameCommand = String.format("particle minecraft:flame %f %f %f 0 0 0 0.02 2 force", x, y, z);
            level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), flameCommand);
         }
      }

      String lavaCommand = String.format("particle minecraft:lava %f %f %f 2.0 2.0 2.0 0 256 force", hitPos.f_82479_, hitPos.f_82480_ + 0.5, hitPos.f_82481_);
      level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), lavaCommand);
      String flashCommand = String.format("particle minecraft:flash %f %f %f 1.0 1.0 1.0 0 10 force", hitPos.f_82479_, hitPos.f_82480_ + 0.5, hitPos.f_82481_);
      level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), flashCommand);
      if (target instanceof ServerPlayer targetPlayer) {
         String killCommand = String.format("kill %s", targetPlayer.m_20148_());
         level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), killCommand);
         player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c§l对象了 玩家 " + targetPlayer.m_7755_().getString() + " 已被击杀！"));
      } else {
         target.m_142687_(RemovalReason.KILLED);
         player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c§l对象了 " + target.m_7755_().getString() + " 已被清除"));
      }

      startLockEffect(level, hitPos, player, target.m_7755_().getString());
   }

   private static void startLockEffect(final ServerLevel level, final Vec3 pos, final ServerPlayer player, String entityName) {
      final LockEffect effect = new LockEffect(pos, player, entityName);
      final UUID effectId = UUID.randomUUID();
      lockEffects.put(effectId, effect);
      String heartCommand = String.format("summon minecraft:snowball %f %f %f {Glowing:1b,Item:{Count:1b,id:\"minecraft:nether_star\"},NoGravity:true,Tags:[\"lexis_lock_heart\"]}", pos.f_82479_, pos.f_82480_ + 1.5, pos.f_82481_);
      level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), heartCommand);
      effect.timer = new Timer();
      effect.timer.scheduleAtFixedRate(new TimerTask() {
         public void run() {
            if (effect.ticks >= 160) {
               level.m_7654_().execute(() -> {
                  String clearHeart = String.format("execute positioned %f %f %f run kill @e[type=minecraft:snowball,tag=lexis_lock_heart,distance=..2]", pos.f_82479_, pos.f_82480_ + 1.5, pos.f_82481_);
                  level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), clearHeart);
                  DogSlayerSkill.lockEffects.remove(effectId);
               });
               effect.timer.cancel();
            } else {
               level.m_7654_().execute(() -> {
                  Vec3 playerPos = player.m_20182_();
                  float r = (float)Math.random();
                  float g = (float)Math.random();
                  float b = (float)Math.random();
                  double dx = pos.f_82479_ - playerPos.f_82479_;
                  double dy = pos.f_82480_ + 1.0 - (playerPos.f_82480_ + 1.0);
                  double dz = pos.f_82481_ - playerPos.f_82481_;
                  double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                  int steps = (int)(distance * 6.0);

                  int i;
                  double fx;
                  double a;
                  double radius;
                  double ex;
                  double ez;
                  double yOffset;
                  for(i = 1; i <= steps; ++i) {
                     fx = (double)i / (double)steps;
                     a = (double)effect.ticks * 0.2 + (double)i * 0.3;
                     radius = Math.cos(a) * 0.4;
                     ex = Math.sin(a) * 0.2;
                     ez = Math.sin(a * 1.5) * 0.4;
                     yOffset = playerPos.f_82479_ + dx * fx + radius;
                     double py = playerPos.f_82480_ + 1.0 + dy * fx + ex;
                     double pz = playerPos.f_82481_ + dz * fx + ez;
                     String dustCommand = String.format("particle dust %.2f %.2f %.2f 0.49 %f %f %f 0 0 0 0 1 force", r, g, b, yOffset, py, pz);
                     level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), dustCommand);
                  }

                  for(i = 0; i < 4; ++i) {
                     fx = pos.f_82479_ + (Math.random() - 0.5) * 2.0;
                     a = pos.f_82480_ + 0.5 + Math.random() * 2.0;
                     radius = pos.f_82481_ + (Math.random() - 0.5) * 2.0;
                     ex = playerPos.f_82479_ - fx;
                     ez = playerPos.f_82480_ + 1.0 - a;
                     yOffset = playerPos.f_82481_ - radius;

                     for(int j = 1; j <= 8; ++j) {
                        double t = (double)j / 8.0;
                        double px = fx + ex * t;
                        double pyx = a + ez * t;
                        double pzx = radius + yOffset * t;
                        String dustCommandx = String.format("particle dust %.2f %.2f %.2f 0.8 %f %f %f 0 0 0 0 1 force", r, g, b, px, pyx, pzx);
                        level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), dustCommandx);
                     }
                  }

                  double angle = (double)effect.ticks * 0.2;

                  for(int ix = 0; ix < 12; ++ix) {
                     a = angle + (double)ix * Math.PI / 6.0;
                     radius = 2.0;
                     ex = pos.f_82479_ + Math.cos(a) * radius;
                     ez = pos.f_82481_ + Math.sin(a) * radius;
                     yOffset = Math.sin((double)effect.ticks * 0.3 + (double)ix) * 0.3;
                     String flameCommand = String.format("particle minecraft:flame %f %f %f 0 0 0 0.03 2 force", ex, pos.f_82480_ + 0.5 + yOffset, ez);
                     level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), flameCommand);
                  }

               });
               ++effect.ticks;
            }
         }
      }, 0L, 50L);
   }

   private static class LockEffect {
      final Vec3 pos;
      final UUID ownerId;
      final String entityName;
      int ticks = 0;
      Timer timer;

      LockEffect(Vec3 pos, ServerPlayer owner, String entityName) {
         this.pos = pos;
         this.ownerId = owner.m_20148_();
         this.entityName = entityName;
      }
   }
}
