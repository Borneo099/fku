package lexis.Client.Goto;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class RotationUtils {
   public static Rotation getNeededRotations(Vec3 vec) {
      Vec3 playerPos = Minecraft.m_91087_().f_91074_.m_20299_(1.0F);
      double diffX = vec.f_82479_ - playerPos.f_82479_;
      double diffY = vec.f_82480_ - playerPos.f_82480_;
      double diffZ = vec.f_82481_ - playerPos.f_82481_;
      double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, dist)));
      return new Rotation(yaw, pitch);
   }

   public static float limitAngleChange(float current, float intended) {
      float change = ((intended - current) % 360.0F + 540.0F) % 360.0F - 180.0F;
      return current + change;
   }

   public static float getHorizontalAngleToLookVec(Vec3 vec) {
      Rotation needed = getNeededRotations(vec);
      float currentYaw = Minecraft.m_91087_().f_91074_.m_146908_();
      return Math.abs(limitAngleChange(currentYaw, needed.getYaw()) - currentYaw);
   }

   public static class Rotation {
      private final float yaw;
      private final float pitch;

      public Rotation(float yaw, float pitch) {
         this.yaw = yaw;
         this.pitch = pitch;
      }

      public float getYaw() {
         return this.yaw;
      }

      public float getPitch() {
         return this.pitch;
      }
   }
}
