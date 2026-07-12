package lexis.Hack.Hacks.Combat;

import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.ConfigUtils;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Hackutil.tpaura.TpAuraConfig;
import lexis.Hack.Utils.FriendsManager;
import lexis.Hack.Utils.RotationUtils;
import lexis.Hack.Utils.ESP.EntitySelectScreen;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class TpAurasHack extends Hack {
   private double range = 4.25;
   private double attackSpeed = 8.0;
   private Priority priority;
   private boolean pauseOnContainers;
   private boolean ignoreNamed;
   private boolean ignorePassive;
   private final Set whitelistEntities;
   private long lastAttackTime;
   private Random random;
   private Entity currentTarget;
   private HackConfig config;
   private TpAuraConfig tpAuraConfig;
   private static final String CONFIG_KEY = "传送光环";

   public TpAurasHack() {
      super("传送光环", "传送到实体身边攻击", Hack.Category.COMBAT, true);
      this.priority = TpAurasHack.Priority.ANGLE;
      this.pauseOnContainers = true;
      this.ignoreNamed = false;
      this.ignorePassive = true;
      this.whitelistEntities = ConcurrentHashMap.newKeySet();
      this.lastAttackTime = 0L;
      this.random = new Random();
      this.currentTarget = null;
      this.addSetting(new Hack.Setting("范围", "攻击范围", 4.25, 1.0, 6.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("攻击速度", "攻击速度谁传送速度？", 8.0, 1.0, 128.0, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("优先级", "目标选择优先级", "角度", new String[]{"距离", "角度", "生命值"}));
      this.addSetting(new Hack.Setting("容器停止", "打开容器时停止", true));
      this.addSetting(new Hack.Setting("绕过命名", "不攻击有名字的实体", false));
      this.addSetting(new Hack.Setting("绕过被动", "不攻击被动实体(动物等)", true));
      this.addSetting(new Hack.Setting("晓过实体", "不攻击的实体", "选择不攻击的实体", () -> {
         if (mc != null) {
            mc.m_91152_(new EntitySelectScreen(mc.f_91080_, "tpauras", this::reloadWhitelistFromConfig));
         }

      }));
      this.config = HackConfig.getInstance();
      this.tpAuraConfig = TpAuraConfig.getInstance();
      this.loadConfig();
      this.loadWhitelist();
   }

   private void loadConfig() {
      this.range = this.config.getDoubleSetting("传送光环", "范围", 4.25);
      this.attackSpeed = this.config.getDoubleSetting("传送光环", "攻击速度", 8.0);
      String priorityStr = this.config.getStringSetting("传送光环", "优先级", "角度");
      this.pauseOnContainers = this.config.getBooleanSetting("传送光环", "容器停止", true);
      this.ignoreNamed = this.config.getBooleanSetting("传送光环", "绕过命名", false);
      this.ignorePassive = this.config.getBooleanSetting("传送光环", "绕过被动", true);
      Priority[] var2 = TpAurasHack.Priority.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Priority p = var2[var4];
         if (p.toString().equals(priorityStr)) {
            this.priority = p;
            break;
         }
      }

      Iterator var6 = this.getSettings().iterator();

      while(var6.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var6.next();
         switch (setting.getName()) {
            case "范围":
               setting.setValue(this.range);
               break;
            case "攻击速度":
               setting.setValue(this.attackSpeed);
               break;
            case "优先级":
               setting.setValue(priorityStr);
               break;
            case "容器停止":
               setting.setValue(this.pauseOnContainers);
               break;
            case "绕过命名":
               setting.setValue(this.ignoreNamed);
               break;
            case "绕过被动":
               setting.setValue(this.ignorePassive);
         }
      }

   }

   private void loadWhitelist() {
      this.reloadWhitelistFromConfig();
   }

   private void reloadWhitelistFromConfig() {
      this.whitelistEntities.clear();
      File f = new File("C:/karucn/Lexis/config/hack/entity_select_tpauras.json");
      Type type = (new TypeToken() {
      }).getType();
      List loaded = (List)ConfigUtils.readConfig(f, type);
      if (loaded != null) {
         this.whitelistEntities.addAll(loaded);
      }

   }

   public void saveWhitelist(Set newWhitelist) {
      this.whitelistEntities.clear();
      this.whitelistEntities.addAll(newWhitelist);
      this.tpAuraConfig.setWhitelist(newWhitelist);
   }

   public void onEnable() {
      this.lastAttackTime = 0L;
      this.currentTarget = null;
   }

   public void onDisable() {
      if (HeadOnlyLook.isLooking()) {
         HeadOnlyLook.stopLooking();
      }

      this.currentTarget = null;
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         Iterator var1 = this.getSettings().iterator();

         label87:
         while(var1.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var1.next();
            switch (setting.getName()) {
               case "范围":
                  this.range = setting.getDouble();
                  break;
               case "攻击速度":
                  this.attackSpeed = setting.getDouble();
                  break;
               case "优先级":
                  String priorityStr = setting.getString();
                  Priority[] var6 = TpAurasHack.Priority.values();
                  int var7 = var6.length;
                  int var8 = 0;

                  while(true) {
                     if (var8 >= var7) {
                        continue label87;
                     }

                     Priority p = var6[var8];
                     if (p.toString().equals(priorityStr)) {
                        this.priority = p;
                        continue label87;
                     }

                     ++var8;
                  }
               case "容器停止":
                  this.pauseOnContainers = setting.getBoolean();
                  break;
               case "绕过命名":
                  this.ignoreNamed = setting.getBoolean();
                  break;
               case "绕过被动":
                  this.ignorePassive = setting.getBoolean();
            }
         }

         if (!(mc.f_91074_.m_36403_(0.5F) < 1.0F)) {
            this.currentTarget = this.findTarget();
            if (this.currentTarget != null && this.currentTarget.m_6084_()) {
               this.lookAtEntity(this.currentTarget);
               if (!this.pauseOnContainers || !(mc.f_91080_ instanceof AbstractContainerScreen)) {
                  double offsetX = (double)(this.random.nextInt(3) * 2 - 2);
                  double offsetZ = (double)(this.random.nextInt(3) * 2 - 2);
                  mc.f_91074_.m_6034_(this.currentTarget.m_20185_() + offsetX, this.currentTarget.m_20186_(), this.currentTarget.m_20189_() + offsetZ);
                  mc.f_91072_.m_105223_(mc.f_91074_, this.currentTarget);
                  mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
                  mc.f_91074_.m_36334_();
               }
            } else {
               if (HeadOnlyLook.isLooking()) {
               }

            }
         } else {
            this.currentTarget = this.findTarget();
            if (this.currentTarget != null && this.currentTarget.m_6084_()) {
               this.lookAtEntity(this.currentTarget);
            } else if (HeadOnlyLook.isLooking()) {
            }

         }
      }
   }

   private Entity findTarget() {
      double rangeSq = this.range * this.range;
      List entities = new ArrayList();
      Iterator var4 = mc.f_91073_.m_104735_().iterator();

      while(var4.hasNext()) {
         Entity entity = (Entity)var4.next();
         entities.add(entity);
      }

      List targets = new ArrayList();
      Iterator var10 = entities.iterator();

      while(true) {
         Entity entity;
         while(true) {
            String entityId;
            do {
               do {
                  do {
                     do {
                        do {
                           do {
                              do {
                                 if (!var10.hasNext()) {
                                    if (targets.isEmpty()) {
                                       return null;
                                    }

                                    switch (this.priority) {
                                       case DISTANCE:
                                          targets.sort(Comparator.comparingDouble((e) -> {
                                             return mc.f_91074_.m_20280_(e);
                                          }));
                                          break;
                                       case ANGLE:
                                          targets.sort(Comparator.comparingDouble((e) -> {
                                             return RotationUtils.getAngleToLookVec(e.m_20191_().m_82399_());
                                          }));
                                          break;
                                       case HEALTH:
                                          targets.sort(Comparator.comparingDouble((e) -> {
                                             return (double)((LivingEntity)e).m_21223_();
                                          }));
                                    }

                                    return (Entity)targets.get(0);
                                 }

                                 entity = (Entity)var10.next();
                              } while(!(entity instanceof LivingEntity));
                           } while(entity == mc.f_91074_);
                        } while(!((LivingEntity)entity).m_6084_());
                     } while(mc.f_91074_.m_20280_(entity) > rangeSq);
                  } while(entity instanceof Player && FriendsManager.getInstance().isFriend((Player)entity));

                  entityId = entity.m_6095_().m_204041_().m_205785_().m_135782_().toString();
               } while(this.whitelistEntities.contains(entityId));
            } while(this.ignoreNamed && entity.m_8077_());

            if (!this.ignorePassive) {
               break;
            }

            if (!(entity instanceof Animal)) {
               if (!(entity instanceof NeutralMob) || !(entity instanceof Mob)) {
                  break;
               }

               Mob mob2 = (Mob)entity;
               if (mob2.m_5912_()) {
                  break;
               }
            }
         }

         targets.add(entity);
      }
   }

   private void lookAtEntity(Entity target) {
      Vec3 targetPos = target.m_20191_().m_82399_();
      Vec3 playerPos = mc.f_91074_.m_146892_();
      Vec3 direction = targetPos.m_82546_(playerPos);
      double distanceXZ = Math.sqrt(direction.f_82479_ * direction.f_82479_ + direction.f_82481_ * direction.f_82481_);
      float yaw = (float)Math.toDegrees(Math.atan2(direction.f_82481_, direction.f_82479_)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(direction.f_82480_, distanceXZ)));
      yaw = this.normalizeAngle(yaw);
      pitch = Math.max(-90.0F, Math.min(90.0F, pitch));
      mc.m_91403_().m_104955_(new ServerboundMovePlayerPacket.Rot(yaw, pitch, mc.f_91074_.m_20096_()));
      if (!HeadOnlyLook.isRotating()) {
         HeadOnlyLook.startRotation(yaw, pitch);
      } else {
         HeadOnlyLook.updateRotation(yaw, pitch);
      }

   }

   private float normalizeAngle(float angle) {
      angle %= 360.0F;
      if (angle > 180.0F) {
         angle -= 360.0F;
      }

      if (angle < -180.0F) {
         angle += 360.0F;
      }

      return angle;
   }

   public void onClick() {
      this.toggle();
   }

   public Set getWhitelist() {
      return this.whitelistEntities;
   }

   public static enum Priority {
      DISTANCE("距离"),
      ANGLE("角度"),
      HEALTH("生命值");

      private final String displayName;

      private Priority(String name) {
         this.displayName = name;
      }

      public String toString() {
         return this.displayName;
      }

      // $FF: synthetic method
      private static Priority[] $values() {
         return new Priority[]{DISTANCE, ANGLE, HEALTH};
      }
   }
}
