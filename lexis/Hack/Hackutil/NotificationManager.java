package lexis.Hack.Hackutil;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Lexis.NotificationHack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class NotificationManager {
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final List notifications = new CopyOnWriteArrayList();
   private static final Map notificationCache = new ConcurrentHashMap();
   private static final long CACHE_TIMEOUT = 3000L;
   private static final int NOTIFICATION_WIDTH = 220;
   private static final int NOTIFICATION_HEIGHT = 60;
   private static final int RIGHT_MARGIN = 10;
   private static final int BOTTOM_MARGIN = 10;
   private static final int SPACING = 10;
   private static final long SLIDE_DURATION = 400L;
   private static final int DEFAULT_DURATION = 3;
   private static long joinServerTime = 0L;
   private static final int[] GRADIENT_COLORS = new int[]{-2461482, -2252579, -1146130, -18751, -38476};

   public static void onJoinServer() {
      joinServerTime = System.currentTimeMillis();
   }

   private static NotificationHack getNotificationHack() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var0.hasNext()) {
            return null;
         }

         hack = (Hack)var0.next();
      } while(!(hack instanceof NotificationHack) || !hack.isEnabled());

      return (NotificationHack)hack;
   }

   private static MutableComponent buildGradientLexis() {
      int startColor = -2461482;
      int endColor = -38476;
      String lex = "[Lexis]";
      MutableComponent result = Component.m_237113_("");

      for(int i = 0; i < lex.length(); ++i) {
         float ratio = (float)i / (float)(lex.length() - 1);
         int r = (int)((float)(startColor >> 16 & 255) * (1.0F - ratio) + (float)(endColor >> 16 & 255) * ratio);
         int g = (int)((float)(startColor >> 8 & 255) * (1.0F - ratio) + (float)(endColor >> 8 & 255) * ratio);
         int b = (int)((float)(startColor & 255) * (1.0F - ratio) + (float)(endColor & 255) * ratio);
         int color = -16777216 | r << 16 | g << 8 | b;
         MutableComponent charComp = Component.m_237113_(String.valueOf(lex.charAt(i))).m_130948_(Style.f_131099_.m_131148_(TextColor.m_131266_(color)));
         result.m_7220_(charComp);
      }

      return result;
   }

   public static void show(String title, String message, NotificationType type) {
      show(title, message, type, 3);
   }

   public static void show(String title, String message, NotificationType type, int seconds) {
      if (System.currentTimeMillis() - joinServerTime >= 1000L) {
         NotificationHack notificationHack = getNotificationHack();
         String prefix;
         if (notificationHack != null && notificationHack.isEnabled() && notificationHack.getMode() == NotificationHack.NotificationMode.CHAT) {
            if (mc.f_91074_ != null) {
               switch (type) {
                  case SUCCESS:
                     prefix = "§a";
                     break;
                  case WARNING:
                     prefix = "§e";
                     break;
                  case ERROR:
                     prefix = "§c";
                     break;
                  default:
                     prefix = "§f";
               }

               MutableComponent comp = buildGradientLexis();
               comp.m_7220_(Component.m_237113_(" " + prefix + title + " " + message));
               mc.f_91074_.m_5661_(comp, false);
            }

         } else {
            prefix = title + "|" + message + "|" + type.name();
            long now = System.currentTimeMillis();
            NotificationCount cached = (NotificationCount)notificationCache.get(prefix);
            if (cached != null && now - cached.lastTime < 3000L) {
               ++cached.count;
               cached.lastTime = now;
               if (cached.notification != null) {
                  cached.notification.count = cached.count;
                  cached.notification.message = message + " [x" + cached.count + "]";
                  cached.notification.endTime = now + (long)seconds * 1000L;
                  cached.notification.slideProgress = 0.0F;
                  cached.notification.fadeProgress = 1.0F;
                  cached.notification.slidingOut = false;
               }
            } else {
               Notification notif = new Notification(title, message, type, seconds);
               notifications.add(notif);
               NotificationCount nc = new NotificationCount();
               nc.notification = notif;
               nc.lastTime = now;
               notificationCache.put(prefix, nc);
            }

            notificationCache.entrySet().removeIf((entry) -> {
               return now - ((NotificationCount)entry.getValue()).lastTime > 6000L;
            });
         }
      }
   }

   public static void info(String title, String message) {
      show(title, message, NotificationManager.NotificationType.INFO, 3);
   }

   public static void info(String title, String message, int seconds) {
      show(title, message, NotificationManager.NotificationType.INFO, seconds);
   }

   public static void success(String title, String message) {
      show(title, message, NotificationManager.NotificationType.SUCCESS, 3);
   }

   public static void success(String title, String message, int seconds) {
      show(title, message, NotificationManager.NotificationType.SUCCESS, seconds);
   }

   public static void warning(String title, String message) {
      show(title, message, NotificationManager.NotificationType.WARNING, 3);
   }

   public static void warning(String title, String message, int seconds) {
      show(title, message, NotificationManager.NotificationType.WARNING, seconds);
   }

   public static void error(String title, String message) {
      show(title, message, NotificationManager.NotificationType.ERROR, 3);
   }

   public static void error(String title, String message, int seconds) {
      show(title, message, NotificationManager.NotificationType.ERROR, seconds);
   }

   @SubscribeEvent
   public static void onRenderGui(RenderGuiEvent.Post event) {
      if (!notifications.isEmpty() && mc.f_91074_ != null) {
         GuiGraphics gui = event.getGuiGraphics();
         int screenWidth = mc.m_91268_().m_85445_();
         int screenHeight = mc.m_91268_().m_85446_();
         long currentTime = System.currentTimeMillis();
         List toRemove = new ArrayList();
         int baseY = screenHeight - 10 - 60;

         Notification notif;
         for(int i = notifications.size() - 1; i >= 0; --i) {
            notif = (Notification)notifications.get(i);
            notif.targetY = (float)(baseY - (notifications.size() - 1 - i) * 70);
         }

         Iterator var14 = notifications.iterator();

         while(true) {
            while(var14.hasNext()) {
               notif = (Notification)var14.next();
               long slideElapsed;
               if (!notif.slidingOut) {
                  slideElapsed = currentTime - notif.startTime;
                  if (slideElapsed < 400L) {
                     notif.slideProgress = easeOutElastic((float)slideElapsed / 400.0F);
                     notif.fadeProgress = Math.min(1.0F, (float)slideElapsed / 200.0F);
                  } else if (currentTime >= notif.endTime) {
                     notif.slidingOut = true;
                     notif.slideStartTime = currentTime;
                  } else {
                     notif.slideProgress = 1.0F;
                     notif.fadeProgress = 1.0F;
                  }
               }

               if (notif.slidingOut) {
                  slideElapsed = currentTime - notif.slideStartTime;
                  if (slideElapsed >= 400L) {
                     toRemove.add(notif);
                     continue;
                  }

                  notif.slideProgress = 1.0F - easeInCubic((float)slideElapsed / 400.0F);
                  notif.fadeProgress = 1.0F - easeInCubic((float)slideElapsed / 400.0F);
               }

               if (Math.abs(notif.currentY - notif.targetY) > 0.5F) {
                  notif.currentY += (notif.targetY - notif.currentY) * 0.15F;
               } else {
                  notif.currentY = notif.targetY;
               }
            }

            var14 = notifications.iterator();

            while(var14.hasNext()) {
               notif = (Notification)var14.next();
               if (!toRemove.contains(notif)) {
                  int baseX = screenWidth - 10 - 220;
                  int x;
                  if (notif.slidingOut) {
                     x = (int)((float)baseX + 220.0F * (1.0F - notif.slideProgress));
                  } else {
                     x = (int)((float)screenWidth - 220.0F * notif.slideProgress);
                  }

                  int y = (int)notif.currentY;
                  int alpha = (int)(255.0F * notif.fadeProgress);
                  renderNotification(gui, notif, x, y, alpha, currentTime);
               }
            }

            notifications.removeAll(toRemove);
            if (notifications.isEmpty()) {
               notificationCache.clear();
            }

            return;
         }
      }
   }

   private static void renderNotification(GuiGraphics gui, Notification notif, int x, int y, int alpha, long currentTime) {
      Color bgColor = new Color(20, 20, 30, Math.min(230, alpha));
      Color titleColor = new Color(255, 255, 255, alpha);
      Color textColor = new Color(200, 200, 220, alpha);
      Color accentColor = new Color(notif.type.color.getRed(), notif.type.color.getGreen(), notif.type.color.getBlue(), alpha);
      int radius = 8;
      gui.m_280509_(x + radius, y, x + 220 - radius, y + 60, bgColor.getRGB());
      gui.m_280509_(x, y + radius, x + radius, y + 60 - radius, bgColor.getRGB());
      gui.m_280509_(x + 220 - radius, y + radius, x + 220, y + 60 - radius, bgColor.getRGB());
      gui.m_280509_(x + radius, y + 60 - radius, x + 220 - radius, y + 60, bgColor.getRGB());
      gui.m_280509_(x, y, x + radius, y + radius, bgColor.getRGB());
      gui.m_280509_(x + 220 - radius, y, x + 220, y + radius, bgColor.getRGB());
      gui.m_280509_(x, y + 60 - radius, x + radius, y + 60, bgColor.getRGB());
      gui.m_280509_(x + 220 - radius, y + 60 - radius, x + 220, y + 60, bgColor.getRGB());
      gui.m_280509_(x, y + 2, x + 3, y + 60 - 2, accentColor.getRGB());
      drawFlowingGradientBorder(gui, x, y, 220, 60);
      String displayTitle = notif.title;
      if (notif.count > 1) {
         displayTitle = notif.title + " §7[x" + notif.count + "]";
      }

      gui.m_280056_(mc.f_91062_, displayTitle, x + 12, y + 8, titleColor.getRGB(), false);
      List wrappedLines = wrapText(notif.message, 196);

      for(int i = 0; i < wrappedLines.size() && i < 2; ++i) {
         gui.m_280056_(mc.f_91062_, (String)wrappedLines.get(i), x + 12, y + 22 + i * 12, textColor.getRGB(), false);
      }

      if (wrappedLines.size() > 2) {
         gui.m_280056_(mc.f_91062_, "...", x + 12, y + 34, textColor.getRGB(), false);
      }

      long totalTime = notif.endTime - notif.startTime;
      long remaining = notif.endTime - currentTime;
      float progress = (float)remaining / (float)totalTime;
      progress = Math.max(0.0F, Math.min(1.0F, progress));
      int barWidth = (int)(212.0F * progress);
      if (barWidth > 0 && !notif.slidingOut) {
         for(int i = 0; i < barWidth; ++i) {
            float p = (float)i / (float)barWidth;
            int r = (int)((float)accentColor.getRed() * (1.0F - p) + 255.0F * p);
            int g = (int)((float)accentColor.getGreen() * (1.0F - p) + 255.0F * p);
            int b = (int)((float)accentColor.getBlue() * (1.0F - p) + 255.0F * p);
            int barColor = -16777216 | r << 16 | g << 8 | b;
            gui.m_280509_(x + 4 + i, y + 60 - 2, x + 4 + i + 1, y + 60, barColor);
         }
      }

   }

   private static void drawFlowingGradientBorder(GuiGraphics gui, int x, int y, int width, int height) {
      long time = System.currentTimeMillis();
      float offset = (float)(time % 3000L) / 3000.0F;

      int i;
      float progress;
      int color;
      for(i = 0; i < width; ++i) {
         progress = ((float)i / (float)width + offset) % 1.0F;
         color = interpolateGradient(progress);
         gui.m_280509_(x + i, y, x + i + 1, y + 1, color);
      }

      for(i = 0; i < width; ++i) {
         progress = (1.0F - (float)i / (float)width + offset) % 1.0F;
         color = interpolateGradient(progress);
         gui.m_280509_(x + i, y + height - 1, x + i + 1, y + height, color);
      }

      for(i = 0; i < height; ++i) {
         progress = ((float)i / (float)height + offset) % 1.0F;
         color = interpolateGradient(progress);
         gui.m_280509_(x, y + i, x + 1, y + i + 1, color);
      }

      for(i = 0; i < height; ++i) {
         progress = (1.0F - (float)i / (float)height + offset) % 1.0F;
         color = interpolateGradient(progress);
         gui.m_280509_(x + width - 1, y + i, x + width, y + i + 1, color);
      }

   }

   private static int interpolateGradient(float progress) {
      int index = (int)(progress * (float)(GRADIENT_COLORS.length - 1));
      float blend = progress * (float)(GRADIENT_COLORS.length - 1) - (float)index;
      if (index >= GRADIENT_COLORS.length - 1) {
         return GRADIENT_COLORS[GRADIENT_COLORS.length - 1];
      } else {
         int c1 = GRADIENT_COLORS[index];
         int c2 = GRADIENT_COLORS[index + 1];
         int r = (int)((float)(c1 >> 16 & 255) * (1.0F - blend) + (float)(c2 >> 16 & 255) * blend);
         int g = (int)((float)(c1 >> 8 & 255) * (1.0F - blend) + (float)(c2 >> 8 & 255) * blend);
         int b = (int)((float)(c1 & 255) * (1.0F - blend) + (float)(c2 & 255) * blend);
         return -16777216 | r << 16 | g << 8 | b;
      }
   }

   private static List wrapText(String text, int maxWidth) {
      List lines = new ArrayList();
      if (text != null && !text.isEmpty()) {
         StringBuilder currentLine = new StringBuilder();
         String[] words = text.split(" ");
         String[] var5 = words;
         int var6 = words.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            String word = var5[var7];
            String testLine = currentLine.length() == 0 ? word : String.valueOf(currentLine) + " " + word;
            if (mc.f_91062_.m_92895_(testLine) <= maxWidth) {
               currentLine.append(currentLine.length() == 0 ? word : " " + word);
            } else if (currentLine.length() > 0) {
               lines.add(currentLine.toString());
               currentLine = new StringBuilder(word);
            } else {
               lines.add(word);
               currentLine = new StringBuilder();
            }
         }

         if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
         }

         return lines;
      } else {
         lines.add("");
         return lines;
      }
   }

   private static float easeOutElastic(float x) {
      if (x == 0.0F) {
         return 0.0F;
      } else if (x == 1.0F) {
         return 1.0F;
      } else {
         float p = 0.3F;
         float s = p / 4.0F;
         return (float)(Math.pow(2.0, (double)(-10.0F * x)) * Math.sin((double)(x - s) * 6.283185307179586 / (double)p) + 1.0);
      }
   }

   private static float easeInCubic(float x) {
      return x * x * x;
   }

   public static enum NotificationType {
      INFO(new Color(70, 130, 200)),
      SUCCESS(new Color(80, 170, 100)),
      WARNING(new Color(230, 180, 40)),
      ERROR(new Color(220, 70, 70));

      public final Color color;

      private NotificationType(Color color) {
         this.color = color;
      }

      // $FF: synthetic method
      private static NotificationType[] $values() {
         return new NotificationType[]{INFO, SUCCESS, WARNING, ERROR};
      }
   }

   private static class NotificationCount {
      int count = 1;
      long lastTime = System.currentTimeMillis();
      Notification notification;
   }

   public static class Notification {
      public final String title;
      public String message;
      public final NotificationType type;
      public final long startTime;
      public long endTime;
      public int count = 1;
      public float slideProgress = 0.0F;
      public float fadeProgress = 0.0F;
      public boolean slidingOut = false;
      public long slideStartTime;
      public float targetY = 0.0F;
      public float currentY = 0.0F;

      public Notification(String title, String message, NotificationType type, int seconds) {
         this.title = title;
         this.message = message;
         this.type = type;
         this.startTime = System.currentTimeMillis();
         this.endTime = this.startTime + (long)seconds * 1000L;
      }

      public void incrementCount() {
         ++this.count;
         String var10001 = this.message.replaceAll(" \\[x\\d+\\]$", "");
         this.message = var10001 + " [x" + this.count + "]";
      }
   }
}
