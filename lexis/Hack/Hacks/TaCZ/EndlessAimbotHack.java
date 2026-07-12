package lexis.Hack.Hacks.TaCZ;

import com.google.gson.reflect.TypeToken;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.ConfigUtils;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.FriendsManager;
import lexis.Hack.Utils.ESP.EntitySelectScreen;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EndlessAimbotHack extends Hack implements UpdateListener {
   private static Field taczDataField = null;
   private static Field taczAimingField = null;
   private static boolean taczReflectionFailed = false;
   private float rotationSpeed = 30.0F;
   private boolean bypassFriends = true;
   private boolean bypassNamed = false;
   private boolean bypassPassive = true;
   private boolean onlyWhenAiming = true;
   private boolean allowThroughWalls = false;
   private boolean onlyOnLeftClick = false;
   private String bodyPart = "身体";
   private final Set entityWhitelist = ConcurrentHashMap.newKeySet();
   private LivingEntity currentTarget = null;
   private boolean hasTarget = false;
   private long lastShootTime = 0L;
   private static final long SHOOT_COOLDOWN_MS = 120L;
   private HackConfig config;
   private static final String CONFIG_KEY = "无尽自瞄";

   public EndlessAimbotHack() {
      super("无尽自瞄", "全图最近实体自瞄+自动开火", Hack.Category.TACZ, true);
      this.addSetting(new Hack.Setting("旋转速度", "旋转速度（度/秒）", 30.0, 1.0, 500.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("在开镜锁定", "仅在使用物品/开镜瞄准时才锁定目标", true));
      this.addSetting(new Hack.Setting("允许穿墙锁定", "关闭时会检测视线是否被方块阻挡", false));
      this.addSetting(new Hack.Setting("锁定部位", "瞄准目标的哪个身体部位（自动=选离准星最近的部位）", "身体", new String[]{"头", "身体", "腿", "脚", "自动"}));
      this.addSetting(new Hack.Setting("绕过好友", "不瞄准好友", true));
      this.addSetting(new Hack.Setting("绕过命名", "不瞄准有名字的实体", false));
      this.addSetting(new Hack.Setting("绕过被动", "不瞄准被动实体（动物等）", true));
      this.addSetting(new Hack.Setting("晓过实体", "实体白名单，不瞄准这些实体", "晓过实体", () -> {
         if (mc != null) {
            mc.m_91152_(new EntitySelectScreen(mc.f_91080_, "endless_aimbot", this::reloadWhitelistFromConfig));
         }

      }));
      this.addSetting(new Hack.Setting("仅左键自动自瞄", "仅当按住左键时自动瞄准并开火", false));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.rotationSpeed = (float)this.config.getDoubleSetting("无尽自瞄", "旋转速度", 30.0);
      this.onlyWhenAiming = this.config.getBooleanSetting("无尽自瞄", "在开镜锁定", true);
      this.allowThroughWalls = this.config.getBooleanSetting("无尽自瞄", "允许穿墙锁定", false);
      this.bodyPart = this.config.getStringSetting("无尽自瞄", "锁定部位", "身体");
      this.bypassFriends = this.config.getBooleanSetting("无尽自瞄", "绕过好友", true);
      this.bypassNamed = this.config.getBooleanSetting("无尽自瞄", "绕过命名", false);
      this.bypassPassive = this.config.getBooleanSetting("无尽自瞄", "绕过被动", true);
      this.onlyOnLeftClick = this.config.getBooleanSetting("无尽自瞄", "仅左键自动自瞄", false);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "旋转速度":
               setting.setValue((double)this.rotationSpeed);
               break;
            case "在开镜锁定":
               setting.setValue(this.onlyWhenAiming);
               break;
            case "允许穿墙锁定":
               setting.setValue(this.allowThroughWalls);
               break;
            case "锁定部位":
               setting.setValue(this.bodyPart);
               break;
            case "绕过好友":
               setting.setValue(this.bypassFriends);
               break;
            case "绕过命名":
               setting.setValue(this.bypassNamed);
               break;
            case "绕过被动":
               setting.setValue(this.bypassPassive);
               break;
            case "仅左键自动自瞄":
               setting.setValue(this.onlyOnLeftClick);
         }
      }

      this.reloadWhitelistFromConfig();
   }

   private void saveConfig() {
      this.config.saveHackSettings("无尽自瞄", this.getSettings());
   }

   private void reloadWhitelistFromConfig() {
      this.entityWhitelist.clear();
      File f = new File("C:/karucn/Lexis/config/hack/entity_select_endless_aimbot.json");
      Type type = (new TypeToken() {
      }).getType();
      List loaded = (List)ConfigUtils.readConfig(f, type);
      if (loaded != null) {
         this.entityWhitelist.addAll(loaded);
      }

   }

   public void onEnable() {
      this.currentTarget = null;
      this.hasTarget = false;
      this.lastShootTime = 0L;
      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      this.currentTarget = null;
      this.hasTarget = false;
      HeadOnlyLook.forceStop();
      EventManager.remove(UpdateListener.class, this);
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         boolean needSave = false;
         Iterator var2 = this.getSettings().iterator();

         while(var2.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var2.next();
            switch (setting.getName()) {
               case "旋转速度":
                  if ((float)setting.getDouble() != this.rotationSpeed) {
                     this.rotationSpeed = (float)setting.getDouble();
                     needSave = true;
                  }
                  break;
               case "在开镜锁定":
                  if (setting.getBoolean() != this.onlyWhenAiming) {
                     this.onlyWhenAiming = setting.getBoolean();
                     needSave = true;
                  }
                  break;
               case "允许穿墙锁定":
                  if (setting.getBoolean() != this.allowThroughWalls) {
                     this.allowThroughWalls = setting.getBoolean();
                     needSave = true;
                  }
                  break;
               case "锁定部位":
                  if (!setting.getString().equals(this.bodyPart)) {
                     this.bodyPart = setting.getString();
                     needSave = true;
                  }
                  break;
               case "绕过好友":
                  if (setting.getBoolean() != this.bypassFriends) {
                     this.bypassFriends = setting.getBoolean();
                     needSave = true;
                  }
                  break;
               case "绕过命名":
                  if (setting.getBoolean() != this.bypassNamed) {
                     this.bypassNamed = setting.getBoolean();
                     needSave = true;
                  }
                  break;
               case "绕过被动":
                  if (setting.getBoolean() != this.bypassPassive) {
                     this.bypassPassive = setting.getBoolean();
                     needSave = true;
                  }
                  break;
               case "仅左键自动自瞄":
                  if (setting.getBoolean() != this.onlyOnLeftClick) {
                     this.onlyOnLeftClick = setting.getBoolean();
                     needSave = true;
                  }
            }
         }

         if (needSave) {
            this.saveConfig();
         }

         HeadOnlyLook.onClientTick();
         if (this.onlyOnLeftClick && !mc.f_91066_.f_92096_.m_90857_()) {
            if (HeadOnlyLook.isLooking()) {
               HeadOnlyLook.stopLooking();
            }

            if (this.hasTarget) {
               this.hasTarget = false;
               this.currentTarget = null;
            }

         } else if (this.onlyWhenAiming && !isPlayerAiming(mc.f_91074_)) {
            if (HeadOnlyLook.isLooking()) {
               HeadOnlyLook.stopLooking();
            }

            if (this.hasTarget) {
               this.hasTarget = false;
               this.currentTarget = null;
            }

         } else {
            if (this.currentTarget != null && !this.currentTarget.m_6084_()) {
               this.currentTarget = null;
               this.hasTarget = false;
            }

            LocalPlayer player = mc.f_91074_;
            List targets = this.getValidTargets();
            LivingEntity nearest = this.findNearestTarget(targets, player);
            if (nearest == null) {
               if (HeadOnlyLook.isLooking()) {
                  HeadOnlyLook.stopLooking();
               }

               if (this.hasTarget) {
                  this.hasTarget = false;
                  this.currentTarget = null;
               }

            } else {
               this.currentTarget = nearest;
               this.hasTarget = true;
               Vec3 targetPos = this.getAimPoint(nearest);
               Vec3 eye = player.m_146892_();
               Vec3 dir = targetPos.m_82546_(eye);
               double distXZ = Math.sqrt(dir.f_82479_ * dir.f_82479_ + dir.f_82481_ * dir.f_82481_);
               if (!(distXZ < 0.01)) {
                  float targetYaw = normalizeAngle((float)Math.toDegrees(Math.atan2(dir.f_82481_, dir.f_82479_)) - 90.0F);
                  float targetPitch = Mth.m_14036_((float)(-Math.toDegrees(Math.atan2(dir.f_82480_, distXZ))), -90.0F, 90.0F);
                  HeadOnlyLook.setRotationSpeedF(this.rotationSpeed, this.rotationSpeed * 0.5F);
                  if (!HeadOnlyLook.isLooking()) {
                     HeadOnlyLook.startRotation(targetYaw, targetPitch);
                  } else {
                     HeadOnlyLook.updateRotation(targetYaw, targetPitch);
                  }

                  if (HeadOnlyLook.hasReachedTarget(3.5F)) {
                     long now = System.currentTimeMillis();
                     if (now - this.lastShootTime >= 120L) {
                        IClientPlayerGunOperator op = IClientPlayerGunOperator.fromLocalPlayer(mc.f_91074_);
                        if (op != null) {
                           ShootResult result = op.shoot();
                           if (result == ShootResult.SUCCESS) {
                              this.lastShootTime = now;
                           }
                        }
                     }
                  }

               }
            }
         }
      }
   }

   private LivingEntity findNearestTarget(List targets, LocalPlayer player) {
      if (targets.isEmpty()) {
         return null;
      } else {
         Vec3 eyePos = player.m_146892_();
         Vec3 lookVec = player.m_20154_();
         LivingEntity best = null;
         double bestDist = Double.MAX_VALUE;
         Iterator var8 = targets.iterator();

         while(true) {
            LivingEntity entity;
            Vec3 targetPos;
            Vec3 toTarget;
            double distance;
            do {
               do {
                  do {
                     do {
                        if (!var8.hasNext()) {
                           return best;
                        }

                        entity = (LivingEntity)var8.next();
                     } while(!entity.m_6084_());

                     targetPos = entity.m_20191_().m_82399_();
                     toTarget = targetPos.m_82546_(eyePos);
                     distance = toTarget.m_82553_();
                  } while(distance < 0.01);
               } while(toTarget.m_82526_(lookVec) < 0.0);
            } while(!this.allowThroughWalls && !this.hasLineOfSight(eyePos, targetPos));

            if (distance < bestDist) {
               bestDist = distance;
               best = entity;
            }
         }
      }
   }

   private boolean hasLineOfSight(Vec3 from, Vec3 to) {
      if (mc.f_91073_ == null) {
         return true;
      } else {
         HitResult result = mc.f_91073_.m_45547_(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, mc.f_91074_));
         return result.m_6662_() == net.minecraft.world.phys.HitResult.Type.MISS;
      }
   }

   private List getValidTargets() {
      List targets = new ArrayList();
      LocalPlayer player = mc.f_91074_;
      if (player != null && mc.f_91073_ != null) {
         Iterator var3 = mc.f_91073_.m_104735_().iterator();

         while(true) {
            LivingEntity living;
            String entityId;
            do {
               Entity entity;
               do {
                  do {
                     do {
                        do {
                           do {
                              do {
                                 if (!var3.hasNext()) {
                                    return targets;
                                 }

                                 entity = (Entity)var3.next();
                              } while(!(entity instanceof LivingEntity));

                              living = (LivingEntity)entity;
                           } while(entity == player);
                        } while(!living.m_6084_());
                     } while(this.bypassFriends && entity instanceof Player && FriendsManager.getInstance().isFriend((Player)entity));
                  } while(this.bypassNamed && entity.m_8077_());
               } while(this.bypassPassive && living instanceof Animal);

               entityId = this.getEntityTypeId(entity);
            } while(entityId != null && this.entityWhitelist.contains(entityId));

            targets.add(living);
         }
      } else {
         return targets;
      }
   }

   private String getEntityTypeId(Entity entity) {
      ResourceLocation key = BuiltInRegistries.f_256780_.m_7981_(entity.m_6095_());
      return key != null ? key.toString() : null;
   }

   private Vec3 getAimPoint(LivingEntity e) {
      Vec3 c = e.m_20191_().m_82399_();
      double feetY = e.m_20186_();
      double h = (double)e.m_20206_();
      double y;
      switch (this.bodyPart) {
         case "头":
            y = feetY + (double)e.m_20192_();
            break;
         case "腿":
            y = feetY + h * 0.28;
            break;
         case "脚":
            y = feetY + h * 0.08;
            break;
         case "自动":
            return this.getAutoAimPoint(e, c);
         case "身体":
         default:
            y = feetY + h * 0.5;
      }

      return new Vec3(c.f_82479_, y, c.f_82481_);
   }

   private Vec3 getAutoAimPoint(LivingEntity e, Vec3 c) {
      if (mc.f_91074_ == null) {
         return new Vec3(c.f_82479_, c.f_82480_, c.f_82481_);
      } else {
         double feetY = e.m_20186_();
         double h = (double)e.m_20206_();
         double[] candidateY = new double[]{feetY + (double)e.m_20192_(), feetY + h * 0.5, feetY + h * 0.28, feetY + h * 0.08};
         Vec3 eye = mc.f_91074_.m_146892_();
         float curYaw = mc.f_91074_.m_146908_();
         float curPitch = mc.f_91074_.m_146909_();
         Vec3 bestPoint = new Vec3(c.f_82479_, c.f_82480_, c.f_82481_);
         double bestAngle = Double.MAX_VALUE;
         double[] var14 = candidateY;
         int var15 = candidateY.length;

         for(int var16 = 0; var16 < var15; ++var16) {
            double y = var14[var16];
            Vec3 p = new Vec3(c.f_82479_, y, c.f_82481_);
            Vec3 d = p.m_82546_(eye);
            double distXZ = Math.sqrt(d.f_82479_ * d.f_82479_ + d.f_82481_ * d.f_82481_);
            if (!(distXZ < 1.0E-4)) {
               float yaw = normalizeAngle((float)(Math.toDegrees(Math.atan2(d.f_82481_, d.f_82479_)) - 90.0));
               float pitch = (float)(-Math.toDegrees(Math.atan2(d.f_82480_, distXZ)));
               double angle = (double)(Math.abs(normalizeAngle(yaw - curYaw)) + Math.abs(pitch - curPitch));
               if (angle < bestAngle) {
                  bestAngle = angle;
                  bestPoint = p;
               }
            }
         }

         return bestPoint;
      }
   }

   private static float normalizeAngle(float angle) {
      angle %= 360.0F;
      if (angle > 180.0F) {
         angle -= 360.0F;
      }

      if (angle < -180.0F) {
         angle += 360.0F;
      }

      return angle;
   }

   private static boolean isPlayerAiming(LocalPlayer player) {
      if (!taczReflectionFailed) {
         try {
            if (taczDataField == null) {
               Field[] var1 = player.getClass().getDeclaredFields();
               int var2 = var1.length;

               for(int var3 = 0; var3 < var2; ++var3) {
                  Field f = var1[var3];
                  if (f.getType().getName().equals("com.tacz.guns.client.gameplay.LocalPlayerDataHolder")) {
                     f.setAccessible(true);
                     taczDataField = f;
                     break;
                  }
               }

               if (taczDataField == null) {
                  taczReflectionFailed = true;
                  return player.m_6117_();
               }
            }

            Object data = taczDataField.get(player);
            if (data == null) {
               return false;
            }

            if (taczAimingField == null) {
               taczAimingField = data.getClass().getDeclaredField("clientIsAiming");
               taczAimingField.setAccessible(true);
            }

            return (Boolean)taczAimingField.get(data);
         } catch (Exception var5) {
            taczReflectionFailed = true;
         }
      }

      return player.m_6117_();
   }

   public void onClick() {
      this.toggle();
   }
}
