package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;

public class SpeedHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "速度";
   private SpeedMode mode;
   private double vanillaSpeed;
   private double strafeSpeed;
   private double groundSpeed;
   private double yportSpeed;
   private boolean speedLimit;
   private boolean inLiquids;
   private boolean whenSneaking;
   private boolean onlyOnGround;
   private int stage;
   private double distance;
   private double currentSpeed;
   private long timer;

   public SpeedHack() {
      super("速度", "多种模式的速度增强", Hack.Category.MOVEMENT, true);
      this.mode = SpeedHack.SpeedMode.VANILLA;
      this.vanillaSpeed = 5.6;
      this.strafeSpeed = 1.6;
      this.groundSpeed = 0.5;
      this.yportSpeed = 1.2;
      this.speedLimit = false;
      this.inLiquids = false;
      this.whenSneaking = false;
      this.onlyOnGround = true;
      this.stage = 0;
      this.distance = 0.0;
      this.currentSpeed = 0.0;
      this.timer = 0L;
      this.addSetting(new Hack.Setting("模式", "速度模式", "原版", new String[]{"原版", "不限德国速度", "地面加速", "跳跃加速"}));
      this.addSetting(new Hack.Setting("原版速度", "每秒方块数", 5.6, 0.0, 64.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("不限德国速度", "不限德国速度模式 I 其实是不限速度吧 可以速度就多少？ 1.0 = 10倍数速度", 1.6, 0.0, 64.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("地面速度", "地面加速倍数", 0.5, 0.0, 64.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("跳跃速度", "跳跃加速倍数", 1.2, 0.0, 64.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("速度限制", "严格反作弊限制", false));
      this.addSetting(new Hack.Setting("液体中", "在水中/岩浆中生效", false));
      this.addSetting(new Hack.Setting("潜行时", "潜行时生效", false));
      this.addSetting(new Hack.Setting("仅地面", "仅站在地面时生效", true));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      String modeStr = this.config.getStringSetting("速度", "模式", "原版");
      this.vanillaSpeed = this.config.getDoubleSetting("速度", "原版速度", 5.6);
      this.strafeSpeed = this.config.getDoubleSetting("速度", "不限德国速度", 1.6);
      this.groundSpeed = this.config.getDoubleSetting("速度", "地面速度", 0.5);
      this.yportSpeed = this.config.getDoubleSetting("速度", "跳跃速度", 1.2);
      this.speedLimit = this.config.getBooleanSetting("速度", "速度限制", false);
      this.inLiquids = this.config.getBooleanSetting("速度", "液体中", false);
      this.whenSneaking = this.config.getBooleanSetting("速度", "潜行时", false);
      this.onlyOnGround = this.config.getBooleanSetting("速度", "仅地面", true);
      SpeedMode[] var2 = SpeedHack.SpeedMode.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         SpeedMode m = var2[var4];
         if (m.toString().equals(modeStr)) {
            this.mode = m;
            break;
         }
      }

      Iterator var6 = this.getSettings().iterator();

      while(var6.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var6.next();
         switch (setting.getName()) {
            case "模式":
               setting.setValue(modeStr);
               break;
            case "原版速度":
               setting.setValue(this.vanillaSpeed);
               break;
            case "不限德国速度速度":
               setting.setValue(this.strafeSpeed);
               break;
            case "地面速度":
               setting.setValue(this.groundSpeed);
               break;
            case "跳跃速度":
               setting.setValue(this.yportSpeed);
               break;
            case "速度限制":
               setting.setValue(this.speedLimit);
               break;
            case "液体中":
               setting.setValue(this.inLiquids);
               break;
            case "潜行时":
               setting.setValue(this.whenSneaking);
               break;
            case "仅地面":
               setting.setValue(this.onlyOnGround);
         }
      }

      this.resetStrafeMode();
   }

   private void resetStrafeMode() {
      this.stage = 0;
      this.distance = 0.0;
      this.currentSpeed = 0.0;
      this.timer = 0L;
   }

   public void onEnable() {
      this.resetStrafeMode();
   }

   public void onDisable() {
   }

   public void onUpdate() {
      if (mc.f_91074_ != null) {
         Iterator var1 = this.getSettings().iterator();

         while(true) {
            label88:
            while(var1.hasNext()) {
               Hack.Setting setting = (Hack.Setting)var1.next();
               switch (setting.getName()) {
                  case "模式":
                     String modeStr = setting.getString();
                     SpeedMode[] var6 = SpeedHack.SpeedMode.values();
                     int var7 = var6.length;
                     int var8 = 0;

                     while(true) {
                        if (var8 >= var7) {
                           continue label88;
                        }

                        SpeedMode m = var6[var8];
                        if (m.toString().equals(modeStr)) {
                           if (this.mode != m) {
                              this.mode = m;
                              this.resetStrafeMode();
                           }
                           continue label88;
                        }

                        ++var8;
                     }
                  case "原版速度":
                     this.vanillaSpeed = setting.getDouble();
                     break;
                  case "不限德国速度":
                     this.strafeSpeed = setting.getDouble();
                     break;
                  case "地面速度":
                     this.groundSpeed = setting.getDouble();
                     break;
                  case "跳跃速度":
                     this.yportSpeed = setting.getDouble();
                     break;
                  case "速度限制":
                     this.speedLimit = setting.getBoolean();
                     break;
                  case "液体中":
                     this.inLiquids = setting.getBoolean();
                     break;
                  case "潜行时":
                     this.whenSneaking = setting.getBoolean();
                     break;
                  case "仅地面":
                     this.onlyOnGround = setting.getBoolean();
               }
            }

            if (this.shouldStop()) {
               return;
            }

            switch (this.mode) {
               case VANILLA:
                  this.handleVanillaMode();
                  break;
               case STRAFE:
                  this.handleStrafeMode();
                  break;
               case ON_GROUND:
                  this.handleOnGroundMode();
                  break;
               case YPORT:
                  this.handleYPortMode();
            }

            return;
         }
      }
   }

   private boolean shouldStop() {
      if (mc.f_91074_ == null) {
         return true;
      } else if (!mc.f_91074_.m_21255_() && !mc.f_91074_.m_6147_() && mc.f_91074_.m_20202_() == null) {
         if (!this.whenSneaking && mc.f_91074_.m_6144_()) {
            return true;
         } else if (this.onlyOnGround && !mc.f_91074_.m_20096_()) {
            return true;
         } else {
            return !this.inLiquids && (mc.f_91074_.m_20069_() || mc.f_91074_.m_20077_());
         }
      } else {
         return true;
      }
   }

   private void handleVanillaMode() {
      Vec3 vel = this.getHorizontalVelocity(this.vanillaSpeed / 20.0);
      if (mc.f_91074_.m_21023_(MobEffects.f_19596_)) {
         int amplifier = mc.f_91074_.m_21124_(MobEffects.f_19596_).m_19564_();
         double value = (double)(amplifier + 1) * 0.205;
         vel = vel.m_82520_(vel.f_82479_ * value, 0.0, vel.f_82481_ * value);
      }

      mc.f_91074_.m_20334_(vel.f_82479_, mc.f_91074_.m_20184_().f_82480_, vel.f_82481_);
   }

   private void handleStrafeMode() {
      if (!this.isMoving()) {
         this.distance = 0.0;
      } else {
         this.distance += mc.f_91074_.m_20184_().m_165924_();
         switch (this.stage) {
            case 0:
               if (mc.f_91074_.m_20096_()) {
                  ++this.stage;
                  this.currentSpeed = 1.1799999475479126 * this.getBaseSpeed() - 0.01;
               }
               break;
            case 1:
               if (mc.f_91074_.m_20096_()) {
                  mc.f_91074_.m_20334_(mc.f_91074_.m_20184_().f_82479_, 0.4, mc.f_91074_.m_20184_().f_82481_);
                  this.currentSpeed *= this.strafeSpeed;
                  ++this.stage;
               }
               break;
            case 2:
               this.currentSpeed = this.distance - 0.76 * (this.distance - this.getBaseSpeed());
               ++this.stage;
               break;
            case 3:
               if (mc.f_91074_.f_19863_) {
                  this.stage = 0;
               }

               this.currentSpeed = this.distance - this.distance / 159.0;
         }

         this.currentSpeed = Math.max(this.currentSpeed, this.getBaseSpeed());
         if (this.speedLimit) {
            if (System.currentTimeMillis() - this.timer > 2500L) {
               this.timer = System.currentTimeMillis();
            }

            double limitSpeed = System.currentTimeMillis() - this.timer > 1250L ? 0.44 : 0.43;
            if (this.currentSpeed > limitSpeed) {
               this.currentSpeed = limitSpeed;
            }
         }

         Vector2d move = this.transformStrafe(this.currentSpeed);
         mc.f_91074_.m_20334_(move.x, mc.f_91074_.m_20184_().f_82480_, move.y);
      }
   }

   private void handleOnGroundMode() {
      if (mc.f_91074_.m_20096_()) {
         Vec3 vel = this.getHorizontalVelocity(this.groundSpeed);
         mc.f_91074_.m_20334_(vel.f_82479_, mc.f_91074_.m_20184_().f_82480_, vel.f_82481_);
      }
   }

   private void handleYPortMode() {
      if (mc.f_91074_.m_20096_() && this.isMoving()) {
         mc.f_91074_.m_20334_(mc.f_91074_.m_20184_().f_82479_, 0.4, mc.f_91074_.m_20184_().f_82481_);
      }

      Vec3 vel = this.getHorizontalVelocity(this.yportSpeed);
      mc.f_91074_.m_20334_(vel.f_82479_, mc.f_91074_.m_20184_().f_82480_, vel.f_82481_);
   }

   private Vec3 getHorizontalVelocity(double speed) {
      float forward = mc.f_91074_.f_20902_;
      float strafe = mc.f_91074_.f_20900_;
      if (forward == 0.0F && strafe == 0.0F) {
         return Vec3.f_82478_;
      } else {
         float yaw = mc.f_91074_.m_146908_();
         double radians = Math.toRadians((double)yaw);
         double motionX = -Math.sin(radians) * (double)forward + Math.cos(radians) * (double)strafe;
         double motionZ = Math.cos(radians) * (double)forward + Math.sin(radians) * (double)strafe;
         double length = Math.sqrt(motionX * motionX + motionZ * motionZ);
         if (length > 0.0) {
            motionX = motionX / length * speed;
            motionZ = motionZ / length * speed;
         }

         return new Vec3(motionX, 0.0, motionZ);
      }
   }

   private Vector2d transformStrafe(double speed) {
      float forward = mc.f_91074_.f_20902_;
      float side = mc.f_91074_.f_20900_;
      float yaw = mc.f_91074_.m_146908_();
      if (forward == 0.0F && side == 0.0F) {
         return new Vector2d(0.0, 0.0);
      } else {
         float strafe = 90.0F * side;
         if (forward != 0.0F) {
            strafe *= forward * 0.5F;
         }

         yaw -= strafe;
         if (forward < 0.0F) {
            yaw -= 180.0F;
         }

         double yawRadians = Math.toRadians((double)yaw);
         return new Vector2d(-Math.sin(yawRadians) * speed, Math.cos(yawRadians) * speed);
      }
   }

   private double getBaseSpeed() {
      return 0.2873;
   }

   private boolean isMoving() {
      return mc.f_91074_.f_20902_ != 0.0F || mc.f_91074_.f_20900_ != 0.0F;
   }

   public void onClick() {
      this.toggle();
   }

   public String getInfoString() {
      return this.mode.toString();
   }

   public String getDisplayName() {
      double spd;
      switch (this.mode) {
         case VANILLA:
            spd = this.vanillaSpeed;
            break;
         case STRAFE:
            spd = this.strafeSpeed;
            break;
         case ON_GROUND:
            spd = this.groundSpeed;
            break;
         case YPORT:
            spd = this.yportSpeed;
            break;
         default:
            spd = 0.0;
      }

      return String.format("%s [%s, %.1f]", this.getName(), this.mode.toString(), spd);
   }

   public static enum SpeedMode {
      VANILLA("原版"),
      STRAFE("不限德国速度"),
      ON_GROUND("地面加速"),
      YPORT("跳跃加速");

      private final String displayName;

      private SpeedMode(String name) {
         this.displayName = name;
      }

      public String toString() {
         return this.displayName;
      }

      // $FF: synthetic method
      private static SpeedMode[] $values() {
         return new SpeedMode[]{VANILLA, STRAFE, ON_GROUND, YPORT};
      }
   }
}
