package lexis.Hack.Hackutil.HUD;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Utils.TaczBridge;
import lexis.Hack.Utils.ThemeColors.ThemeColors;
import lexis.Hack.Utils.ThemeColors.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class TabGui {
   private static final Minecraft mc = Minecraft.m_91087_();
   private final List tabs = new ArrayList();
   private int selectedTab = 0;
   private boolean tabOpened = false;
   private int tabWidth = 80;
   private int tabHeight = 0;
   private boolean initialized = false;
   private float openProgress = 0.0F;
   private float targetProgress = 0.0F;
   private long lastUpdateTime = System.currentTimeMillis();
   private static final float ANIMATION_SPEED = 8.0F;
   private final int[] gradientColors = new int[]{-2461482, -2252579, -1146130, -18751, -38476};

   public TabGui() {
      this.init();
   }

   private void init() {
      if (mc.f_91062_ != null) {
         this.tabs.clear();
         LinkedHashMap tabMap = new LinkedHashMap();
         Hack.Category[] var2 = Hack.Category.values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Hack.Category category = var2[var4];
            if (category != Hack.Category.TACZ_SERVER || TaczBridge.isAvailable()) {
               tabMap.put(category, new Tab(category.displayName));
            }
         }

         Iterator var6 = HackManager.getInstance().getHacks().iterator();

         while(var6.hasNext()) {
            Hack hack = (Hack)var6.next();
            if (hack.getCategory() != null) {
               Tab tab = (Tab)tabMap.get(hack.getCategory());
               if (tab != null) {
                  tab.addHack(hack);
               }
            }
         }

         this.tabs.addAll(tabMap.values());
         var6 = this.tabs.iterator();

         while(var6.hasNext()) {
            Tab tab = (Tab)var6.next();
            tab.updateSize();
         }

         this.updateSize();
         this.initialized = true;
      }
   }

   private void updateSize() {
      if (mc.f_91062_ != null) {
         this.tabWidth = 80;
         Iterator var1 = this.tabs.iterator();

         while(var1.hasNext()) {
            Tab tab = (Tab)var1.next();
            int nameWidth = mc.f_91062_.m_92895_(tab.name) + 20;
            if (nameWidth > this.tabWidth) {
               this.tabWidth = nameWidth;
            }
         }

         this.tabHeight = this.tabs.size() * 18;
      }
   }

   private void updateAnimation() {
      long currentTime = System.currentTimeMillis();
      float deltaTime = (float)(currentTime - this.lastUpdateTime) / 1000.0F;
      this.lastUpdateTime = currentTime;
      if (Math.abs(this.openProgress - this.targetProgress) > 0.001F) {
         float smoothing = Math.min(1.0F, 8.0F * deltaTime);
         this.openProgress += (this.targetProgress - this.openProgress) * smoothing;
      } else {
         this.openProgress = this.targetProgress;
      }

   }

   public void handleKeyPress(int keyCode, int action) {
      if (!this.initialized) {
         this.init();
      }

      if (action == 1) {
         if (this.tabOpened) {
            switch (keyCode) {
               case 263:
                  this.tabOpened = false;
                  this.targetProgress = 0.0F;
                  break;
               default:
                  if (this.selectedTab >= 0 && this.selectedTab < this.tabs.size()) {
                     ((Tab)this.tabs.get(this.selectedTab)).handleKeyPress(keyCode);
                  }
            }
         } else {
            switch (keyCode) {
               case 262:
                  this.tabOpened = true;
                  this.targetProgress = 1.0F;
               case 263:
               default:
                  break;
               case 264:
                  this.selectedTab = (this.selectedTab + 1) % this.tabs.size();
                  break;
               case 265:
                  this.selectedTab = (this.selectedTab - 1 + this.tabs.size()) % this.tabs.size();
            }
         }

      }
   }

   public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
      if (!this.initialized) {
         this.init();
      }

      if (mc.f_91062_ != null && !this.tabs.isEmpty()) {
         this.updateAnimation();
         ThemeColors colors = ThemeManager.getColors();
         int x = 5;
         int y = 30;
         gui.m_280509_(x, y, x + this.tabWidth, y + this.tabHeight, colors.windowBackground.getPacked());
         this.drawFlowingGradientBorder(gui, x, y, this.tabWidth, this.tabHeight);
         int textY = y + 5;

         for(int i = 0; i < this.tabs.size(); ++i) {
            Tab tab = (Tab)this.tabs.get(i);
            if (i == this.selectedTab) {
               gui.m_280509_(x + 2, textY - 2, x + this.tabWidth - 2, textY + 10, colors.buttonHovered.getPacked());
               gui.m_280509_(x, textY - 2, x + 2, textY + 10, colors.buttonOn.getPacked());
            }

            gui.m_280488_(mc.f_91062_, tab.name, x + 5, textY, colors.titleText.getPacked());
            textY += 18;
         }

         if (this.selectedTab >= 0 && this.selectedTab < this.tabs.size()) {
            Tab currentTab = (Tab)this.tabs.get(this.selectedTab);
            int subX = x + this.tabWidth + 2;
            int alpha = (int)(255.0F * this.openProgress);
            int subTextY;
            int i;
            Hack hack;
            String displayName;
            int nameColor;
            if (alpha > 5) {
               gui.m_280509_(subX, y, subX + currentTab.width, y + currentTab.height, alpha << 24 | colors.windowBackground.getPacked() & 16777215);
               this.drawFlowingGradientBorderWithAlpha(gui, subX, y, currentTab.width, currentTab.height, alpha);
               subTextY = y + 5;

               for(i = 0; i < currentTab.hacks.size(); ++i) {
                  hack = (Hack)currentTab.hacks.get(i);
                  if (i == currentTab.selectedFeature) {
                     gui.m_280509_(subX + 2, subTextY - 2, subX + currentTab.width - 2, subTextY + 10, alpha << 24 | colors.buttonHovered.getPacked() & 16777215);
                     gui.m_280509_(subX, subTextY - 2, subX + 2, subTextY + 10, alpha << 24 | colors.buttonOn.getPacked() & 16777215);
                  }

                  displayName = hack.getName();
                  nameColor = hack.isEnabled() ? colors.buttonOn.getPacked() : colors.titleText.getPacked();
                  nameColor = alpha << 24 | nameColor & 16777215;
                  gui.m_280488_(mc.f_91062_, displayName, subX + 5, subTextY, nameColor);
                  subTextY += 18;
               }
            } else if (this.tabOpened) {
               gui.m_280509_(subX, y, subX + currentTab.width, y + currentTab.height, colors.windowBackground.getPacked());
               this.drawFlowingGradientBorder(gui, subX, y, currentTab.width, currentTab.height);
               subTextY = y + 5;

               for(i = 0; i < currentTab.hacks.size(); ++i) {
                  hack = (Hack)currentTab.hacks.get(i);
                  if (i == currentTab.selectedFeature) {
                     gui.m_280509_(subX + 2, subTextY - 2, subX + currentTab.width - 2, subTextY + 10, colors.buttonHovered.getPacked());
                     gui.m_280509_(subX, subTextY - 2, subX + 2, subTextY + 10, colors.buttonOn.getPacked());
                  }

                  displayName = hack.getName();
                  nameColor = hack.isEnabled() ? colors.buttonOn.getPacked() : colors.titleText.getPacked();
                  gui.m_280488_(mc.f_91062_, displayName, subX + 5, subTextY, nameColor);
                  subTextY += 18;
               }
            }
         }

      }
   }

   private void drawFlowingGradientBorderWithAlpha(GuiGraphics gui, int x, int y, int width, int height, int alpha) {
      long time = System.currentTimeMillis();
      float offset = (float)(time % 3000L) / 3000.0F;

      int i;
      float progress;
      int colorIndex;
      float blend;
      int color;
      for(i = 0; i < width; ++i) {
         progress = ((float)i / (float)width + offset) % 1.0F;
         colorIndex = (int)(progress * (float)(this.gradientColors.length - 1));
         blend = progress * (float)(this.gradientColors.length - 1) - (float)colorIndex;
         if (colorIndex < this.gradientColors.length - 1) {
            color = this.interpolateColor(this.gradientColors[colorIndex], this.gradientColors[colorIndex + 1], blend);
         } else {
            color = this.interpolateColor(this.gradientColors[colorIndex], this.gradientColors[0], blend);
         }

         color = alpha << 24 | color & 16777215;
         gui.m_280509_(x + i, y, x + i + 1, y + 1, color);
      }

      for(i = 0; i < width; ++i) {
         progress = (1.0F - (float)i / (float)width + offset) % 1.0F;
         colorIndex = (int)(progress * (float)(this.gradientColors.length - 1));
         blend = progress * (float)(this.gradientColors.length - 1) - (float)colorIndex;
         if (colorIndex < this.gradientColors.length - 1) {
            color = this.interpolateColor(this.gradientColors[colorIndex], this.gradientColors[colorIndex + 1], blend);
         } else {
            color = this.interpolateColor(this.gradientColors[colorIndex], this.gradientColors[0], blend);
         }

         color = alpha << 24 | color & 16777215;
         gui.m_280509_(x + i, y + height - 1, x + i + 1, y + height, color);
      }

      for(i = 0; i < height; ++i) {
         progress = ((float)i / (float)height + offset) % 1.0F;
         colorIndex = (int)(progress * (float)(this.gradientColors.length - 1));
         blend = progress * (float)(this.gradientColors.length - 1) - (float)colorIndex;
         if (colorIndex < this.gradientColors.length - 1) {
            color = this.interpolateColor(this.gradientColors[colorIndex], this.gradientColors[colorIndex + 1], blend);
         } else {
            color = this.interpolateColor(this.gradientColors[colorIndex], this.gradientColors[0], blend);
         }

         color = alpha << 24 | color & 16777215;
         gui.m_280509_(x, y + i, x + 1, y + i + 1, color);
         gui.m_280509_(x + width - 1, y + i, x + width, y + i + 1, color);
      }

   }

   private void drawFlowingGradientBorder(GuiGraphics gui, int x, int y, int width, int height) {
      long time = System.currentTimeMillis();
      float offset = (float)(time % 3000L) / 3000.0F;

      int i;
      float progress;
      int colorIndex;
      float blend;
      int color;
      for(i = 0; i < width; ++i) {
         progress = ((float)i / (float)width + offset) % 1.0F;
         colorIndex = (int)(progress * (float)(this.gradientColors.length - 1));
         blend = progress * (float)(this.gradientColors.length - 1) - (float)colorIndex;
         if (colorIndex < this.gradientColors.length - 1) {
            color = this.interpolateColor(this.gradientColors[colorIndex], this.gradientColors[colorIndex + 1], blend);
         } else {
            color = this.interpolateColor(this.gradientColors[colorIndex], this.gradientColors[0], blend);
         }

         gui.m_280509_(x + i, y, x + i + 1, y + 1, color);
      }

      for(i = 0; i < width; ++i) {
         progress = (1.0F - (float)i / (float)width + offset) % 1.0F;
         colorIndex = (int)(progress * (float)(this.gradientColors.length - 1));
         blend = progress * (float)(this.gradientColors.length - 1) - (float)colorIndex;
         if (colorIndex < this.gradientColors.length - 1) {
            color = this.interpolateColor(this.gradientColors[colorIndex], this.gradientColors[colorIndex + 1], blend);
         } else {
            color = this.interpolateColor(this.gradientColors[colorIndex], this.gradientColors[0], blend);
         }

         gui.m_280509_(x + i, y + height - 1, x + i + 1, y + height, color);
      }

      for(i = 0; i < height; ++i) {
         progress = ((float)i / (float)height + offset) % 1.0F;
         colorIndex = (int)(progress * (float)(this.gradientColors.length - 1));
         blend = progress * (float)(this.gradientColors.length - 1) - (float)colorIndex;
         if (colorIndex < this.gradientColors.length - 1) {
            color = this.interpolateColor(this.gradientColors[colorIndex], this.gradientColors[colorIndex + 1], blend);
         } else {
            color = this.interpolateColor(this.gradientColors[colorIndex], this.gradientColors[0], blend);
         }

         gui.m_280509_(x, y + i, x + 1, y + i + 1, color);
         gui.m_280509_(x + width - 1, y + i, x + width, y + i + 1, color);
      }

   }

   private int interpolateColor(int color1, int color2, float progress) {
      int r1 = color1 >> 16 & 255;
      int g1 = color1 >> 8 & 255;
      int b1 = color1 & 255;
      int r2 = color2 >> 16 & 255;
      int g2 = color2 >> 8 & 255;
      int b2 = color2 & 255;
      int r = (int)((float)r1 + (float)(r2 - r1) * progress);
      int g = (int)((float)g1 + (float)(g2 - g1) * progress);
      int b = (int)((float)b1 + (float)(b2 - b1) * progress);
      return -16777216 | r << 16 | g << 8 | b;
   }

   private class Tab {
      private final String name;
      private final List hacks = new ArrayList();
      private int selectedFeature = 0;
      private int width = 80;
      private int height = 0;

      public Tab(String name) {
         this.name = name;
      }

      public void addHack(Hack hack) {
         this.hacks.add(hack);
      }

      public void updateSize() {
         if (TabGui.mc.f_91062_ != null) {
            this.width = 80;
            Iterator var1 = this.hacks.iterator();

            while(var1.hasNext()) {
               Hack hack = (Hack)var1.next();
               int nameWidth = TabGui.mc.f_91062_.m_92895_(hack.getName()) + 20;
               if (nameWidth > this.width) {
                  this.width = nameWidth;
               }
            }

            this.height = this.hacks.size() * 18 + 5;
         }
      }

      public void handleKeyPress(int keyCode) {
         switch (keyCode) {
            case 257:
            case 335:
               if (this.selectedFeature >= 0 && this.selectedFeature < this.hacks.size()) {
                  Hack hack = (Hack)this.hacks.get(this.selectedFeature);
                  if (hack.isToggleable()) {
                     hack.toggle();
                  } else {
                     hack.onClick();
                  }
               }
               break;
            case 264:
               this.selectedFeature = (this.selectedFeature + 1) % this.hacks.size();
               break;
            case 265:
               this.selectedFeature = (this.selectedFeature - 1 + this.hacks.size()) % this.hacks.size();
         }

      }
   }
}
