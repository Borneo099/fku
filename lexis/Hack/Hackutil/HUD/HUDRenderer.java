package lexis.Hack.Hackutil.HUD;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Lexis.HUDSettingsHack;
import lexis.Hack.Hacks.Lexis.NotificationHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Hackutil.NotificationManager;
import lexis.Hack.Hackutil.config.ToggleHacksConfig;
import lexis.Hack.Utils.Colors.SettingColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

public class HUDRenderer {
   private static final Minecraft mc = Minecraft.m_91087_();
   private static boolean enabled = true;
   private static ArrayList topHacks = new ArrayList();
   private static ArrayList middleHacks = new ArrayList();
   private static ArrayList bottomHacks = new ArrayList();
   private static boolean hasJoinedServer = false;
   private static int bgColor = -2013265920;
   private static int topY = 10;
   private static int middleY = 10;
   private static int bottomY = 10;
   private static final int ANIMATION_MAX_OFFSET = 50;
   private static final float ANIMATION_SPEED = 0.12F;
   private static long startTime = System.currentTimeMillis();
   private static boolean isInitialized = false;

   public static void init() {
      if (!isInitialized) {
         isInitialized = true;
         (new Thread(() -> {
            try {
               Thread.sleep(1000L);
               Minecraft.m_91087_().execute(() -> {
                  loadAllEnabledHacks();
               });
            } catch (InterruptedException var1) {
               var1.printStackTrace();
            }

         })).start();
      }
   }

   private static NotificationHack getNotificationHack() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var0.hasNext()) {
            return null;
         }

         hack = (Hack)var0.next();
      } while(!(hack instanceof NotificationHack));

      return (NotificationHack)hack;
   }

   private static void loadAllEnabledHacks() {
      topHacks.clear();
      middleHacks.clear();
      bottomHacks.clear();
      ToggleHacksConfig toggleConfig = ToggleHacksConfig.getInstance();
      Map toggleStates = toggleConfig.getAllStates();
      Iterator var2 = toggleStates.entrySet().iterator();

      while(true) {
         while(true) {
            Map.Entry entry;
            do {
               if (!var2.hasNext()) {
                  return;
               }

               entry = (Map.Entry)var2.next();
            } while(!(Boolean)entry.getValue());

            String hackName = (String)entry.getKey();
            Iterator var5 = HackManager.getInstance().getHacks().iterator();

            while(var5.hasNext()) {
               Hack hack = (Hack)var5.next();
               if (hack.getName().equals(hackName)) {
                  onHackToggle(hack);
                  break;
               }
            }
         }
      }
   }

   public static void render(GuiGraphics gui, float partialTicks) {
      HUDSettingsHack hudHack = getHUDSettingsHack();
      if (hudHack != null && hudHack.isEnabled()) {
         if (!mc.f_91066_.f_92063_) {
            if (enabled && mc.f_91074_ != null) {
               updateAnimations();
               topHacks.removeIf((entry) -> {
                  return !entry.hack.isEnabled() && entry.progress <= 0.0F;
               });
               middleHacks.removeIf((entry) -> {
                  return !entry.hack.isEnabled() && entry.progress <= 0.0F;
               });
               bottomHacks.removeIf((entry) -> {
                  return !entry.hack.isEnabled() && entry.progress <= 0.0F;
               });
               int screenWidth = mc.m_91268_().m_85445_();
               int screenHeight = mc.m_91268_().m_85446_();
               topY = 10;
               middleY = screenHeight / 2 - getMiddleTotalHeight() / 2;
               bottomY = screenHeight - 10 - getBottomTotalHeight();
               bgColor = hudHack.getBgColor().getPacked();
               refreshCachedNames(topHacks);
               refreshCachedNames(middleHacks);
               refreshCachedNames(bottomHacks);
               renderMergedBackground(gui, screenWidth);
               renderAreaText(gui, topHacks, screenWidth, topY, partialTicks, hudHack);
               renderAreaText(gui, middleHacks, screenWidth, middleY, partialTicks, hudHack);
               renderAreaText(gui, bottomHacks, screenWidth, bottomY, partialTicks, hudHack);
            }
         }
      }
   }

   private static void refreshCachedNames(ArrayList hacks) {
      HackEntry entry;
      for(Iterator var1 = hacks.iterator(); var1.hasNext(); entry.cachedName = entry.hack.getDisplayName()) {
         entry = (HackEntry)var1.next();
      }

   }

   private static void renderMergedBackground(GuiGraphics gui, int screenWidth) {
      ArrayList rects = new ArrayList();
      collectBackgroundRects(rects, topHacks, screenWidth, topY);
      collectBackgroundRects(rects, middleHacks, screenWidth, middleY);
      collectBackgroundRects(rects, bottomHacks, screenWidth, bottomY);
      ArrayList merged = mergeRects(rects);
      Iterator var4 = merged.iterator();

      while(var4.hasNext()) {
         BackgroundRect rect = (BackgroundRect)var4.next();
         int bgColorWithAlpha = bgColor;
         int bgAlpha = bgColor >> 24 & 255;
         int animAlpha = (int)((float)bgAlpha * rect.progress);
         int finalColor = bgColor & 16777215 | animAlpha << 24;
         gui.m_280509_(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, finalColor);
      }

   }

   private static void collectBackgroundRects(ArrayList rects, ArrayList hacks, int screenWidth, int startY) {
      int currentY = startY;

      for(Iterator var5 = hacks.iterator(); var5.hasNext(); currentY += 12) {
         HackEntry entry = (HackEntry)var5.next();
         String name = entry.cachedName;
         int textWidth = mc.f_91062_.m_92895_(name);
         float x;
         if (entry.hack.isEnabled()) {
            x = (float)(screenWidth - textWidth - 5) - 50.0F * (1.0F - entry.progress);
         } else {
            x = (float)(screenWidth - textWidth - 5) + 50.0F * (1.0F - entry.progress);
         }

         rects.add(new BackgroundRect((int)x - 2, currentY - 1, screenWidth - (int)x + 2, 10, entry.progress));
      }

   }

   private static ArrayList mergeRects(ArrayList rects) {
      if (rects.isEmpty()) {
         return rects;
      } else {
         rects.sort((a, b) -> {
            return Integer.compare(a.y, b.y);
         });
         ArrayList merged = new ArrayList();
         BackgroundRect current = (BackgroundRect)rects.get(0);

         for(int i = 1; i < rects.size(); ++i) {
            BackgroundRect next = (BackgroundRect)rects.get(i);
            if (next.y <= current.y + current.height + 1) {
               current.height = Math.max(current.height, next.y + next.height - current.y);
               current.progress = Math.max(current.progress, next.progress);
            } else {
               merged.add(current);
               current = next;
            }
         }

         merged.add(current);
         return merged;
      }
   }

   public static void onJoinServer() {
      if (!hasJoinedServer) {
         hasJoinedServer = true;
         loadAllEnabledHacks();
      }
   }

   private static void renderAreaText(GuiGraphics gui, ArrayList hacks, int screenWidth, int startY, float partialTicks, HUDSettingsHack hudHack) {
      int currentY = startY;

      for(Iterator var7 = hacks.iterator(); var7.hasNext(); currentY += 12) {
         HackEntry entry = (HackEntry)var7.next();
         renderText(gui, entry, screenWidth, currentY, partialTicks, hudHack);
      }

   }

   private static void renderText(GuiGraphics gui, HackEntry entry, int screenWidth, int y, float partialTicks, HUDSettingsHack hudHack) {
      String name = entry.cachedName;
      boolean hasColorCode = name.contains("§");
      int textWidth;
      float x;
      if (hasColorCode) {
         textWidth = mc.f_91062_.m_92895_(name);
         if (hudHack == null) {
            return;
         }

         if (entry.hack.isEnabled()) {
            x = (float)(screenWidth - textWidth - 5) - 50.0F * (1.0F - entry.progress);
         } else {
            x = (float)(screenWidth - textWidth - 5) + 50.0F * (1.0F - entry.progress);
         }

         int color = -1;
         if (name.contains("§a")) {
            color = -11141291;
         } else if (name.contains("§b")) {
            color = -11141121;
         } else if (name.contains("§c")) {
            color = -43691;
         } else if (name.contains("§d")) {
            color = -43521;
         } else if (name.contains("§e")) {
            color = -171;
         } else if (name.contains("§f")) {
            color = -1;
         } else if (name.contains("§0")) {
            color = -16777216;
         } else if (name.contains("§1")) {
            color = -16777046;
         } else if (name.contains("§2")) {
            color = -16733696;
         } else if (name.contains("§3")) {
            color = -16733526;
         } else if (name.contains("§4")) {
            color = -5636096;
         } else if (name.contains("§5")) {
            color = -5635926;
         } else if (name.contains("§6")) {
            color = -22016;
         } else if (name.contains("§7")) {
            color = -5592406;
         } else if (name.contains("§8")) {
            color = -11184811;
         } else if (name.contains("§9")) {
            color = -11184641;
         }

         gui.m_280056_(mc.f_91062_, name, (int)x, y, color, false);
      } else {
         if (hudHack == null) {
            return;
         }

         textWidth = mc.f_91062_.m_92895_(name);
         if (entry.hack.isEnabled()) {
            x = (float)(screenWidth - textWidth - 5) - 50.0F * (1.0F - entry.progress);
         } else {
            x = (float)(screenWidth - textWidth - 5) + 50.0F * (1.0F - entry.progress);
         }

         long time = System.currentTimeMillis() - startTime;
         SettingColor startCol = hudHack.getTextStartColor();
         SettingColor endCol = hudHack.getTextEndColor();
         int hashSeed = entry.hashCode();
         MutableComponent line = Component.m_237119_();

         for(int i = 0; i < name.length(); ++i) {
            float charWave = (float)Math.sin((double)time * 0.005 + (double)i * 0.5 + (double)hashSeed);
            float charGray = 0.3F + 0.7F * ((charWave + 1.0F) / 2.0F);
            int r = (int)((float)startCol.r * (1.0F - charGray) + (float)endCol.r * charGray);
            int g = (int)((float)startCol.g * (1.0F - charGray) + (float)endCol.g * charGray);
            int b = (int)((float)startCol.b * (1.0F - charGray) + (float)endCol.b * charGray);
            int rgb = r << 16 | g << 8 | b;
            line.m_7220_(Component.m_237113_(String.valueOf(name.charAt(i))).m_6270_(Style.f_131099_.m_131148_(TextColor.m_131266_(rgb))));
         }

         FormattedCharSequence seq = line.m_7532_();
         gui.m_280649_(mc.f_91062_, seq, (int)x, y, -1, false);
      }

   }

   private static int findHackIndex(Hack hack) {
      int index = 0;

      for(Iterator var2 = topHacks.iterator(); var2.hasNext(); ++index) {
         HackEntry entry = (HackEntry)var2.next();
         if (entry.hack == hack) {
            return index;
         }
      }

      return 0;
   }

   private static int getTotalActiveCount() {
      return topHacks.size();
   }

   private static HUDSettingsHack getHUDSettingsHack() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var0.hasNext()) {
            return null;
         }

         hack = (Hack)var0.next();
      } while(!(hack instanceof HUDSettingsHack));

      return (HUDSettingsHack)hack;
   }

   private static void updateAnimations() {
      updateAreaAnimations(topHacks);
      updateAreaAnimations(middleHacks);
      updateAreaAnimations(bottomHacks);
   }

   private static void updateAreaAnimations(ArrayList hacks) {
      Iterator var1 = hacks.iterator();

      while(true) {
         while(var1.hasNext()) {
            HackEntry entry = (HackEntry)var1.next();
            boolean enabled = entry.hack.isEnabled();
            if (enabled && entry.progress < 1.0F) {
               entry.progress += 0.12F;
               if (entry.progress > 1.0F) {
                  entry.progress = 1.0F;
               }
            } else if (!enabled && entry.progress > 0.0F) {
               entry.progress -= 0.12F;
               if (entry.progress < 0.0F) {
                  entry.progress = 0.0F;
               }
            }
         }

         return;
      }
   }

   private static int getMiddleTotalHeight() {
      return middleHacks.size() * 12;
   }

   private static int getBottomTotalHeight() {
      return bottomHacks.size() * 12;
   }

   public static void onHackToggle(Hack hack) {
      onHackToggle(hack, HUDRenderer.HudArea.TOP);
      NotificationHack notification = getNotificationHack();
      if (notification != null && notification.isEnabled()) {
         String status = hack.isEnabled() ? "§7[§a+§7]" : "§7[§c-§7]";
         String title = hack.isEnabled() ? "功能切换" : "功能切换";
         String message = " " + title + " §b[§6" + hack.getName() + "§b] §f" + status;
         if (notification.getMode() == NotificationHack.NotificationMode.CHAT) {
            Minecraft mc = Minecraft.m_91087_();
            if (mc.f_91074_ != null) {
               int startColor = -2461482;
               int endColor = -38476;
               String lex = "[Lexis]";
               MutableComponent component = Component.m_237113_("");

               for(int i = 0; i < lex.length(); ++i) {
                  float ratio = (float)i / (float)(lex.length() - 1);
                  int r = (int)((float)(startColor >> 16 & 255) * (1.0F - ratio) + (float)(endColor >> 16 & 255) * ratio);
                  int g = (int)((float)(startColor >> 8 & 255) * (1.0F - ratio) + (float)(endColor >> 8 & 255) * ratio);
                  int b = (int)((float)(startColor & 255) * (1.0F - ratio) + (float)(endColor & 255) * ratio);
                  int color = -16777216 | r << 16 | g << 8 | b;
                  MutableComponent charComp = Component.m_237113_(String.valueOf(lex.charAt(i))).m_130948_(Style.f_131099_.m_131148_(TextColor.m_131266_(color)));
                  component.m_7220_(charComp);
               }

               component.m_7220_(Component.m_237113_(message));
               mc.f_91074_.m_5661_(component, false);
            }
         } else {
            NotificationManager.show(title, message, hack.isEnabled() ? NotificationManager.NotificationType.SUCCESS : NotificationManager.NotificationType.INFO, 1);
         }
      }

   }

   public static void onHackToggle(Hack hack, HudArea area) {
      ArrayList targetArea = getAreaList(area);
      HackEntry existing = findEntry(hack, targetArea);
      if (existing != null) {
         if (hack.isEnabled()) {
            existing.progress = 0.0F;
         }
      } else if (hack.isEnabled()) {
         targetArea.add(new HackEntry(hack));
         sortArea(targetArea);
      }

   }

   private static ArrayList getAreaList(HudArea area) {
      switch (area) {
         case TOP:
            return topHacks;
         case MIDDLE:
            return middleHacks;
         case BOTTOM:
            return bottomHacks;
         default:
            return topHacks;
      }
   }

   private static HackEntry findEntry(Hack hack, ArrayList area) {
      Iterator var2 = area.iterator();

      HackEntry entry;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         entry = (HackEntry)var2.next();
      } while(entry.hack != hack);

      return entry;
   }

   private static void sortArea(ArrayList area) {
      area.sort((a, b) -> {
         String nameA = a.hack.getDisplayName();
         String nameB = b.hack.getDisplayName();
         int lenCompare = Integer.compare(nameB.length(), nameA.length());
         return lenCompare != 0 ? lenCompare : nameA.compareTo(nameB);
      });
   }

   public static void refreshSorting() {
      sortArea(topHacks);
      sortArea(middleHacks);
      sortArea(bottomHacks);
   }

   public static void setEnabled(boolean enabled) {
      if (HUDRenderer.enabled != enabled) {
         HUDRenderer.enabled = enabled;
         if (enabled) {
            loadAllEnabledHacks();
         } else {
            topHacks.clear();
            middleHacks.clear();
            bottomHacks.clear();
         }

      }
   }

   public static boolean isEnabled() {
      return enabled;
   }

   private static int interpolateColor(SettingColor start, SettingColor end, float progress) {
      int r = (int)((float)start.r + (float)(end.r - start.r) * progress);
      int g = (int)((float)start.g + (float)(end.g - start.g) * progress);
      int b = (int)((float)start.b + (float)(end.b - start.b) * progress);
      return r << 16 | g << 8 | b;
   }

   private static class HackEntry {
      private final Hack hack;
      private float progress;
      private String cachedName = "";

      public HackEntry(Hack hack) {
         this.hack = hack;
         this.progress = 0.0F;
         this.cachedName = hack.getDisplayName();
      }
   }

   private static class BackgroundRect {
      int x;
      int y;
      int width;
      int height;
      float progress;

      BackgroundRect(int x, int y, int width, int height, float progress) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.progress = progress;
      }
   }

   public static enum HudArea {
      TOP,
      MIDDLE,
      BOTTOM;

      // $FF: synthetic method
      private static HudArea[] $values() {
         return new HudArea[]{TOP, MIDDLE, BOTTOM};
      }
   }
}
