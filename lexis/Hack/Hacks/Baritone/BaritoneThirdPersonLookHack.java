package lexis.Hack.Hacks.Baritone;

import lexis.Hack.Hack;
import lexis.Hack.Utils.BaritoneBridge;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import net.minecraft.client.CameraType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class BaritoneThirdPersonLookHack extends Hack {
   public static boolean enabled = false;
   private boolean driving = false;
   private boolean hasLast = false;
   private float lastYaw = 0.0F;
   private float lastPitch = 0.0F;

   public BaritoneThirdPersonLookHack() {
      super("任务中允许自己第三人称看见", new String[]{"Baritone 执行任务时，切换第三人称可看到角色朝向导航方向"}, Hack.Category.BARITONE, true);
   }

   public void onEnable() {
      enabled = true;
   }

   public void onDisable() {
      enabled = false;
      this.stopDrive();
      this.hasLast = false;
   }

   public void onUpdate() {
      if (mc.f_91074_ == null) {
         this.stopDrive();
      } else {
         boolean thirdPerson = mc.f_91066_.m_92176_() != CameraType.FIRST_PERSON;
         if (thirdPerson && BaritoneBridge.isActive()) {
            float[] rot = BaritoneBridge.getLookRotation();
            float yaw;
            float pitch;
            if (rot != null) {
               yaw = rot[0];
               pitch = Mth.m_14036_(rot[1], -90.0F, 90.0F);
               this.hasLast = true;
               this.lastYaw = yaw;
               this.lastPitch = pitch;
            } else {
               Vec3 v = mc.f_91074_.m_20184_();
               double horiz = Math.sqrt(v.f_82479_ * v.f_82479_ + v.f_82481_ * v.f_82481_);
               if (horiz > 0.04) {
                  yaw = (float)Math.toDegrees(Math.atan2(-v.f_82479_, v.f_82481_));
                  pitch = Mth.m_14036_((float)(-Math.toDegrees(Math.atan2(v.f_82480_, horiz))), -90.0F, 90.0F);
                  this.hasLast = true;
                  this.lastYaw = yaw;
                  this.lastPitch = pitch;
               } else if (this.hasLast) {
                  yaw = this.lastYaw;
                  pitch = this.lastPitch;
               } else {
                  yaw = mc.f_91074_.m_146908_();
                  pitch = mc.f_91074_.m_146909_();
               }
            }

            if (!HeadOnlyLook.isLooking()) {
               HeadOnlyLook.startRotation(yaw, pitch, 5000L);
            } else {
               HeadOnlyLook.updateRotation(yaw, pitch);
            }

            this.driving = true;
         } else {
            this.stopDrive();
         }
      }
   }

   private void stopDrive() {
      if (this.driving) {
         HeadOnlyLook.stopLooking();
         this.driving = false;
      }

   }

   public void onClick() {
      this.toggle();
   }
}
