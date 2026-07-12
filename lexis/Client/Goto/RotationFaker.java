package lexis.Client.Goto;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class RotationFaker implements PreMotionListener, PostMotionListener {
   private boolean fakeRotation;
   private float serverYaw;
   private float serverPitch;
   private float realYaw;
   private float realPitch;

   public void onPreMotion() {
      if (this.fakeRotation && LexisUtil.MC.f_91074_ != null) {
         LocalPlayer player = LexisUtil.MC.f_91074_;
         this.realYaw = player.m_146908_();
         this.realPitch = player.m_146909_();
         player.m_146922_(this.serverYaw);
         player.m_146926_(this.serverPitch);
      }

   }

   public void onPostMotion() {
      if (this.fakeRotation && LexisUtil.MC.f_91074_ != null) {
         LocalPlayer player = LexisUtil.MC.f_91074_;
         player.m_146922_(this.realYaw);
         player.m_146926_(this.realPitch);
         this.fakeRotation = false;
      }

   }

   public void faceVectorPacket(Vec3 vec) {
      if (LexisUtil.MC.f_91074_ != null) {
         RotationUtils.Rotation needed = RotationUtils.getNeededRotations(vec);
         LocalPlayer player = LexisUtil.MC.f_91074_;
         this.fakeRotation = true;
         this.serverYaw = RotationUtils.limitAngleChange(player.m_146908_(), needed.getYaw());
         this.serverPitch = needed.getPitch();
      }
   }

   public void faceVectorClientIgnorePitch(Vec3 vec) {
      if (LexisUtil.MC.f_91074_ != null) {
         RotationUtils.Rotation needed = RotationUtils.getNeededRotations(vec);
         LocalPlayer player = LexisUtil.MC.f_91074_;
         player.m_146922_(RotationUtils.limitAngleChange(player.m_146908_(), needed.getYaw()));
         player.m_146926_(0.0F);
      }
   }
}
