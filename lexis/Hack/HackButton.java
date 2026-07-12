package lexis.Hack;

import java.util.ArrayList;
import java.util.List;
import lexis.Hack.Hacks.Lexis.GuiKeyBindHack;
import lexis.Hack.Hackutil.FavoritesManager;
import lexis.Hack.Hackutil.config.KeyBindConfig;
import lexis.Hack.Utils.ThemeColors.ThemeColors;
import lexis.Hack.Utils.ThemeColors.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class HackButton {
   private static final Minecraft mc = Minecraft.m_91087_();
   private Hack hack;
   private boolean hovered;
   private boolean bindingMode;
   private HackGui parentGui;
   private KeyBindConfig keyBindConfig;
   private long lastColorChange = 0L;
   private float currentHue = 0.0F;
   private boolean starHovered;
   private static final int[][] STAR_ROWS = new int[][]{{4, 5}, {3, 6}, {3, 6}, {0, 9}, {1, 8}, {2, 7}, {2, 4, 5, 7}, {1, 3, 6, 8}};

   public HackButton(Hack hack, HackGui parentGui) {
      this.hack = hack;
      this.parentGui = parentGui;
      this.bindingMode = false;
      this.keyBindConfig = KeyBindConfig.getInstance();
   }

   public void render(GuiGraphics gui, int x, int y, int width, int height, int mouseX, int mouseY) {
      this.renderWithAlpha(gui, x, y, width, height, mouseX, mouseY, 1.0F);
   }

   public void renderWithAlpha(GuiGraphics gui, int x, int y, int width, int height, int mouseX, int mouseY, float alpha) {
      this.hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
      ThemeColors colors = ThemeManager.getColors();
      boolean isFavorite = FavoritesManager.getInstance().isFavorite(this.hack.getName());
      int bgColor = this.hack.isEnabled() ? -13391309 : -13421773;
      int bgAlphaInt = (int)(alpha * 255.0F) << 24;
      bgColor = bgColor & 16777215 | bgAlphaInt;
      gui.m_280509_(x, y, x + width, y + height, bgColor);
      int overlayColor = 1157627903;
      int overlayAlphaInt = (int)(alpha * 255.0F) << 24;
      overlayColor = overlayColor & 16777215 | overlayAlphaInt;
      if (this.hovered) {
         gui.m_280509_(x, y, x + width, y + height, overlayColor);
      }

      gui.m_280509_(x, y, x + 3, y + height, this.hack.getCategory().color);
      int starX = x + width - 45;
      int starY = y + 2;
      int starSize = 12;
      this.starHovered = mouseX >= starX && mouseX <= starX + starSize && mouseY >= starY && mouseY <= starY + starSize;
      int starColor = isFavorite ? -10496 : -8947849;
      if (this.starHovered) {
         starColor = isFavorite ? -6029 : -4473925;
      }

      int starA = (int)((float)(starColor >>> 24 & 255) * alpha) << 24;
      starColor = starColor & 16777215 | starA;
      this.drawPixelStar(gui, starX + 1, starY + 2, starColor);
      String tipText;
      if (this.starHovered) {
         tipText = isFavorite ? "§e点击取消收藏" : "§e点击收藏";
         int tipWidth = mc.f_91062_.m_92895_(tipText);
         int tipX = starX + 10;
         int tipY = starY - 10;
         int screenW = mc.m_91268_().m_85445_();
         if (tipX + tipWidth + 5 > screenW) {
            tipX = starX - tipWidth - 10;
         }

         if (tipY < 0) {
            tipY = starY + 20;
         }

         gui.m_280488_(mc.f_91062_, tipText, tipX, tipY, 16777215);
      }

      gui.m_280488_(mc.f_91062_, this.hack.getButtonName(), x + 8, y + 4, colors.buttonText.getPacked());
      if (this.hack.getKeyBind() != -1 && !this.bindingMode) {
         tipText = this.getKeyName(this.hack.getKeyBind());
         gui.m_280488_(mc.f_91062_, tipText, x + width - 31, y + 4, colors.keyText.getPacked());
      }

      if (this.bindingMode) {
         gui.m_280488_(mc.f_91062_, "?", x + width - 15, y + 4, colors.keyText.getPacked());
      }

   }

   public void renderTooltip(GuiGraphics gui, int mouseX, int mouseY) {
      List tooltip = new ArrayList();
      tooltip.add(Component.m_237113_("§6" + this.hack.getName()));
      String[] var5 = this.hack.getDescriptionLines();
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         String line = var5[var7];
         tooltip.add(Component.m_237113_("§7" + line));
      }

      if (this.hack.getKeyBind() != -1) {
         tooltip.add(Component.m_237113_("§8按键: §f" + this.getKeyName(this.hack.getKeyBind())));
      }

      gui.m_280666_(mc.f_91062_, tooltip, mouseX, mouseY);
   }

   private void drawPixelStar(GuiGraphics gui, int sx, int sy, int color) {
      for(int row = 0; row < STAR_ROWS.length; ++row) {
         int[] runs = STAR_ROWS[row];

         for(int k = 0; k + 1 < runs.length; k += 2) {
            gui.m_280509_(sx + runs[k], sy + row, sx + runs[k + 1], sy + row + 1, color);
         }
      }

   }

   public void mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
      if (this.hovered) {
         int starX = x + width - 45;
         int starY = y + 2;
         int starSize = 12;
         if (mouseX >= (double)starX && mouseX <= (double)(starX + starSize) && mouseY >= (double)starY && mouseY <= (double)(starY + starSize)) {
            FavoritesManager.getInstance().toggleFavorite(this.hack.getName());
         } else if (this.bindingMode) {
            this.bindingMode = false;
            if (this.parentGui != null) {
               this.parentGui.setCurrentBindingButton((HackButton)null);
            }

         } else {
            if (button == 0) {
               if (this.hack instanceof GuiKeyBindHack) {
                  this.hack.onClick();
               } else if (this.hack.isToggleable()) {
                  this.hack.toggle();
               } else {
                  this.hack.onClick();
               }
            } else if (button == 1) {
               if (this.hack.getSettings() != null && !this.hack.getSettings().isEmpty()) {
                  SettingsWindow settingsWindow = new SettingsWindow(this.hack, this.parentGui);
                  this.parentGui.startSlideOut(settingsWindow);
               }
            } else if (button == 2) {
               this.bindingMode = true;
               if (this.parentGui != null) {
                  this.parentGui.setCurrentBindingButton(this);
               }
            }

         }
      }
   }

   public boolean keyPressed(int keyCode) {
      if (this.bindingMode) {
         if (keyCode == 256) {
            this.cancelBinding();
            return true;
         } else if (keyCode != 261 && keyCode != 330) {
            this.hack.setKeyBind(keyCode);
            this.bindingMode = false;
            this.saveKeyBind();
            return true;
         } else {
            this.hack.setKeyBind(-1);
            this.bindingMode = false;
            this.saveKeyBind();
            return true;
         }
      } else {
         return false;
      }
   }

   private void saveKeyBind() {
      this.keyBindConfig.setKeyBind(this.hack.getName(), this.hack.getKeyBind());
      this.keyBindConfig.save();
   }

   public void cancelBinding() {
      this.bindingMode = false;
   }

   public Hack getHack() {
      return this.hack;
   }

   public boolean isStarHovered() {
      return this.starHovered;
   }

   private String getKeyName(int key) {
      if (key >= 65 && key <= 90) {
         return String.valueOf((char)key);
      } else if (key >= 48 && key <= 57) {
         return String.valueOf((char)key);
      } else if (key >= 290 && key <= 301) {
         return "F" + (key - 290 + 1);
      } else if (key >= 320 && key <= 329) {
         return "小键盘" + (key - 320);
      } else {
         switch (key) {
            case 32:
               return "Space";
            case 39:
               return "'";
            case 44:
               return ",";
            case 45:
               return "-";
            case 46:
               return ".";
            case 47:
               return "/";
            case 59:
               return ";";
            case 61:
               return "=";
            case 91:
               return "[";
            case 92:
               return "\\";
            case 93:
               return "]";
            case 96:
               return "`";
            case 257:
               return "Enter";
            case 258:
               return "Tab";
            case 259:
               return "Backspace";
            case 260:
               return "Insert";
            case 261:
               return "Delete";
            case 262:
               return "→";
            case 263:
               return "←";
            case 264:
               return "↓";
            case 265:
               return "↑";
            case 266:
               return "PageUp";
            case 267:
               return "PageDown";
            case 268:
               return "Home";
            case 269:
               return "End";
            case 280:
               return "CapsLock";
            case 281:
               return "ScrollLock";
            case 282:
               return "NumLock";
            case 283:
               return "PrintScreen";
            case 284:
               return "Pause";
            case 330:
               return "小键盘.";
            case 331:
               return "小键盘/";
            case 332:
               return "小键盘*";
            case 333:
               return "小键盘-";
            case 334:
               return "小键盘+";
            case 335:
               return "小键盘Enter";
            case 336:
               return "小键盘=";
            case 340:
               return "LShift";
            case 341:
               return "LCtrl";
            case 342:
               return "LAlt";
            case 343:
               return "LWin";
            case 344:
               return "RShift";
            case 345:
               return "RCtrl";
            case 346:
               return "RAlt";
            case 347:
               return "RWin";
            case 348:
               return "Menu";
            default:
               return "键" + key;
         }
      }
   }
}
