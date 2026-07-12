package lexis.Hack.Hackutil.music;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class DynamicIsland {
   private static long lastUpdateMs = 0L;
   private static float expandProgress = 1.0F;
   private static boolean expanded = true;
   private static long lastScrollDirChange = 0L;
   private static int scrollDirection = 1;
   private static float scrollPixelOffset = 0.0F;
   private static String lastLyric = "";
   private static final int SCROLL_SPEED = 12;
   private static final int PAUSE_MS = 1500;

   public static void render(GuiGraphics gfx) {
      if (MusicState.current != null) {
         Minecraft mc = Minecraft.m_91087_();
         if (!mc.f_91066_.f_92062_) {
            int sw = mc.m_91268_().m_85445_();
            long now = System.currentTimeMillis();
            if (lastUpdateMs == 0L) {
               lastUpdateMs = now;
            }

            float dt = (float)(now - lastUpdateMs) / 1000.0F;
            lastUpdateMs = now;
            float target = expanded ? 1.0F : 0.0F;
            expandProgress += (target - expandProgress) * Math.min(1.0F, dt * 8.0F);
            int w = (int)(140.0F + 80.0F * expandProgress);
            int h = (int)(24.0F + 22.0F * expandProgress);
            int x = (sw - w) / 2;
            int y = 4;
            gfx.m_280509_(x - 1, y - 1, x + w + 1, y + h + 1, Integer.MIN_VALUE);
            gfx.m_280509_(x, y, x + w, y + h, -435154928);
            gfx.m_280509_(x, y, x + w, y + 1, 1090519039);
            int coverSize = h - 4;
            int coverX = x + 2;
            int coverY = y + 2;
            if (MusicState.coverTexture != null) {
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               gfx.m_280163_(MusicState.coverTexture, coverX, coverY, 0.0F, 0.0F, coverSize, coverSize, coverSize, coverSize);
            }

            Font font = mc.f_91062_;
            int textX = coverX + coverSize + 5;
            int maxTextW = x + w - textX - 4;
            String name = MusicState.current.name;
            String var10000;
            if (font.m_92895_(name) > maxTextW) {
               var10000 = font.m_92834_(name, maxTextW - 6);
               name = var10000 + "..";
            }

            gfx.m_280056_(font, name, textX, y + 3, -1, false);
            String artist = "§7" + MusicState.current.artist;
            if (font.m_92895_(artist) > maxTextW) {
               var10000 = font.m_92834_(artist, maxTextW - 6);
               artist = var10000 + "..";
            }

            gfx.m_280056_(font, artist, textX, y + 13, -5592406, false);
            if (expandProgress > 0.4F) {
               float a = (expandProgress - 0.4F) / 0.6F;
               int alpha = (int)(255.0F * Math.min(1.0F, a));
               String lrc = MusicState.getCurrentLyric();
               int offsetInt;
               if (!lrc.isEmpty()) {
                  int lrcWidth = font.m_92895_(lrc);
                  if (lrcWidth <= maxTextW) {
                     lastLyric = "";
                     lastScrollDirChange = 0L;
                     scrollDirection = 1;
                     scrollPixelOffset = 0.0F;
                     gfx.m_280056_(font, lrc, textX, y + 24, alpha << 24 | 6737151, false);
                  } else {
                     if (!lrc.equals(lastLyric)) {
                        lastLyric = lrc;
                        lastScrollDirChange = now;
                        scrollDirection = 1;
                        scrollPixelOffset = 0.0F;
                     }

                     int totalScrollDist = lrcWidth - maxTextW;
                     if (lastScrollDirChange == 0L) {
                        lastScrollDirChange = now;
                        scrollDirection = 1;
                        scrollPixelOffset = 0.0F;
                     }

                     if (scrollDirection == 1 && scrollPixelOffset >= (float)totalScrollDist) {
                        scrollDirection = -1;
                        lastScrollDirChange = now;
                     } else if (scrollDirection == -1 && scrollPixelOffset <= 0.0F) {
                        scrollDirection = 1;
                        lastScrollDirChange = now;
                     }

                     long elapsed = now - lastScrollDirChange;
                     if (elapsed > 1500L) {
                        float moveDelta = (float)((elapsed - 1500L) * 12L) / 1000.0F;
                        if (scrollDirection == 1) {
                           scrollPixelOffset = Math.min((float)totalScrollDist, scrollPixelOffset + moveDelta);
                        } else {
                           scrollPixelOffset = Math.max(0.0F, scrollPixelOffset - moveDelta);
                        }
                     }

                     offsetInt = (int)scrollPixelOffset;
                     gfx.m_280588_(textX, y + 24, textX + maxTextW, y + 34);
                     gfx.m_280056_(font, lrc, textX - offsetInt, y + 24, alpha << 24 | 6737151, false);
                     gfx.m_280618_();
                  }
               } else {
                  lastLyric = "";
                  lastScrollDirChange = 0L;
                  scrollDirection = 1;
                  scrollPixelOffset = 0.0F;
               }

               long cur = MusicPlayer.getCurrentMs();
               long tot = MusicPlayer.getTotalMs();
               var10000 = formatTime(cur);
               String timeText = var10000 + "/" + formatTime(tot);
               offsetInt = font.m_92895_(timeText);
               gfx.m_280056_(font, timeText, x + w - offsetInt - 4, y + 3, alpha << 24 | 13421772, false);
               int barY = y + h - 4;
               gfx.m_280509_(textX, barY, textX + maxTextW, barY + 2, alpha << 24 | 3158064);
               if (tot > 0L) {
                  int fill = (int)((double)((long)maxTextW * cur) / (double)tot);
                  if (fill > 0) {
                     gfx.m_280509_(textX, barY, textX + fill, barY + 2, alpha << 24 | '\uaaff');
                  }
               }
            }

         }
      }
   }

   private static String formatTime(long ms) {
      if (ms <= 0L) {
         return "0:00";
      } else {
         long total = ms / 1000L;
         long m = total / 60L;
         long s = total % 60L;
         return "" + m + ":" + (s < 10L ? "0" : "") + s;
      }
   }

   public static void toggle() {
      expanded = !expanded;
   }
}
