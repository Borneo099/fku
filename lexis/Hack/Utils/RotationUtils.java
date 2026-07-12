package lexis.Hack.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class RotationUtils {
   private static final Minecraft mc = Minecraft.m_91087_();

   public static Vec3 getEyesPos() {
      LocalPlayer player = mc.f_91074_;
      return player == null ? Vec3.f_82478_ : player.m_146892_();
   }

   public static Vec3 getClientLookVec(float partialTicks) {
      LocalPlayer player = mc.f_91074_;
      if (player == null) {
         return Vec3.f_82478_;
      } else {
         float yaw = player.m_5675_(partialTicks);
         float pitch = player.m_5686_(partialTicks);
         return RotationUtils.Rotation.from(yaw, pitch).toLookVec();
      }
   }

   public static Rotation getNeededRotations(Vec3 vec) {
      Vec3 eyes = getEyesPos();
      double diffX = vec.f_82479_ - eyes.f_82479_;
      double diffZ = vec.f_82481_ - eyes.f_82481_;
      double yaw = Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0;
      double diffY = vec.f_82480_ - eyes.f_82480_;
      double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
      double pitch = -Math.toDegrees(Math.atan2(diffY, diffXZ));
      return RotationUtils.Rotation.wrapped((float)yaw, (float)pitch);
   }

   public static double getAngleToLookVec(Vec3 vec) {
      LocalPlayer player = mc.f_91074_;
      if (player == null) {
         return 0.0;
      } else {
         Rotation current = RotationUtils.Rotation.from(player.m_146908_(), player.m_146909_());
         Rotation needed = getNeededRotations(vec);
         return current.getAngleTo(needed);
      }
   }

   public static float limitAngleChange(float current, float intended, float maxChange) {
      float currentWrapped = Mth.m_14177_(current);
      float intendedWrapped = Mth.m_14177_(intended);
      float change = Mth.m_14177_(intendedWrapped - currentWrapped);
      change = Mth.m_14036_(change, -maxChange, maxChange);
      return current + change;
   }

   public static class Rotation {
      private final float yaw;
      private final float pitch;

      public Rotation(float yaw, float pitch) {
         this.yaw = yaw;
         this.pitch = pitch;
      }

      public static Rotation from(float yaw, float pitch) {
         return new Rotation(yaw, pitch);
      }

      public static Rotation wrapped(float yaw, float pitch) {
         return new Rotation(Mth.m_14177_(yaw), Mth.m_14177_(pitch));
      }

      public float yaw() {
         return this.yaw;
      }

      public float pitch() {
         return this.pitch;
      }

      public Vec3 toLookVec() {
         float xzLen = Mth.m_14089_(this.pitch * 0.017453292F);
         float x = xzLen * Mth.m_14031_(-this.yaw * 0.017453292F - 3.1415927F);
         float z = xzLen * Mth.m_14089_(-this.yaw * 0.017453292F - 3.1415927F);
         float y = Mth.m_14031_(this.pitch * 0.017453292F);
         return new Vec3((double)x, (double)(-y), (double)z);
      }

      public double getAngleTo(Rotation other) {
         float yawDiff = Math.abs(Mth.m_14177_(this.yaw - other.yaw));
         float pitchDiff = Math.abs(Mth.m_14177_(this.pitch - other.pitch));
         return Math.sqrt((double)(yawDiff * yawDiff + pitchDiff * pitchDiff));
      }
   }
}
