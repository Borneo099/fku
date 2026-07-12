package lexis.Server.DogSlayerSword;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class LexisBowSkill {
   private static final String BOW_TAG = "lexisbow";
   private static final String ARROW_TAG = "lexisarrow";
   private static final Map nameHueMap = new ConcurrentHashMap();
   private static final Map lastHandState = new ConcurrentHashMap();
   private static final Map activeArrows = new ConcurrentHashMap();
   private static final Map bowModeMap = new ConcurrentHashMap();
   private static final double MAX_DISTANCE = 135.0;
   private static final double EXPLOSION_RADIUS = 7.0;
   private static final Map arrowMarkerMap = new ConcurrentHashMap();

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase != Phase.END) {
         Player var2 = event.player;
         if (var2 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)var2;
            UUID playerId = player.m_20148_();
            ItemStack mainHand = player.m_21205_();
            boolean hasBow = isLexisBow(mainHand);
            Boolean lastHasBow = (Boolean)lastHandState.get(playerId);
            if (lastHasBow == null) {
               lastHasBow = false;
            }

            if (hasBow && !lastHasBow) {
               bowModeMap.put(playerId, true);
            }

            if (!hasBow && lastHasBow) {
               bowModeMap.remove(playerId);
            }

            if (hasBow) {
               updateRainbowName(player, mainHand);
               spawnStarParticle(player);
            }

            lastHandState.put(playerId, hasBow);
         }
      }
   }

   private static void spawnStarParticle(ServerPlayer player) {
      if (player.f_19797_ % 2 == 0) {
         ServerLevel level = (ServerLevel)player.m_9236_();
         double x = player.m_20185_();
         double y = player.m_20186_();
         double z = player.m_20189_();
         double time = (double)player.f_19797_ * 0.05;
         double speed = 0.02;
         int starPoints = 5;
         int pointsPerEdge = 10;

         int i;
         double angle;
         double radius;
         double px;
         double pz;
         for(i = 0; i < starPoints; ++i) {
            angle = (double)i * Math.PI * 2.0 / (double)starPoints;
            radius = (double)(i + 1) * Math.PI * 2.0 / (double)starPoints;
            px = x + Math.cos(angle) * 1.8;
            pz = z + Math.sin(angle) * 1.8;
            double x2 = x + Math.cos(angle + Math.PI / (double)starPoints) * 0.8;
            double z2 = z + Math.sin(angle + Math.PI / (double)starPoints) * 0.8;

            int nextIdx;
            double nextAngle;
            double x3;
            double z3;
            for(nextIdx = 0; nextIdx <= pointsPerEdge; ++nextIdx) {
               nextAngle = (double)nextIdx / (double)pointsPerEdge;
               x3 = px + (x2 - px) * nextAngle;
               z3 = pz + (z2 - pz) * nextAngle;
               double rotAngle = (double)player.f_19797_ * speed;
               double rx = x + (x3 - x) * Math.cos(rotAngle) - (z3 - z) * Math.sin(rotAngle);
               double rz = z + (x3 - x) * Math.sin(rotAngle) + (z3 - z) * Math.cos(rotAngle);
               float hue = (float)((time + (double)i * 0.2 + (double)nextIdx * 0.05) % 1.0);
               int rgb = Color.HSBtoRGB(hue, 0.9F, 1.0F);
               float r = (float)(rgb >> 16 & 255) / 255.0F;
               float g = (float)(rgb >> 8 & 255) / 255.0F;
               float b = (float)(rgb & 255) / 255.0F;
               String cmd = String.format("particle dust %.2f %.2f %.2f 0.27 %f %f %f 0 0 0 0 1 force", r, g, b, rx, y + 0.1, rz);
               level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), cmd);
            }

            nextIdx = (i + 1) % starPoints;
            nextAngle = (double)nextIdx * Math.PI * 2.0 / (double)starPoints;
            x3 = x + Math.cos(nextAngle) * 1.8;
            z3 = z + Math.sin(nextAngle) * 1.8;

            for(int j = 0; j <= pointsPerEdge; ++j) {
               double t = (double)j / (double)pointsPerEdge;
               double px = x2 + (x3 - x2) * t;
               double pz = z2 + (z3 - z2) * t;
               double rotAngle = (double)player.f_19797_ * speed;
               double rx = x + (px - x) * Math.cos(rotAngle) - (pz - z) * Math.sin(rotAngle);
               double rz = z + (px - x) * Math.sin(rotAngle) + (pz - z) * Math.cos(rotAngle);
               float hue = (float)((time + (double)i * 0.2 + (double)j * 0.05 + 0.5) % 1.0);
               int rgb = Color.HSBtoRGB(hue, 0.9F, 1.0F);
               float r = (float)(rgb >> 16 & 255) / 255.0F;
               float g = (float)(rgb >> 8 & 255) / 255.0F;
               float b = (float)(rgb & 255) / 255.0F;
               String cmd = String.format("particle dust %.2f %.2f %.2f 0.27 %f %f %f 0 0 0 0 1 force", r, g, b, rx, y + 0.15, rz);
               level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), cmd);
            }
         }

         int rgb;
         float g;
         float hue;
         float r;
         float b;
         String cmd;
         for(i = 0; i < 32; ++i) {
            angle = (double)player.f_19797_ * speed * 0.5 + (double)i * Math.PI * 2.0 / 32.0;
            radius = 2.2;
            px = x + Math.cos(angle) * radius;
            pz = z + Math.sin(angle) * radius;
            hue = (float)((time + (double)i * 0.03) % 1.0);
            rgb = Color.HSBtoRGB(hue, 0.8F, 1.0F);
            r = (float)(rgb >> 16 & 255) / 255.0F;
            g = (float)(rgb >> 8 & 255) / 255.0F;
            b = (float)(rgb & 255) / 255.0F;
            cmd = String.format("particle dust %.2f %.2f %.2f 0.4 %f %f %f 0 0 0 0 1 force", r, g, b, px, y + 0.05, pz);
            level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), cmd);
         }

         for(i = 0; i < 5; ++i) {
            angle = (double)player.f_19797_ * speed * 1.5 + (double)i * Math.PI * 2.0 / 5.0;
            radius = 1.0;
            px = x + Math.cos(angle) * radius;
            pz = z + Math.sin(angle) * radius;
            hue = (float)((time + (double)i * 0.2 + 0.3) % 1.0);
            rgb = Color.HSBtoRGB(hue, 1.0F, 1.0F);
            r = (float)(rgb >> 16 & 255) / 255.0F;
            g = (float)(rgb >> 8 & 255) / 255.0F;
            b = (float)(rgb & 255) / 255.0F;
            cmd = String.format("particle dust %.2f %.2f %.2f 0.6 %f %f %f 0 0 0 0 1 force", r, g, b, px, y + 0.2, pz);
            level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), cmd);
         }

      }
   }

   @SubscribeEvent
   public static void onEntityJoin(EntityJoinLevelEvent event) {
      if (!event.getLevel().f_46443_) {
         Entity var2 = event.getEntity();
         if (var2 instanceof AbstractArrow) {
            AbstractArrow arrow = (AbstractArrow)var2;
            Entity var3 = arrow.m_19749_();
            if (var3 instanceof ServerPlayer) {
               ServerPlayer player = (ServerPlayer)var3;
               ItemStack var8 = player.m_21205_();
               if (isLexisBow(var8)) {
                  CompoundTag arrowTag = arrow.getPersistentData();
                  arrowTag.m_128344_("lexisarrow", (byte)1);
                  arrow.m_20242_(true);
                  Vec3 velocity = arrow.m_20184_();
                  arrow.m_20256_(velocity.m_82490_(32.0));
                  ArrowData data = new ArrowData(player.m_20148_());
                  activeArrows.put(arrow.m_19879_(), data);
                  startArrowEffect(arrow);
               }
            }
         }
      }
   }

   private static void startArrowEffect(final AbstractArrow arrow) {
      final ServerLevel level = (ServerLevel)arrow.m_9236_();
      final ArrowData data = (ArrowData)activeArrows.get(arrow.m_19879_());
      if (data != null) {
         final CommandSourceStack silentSource = level.m_7654_().m_129893_().m_81324_();
         final Vec3 startPos = arrow.m_20182_();
         String summonMarker = String.format("summon minecraft:marker %f %f %f {Tags:[\"lexismarker_%d\"],Invulnerable:1b,Persistent:1b}", startPos.f_82479_, startPos.f_82480_, startPos.f_82481_, arrow.m_19879_());
         level.m_7654_().m_129892_().m_230957_(silentSource, summonMarker);
         data.effectTimer = new Timer();
         data.effectTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
               if (!arrow.m_6084_()) {
                  String killMarker = String.format("kill @e[type=minecraft:marker,tag=lexismarker_%d]", arrow.m_19879_());
                  level.m_7654_().m_129892_().m_230957_(silentSource, killMarker);
                  LexisBowSkill.arrowMarkerMap.remove(arrow.m_19879_());
                  data.effectTimer.cancel();
                  LexisBowSkill.activeArrows.remove(arrow.m_19879_());
               } else {
                  level.m_7654_().execute(() -> {
                     Vec3 arrowPos = arrow.m_20182_();
                     double distFromStart = Math.sqrt(Math.pow(arrowPos.f_82479_ - startPos.f_82479_, 2.0) + Math.pow(arrowPos.f_82480_ - startPos.f_82480_, 2.0) + Math.pow(arrowPos.f_82481_ - startPos.f_82481_, 2.0));
                     if (distFromStart > 135.0) {
                        LexisBowSkill.createSphericalExplosion(level, arrowPos, data.shooterId, (HitResult)null);
                        arrow.m_146870_();
                        String killMarker = String.format("kill @e[type=minecraft:marker,tag=lexismarker_%d]", arrow.m_19879_());
                        level.m_7654_().m_129892_().m_230957_(silentSource, killMarker);
                        data.effectTimer.cancel();
                        LexisBowSkill.activeArrows.remove(arrow.m_19879_());
                     } else {
                        double dx = arrowPos.f_82479_ - startPos.f_82479_;
                        double dy = arrowPos.f_82480_ - startPos.f_82480_;
                        double dz = arrowPos.f_82481_ - startPos.f_82481_;
                        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        int steps = (int)(distance * 8.0);
                        float hue = (float)((double)System.currentTimeMillis() * 0.001 % 1.0);
                        int rgb = Color.HSBtoRGB(hue, 0.9F, 1.0F);
                        float r = (float)(rgb >> 16 & 255) / 255.0F;
                        float g = (float)(rgb >> 8 & 255) / 255.0F;
                        float b = (float)(rgb & 255) / 255.0F;

                        for(int step = 1; step <= steps; ++step) {
                           double t = (double)step / (double)steps;
                           double wave = Math.sin((double)System.currentTimeMillis() * 0.01 + (double)step * 0.5) * 0.3;
                           double px = startPos.f_82479_ + dx * t + wave * (dz / distance);
                           double pz = startPos.f_82481_ + dz * t + wave * (dx / distance);
                           double py = startPos.f_82480_ + dy * t;
                           String dustCmd = String.format("particle dust %.2f %.2f %.2f 1.0 %f %f %f 0 0 0 0 1 force", r, g, b, px, py, pz);
                           level.m_7654_().m_129892_().m_230957_(silentSource, dustCmd);
                           if (step % 3 == 0) {
                              String flashCmd = String.format("particle minecraft:flash %f %f %f 0.1 0.1 0.1 0 1 force", px, py, pz);
                              level.m_7654_().m_129892_().m_230957_(silentSource, flashCmd);
                           }
                        }

                        String arrowFlashCmd = String.format("particle minecraft:flash %f %f %f 0.3 0.3 0.3 0 2 force", arrowPos.f_82479_, arrowPos.f_82480_, arrowPos.f_82481_);
                        level.m_7654_().m_129892_().m_230957_(silentSource, arrowFlashCmd);
                        String markerFlashCmd = String.format("particle minecraft:flash %f %f %f 0.2 0.2 0.2 0 1 force", startPos.f_82479_, startPos.f_82480_ + 0.5, startPos.f_82481_);
                        level.m_7654_().m_129892_().m_230957_(silentSource, markerFlashCmd);
                     }
                  });
               }
            }
         }, 0L, 40L);
      }
   }

   @SubscribeEvent
   public static void onProjectileImpact(ProjectileImpactEvent event) {
      if (!event.getProjectile().m_9236_().f_46443_) {
         Projectile var2 = event.getProjectile();
         if (var2 instanceof AbstractArrow) {
            AbstractArrow arrow = (AbstractArrow)var2;
            CompoundTag var9 = arrow.getPersistentData();
            if (var9.m_128441_("lexisarrow")) {
               Entity var4 = arrow.m_19749_();
               if (var4 instanceof ServerPlayer) {
                  ServerPlayer shooter = (ServerPlayer)var4;
                  HitResult hitResult = event.getRayTraceResult();
                  Vec3 hitPos = hitResult.m_82450_();
                  ServerLevel level = (ServerLevel)arrow.m_9236_();
                  event.setResult(Result.DENY);
                  arrow.m_146870_();
                  createSphericalExplosion(level, hitPos, shooter.m_20148_(), hitResult);
                  if (hitResult instanceof EntityHitResult) {
                     EntityHitResult entityHit = (EntityHitResult)hitResult;
                     Entity target = entityHit.m_82443_();
                     if (target != shooter) {
                        handleEntityHit(target, shooter, hitPos);
                     }
                  }

                  ArrowData data = (ArrowData)activeArrows.remove(arrow.m_19879_());
                  if (data != null && data.effectTimer != null) {
                     data.effectTimer.cancel();
                  }

               }
            }
         }
      }
   }

   private static void createSphericalExplosion(ServerLevel level, Vec3 center, UUID shooterId, HitResult hitResult) {
      CommandSourceStack silentSource = level.m_7654_().m_129893_().m_81324_();
      int[] sizes = new int[]{2, 4, 6, 8, 10};
      int[] var6 = sizes;
      int var7 = sizes.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         int size = var6[var8];
         double radius = (double)size * 0.5;
         int particles = size * 20;

         for(int i = 0; i < particles; ++i) {
            double theta = Math.random() * Math.PI * 2.0;
            double phi = Math.acos(2.0 * Math.random() - 1.0);
            double x = center.f_82479_ + Math.sin(phi) * Math.cos(theta) * radius;
            double y = center.f_82480_ + Math.sin(phi) * Math.sin(theta) * radius;
            double z = center.f_82481_ + Math.cos(phi) * radius;
            String cmd = String.format("particle minecraft:end_rod %f %f %f 0 0 0 0.02 1 force", x, y, z);
            level.m_7654_().m_129892_().m_230957_(silentSource, cmd);
         }
      }

      for(int i = 0; i < 200; ++i) {
         double theta = Math.random() * Math.PI * 2.0;
         double phi = Math.acos(2.0 * Math.random() - 1.0);
         double radius = 12.0;
         double x = center.f_82479_ + Math.sin(phi) * Math.cos(theta) * radius;
         double y = center.f_82480_ + Math.sin(phi) * Math.sin(theta) * radius;
         double z = center.f_82481_ + Math.cos(phi) * radius;
         String cmd = String.format("particle minecraft:end_rod %f %f %f 0 0 0 0.05 1 force", x, y, z);
         level.m_7654_().m_129892_().m_230957_(silentSource, cmd);
      }

      if (hitResult == null || hitResult instanceof BlockHitResult) {
         List entitiesToRemove = new ArrayList();
         level.m_142646_().m_142273_().forEach((entityx) -> {
            if (entityx != level.m_8791_(shooterId)) {
               if (entityx instanceof LivingEntity) {
                  double dist = entityx.m_20238_(center);
                  if (dist < 49.0) {
                     entitiesToRemove.add(entityx);
                  }

               }
            }
         });
         Iterator var28 = entitiesToRemove.iterator();

         while(var28.hasNext()) {
            Entity entity = (Entity)var28.next();
            if (entity instanceof ServerPlayer) {
               ServerPlayer targetPlayer = (ServerPlayer)entity;
               targetPlayer.m_21153_(-1.0F);
               targetPlayer.m_6469_(targetPlayer.m_269291_().m_269425_(), Float.MAX_VALUE);
               targetPlayer.m_6074_();
            } else {
               entity.m_142687_(RemovalReason.KILLED);
            }
         }
      }

   }

   private static void handleEntityHit(Entity target, ServerPlayer shooter, Vec3 hitPos) {
      if (target instanceof ServerPlayer targetPlayer) {
         targetPlayer.m_21153_(-1.0F);
         targetPlayer.m_6469_(targetPlayer.m_269291_().m_269075_(shooter), Float.MAX_VALUE);
         targetPlayer.m_6074_();
         float hue1 = (float)((double)System.currentTimeMillis() * 0.001 % 1.0);
         int rgb1 = Color.HSBtoRGB(hue1, 1.0F, 1.0F);
         String color1 = String.format("#%06X", rgb1 & 16777215);
         float hue2 = (float)(((double)System.currentTimeMillis() * 0.001 + 0.2) % 1.0);
         int rgb2 = Color.HSBtoRGB(hue2, 1.0F, 1.0F);
         String color2 = String.format("#%06X", rgb2 & 16777215);
         float hue3 = (float)(((double)System.currentTimeMillis() * 0.001 + 0.4) % 1.0);
         int rgb3 = Color.HSBtoRGB(hue3, 1.0F, 1.0F);
         String color3 = String.format("#%06X", rgb3 & 16777215);
         float hue4 = (float)(((double)System.currentTimeMillis() * 0.001 + 0.6) % 1.0);
         int rgb4 = Color.HSBtoRGB(hue4, 1.0F, 1.0F);
         String color4 = String.format("#%06X", rgb4 & 16777215);
         float hue5 = (float)(((double)System.currentTimeMillis() * 0.001 + 0.8) % 1.0);
         int rgb5 = Color.HSBtoRGB(hue5, 1.0F, 1.0F);
         String color5 = String.format("#%06X", rgb5 & 16777215);
         String message = String.format("%s 被 %s 使用词汇仙弓§f[§6词§k-§4汇§k-§r寰§k-§d圣§k-§b箭§f] 射杀了", targetPlayer.m_7755_().getString(), shooter.m_7755_().getString(), color1, color2, color3, color4, color5);
         shooter.m_20194_().m_6846_().m_240416_(Component.m_237113_(message), false);
      } else if (target instanceof LivingEntity living) {
         living.m_21153_(0.0F);
         living.m_6469_(living.m_269291_().m_269425_(), Float.MAX_VALUE);
         living.m_6074_();
         living.m_142687_(RemovalReason.KILLED);
      }

   }

   private static void spawnArrowParticle(ServerLevel level, Vec3 pos) {
      float hue = (float)Math.random();
      int rgb = Color.HSBtoRGB(hue, 1.0F, 1.0F);
      float r = (float)(rgb >> 16 & 255) / 255.0F;
      float g = (float)(rgb >> 8 & 255) / 255.0F;
      float b = (float)(rgb & 255) / 255.0F;
      String flashCmd = String.format("particle minecraft:flash %f %f %f 0 0 0 0 1 force", pos.f_82479_, pos.f_82480_, pos.f_82481_);
      level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), flashCmd);
      String dustCmd = String.format("particle dust %.2f %.2f %.2f 1 %f %f %f 0 0 0 0 1 force", r, g, b, pos.f_82479_, pos.f_82480_, pos.f_82481_);
      level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), dustCmd);
   }

   private static void updateRainbowName(ServerPlayer player, ItemStack bow) {
      UUID playerId = player.m_20148_();
      float hue = (Float)nameHueMap.getOrDefault(playerId, 0.0F);
      hue += 0.02F;
      if (hue > 1.0F) {
         hue = 0.0F;
      }

      nameHueMap.put(playerId, hue);
      int rgb = Color.HSBtoRGB(hue, 1.0F, 1.0F);
      String hexColor = String.format("#%06X", rgb & 16777215);
      Component rainbowName = Component.m_237113_("词汇仙弓").m_6270_(Style.f_131099_.m_131148_(TextColor.m_131268_(hexColor)).m_131136_(true));
      bow.m_41714_(rainbowName);
   }

   public static ItemStack createLexisBow() {
      ItemStack bow = new ItemStack(Items.f_42411_);
      CompoundTag tag = bow.m_41784_();
      tag.m_128344_("lexisbow", (byte)1);
      bow.m_41663_(Enchantments.f_44986_, 10);
      bow.m_41663_(Enchantments.f_44988_, 10);
      tag.m_128405_("HideFlags", 1);
      bow.m_41751_(tag);
      bow.m_41714_(Component.m_237113_("词汇仙弓"));
      return bow;
   }

   public static boolean isLexisBow(ItemStack stack) {
      if (!stack.m_41619_() && stack.m_41720_() == Items.f_42411_) {
         CompoundTag tag = stack.m_41783_();
         return tag != null && tag.m_128441_("lexisbow") && tag.m_128445_("lexisbow") == 1;
      } else {
         return false;
      }
   }

   public static boolean isInBowMode(UUID playerId) {
      return bowModeMap.containsKey(playerId);
   }

   private static class ArrowData {
      final UUID shooterId;
      int age = 0;
      Timer effectTimer;

      ArrowData(UUID shooterId) {
         this.shooterId = shooterId;
      }
   }
}
