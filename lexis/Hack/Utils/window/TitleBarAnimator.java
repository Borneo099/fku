package lexis.Hack.Utils.window;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class TitleBarAnimator {
   private static boolean running = false;
   private static Thread thread;
   private static final int[] PURPLE_PINK = new int[]{11894492, 13073919, 14723839, 16757726, 16751052, 16761056, 14723839, 13073919};

   public static void start() {
      if (!running) {
         running = true;
         thread = new Thread(() -> {
            float barT = 0.0F;
            float textHue = 0.0F;

            while(running) {
               int barColor = sampleGradient(PURPLE_PINK, barT);
               int rainbowText = hsvToRgb(textHue, 1.0F, 1.0F);
               WindowColorUtil.setCaptionColor(barColor);
               WindowColorUtil.setTextColor(rainbowText);
               WindowColorUtil.setBorderColor(brighten(barColor, 1.15F));
               Minecraft mc = Minecraft.m_91087_();
               if (mc.m_91094_() != null) {
                  String name = mc.m_91094_().m_92546_();
                  GLFW.glfwSetWindowTitle(mc.m_91268_().m_85439_(), "Minecraft 1.20.1 *Forge | You username: " + name);
               }

               barT += 0.005F;
               if (barT >= 1.0F) {
                  --barT;
               }

               ++textHue;
               if (textHue >= 360.0F) {
                  textHue -= 360.0F;
               }

               try {
                  Thread.sleep(33L);
               } catch (InterruptedException var6) {
                  break;
               }
            }

         }, "Lexis-TitleBar-Animator");
         thread.setDaemon(true);
         thread.start();
      }
   }

   public static void stop() {
      running = false;
      if (thread != null) {
         thread.interrupt();
      }

   }

   private static int sampleGradient(int[] palette, float t) {
      float scaled = t * (float)palette.length;
      int idx = (int)scaled;
      float frac = scaled - (float)idx;
      int c1 = palette[idx % palette.length];
      int c2 = palette[(idx + 1) % palette.length];
      return lerpColor(c1, c2, frac);
   }

   private static int lerpColor(int c1, int c2, float t) {
      int r1 = c1 >> 16 & 255;
      int g1 = c1 >> 8 & 255;
      int b1 = c1 & 255;
      int r2 = c2 >> 16 & 255;
      int g2 = c2 >> 8 & 255;
      int b2 = c2 & 255;
      return (int)((float)r1 + (float)(r2 - r1) * t) << 16 | (int)((float)g1 + (float)(g2 - g1) * t) << 8 | (int)((float)b1 + (float)(b2 - b1) * t);
   }

   private static int brighten(int rgb, float factor) {
      int r = Math.min(255, (int)((float)(rgb >> 16 & 255) * factor));
      int g = Math.min(255, (int)((float)(rgb >> 8 & 255) * factor));
      int b = Math.min(255, (int)((float)(rgb & 255) * factor));
      return r << 16 | g << 8 | b;
   }

   private static int hsvToRgb(float h, float s, float v) {
      float c = v * s;
      float x = c * (1.0F - Math.abs(h / 60.0F % 2.0F - 1.0F));
      float m = v - c;
      float r;
      float g;
      float b;
      if (h < 60.0F) {
         r = c;
         g = x;
         b = 0.0F;
      } else if (h < 120.0F) {
         r = x;
         g = c;
         b = 0.0F;
      } else if (h < 180.0F) {
         r = 0.0F;
         g = c;
         b = x;
      } else if (h < 240.0F) {
         r = 0.0F;
         g = x;
         b = c;
      } else if (h < 300.0F) {
         r = x;
         g = 0.0F;
         b = c;
      } else {
         r = c;
         g = 0.0F;
         b = x;
      }

      return (int)((r + m) * 255.0F) << 16 | (int)((g + m) * 255.0F) << 8 | (int)((b + m) * 255.0F);
   }
}
