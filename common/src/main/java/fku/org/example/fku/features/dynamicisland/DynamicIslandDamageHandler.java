package fku.org.example.fku.features.dynamicisland;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(
   modid = "fku",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public class DynamicIslandDamageHandler {
   private static final long HOLD_MS = 1500L;
   private static final int UNMEASURED_TICKS = 14;
   private static LivingEntity displayEntity = null;
   private static String displayName = "";
   private static float measuredDamage = 0.0F;
   private static boolean measured = false;
   private static boolean active = false;
   private static long displayUntil = 0L;
   private static int measureTicks = 0;
   private static float preHealth = 0.0F;
   private static int pendingAttackId = -1;

   public static void notifyAttack(int entityId) {
      if (entityId < 0) {
         pendingAttackId = -1;
      } else {
         DynamicIslandConfig cfg = DynamicIslandConfig.getInstance();
         if (cfg.enabled && cfg.showDamage) {
            pendingAttackId = entityId;
         } else {
            pendingAttackId = -1;
         }
      }
   }

   public static void notifyAttack(LivingEntity target) {
      if (target != null) {
         notifyAttack(target.getId());
      }
   }

   @SubscribeEvent
   public static void onAttack(AttackEntityEvent event) {
      Player var2 = event.getEntity();
      if (var2 instanceof LocalPlayer player) {
         if (player == Minecraft.getInstance().player) {
            Entity var3 = event.getTarget();
            if (var3 instanceof LivingEntity) {
               LivingEntity target = (LivingEntity)var3;
               notifyAttack(target);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null) {
            if (pendingAttackId >= 0) {
               Entity e = mc.level.getEntity(pendingAttackId);
               if (e instanceof LivingEntity) {
                  LivingEntity le = (LivingEntity)e;
                  if (!le.isRemoved()) {
                     displayEntity = le;
                     displayName = le.getName().getString();
                     preHealth = le.getHealth();
                     measured = false;
                     measuredDamage = 0.0F;
                     measureTicks = 0;
                     active = true;
                     displayUntil = System.currentTimeMillis() + 1500L;
                     NotificationCenter.interruptForDamage();
                  }
               }

               pendingAttackId = -1;
            }

            if (active) {
               if (displayEntity != null && !displayEntity.isRemoved()) {
                  long now = System.currentTimeMillis();
                  ++measureTicks;
                  float nowHealth = displayEntity.getHealth();
                  float dmg = preHealth - nowHealth;
                  if (dmg > 0.01F && !measured) {
                     measured = true;
                     measuredDamage = dmg;
                     DynamicIslandConfig cfg = DynamicIslandConfig.getInstance();
                     if (cfg.damageShakeEnabled) {
                        NotificationRenderer.pulseShake(Math.min(9.0F, 3.5F + dmg * 0.4F));
                     }

                     displayUntil = Math.max(displayUntil, now + 1500L);
                  }

                  if (measured) {
                     if (now > displayUntil) {
                        active = false;
                     }
                  } else if (measureTicks > 14) {
                     active = false;
                  }

               } else {
                  active = false;
               }
            }
         }
      }
   }

   public static boolean isActive() {
      return active;
   }

   public static String getDisplayName() {
      return displayName;
   }

   public static float getMeasuredDamage() {
      return measuredDamage;
   }

   public static IslandNotification toNotification() {
      if (active && displayEntity != null && !displayEntity.isRemoved()) {
         IslandNotification n = new IslandNotification("damage_dealt", displayName, NotificationType.COMBAT, "造成 " + trim1(measuredDamage) + " 伤害");
         n.targetEntity = displayEntity;
         n.damageAmount = measuredDamage;
         n.damageMeasured = measured;
         return n;
      } else {
         return null;
      }
   }

   private static String trim1(float v) {
      return v == (float)((long)v) ? String.valueOf((long)v) : String.format("%.1f", v);
   }
}
