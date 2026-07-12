package lexis.Hack.Utils.ThemeColors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.HackGui;
import lexis.Hack.Utils.Colors.SettingColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ThemeSettingsScreen extends Screen {
   private final Screen parent;
   private ThemeColors themeColors;
   private ThemeColors workingColors;
   private String currentEditing = "";
   private int tempRed;
   private int tempGreen;
   private int tempBlue;
   private int tempAlpha;
   private boolean draggingRed = false;
   private boolean draggingGreen = false;
   private boolean draggingBlue = false;
   private boolean draggingAlpha = false;
   private int windowX;
   private int windowY;
   private int windowWidth = 700;
   private int windowHeight = 500;
   private boolean dragging = false;
   private int dragX;
   private int dragY;
   private boolean dragHandleHovered = false;
   private int scrollOffset = 0;
   private int maxScroll = 0;
   private static final int ITEM_HEIGHT = 40;
   private int rSliderY;
   private int gSliderY;
   private int bSliderY;
   private int aSliderY;
   private int leftX;
   private List colorItems = new ArrayList();

   public ThemeSettingsScreen(Screen parent) {
      super(Component.m_237113_("主题设置"));
      this.parent = parent;
      this.themeColors = ThemeManager.getColors();
      this.workingColors = new ThemeColors();
      this.copyColors(this.themeColors, this.workingColors);
      this.initColorItems();
   }

   private void copyColors(ThemeColors src, ThemeColors dst) {
      dst.windowBackground = new SettingColor(src.windowBackground);
      dst.titleBackground = new SettingColor(src.titleBackground);
      dst.titleBackgroundHovered = new SettingColor(src.titleBackgroundHovered);
      dst.titleText = new SettingColor(src.titleText);
      dst.buttonOn = new SettingColor(src.buttonOn);
      dst.buttonOff = new SettingColor(src.buttonOff);
      dst.buttonHovered = new SettingColor(src.buttonHovered);
      dst.buttonText = new SettingColor(src.buttonText);
      dst.keyText = new SettingColor(src.keyText);
      dst.scrollbarBg = new SettingColor(src.scrollbarBg);
      dst.scrollbarKnob = new SettingColor(src.scrollbarKnob);
      dst.scrollbarKnobHovered = new SettingColor(src.scrollbarKnobHovered);
      dst.tooltipBg = new SettingColor(src.tooltipBg);
      dst.tooltipBorder = new SettingColor(src.tooltipBorder);
      dst.tooltipText = new SettingColor(src.tooltipText);
   }

   private void initColorItems() {
      this.colorItems.add(new ColorItem("titleText", "标题文字"));
      this.colorItems.add(new ColorItem("buttonOn", "按钮开启"));
      this.colorItems.add(new ColorItem("buttonOff", "按钮关闭"));
      this.colorItems.add(new ColorItem("buttonHovered", "按钮悬停"));
      this.colorItems.add(new ColorItem("buttonText", "按钮文字"));
      this.colorItems.add(new ColorItem("keyText", "按键文字"));
      this.colorItems.add(new ColorItem("tooltipBg", "提示框背景"));
      this.colorItems.add(new ColorItem("tooltipBorder", "提示框边框"));
      this.colorItems.add(new ColorItem("tooltipText", "提示框文字"));
   }

   protected void m_7856_() {
      super.m_7856_();
      this.windowX = (this.f_96543_ - this.windowWidth) / 2;
      this.windowY = (this.f_96544_ - this.windowHeight) / 2;
      this.maxScroll = Math.max(0, this.colorItems.size() * 40 - (this.windowHeight - 150));
   }

   public void m_88315_(GuiGraphics gui, int mouseX, int mouseY, float delta) {
      gui.m_280509_(0, 0, this.f_96543_, this.f_96544_, -2013265920);

      int titleColor;
      for(titleColor = 1; titleColor <= 4; ++titleColor) {
         int alpha = 10 * titleColor;
         gui.m_280509_(this.windowX - titleColor, this.windowY - titleColor, this.windowX + this.windowWidth + titleColor, this.windowY + this.windowHeight + titleColor, alpha << 24 | 0);
      }

      gui.m_280509_(this.windowX, this.windowY, this.windowX + this.windowWidth, this.windowY + this.windowHeight, -13816518);
      this.dragHandleHovered = mouseX >= this.windowX && mouseX <= this.windowX + this.windowWidth && mouseY >= this.windowY && mouseY <= this.windowY + 30;
      titleColor = this.dragHandleHovered ? -11711142 : -12763830;
      gui.m_280509_(this.windowX, this.windowY, this.windowX + this.windowWidth, this.windowY + 30, titleColor);
      gui.m_280488_(this.f_96547_, "§l主题设置", this.windowX + 12, this.windowY + 8, -1);
      boolean closeHovered = mouseX >= this.windowX + this.windowWidth - 25 && mouseX <= this.windowX + this.windowWidth - 10 && mouseY >= this.windowY + 5 && mouseY <= this.windowY + 25;
      gui.m_280488_(this.f_96547_, "✕", this.windowX + this.windowWidth - 18, this.windowY + 8, closeHovered ? -43691 : -5592406);
      this.leftX = this.windowX + 20;
      int leftY = this.windowY + 45;
      int leftWidth = 200;
      gui.m_280509_(this.leftX, leftY, this.leftX + leftWidth, this.windowY + this.windowHeight - 60, 1144206131);
      int index = 0;

      int y;
      boolean hovered;
      for(Iterator var10 = this.colorItems.iterator(); var10.hasNext(); ++index) {
         ColorItem item = (ColorItem)var10.next();
         y = leftY + 5 + index * 40 - this.scrollOffset;
         if (y >= leftY - 40 && y <= this.windowY + this.windowHeight - 70) {
            hovered = mouseX >= this.leftX + 5 && mouseX <= this.leftX + leftWidth - 5 && mouseY >= y && mouseY <= y + 40 - 10;
            boolean selected = item.name.equals(this.currentEditing);
            int bgColor;
            if (selected) {
               bgColor = -2008199846;
            } else if (hovered) {
               bgColor = 1714631475;
            } else {
               bgColor = 0;
            }

            if (bgColor != 0) {
               gui.m_280509_(this.leftX + 5, y, this.leftX + leftWidth - 5, y + 40 - 10, bgColor);
            }

            SettingColor color = this.getColorByName(item.name);
            gui.m_280509_(this.leftX + 10, y + 5, this.leftX + 30, y + 40 - 15, color.getPacked());
            gui.m_280488_(this.f_96547_, item.displayName, this.leftX + 40, y + 5, 16777215);
         }
      }

      int btnY;
      if (!this.currentEditing.isEmpty()) {
         btnY = this.leftX + leftWidth + 40;
         int baseY = this.windowY + 60;
         SettingColor color = this.getColorByName(this.currentEditing);
         gui.m_280488_(this.f_96547_, "编辑: " + this.getDisplayName(this.currentEditing), btnY, baseY, 16777130);
         baseY += 30;
         gui.m_280509_(btnY, baseY, btnY + 200, baseY + 40, color.getPacked());
         baseY += 50;
         this.rSliderY = baseY;
         this.gSliderY = baseY + 45;
         this.bSliderY = baseY + 90;
         this.aSliderY = baseY + 135;
         this.drawColorSlider(gui, btnY, this.rSliderY, "R", this.tempRed, this.draggingRed, mouseX, mouseY);
         this.drawColorSlider(gui, btnY, this.gSliderY, "G", this.tempGreen, this.draggingGreen, mouseX, mouseY);
         this.drawColorSlider(gui, btnY, this.bSliderY, "B", this.tempBlue, this.draggingBlue, mouseX, mouseY);
         this.drawColorSlider(gui, btnY, this.aSliderY, "A", this.tempAlpha, this.draggingAlpha, mouseX, mouseY);
      }

      btnY = this.windowY + this.windowHeight - 30;
      boolean saveHovered = mouseX >= this.windowX + 250 && mouseX <= this.windowX + 330 && mouseY >= btnY && mouseY <= btnY + 20;
      y = saveHovered ? -11184811 : -13421773;
      gui.m_280509_(this.windowX + 250, btnY, this.windowX + 330, btnY + 20, y);
      gui.m_280137_(this.f_96547_, "保存", this.windowX + 290, btnY + 5, 16777215);
      hovered = mouseX >= this.windowX + 350 && mouseX <= this.windowX + 430 && mouseY >= btnY && mouseY <= btnY + 20;
      int cancelBg = hovered ? -11184811 : -13421773;
      gui.m_280509_(this.windowX + 350, btnY, this.windowX + 430, btnY + 20, cancelBg);
      gui.m_280137_(this.f_96547_, "取消", this.windowX + 390, btnY + 5, 16777215);
      boolean resetHovered = mouseX >= this.windowX + 450 && mouseX <= this.windowX + 530 && mouseY >= btnY && mouseY <= btnY + 20;
      int resetBg = resetHovered ? -11184811 : -13421773;
      gui.m_280509_(this.windowX + 450, btnY, this.windowX + 530, btnY + 20, resetBg);
      gui.m_280137_(this.f_96547_, "重置", this.windowX + 490, btnY + 5, 16777215);
      gui.m_280509_(this.windowX, this.windowY, this.windowX + this.windowWidth, this.windowY + 1, -11711142);
      gui.m_280509_(this.windowX, this.windowY + this.windowHeight - 1, this.windowX + this.windowWidth, this.windowY + this.windowHeight, -11711142);
      gui.m_280509_(this.windowX, this.windowY, this.windowX + 1, this.windowY + this.windowHeight, -11711142);
      gui.m_280509_(this.windowX + this.windowWidth - 1, this.windowY, this.windowX + this.windowWidth, this.windowY + this.windowHeight, -11711142);
   }

   private void drawColorSlider(GuiGraphics gui, int x, int y, String label, int value, boolean dragging, int mouseX, int mouseY) {
      gui.m_280488_(this.f_96547_, label + ": " + value, x, y, 16777215);
      gui.m_280509_(x, y + 12, x + 200, y + 22, -13421773);
      int progressWidth = value * 200 / 255;
      gui.m_280509_(x, y + 12, x + progressWidth, y + 22, -11711142);
      int indicatorX = x + progressWidth;
      boolean hovered = mouseX >= indicatorX - 4 && mouseX <= indicatorX + 4 && mouseY >= y + 10 && mouseY <= y + 24;
      int knobColor;
      if (dragging) {
         knobColor = -1;
      } else if (hovered) {
         knobColor = -3355444;
      } else {
         knobColor = -5592406;
      }

      gui.m_280509_(indicatorX - 2, y + 10, indicatorX + 3, y + 24, knobColor);
   }

   private SettingColor getColorByName(String name) {
      switch (name) {
         case "windowBackground":
            return this.workingColors.windowBackground;
         case "titleBackground":
            return this.workingColors.titleBackground;
         case "titleBackgroundHovered":
            return this.workingColors.titleBackgroundHovered;
         case "titleText":
            return this.workingColors.titleText;
         case "buttonOn":
            return this.workingColors.buttonOn;
         case "buttonOff":
            return this.workingColors.buttonOff;
         case "buttonHovered":
            return this.workingColors.buttonHovered;
         case "buttonText":
            return this.workingColors.buttonText;
         case "keyText":
            return this.workingColors.keyText;
         case "scrollbarBg":
            return this.workingColors.scrollbarBg;
         case "scrollbarKnob":
            return this.workingColors.scrollbarKnob;
         case "scrollbarKnobHovered":
            return this.workingColors.scrollbarKnobHovered;
         case "tooltipBg":
            return this.workingColors.tooltipBg;
         case "tooltipBorder":
            return this.workingColors.tooltipBorder;
         case "tooltipText":
            return this.workingColors.tooltipText;
         default:
            return null;
      }
   }

   private String getDisplayName(String name) {
      Iterator var2 = this.colorItems.iterator();

      ColorItem item;
      do {
         if (!var2.hasNext()) {
            return name;
         }

         item = (ColorItem)var2.next();
      } while(!item.name.equals(name));

      return item.displayName;
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (mouseX >= (double)(this.windowX + this.windowWidth - 25) && mouseX <= (double)(this.windowX + this.windowWidth - 10) && mouseY >= (double)(this.windowY + 5) && mouseY <= (double)(this.windowY + 25)) {
         Minecraft.m_91087_().m_91152_(new HackGui());
         return true;
      } else if (button == 0 && mouseX >= (double)this.windowX && mouseX <= (double)(this.windowX + this.windowWidth) && mouseY >= (double)this.windowY && mouseY <= (double)(this.windowY + 30)) {
         this.dragging = true;
         this.dragX = (int)(mouseX - (double)this.windowX);
         this.dragY = (int)(mouseY - (double)this.windowY);
         return true;
      } else {
         int leftY = this.windowY + 45;
         int leftWidth = 200;
         int index = 0;

         for(Iterator var9 = this.colorItems.iterator(); var9.hasNext(); ++index) {
            ColorItem item = (ColorItem)var9.next();
            int y = leftY + 5 + index * 40 - this.scrollOffset;
            if (mouseX >= (double)(this.leftX + 5) && mouseX <= (double)(this.leftX + leftWidth - 5) && mouseY >= (double)y && mouseY <= (double)(y + 40 - 10)) {
               this.currentEditing = item.name;
               SettingColor color = this.getColorByName(item.name);
               this.tempRed = color.r;
               this.tempGreen = color.g;
               this.tempBlue = color.b;
               this.tempAlpha = color.a;
               return true;
            }
         }

         int btnY;
         if (!this.currentEditing.isEmpty()) {
            btnY = this.leftX + 240;
            if (mouseX >= (double)btnY && mouseX <= (double)(btnY + 200)) {
               if (mouseY >= (double)(this.rSliderY + 12) && mouseY <= (double)(this.rSliderY + 22)) {
                  this.draggingRed = true;
                  return true;
               }

               if (mouseY >= (double)(this.gSliderY + 12) && mouseY <= (double)(this.gSliderY + 22)) {
                  this.draggingGreen = true;
                  return true;
               }

               if (mouseY >= (double)(this.bSliderY + 12) && mouseY <= (double)(this.bSliderY + 22)) {
                  this.draggingBlue = true;
                  return true;
               }

               if (mouseY >= (double)(this.aSliderY + 12) && mouseY <= (double)(this.aSliderY + 22)) {
                  this.draggingAlpha = true;
                  return true;
               }
            }
         }

         btnY = this.windowY + this.windowHeight - 30;
         if (mouseX >= (double)(this.windowX + 250) && mouseX <= (double)(this.windowX + 330) && mouseY >= (double)btnY && mouseY <= (double)(btnY + 20)) {
            this.copyColors(this.workingColors, this.themeColors);
            ThemeManager.save(this.themeColors);
            Minecraft.m_91087_().m_91152_(new HackGui());
            return true;
         } else if (mouseX >= (double)(this.windowX + 350) && mouseX <= (double)(this.windowX + 430) && mouseY >= (double)btnY && mouseY <= (double)(btnY + 20)) {
            Minecraft.m_91087_().m_91152_(new HackGui());
            return true;
         } else if (mouseX >= (double)(this.windowX + 450) && mouseX <= (double)(this.windowX + 530) && mouseY >= (double)btnY && mouseY <= (double)(btnY + 20)) {
            this.workingColors = new ThemeColors();
            if (!this.currentEditing.isEmpty()) {
               SettingColor color = this.getColorByName(this.currentEditing);
               this.tempRed = color.r;
               this.tempGreen = color.g;
               this.tempBlue = color.b;
               this.tempAlpha = color.a;
            }

            return true;
         } else {
            return super.m_6375_(mouseX, mouseY, button);
         }
      }
   }

   public boolean m_6348_(double mouseX, double mouseY, int button) {
      this.dragging = false;
      this.draggingRed = false;
      this.draggingGreen = false;
      this.draggingBlue = false;
      this.draggingAlpha = false;
      return super.m_6348_(mouseX, mouseY, button);
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (this.dragging) {
         this.windowX = (int)(mouseX - (double)this.dragX);
         this.windowY = (int)(mouseY - (double)this.dragY);
         this.windowX = Math.max(0, Math.min(this.windowX, this.f_96543_ - this.windowWidth));
         this.windowY = Math.max(0, Math.min(this.windowY, this.f_96544_ - this.windowHeight));
         return true;
      } else {
         int rightX = this.leftX + 240;
         double newValue;
         SettingColor color;
         if (this.draggingRed) {
            newValue = (mouseX - (double)rightX) * 255.0 / 200.0;
            newValue = Math.max(0.0, Math.min(255.0, newValue));
            this.tempRed = (int)newValue;
            color = this.getColorByName(this.currentEditing);
            if (color != null) {
               color.r = this.tempRed;
            }

            return true;
         } else if (this.draggingGreen) {
            newValue = (mouseX - (double)rightX) * 255.0 / 200.0;
            newValue = Math.max(0.0, Math.min(255.0, newValue));
            this.tempGreen = (int)newValue;
            color = this.getColorByName(this.currentEditing);
            if (color != null) {
               color.g = this.tempGreen;
            }

            return true;
         } else if (this.draggingBlue) {
            newValue = (mouseX - (double)rightX) * 255.0 / 200.0;
            newValue = Math.max(0.0, Math.min(255.0, newValue));
            this.tempBlue = (int)newValue;
            color = this.getColorByName(this.currentEditing);
            if (color != null) {
               color.b = this.tempBlue;
            }

            return true;
         } else if (this.draggingAlpha) {
            newValue = (mouseX - (double)rightX) * 255.0 / 200.0;
            newValue = Math.max(0.0, Math.min(255.0, newValue));
            this.tempAlpha = (int)newValue;
            color = this.getColorByName(this.currentEditing);
            if (color != null) {
               color.a = this.tempAlpha;
            }

            return true;
         } else {
            return super.m_7979_(mouseX, mouseY, button, dragX, dragY);
         }
      }
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      if (mouseX >= (double)(this.windowX + 20) && mouseX <= (double)(this.windowX + 220) && mouseY >= (double)(this.windowY + 45) && mouseY <= (double)(this.windowY + this.windowHeight - 60)) {
         this.scrollOffset = (int)Math.max(0.0, Math.min((double)this.maxScroll, (double)this.scrollOffset - delta * 20.0));
         return true;
      } else {
         return false;
      }
   }

   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         Minecraft.m_91087_().m_91152_(new HackGui());
         return true;
      } else {
         return super.m_7933_(keyCode, scanCode, modifiers);
      }
   }

   public boolean m_7043_() {
      return false;
   }

   private static class ColorItem {
      String name;
      String displayName;

      ColorItem(String name, String displayName) {
         this.name = name;
         this.displayName = displayName;
      }
   }
}
