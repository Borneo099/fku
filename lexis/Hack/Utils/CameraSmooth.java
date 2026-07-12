package lexis.Hack.Utils;

import net.minecraft.world.phys.Vec3;

public class CameraSmooth {
   private static boolean enabled = false;
   private static double speed = 0.2;
   private static Vec3 currentPos = null;
   private static Vec3 targetPos = null;

   public static void setEnabled(boolean e) {
      enabled = e;
      if (!e) {
         reset();
      }

   }

   public static void setSmoothSpeed(double s) {
      speed = s;
   }

   public static boolean isEnabled() {
      return enabled;
   }

   public static void reset() {
      currentPos = null;
      targetPos = null;
   }

   public static Vec3 applySmooth(Vec3 original) {
      if (!enabled) {
         return original;
      } else if (targetPos == null) {
         targetPos = original;
         currentPos = original;
         return original;
      } else {
         if (!targetPos.equals(original)) {
            targetPos = original;
         }

         if (currentPos.m_82554_(targetPos) > 0.001) {
            currentPos = currentPos.m_82549_(targetPos.m_82546_(currentPos).m_82490_(speed));
         } else {
            currentPos = targetPos;
         }

         return currentPos;
      }
   }
}
