package lexis.Gui;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class GuiAnimator {
   private static final Minecraft MC = Minecraft.m_91087_();
   private static boolean enabled = false;
   private static int mode = 0;
   private static double speed = 1.0;
   private static double intensity = 1.0;
   private static float cachedScale = 1.0F;
   private static float cachedAlpha = 1.0F;
   private static float cachedOffsetX = 0.0F;
   private static float cachedOffsetY = 0.0F;
   private static float cachedRotation = 0.0F;
   private static long openTime = 0L;
   private static boolean isAnimating = false;
   private static boolean wasScreenOpen = false;
   private static final long BASE_DURATION = 250L;

   public static void setEnabled(boolean e) {
      enabled = e;
   }

   public static void setMode(int m) {
      mode = m;
   }

   public static void setSpeed(double s) {
      speed = s;
   }

   public static void setIntensity(double i) {
      intensity = i;
   }

   public static void onGuiOpen() {
      if (enabled) {
         openTime = System.currentTimeMillis();
         isAnimating = true;
         cachedScale = 1.0F;
         cachedAlpha = 1.0F;
         cachedOffsetX = 0.0F;
         cachedOffsetY = 0.0F;
         cachedRotation = 0.0F;
      }
   }

   public static void onGuiClose() {
      isAnimating = false;
      cachedScale = 1.0F;
      cachedAlpha = 1.0F;
      cachedOffsetX = 0.0F;
      cachedOffsetY = 0.0F;
      cachedRotation = 0.0F;
   }

   public static float getScale() {
      if (enabled && isAnimating && mode != 13) {
         updateAnimation();
         return cachedScale;
      } else {
         return 1.0F;
      }
   }

   public static float getAlpha() {
      if (enabled && isAnimating && mode != 13) {
         updateAnimation();
         return cachedAlpha;
      } else {
         return 1.0F;
      }
   }

   public static float getOffsetX() {
      if (enabled && isAnimating) {
         updateAnimation();
         return cachedOffsetX;
      } else {
         return 0.0F;
      }
   }

   public static float getOffsetY() {
      if (enabled && isAnimating) {
         updateAnimation();
         return cachedOffsetY;
      } else {
         return 0.0F;
      }
   }

   public static float getRotation() {
      if (enabled && isAnimating) {
         updateAnimation();
         return cachedRotation;
      } else {
         return 0.0F;
      }
   }

   private static void updateAnimation() {
      if (enabled && isAnimating && mode != 13) {
         long elapsed = System.currentTimeMillis() - openTime;
         long duration = (long)(250.0 / speed);
         if (elapsed >= duration) {
            isAnimating = false;
            cachedScale = 1.0F;
            cachedAlpha = 1.0F;
            cachedOffsetX = 0.0F;
            cachedOffsetY = 0.0F;
            cachedRotation = 0.0F;
         } else {
            float progress = (float)elapsed / (float)duration;
            float eased = easeOutCubic(progress);
            float intensityF = (float)intensity;
            switch (mode) {
               case 0:
                  cachedScale = bounce(progress);
                  cachedAlpha = 1.0F;
                  cachedOffsetX = 0.0F;
                  cachedOffsetY = 0.0F;
                  cachedRotation = 0.0F;
                  break;
               case 1:
                  cachedScale = 0.9F + 0.1F * eased;
                  cachedAlpha = 1.0F;
                  break;
               case 2:
                  cachedScale = 0.1F + 0.9F * easeOutBack(progress);
                  cachedAlpha = 1.0F;
                  break;
               case 3:
                  cachedScale = 1.0F;
                  cachedAlpha = eased;
                  break;
               case 4:
                  cachedScale = 0.8F + 0.2F * eased;
                  cachedRotation = 360.0F * (1.0F - eased) * intensityF;
                  cachedAlpha = 1.0F;
                  break;
               case 5:
                  cachedScale = 1.5F - 0.5F * eased;
                  cachedAlpha = eased;
                  break;
               case 6:
                  cachedScale = 1.0F;
                  cachedOffsetY = 100.0F * (1.0F - eased) * intensityF;
                  cachedAlpha = eased;
                  break;
               case 7:
                  cachedScale = 1.0F;
                  cachedOffsetY = -100.0F * (1.0F - eased) * intensityF;
                  cachedAlpha = eased;
                  break;
               case 8:
                  cachedScale = 1.0F;
                  cachedOffsetX = 100.0F * (1.0F - eased) * intensityF;
                  cachedAlpha = eased;
                  break;
               case 9:
                  cachedScale = 1.0F;
                  cachedOffsetX = -100.0F * (1.0F - eased) * intensityF;
                  cachedAlpha = eased;
                  break;
               case 10:
                  cachedScale = 0.5F + 0.5F * eased;
                  cachedRotation = 360.0F * (1.0F - eased) * intensityF;
                  cachedAlpha = eased;
                  break;
               case 11:
                  cachedScale = 0.1F + 0.9F * eased;
                  cachedRotation = 180.0F * (1.0F - eased) * intensityF;
                  cachedAlpha = 1.0F;
                  break;
               case 12:
                  float shake = (float)Math.sin((double)(progress * 30.0F)) * 0.05F * (1.0F - progress) * intensityF;
                  cachedOffsetX = shake * 100.0F;
                  cachedScale = 1.1F - 0.1F * eased;
                  cachedAlpha = 1.0F;
            }

         }
      }
   }

   private static float bounce(float t) {
      return t < 0.5F ? 0.8F + 0.6F * easeOutQuad(t * 2.0F) : 1.4F - 0.4F * easeOutQuad((t - 0.5F) * 2.0F);
   }

   private static float easeOutQuad(float x) {
      return x * (2.0F - x);
   }

   private static float easeOutCubic(float x) {
      return 1.0F - (float)Math.pow((double)(1.0F - x), 3.0);
   }

   private static float easeOutBack(float x) {
      float c1 = 1.70158F;
      float c3 = c1 + 1.0F;
      return 1.0F + c3 * (float)Math.pow((double)(x - 1.0F), 3.0) + c1 * (float)Math.pow((double)(x - 1.0F), 2.0);
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         boolean isScreenOpen = MC.f_91080_ != null;
         if (wasScreenOpen && !isScreenOpen) {
            onGuiClose();
         }

         wasScreenOpen = isScreenOpen;
         if (enabled && isAnimating && isScreenOpen) {
            MC.f_91080_.m_6575_(MC, MC.m_91268_().m_85445_(), MC.m_91268_().m_85446_());
         }
      }

   }
}
