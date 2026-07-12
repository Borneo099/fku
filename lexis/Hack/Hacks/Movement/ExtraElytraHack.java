package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ExtraElytraHack extends Hack {
   private boolean instantFly = true;
   private boolean speedCtrl = true;
   private boolean heightCtrl = false;
   private boolean stopInWater = true;
   private double maxSpeed = 2.0;
   private double maxHeightSpeed = 0.5;
   private int jumpTimer = 0;
   private HackConfig config;

   public ExtraElytraHack() {
      super("简单鞘翅", new String[]{"让鞘翅飞行更简单"}, Hack.Category.MOVEMENT, true);
      this.addSetting(new Hack.Setting("瞬间飞行", "跳一下就能飞，不需要双跳", true));
      this.addSetting(new Hack.Setting("速度控制", "用前进后退键控制速度 (W/S)", true));
      this.addSetting(new Hack.Setting("高度控制", "用跳跃/潜行控制高度 (空格/Shift)", false));
      this.addSetting(new Hack.Setting("水中停止", "在水中自动停止飞行", true));
      this.addSetting(new Hack.Setting("最大速度", "最大飞行速度", 2.0, 0.5, 25.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("爬升速度", "最大爬升速度", 0.5, 0.1, 15.0, Hack.ValueDisplay.DECIMAL));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.instantFly = this.config.getBooleanSetting("简单鞘翅", "瞬间飞行", true);
      this.speedCtrl = this.config.getBooleanSetting("简单鞘翅", "速度控制", true);
      this.heightCtrl = this.config.getBooleanSetting("简单鞘翅", "高度控制", false);
      this.stopInWater = this.config.getBooleanSetting("简单鞘翅", "水中停止", true);
      this.maxSpeed = this.config.getDoubleSetting("简单鞘翅", "最大速度", 2.0);
      this.maxHeightSpeed = this.config.getDoubleSetting("简单鞘翅", "爬升速度", 0.5);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "瞬间飞行":
               setting.setValue(this.instantFly);
               break;
            case "速度控制":
               setting.setValue(this.speedCtrl);
               break;
            case "高度控制":
               setting.setValue(this.heightCtrl);
               break;
            case "水中停止":
               setting.setValue(this.stopInWater);
               break;
            case "最大速度":
               setting.setValue(this.maxSpeed);
               break;
            case "爬升速度":
               setting.setValue(this.maxHeightSpeed);
         }
      }

   }

   public void onEnable() {
      this.jumpTimer = 0;
   }

   public void onDisable() {
   }

   public String getDisplayName() {
      return String.format("%s [%.1f,%.1f]", this.getName(), this.maxSpeed, this.maxHeightSpeed);
   }

   public void onUpdate() {
      if (mc.f_91074_ != null) {
         Iterator var1 = this.getSettings().iterator();

         while(var1.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var1.next();
            switch (setting.getName()) {
               case "瞬间飞行":
                  this.instantFly = setting.getBoolean();
                  break;
               case "速度控制":
                  this.speedCtrl = setting.getBoolean();
                  break;
               case "高度控制":
                  this.heightCtrl = setting.getBoolean();
                  break;
               case "水中停止":
                  this.stopInWater = setting.getBoolean();
                  break;
               case "最大速度":
                  this.maxSpeed = setting.getDouble();
                  break;
               case "爬升速度":
                  this.maxHeightSpeed = setting.getDouble();
            }
         }

         if (this.jumpTimer > 0) {
            --this.jumpTimer;
         }

         ItemStack chest = mc.f_91074_.m_6844_(EquipmentSlot.CHEST);
         if (chest.m_41720_() == Items.f_42741_) {
            if (mc.f_91074_.m_21255_()) {
               if (this.stopInWater && mc.f_91074_.m_20069_()) {
                  this.stopFlying();
               } else {
                  this.controlSpeed();
                  this.controlHeight();
                  this.limitSpeed();
               }
            } else if (mc.f_91066_.f_92089_.m_90857_() && this.instantFly) {
               this.startFlying();
            }
         }

      }
   }

   private void startFlying() {
      if (this.jumpTimer <= 0) {
         this.jumpTimer = 5;
         mc.f_91074_.m_6135_();
         if (mc.m_91403_() != null) {
            mc.m_91403_().m_104955_(new ServerboundPlayerCommandPacket(mc.f_91074_, Action.START_FALL_FLYING));
         }

         mc.f_91074_.m_36320_();
         mc.f_91074_.m_183634_();
      }
   }

   private void stopFlying() {
      if (mc.m_91403_() != null) {
         mc.f_91074_.m_36321_();
      }

   }

   private void controlHeight() {
      if (this.heightCtrl) {
         Vec3 v = mc.f_91074_.m_20184_();
         if (mc.f_91066_.f_92089_.m_90857_()) {
            mc.f_91074_.m_20334_(v.f_82479_, v.f_82480_ + this.maxHeightSpeed, v.f_82481_);
         } else if (mc.f_91066_.f_92090_.m_90857_()) {
            mc.f_91074_.m_20334_(v.f_82479_, v.f_82480_ - this.maxHeightSpeed, v.f_82481_);
         }

      }
   }

   private void controlSpeed() {
      if (this.speedCtrl) {
         float yaw = (float)Math.toRadians((double)mc.f_91074_.m_146908_());
         Vec3 forward = new Vec3((double)(-Mth.m_14031_(yaw)) * 0.1, 0.0, (double)Mth.m_14089_(yaw) * 0.1);
         Vec3 v = mc.f_91074_.m_20184_();
         if (mc.f_91066_.f_92085_.m_90857_()) {
            mc.f_91074_.m_20256_(v.m_82549_(forward));
         } else if (mc.f_91066_.f_92087_.m_90857_()) {
            mc.f_91074_.m_20256_(v.m_82546_(forward));
         }

      }
   }

   private void limitSpeed() {
      Vec3 v = mc.f_91074_.m_20184_();
      double speed = Math.sqrt(v.f_82479_ * v.f_82479_ + v.f_82481_ * v.f_82481_);
      if (speed > this.maxSpeed) {
         double scale = this.maxSpeed / speed;
         mc.f_91074_.m_20334_(v.f_82479_ * scale, v.f_82480_, v.f_82481_ * scale);
      }

   }

   public void onClick() {
      this.toggle();
   }
}
