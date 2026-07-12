package lexis.Hack.Utils.HeadOnlyLookUtils;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class HeadOnlyLook {
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final Random RNG = new Random();
   private static final long DEFAULT_AUTO_STOP_MS = 900L;
   private static Phase phase;
   private static boolean enabled;
   private static float targetYaw;
   private static float targetPitch;
   private static float currentYaw;
   private static float currentPitch;
   private static float originalYaw;
   private static float originalPitch;
   private static boolean hasOriginalAngles;
   private static float lastPlayerYaw;
   private static float lastPlayerPitch;
   private static long lastPlayerMoveTime;
   private static float serverYaw;
   private static float serverPitch;
   private static boolean rotationMode;
   private static boolean rotationEnabled;
   private static float rotationYaw;
   private static float rotationPitch;
   private static boolean lockMovement;
   private static float lockedYaw;
   private static float lockedPitch;
   private static long watchdogMs;
   private static long lastKeepAliveTime;
   private static boolean active;
   private static float activeYaw;
   private static float activePitch;
   private static float baseYawSpeed;
   private static float basePitchSpeed;
   private static float minSpeedFactor;
   private static float maxSpeedFactor;
   private static float smoothingFactor;
   private static float jitterAmount;
   private static boolean useGcdSnap;
   private static boolean useAdaptiveSpeed;
   private static long lastTickTime;

   public static void setRotationSpeedF(float maxYaw, float maxPitch) {
      baseYawSpeed = maxYaw;
      basePitchSpeed = maxPitch;
   }

   public static void setSmoothingFactor(float smooth) {
      smoothingFactor = Mth.m_14036_(smooth, 0.05F, 1.0F);
   }

   public static void setEaseFactor(float ease) {
      smoothingFactor = Mth.m_14036_(ease, 0.05F, 1.0F);
   }

   public static void setJitterAmount(float jitter) {
      jitterAmount = Math.max(0.0F, jitter);
   }

   public static void setGcdSnap(boolean gcd) {
      useGcdSnap = gcd;
   }

   public static void setAdaptiveSpeed(boolean adaptive) {
      useAdaptiveSpeed = adaptive;
   }

   public static void setLockMovement(boolean lock) {
      lockMovement = lock;
   }

   public static boolean isLockMovement() {
      return lockMovement;
   }

   public static void setDefaultAutoStopMs(long ms) {
      if (watchdogMs == 900L) {
         watchdogMs = ms;
      }

   }

   public static void lookAtEntity(Vec3 targetPos) {
      lookAtEntity(targetPos, 900L);
   }

   public static void lookAtEntity(Vec3 targetPos, long autoStopMs) {
      if (mc.f_91074_ != null) {
         watchdogMs = autoStopMs;
         setTargetByPos(targetPos);
         startOrContinue();
      }
   }

   public static void startLookingAt(BlockPos blockPos) {
      lookAtEntity(Vec3.m_82512_(blockPos), 900L);
   }

   public static void startLookingAt(BlockPos blockPos, long durationMs) {
      lookAtEntity(Vec3.m_82512_(blockPos), durationMs);
   }

   private static void setTargetByPos(Vec3 targetPos) {
      Vec3 eye = mc.f_91074_.m_146892_();
      Vec3 dir = targetPos.m_82546_(eye);
      double distXZ = Math.sqrt(dir.f_82479_ * dir.f_82479_ + dir.f_82481_ * dir.f_82481_);
      targetYaw = normalizeAngle((float)Math.toDegrees(Math.atan2(dir.f_82481_, dir.f_82479_)) - 90.0F);
      targetPitch = Mth.m_14036_((float)(-Math.toDegrees(Math.atan2(dir.f_82480_, distXZ))), -90.0F, 90.0F);
      serverYaw = targetYaw;
      serverPitch = targetPitch;
      if (lockMovement) {
         lockedYaw = targetYaw;
         lockedPitch = targetPitch;
      }

   }

   private static void startOrContinue() {
      if (!hasOriginalAngles) {
         originalYaw = mc.f_91074_.m_146908_();
         originalPitch = mc.f_91074_.m_146909_();
         currentYaw = originalYaw;
         currentPitch = originalPitch;
         lastPlayerYaw = originalYaw;
         lastPlayerPitch = originalPitch;
         hasOriginalAngles = true;
         lastTickTime = System.nanoTime();
      }

      enabled = true;
      phase = HeadOnlyLook.Phase.LOOKING;
      rotationMode = false;
      rotationEnabled = false;
      lastKeepAliveTime = System.currentTimeMillis();
      lastPlayerMoveTime = System.currentTimeMillis();
      setActive(currentYaw, currentPitch);
   }

   public static void startRotation(float yaw, float pitch) {
      startRotation(yaw, pitch, 900L);
   }

   public static void startRotation(float yaw, float pitch, long durationMs) {
      if (mc.f_91074_ != null) {
         watchdogMs = durationMs;
         if (!hasOriginalAngles) {
            originalYaw = mc.f_91074_.m_146908_();
            originalPitch = mc.f_91074_.m_146909_();
            currentYaw = originalYaw;
            currentPitch = originalPitch;
            lastPlayerYaw = originalYaw;
            lastPlayerPitch = originalPitch;
            hasOriginalAngles = true;
            lastTickTime = System.nanoTime();
         }

         rotationYaw = normalizeAngle(yaw);
         rotationPitch = Mth.m_14036_(pitch, -90.0F, 90.0F);
         targetYaw = rotationYaw;
         targetPitch = rotationPitch;
         rotationMode = true;
         rotationEnabled = true;
         enabled = true;
         phase = HeadOnlyLook.Phase.LOOKING;
         lastKeepAliveTime = System.currentTimeMillis();
         setActive(currentYaw, currentPitch);
      }
   }

   public static void updateRotation(float yaw, float pitch) {
      if (enabled) {
         rotationYaw = normalizeAngle(yaw);
         rotationPitch = Mth.m_14036_(pitch, -90.0F, 90.0F);
         targetYaw = rotationYaw;
         targetPitch = rotationPitch;
         rotationMode = true;
         rotationEnabled = true;
         lastKeepAliveTime = System.currentTimeMillis();
      }
   }

   public static void stopLooking() {
      if (enabled) {
         if (!hasOriginalAngles) {
            resetAll();
         } else {
            phase = HeadOnlyLook.Phase.REVERTING;
            targetYaw = originalYaw;
            targetPitch = originalPitch;
            rotationMode = false;
            rotationEnabled = false;
         }
      }
   }

   public static void stopRotation() {
      stopLooking();
   }

   public static void forceStop() {
      if (enabled && mc.f_91074_ != null && hasOriginalAngles) {
         mc.f_91074_.f_108617_.m_104955_(new ServerboundMovePlayerPacket.Rot(originalYaw, originalPitch, mc.f_91074_.m_20096_()));
      }

      resetAll();
   }

   private static void resetAll() {
      enabled = false;
      phase = HeadOnlyLook.Phase.IDLE;
      rotationMode = false;
      rotationEnabled = false;
      hasOriginalAngles = false;
      watchdogMs = 900L;
      lastPlayerYaw = 0.0F;
      lastPlayerPitch = 0.0F;
      lastPlayerMoveTime = 0L;
      lastTickTime = 0L;
      clearActive();
   }

   public static boolean isRotating() {
      return rotationMode && rotationEnabled && enabled;
   }

   public static boolean isLooking() {
      return enabled;
   }

   public static boolean isReverting() {
      return phase == HeadOnlyLook.Phase.REVERTING;
   }

   public static boolean hasReachedTarget() {
      return hasReachedTarget(2.5F);
   }

   public static boolean hasReachedTarget(float thresholdDeg) {
      if (enabled && phase == HeadOnlyLook.Phase.LOOKING) {
         float yawDiff = Math.abs(Mth.m_14177_(targetYaw - currentYaw));
         float pitchDiff = Math.abs(targetPitch - currentPitch);
         return yawDiff < thresholdDeg && pitchDiff < thresholdDeg;
      } else {
         return false;
      }
   }

   public static void onClientTick() {
      if (enabled && mc.f_91074_ != null) {
         long currentTime = System.nanoTime();
         float deltaTime = lastTickTime == 0L ? 0.016F : (float)(currentTime - lastTickTime) / 1.0E9F;
         lastTickTime = currentTime;
         deltaTime = Mth.m_14036_(deltaTime, 0.001F, 0.1F);
         float currentPlayerYaw = mc.f_91074_.m_146908_();
         float currentPlayerPitch = mc.f_91074_.m_146909_();
         float yawChange = Math.abs(Mth.m_14177_(currentPlayerYaw - lastPlayerYaw));
         float pitchChange = Math.abs(currentPlayerPitch - lastPlayerPitch);
         if (yawChange > 0.3F || pitchChange > 0.3F) {
            originalYaw = currentPlayerYaw;
            originalPitch = currentPlayerPitch;
            lastPlayerMoveTime = System.currentTimeMillis();
         }

         lastPlayerYaw = currentPlayerYaw;
         lastPlayerPitch = currentPlayerPitch;
         if (phase == HeadOnlyLook.Phase.LOOKING && watchdogMs > 0L && System.currentTimeMillis() - lastKeepAliveTime >= watchdogMs) {
            phase = HeadOnlyLook.Phase.REVERTING;
            targetYaw = originalYaw;
            targetPitch = originalPitch;
            rotationMode = false;
            rotationEnabled = false;
         }

         float yawDelta = Mth.m_14177_(targetYaw - currentYaw);
         float pitchDelta = targetPitch - currentPitch;
         float totalDistance = (float)Math.sqrt((double)(yawDelta * yawDelta + pitchDelta * pitchDelta));
         float speedMultiplier = 1.0F;
         float maxYawThisFrame;
         if (useAdaptiveSpeed && totalDistance > 0.1F) {
            maxYawThisFrame = Mth.m_14036_(totalDistance / 90.0F, 0.0F, 1.0F);
            speedMultiplier = minSpeedFactor + (maxSpeedFactor - minSpeedFactor) * maxYawThisFrame;
         }

         maxYawThisFrame = baseYawSpeed * speedMultiplier * deltaTime;
         float maxPitchThisFrame = basePitchSpeed * speedMultiplier * deltaTime;
         yawDelta *= smoothingFactor;
         pitchDelta *= smoothingFactor;
         yawDelta = Mth.m_14036_(yawDelta, -maxYawThisFrame, maxYawThisFrame);
         pitchDelta = Mth.m_14036_(pitchDelta, -maxPitchThisFrame, maxPitchThisFrame);
         float yawDiff;
         if (jitterAmount > 0.0F && phase == HeadOnlyLook.Phase.LOOKING && totalDistance > 5.0F) {
            yawDiff = Mth.m_14036_(totalDistance / 30.0F, 0.2F, 1.0F);
            yawDelta += (RNG.nextFloat() - 0.5F) * jitterAmount * yawDiff * deltaTime * 60.0F;
            pitchDelta += (RNG.nextFloat() - 0.5F) * jitterAmount * yawDiff * deltaTime * 60.0F;
         }

         if (useGcdSnap) {
            yawDiff = computeGcd();
            if (yawDiff > 1.0E-4F) {
               yawDelta = (float)Math.round(yawDelta / yawDiff) * yawDiff;
               pitchDelta = (float)Math.round(pitchDelta / yawDiff) * yawDiff;
            }
         }

         currentYaw = normalizeAngle(currentYaw + yawDelta);
         currentPitch = Mth.m_14036_(currentPitch + pitchDelta, -90.0F, 90.0F);
         mc.f_91074_.f_108617_.m_104955_(new ServerboundMovePlayerPacket.Rot(currentYaw, currentPitch, mc.f_91074_.m_20096_()));
         setActive(currentYaw, currentPitch);
         if (lockMovement && !rotationMode) {
            mc.f_91074_.m_146922_(currentYaw);
            mc.f_91074_.m_146926_(currentPitch);
         }

         if (phase == HeadOnlyLook.Phase.REVERTING) {
            targetYaw = originalYaw;
            targetPitch = originalPitch;
            yawDiff = Math.abs(Mth.m_14177_(targetYaw - currentYaw));
            float pitchDiff = Math.abs(targetPitch - currentPitch);
            if (yawDiff < 0.5F && pitchDiff < 0.5F) {
               resetAll();
            }
         }

      }
   }

   private static float computeGcd() {
      try {
         double sens = (Double)mc.f_91066_.m_231964_().m_231551_();
         double f = sens * 0.6 + 0.2;
         return (float)(f * f * f * 1.2);
      } catch (Throwable var4) {
         return 0.15F;
      }
   }

   public static float getServerYaw() {
      return currentYaw;
   }

   public static float getServerPitch() {
      return currentPitch;
   }

   public static float getTargetYaw() {
      return targetYaw;
   }

   public static float getTargetPitch() {
      return targetPitch;
   }

   public static float getCurrentYaw() {
      return currentYaw;
   }

   public static float getCurrentPitch() {
      return currentPitch;
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

   private static void setActive(float yaw, float pitch) {
      activeYaw = yaw;
      activePitch = pitch;
      active = true;
   }

   private static void clearActive() {
      active = false;
   }

   public static boolean isActivelyRotating() {
      return active;
   }

   public static void setHoldTicks(int ticks) {
   }

   public static int getHoldTicks() {
      return 5;
   }

   public static void setRotationSpeed(int speed) {
      baseYawSpeed = Mth.m_14036_((float)speed * 0.5F, 10.0F, 120.0F);
      basePitchSpeed = Mth.m_14036_((float)speed * 0.4F, 8.0F, 100.0F);
   }

   public static int getRotationSpeed() {
      return (int)(baseYawSpeed / 0.5F);
   }

   static {
      phase = HeadOnlyLook.Phase.IDLE;
      enabled = false;
      targetYaw = 0.0F;
      targetPitch = 0.0F;
      currentYaw = 0.0F;
      currentPitch = 0.0F;
      originalYaw = 0.0F;
      originalPitch = 0.0F;
      hasOriginalAngles = false;
      lastPlayerYaw = 0.0F;
      lastPlayerPitch = 0.0F;
      lastPlayerMoveTime = 0L;
      serverYaw = 0.0F;
      serverPitch = 0.0F;
      rotationMode = false;
      rotationEnabled = false;
      rotationYaw = 0.0F;
      rotationPitch = 0.0F;
      lockMovement = false;
      lockedYaw = 0.0F;
      lockedPitch = 0.0F;
      watchdogMs = 900L;
      lastKeepAliveTime = 0L;
      active = false;
      activeYaw = 0.0F;
      activePitch = 0.0F;
      baseYawSpeed = 75.0F;
      basePitchSpeed = 35.0F;
      minSpeedFactor = 3.2F;
      maxSpeedFactor = 7.1F;
      smoothingFactor = 0.5F;
      jitterAmount = 0.28F;
      useGcdSnap = true;
      useAdaptiveSpeed = true;
      lastTickTime = 0L;
   }

   private static enum Phase {
      IDLE,
      LOOKING,
      REVERTING;

      // $FF: synthetic method
      private static Phase[] $values() {
         return new Phase[]{IDLE, LOOKING, REVERTING};
      }
   }
}
