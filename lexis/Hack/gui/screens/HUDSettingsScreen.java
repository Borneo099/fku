package lexis.Hack.gui.screens;

import lexis.Hack.Hack;
import lexis.Hack.Hacks.Lexis.HUDSettingsHack;
import lexis.Hack.Utils.Colors.ColorSettingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HUDSettingsScreen extends Screen {
   private final HUDSettingsHack hack;
   private final Screen parent;
   private int windowX;
   private int windowY;
   private int windowWidth = 400;
   private int windowHeight = 250;
   private boolean dragging = false;
   private int dragX;
   private int dragY;
   private boolean dragHandleHovered = false;

   public HUDSettingsScreen(HUDSettingsHack hack, Screen parent) {
      super(Component.m_237113_("HUD 设置"));
      this.hack = hack;
      this.parent = parent;
   }

   protected void m_7856_() {
      super.m_7856_();
      this.windowX = (this.f_96543_ - this.windowWidth) / 2;
      this.windowY = (this.f_96544_ - this.windowHeight) / 2;
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
      gui.m_280488_(this.f_96547_, "§lHUD 设置", this.windowX + 12, this.windowY + 8, -1);
      boolean closeHovered = mouseX >= this.windowX + this.windowWidth - 25 && mouseX <= this.windowX + this.windowWidth - 10 && mouseY >= this.windowY + 5 && mouseY <= this.windowY + 25;
      gui.m_280488_(this.f_96547_, "✕", this.windowX + this.windowWidth - 18, this.windowY + 8, closeHovered ? -43691 : -5592406);
      int y = this.windowY + 50;
      gui.m_280488_(this.f_96547_, "文字起始颜色:", this.windowX + 20, y, 16777215);
      this.drawColorBox(gui, this.windowX + 200, y, this.hack.getTextStartColor().getPacked());
      y += 30;
      gui.m_280488_(this.f_96547_, "文字结束颜色:", this.windowX + 20, y, 16777215);
      this.drawColorBox(gui, this.windowX + 200, y, this.hack.getTextEndColor().getPacked());
      y += 30;
      gui.m_280488_(this.f_96547_, "背景颜色:", this.windowX + 20, y, 16777215);
      this.drawColorBox(gui, this.windowX + 200, y, this.hack.getBgColor().getPacked());
      gui.m_280509_(this.windowX, this.windowY, this.windowX + this.windowWidth, this.windowY + 1, -11711142);
      gui.m_280509_(this.windowX, this.windowY + this.windowHeight - 1, this.windowX + this.windowWidth, this.windowY + this.windowHeight, -11711142);
      gui.m_280509_(this.windowX, this.windowY, this.windowX + 1, this.windowY + this.windowHeight, -11711142);
      gui.m_280509_(this.windowX + this.windowWidth - 1, this.windowY, this.windowX + this.windowWidth, this.windowY + this.windowHeight, -11711142);
   }

   private void drawColorBox(GuiGraphics gui, int x, int y, int color) {
      gui.m_280509_(x, y, x + 40, y + 20, color);
      gui.m_280509_(x, y, x + 40, y + 1, -1);
      gui.m_280509_(x, y, x + 1, y + 20, -1);
      gui.m_280509_(x + 39, y, x + 40, y + 20, -1);
      gui.m_280509_(x, y + 19, x + 40, y + 20, -1);
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (mouseX >= (double)(this.windowX + this.windowWidth - 25) && mouseX <= (double)(this.windowX + this.windowWidth - 10) && mouseY >= (double)(this.windowY + 5) && mouseY <= (double)(this.windowY + 25)) {
         this.m_7379_();
         return true;
      } else if (button == 0 && mouseX >= (double)this.windowX && mouseX <= (double)(this.windowX + this.windowWidth) && mouseY >= (double)this.windowY && mouseY <= (double)(this.windowY + 30)) {
         this.dragging = true;
         this.dragX = (int)(mouseX - (double)this.windowX);
         this.dragY = (int)(mouseY - (double)this.windowY);
         return true;
      } else {
         int y = this.windowY + 50;
         if (mouseX >= (double)(this.windowX + 200) && mouseX <= (double)(this.windowX + 240) && mouseY >= (double)y && mouseY <= (double)(y + 20)) {
            Minecraft.m_91087_().m_91152_(new ColorSettingScreen((Hack.Setting)this.hack.getSettings().get(0), this));
            return true;
         } else {
            y += 30;
            if (mouseX >= (double)(this.windowX + 200) && mouseX <= (double)(this.windowX + 240) && mouseY >= (double)y && mouseY <= (double)(y + 20)) {
               Minecraft.m_91087_().m_91152_(new ColorSettingScreen((Hack.Setting)this.hack.getSettings().get(1), this));
               return true;
            } else {
               y += 30;
               if (mouseX >= (double)(this.windowX + 200) && mouseX <= (double)(this.windowX + 240) && mouseY >= (double)y && mouseY <= (double)(y + 20)) {
                  Minecraft.m_91087_().m_91152_(new ColorSettingScreen((Hack.Setting)this.hack.getSettings().get(2), this));
                  return true;
               } else {
                  return super.m_6375_(mouseX, mouseY, button);
               }
            }
         }
      }
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (this.dragging) {
         this.windowX = (int)(mouseX - (double)this.dragX);
         this.windowY = (int)(mouseY - (double)this.dragY);
         this.windowX = Math.max(0, Math.min(this.windowX, this.f_96543_ - this.windowWidth));
         this.windowY = Math.max(0, Math.min(this.windowY, this.f_96544_ - this.windowHeight));
         return true;
      } else {
         return super.m_7979_(mouseX, mouseY, button, dragX, dragY);
      }
   }

   public boolean m_6348_(double mouseX, double mouseY, int button) {
      this.dragging = false;
      return super.m_6348_(mouseX, mouseY, button);
   }

   public void m_7379_() {
      if (this.parent != null) {
         Minecraft.m_91087_().m_91152_(this.parent);
      } else {
         Minecraft.m_91087_().m_91152_((Screen)null);
      }

   }

   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.m_7379_();
         return true;
      } else {
         return super.m_7933_(keyCode, scanCode, modifiers);
      }
   }

   public boolean m_7043_() {
      return false;
   }
}
