package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class ElytraFlysHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "幻环飞行鞘翹";
   private float speed = 4.5F;
   private boolean verticalControl = true;
   private boolean noGravity = true;

   public ElytraFlysHack() {
      super("幻环飞行鞘翹", "在鞘翅飞行模式下，幻飞行功能一样", Hack.Category.MOVEMENT, true);
      this.addSetting(new Hack.Setting("速度", "飞行速度", 4.5, 1.0, 20.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("垂直控制", "允许空格上升、Shift下降", true));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.speed = (float)this.config.getDoubleSetting("幻环飞行鞘翹", "速度", 4.5);
      this.verticalControl = this.config.getBooleanSetting("幻环飞行鞘翹", "垂直控制", true);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "速度":
               setting.setValue((double)this.speed);
               break;
            case "垂直控制":
               setting.setValue(this.verticalControl);
         }
      }

   }

   public void onEnable() {
   }

   public void onDisable() {
      if (mc.f_91074_ != null) {
         mc.f_91074_.m_20242_(false);
      }

   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      float yaw;
      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "速度":
               yaw = (float)setting.getDouble();
               if (yaw != this.speed) {
                  this.speed = yaw;
                  needSave = true;
               }
               break;
            case "垂直控制":
               boolean newVertical = setting.getBoolean();
               if (newVertical != this.verticalControl) {
                  this.verticalControl = newVertical;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("幻环飞行鞘翹", this.getSettings());
      }

      if (mc.f_91074_ != null && this.isEnabled()) {
         LocalPlayer player = mc.f_91074_;
         if (player.m_21255_()) {
            float forward = 0.0F;
            float strafe = 0.0F;
            float vertical = 0.0F;
            if (mc.f_91066_.f_92085_.m_90857_()) {
               ++forward;
            }

            if (mc.f_91066_.f_92087_.m_90857_()) {
               --forward;
            }

            if (mc.f_91066_.f_92086_.m_90857_()) {
               ++strafe;
            }

            if (mc.f_91066_.f_92088_.m_90857_()) {
               --strafe;
            }

            if (this.verticalControl) {
               if (mc.f_91066_.f_92089_.m_90857_()) {
                  ++vertical;
               }

               if (mc.f_91066_.f_92090_.m_90857_()) {
                  --vertical;
               }
            }

            if (forward == 0.0F && strafe == 0.0F && vertical == 0.0F) {
               player.m_20256_(Vec3.f_82478_);
               player.m_20334_(0.0, 0.0, 0.0);
            } else {
               yaw = player.m_146908_();
               double rad = Math.toRadians((double)yaw);
               double moveX = 0.0;
               double moveZ = 0.0;
               if (forward != 0.0F) {
                  moveX += -Math.sin(rad) * (double)forward;
                  moveZ += Math.cos(rad) * (double)forward;
               }

               if (strafe != 0.0F) {
                  moveX += -Math.sin(rad - 1.5707963267948966) * (double)strafe;
                  moveZ += Math.cos(rad - 1.5707963267948966) * (double)strafe;
               }

               double length = Math.hypot(moveX, moveZ);
               if (length > 0.0) {
                  moveX = moveX / length * (double)this.speed;
                  moveZ = moveZ / length * (double)this.speed;
               }

               double moveY = this.verticalControl ? (double)(vertical * this.speed) : 0.0;
               player.m_20334_(moveX, moveY, moveZ);
               if (moveY == 0.0 && vertical == 0.0F) {
                  player.m_20334_(player.m_20184_().f_82479_, 0.0, player.m_20184_().f_82481_);
               }

            }
         }
      }
   }

   public void onClick() {
      this.toggle();
   }
}
