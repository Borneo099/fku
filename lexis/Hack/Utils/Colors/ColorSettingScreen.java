package lexis.Hack.Utils.Colors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lexis.Hack.Hack;
import lexis.Hack.SettingsWindow;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ColorSettingScreen extends Screen {
   private final Hack.Setting setting;
   private final Screen parent;
   private final String hackName;
   private final String settingName;
   private int r = 255;
   private int g = 255;
   private int b = 255;
   private int a = 255;
   private float hue;
   private float saturation;
   private float brightness;
   private boolean draggingHue = false;
   private boolean draggingSV = false;
   private boolean draggingSlider = false;
   private int activeSlider = -1;
   private int windowX;
   private int windowY;
   private int windowWidth = 370;
   private int windowHeight = 440;
   private boolean draggingWindow = false;
   private int dragOffX;
   private int dragOffY;
   private static final int SV_SIZE = 120;
   private static final int HUE_BAR_W = 16;
   private static final int PICKER_X = 18;
   private static final int PICKER_Y = 42;
   private EditBox rgbInput;
   private static final int RADIUS = 6;
   private static final Map rainbowMap = new LinkedHashMap();
   private static final Set rainbowKeys = new HashSet();
   private static final File RAINBOW_FILE = new File("C:/karucn/Lexis/config/hack/rainbow.json");
   private static int rainbowCount = 0;
   private boolean rainbow;

   private static void loadRainbowKeys() {
      if (RAINBOW_FILE.exists()) {
         try {
            BufferedReader reader = new BufferedReader(new FileReader(RAINBOW_FILE));

            String line;
            while((line = reader.readLine()) != null) {
               line = line.trim();
               if (!line.isEmpty()) {
                  rainbowKeys.add(line);
               }
            }

            reader.close();
         } catch (Exception var2) {
         }

      }
   }

   private static void saveRainbowKeys() {
      try {
         RAINBOW_FILE.getParentFile().mkdirs();
         PrintWriter w = new PrintWriter(new FileWriter(RAINBOW_FILE));
         Iterator var1 = rainbowKeys.iterator();

         while(var1.hasNext()) {
            String k = (String)var1.next();
            w.println(k);
         }

         w.close();
      } catch (Exception var3) {
      }

   }

   public static void initRainbow() {
   }

   private static void ensureRainbowRefs() {
      if (!rainbowKeys.isEmpty() && rainbowMap.isEmpty()) {
         List hacks = HackManager.getInstance().getHacks();
         Iterator var1 = hacks.iterator();

         while(var1.hasNext()) {
            Hack hack = (Hack)var1.next();
            Iterator var3 = hack.getSettings().iterator();

            while(var3.hasNext()) {
               Hack.Setting s = (Hack.Setting)var3.next();
               String var10000 = hack.getName();
               String key = var10000 + "|" + s.getName();
               if (rainbowKeys.contains(key) && !rainbowMap.containsKey(s)) {
                  rainbowMap.put(s, (float)rainbowCount * 0.17F);
                  ++rainbowCount;
               }
            }
         }

      }
   }

   public ColorSettingScreen(Hack.Setting setting, Screen parent) {
      super(Component.m_237113_("拾色器"));
      this.setting = setting;
      this.parent = parent;
      this.hackName = setting.getName();
      this.settingName = setting.getName();
      int packed = (Integer)setting.getValue();
      this.a = packed >> 24 & 255;
      this.r = packed >> 16 & 255;
      this.g = packed >> 8 & 255;
      this.b = packed & 255;
      if (this.a == 0) {
         this.a = 255;
      }

      float[] hsb = java.awt.Color.RGBtoHSB(this.r, this.g, this.b, (float[])null);
      this.hue = hsb[0];
      this.saturation = hsb[1];
      this.brightness = hsb[2];
      this.rainbow = rainbowMap.containsKey(setting);
   }

   protected void m_7856_() {
      super.m_7856_();
      this.windowX = (this.f_96543_ - this.windowWidth) / 2;
      this.windowY = (this.f_96544_ - this.windowHeight) / 2;
      this.rgbInput = new EditBox(this.f_96547_, this.windowX + 18, this.windowY + this.windowHeight - 60, 130, 16, Component.m_237113_(""));
      this.rgbInput.m_94199_(15);
      this.rgbInput.m_94182_(false);
      this.rgbInput.m_94151_((text) -> {
         String[] parts = text.split("[,\\s]+");
         if (parts.length == 3) {
            try {
               int nr = clamp(Integer.parseInt(parts[0].trim()), 0, 255);
               int ng = clamp(Integer.parseInt(parts[1].trim()), 0, 255);
               int nb = clamp(Integer.parseInt(parts[2].trim()), 0, 255);
               this.r = nr;
               this.g = ng;
               this.b = nb;
               float[] hsb = java.awt.Color.RGBtoHSB(this.r, this.g, this.b, (float[])null);
               this.hue = hsb[0];
               this.saturation = hsb[1];
               this.brightness = hsb[2];
            } catch (NumberFormatException var7) {
            }
         }

      });
      this.m_142416_(this.rgbInput);
      this.updateRgbInput();
   }

   private void updateRgbInput() {
      this.rgbInput.m_94144_(this.r + "," + this.g + "," + this.b);
   }

   private void updateRgbFromHSB() {
      int packed = java.awt.Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
      this.r = packed >> 16 & 255;
      this.g = packed >> 8 & 255;
      this.b = packed & 255;
      this.updateRgbInput();
   }

   public void m_88315_(GuiGraphics gui, int mouseX, int mouseY, float delta) {
      int bg;
      if (this.rainbow) {
         bg = (Integer)this.setting.getValue();
         this.a = bg >> 24 & 255;
         this.r = bg >> 16 & 255;
         this.g = bg >> 8 & 255;
         this.b = bg & 255;
         if (this.a == 0) {
            this.a = 255;
         }

         float[] hsb = java.awt.Color.RGBtoHSB(this.r, this.g, this.b, (float[])null);
         this.hue = hsb[0];
         this.saturation = hsb[1];
         this.brightness = hsb[2];
      }

      this.fillRoundedRect(gui, this.windowX + 4, this.windowY + 4, this.windowX + this.windowWidth + 4, this.windowY + this.windowHeight + 4, 6, (new java.awt.Color(0, 0, 0, 90)).getRGB());
      bg = (new java.awt.Color(18, 18, 28, 240)).getRGB();
      this.fillRoundedRect(gui, this.windowX, this.windowY, this.windowX + this.windowWidth, this.windowY + this.windowHeight, 6, bg);
      boolean titleHover = mouseX >= this.windowX && mouseX <= this.windowX + this.windowWidth && mouseY >= this.windowY && mouseY <= this.windowY + 30;
      int tLeft = titleHover ? -12763819 : -14342861;
      int tRight = titleHover ? -11910560 : -13488828;

      int row;
      int dx;
      for(row = 0; row <= 30; ++row) {
         float t = (float)(row - 6) / 24.0F;
         if (t < 0.0F) {
            t = 0.0F;
         }

         int c = lerpColor(tLeft, tRight, t);
         if (row < 6) {
            double dy = (double)(6 - row) - 0.5;
            dx = (int)Math.round(6.0 - Math.sqrt(36.0 - dy * dy));
            gui.m_280509_(this.windowX + dx, this.windowY + row, this.windowX + this.windowWidth - dx, this.windowY + row + 1, c);
         } else {
            gui.m_280509_(this.windowX, this.windowY + row, this.windowX + this.windowWidth, this.windowY + row + 1, c);
         }
      }

      gui.m_280509_(this.windowX, this.windowY + 30, this.windowX + this.windowWidth, this.windowY + 31, -9548620);
      this.drawString(gui, "§l拾色器", this.windowX + 12, this.windowY + 8, -1);
      row = this.windowX + this.windowWidth - 15;
      int cy = this.windowY + 13;
      boolean closeHov = mouseX >= row - 7 && mouseX <= row + 7 && mouseY >= cy - 7 && mouseY <= cy + 7;
      if (closeHov) {
         this.fillRoundedRect(gui, row - 7, cy - 7, row + 8, cy + 8, 4, -573820109);
         this.drawString(gui, "✕", row - 4, cy - 5, -1);
      } else {
         this.drawString(gui, "✕", row - 4, cy - 5, -5592406);
      }

      int svX = this.windowX + 18;
      int svY = this.windowY + 42;
      this.drawSVSquare(gui, svX, svY, 120);
      dx = svX + (int)(this.saturation * 120.0F);
      int svCurY = svY + (int)((1.0F - this.brightness) * 120.0F);
      gui.m_280509_(dx - 3, svCurY - 3, dx + 4, svCurY + 4, -1);
      gui.m_280509_(dx - 2, svCurY - 2, dx + 3, svCurY + 3, -16777216);
      int hueX = svX + 120 + 8;
      this.drawHueBar(gui, hueX, svY, 16, 120);
      int hueCurY = svY + (int)(this.hue * 120.0F);
      gui.m_280509_(hueX - 2, hueCurY - 2, hueX + 16 + 2, hueCurY + 3, -1);
      gui.m_280509_(hueX - 1, hueCurY - 1, hueX + 16 + 1, hueCurY + 2, -16777216);
      int prevX = hueX + 16 + 10;
      int prevW = 55;
      int prevColor = this.a << 24 | this.r << 16 | this.g << 8 | this.b;
      int prevBorder = -8355712;
      this.fillRoundedRect(gui, prevX, svY, prevX + prevW, svY + 40, 3, prevBorder);
      this.fillRoundedRect(gui, prevX + 1, svY + 1, prevX + prevW - 1, svY + 39, 2, prevColor);
      String var10002 = hex(this.r);
      this.drawString(gui, "#" + var10002 + hex(this.g) + hex(this.b), prevX + 2, svY + 44, -3355444);
      this.drawString(gui, "Alpha: " + this.a + " (" + this.a * 100 / 255 + "%)", this.windowX + 18, svY + 120 + 14, -4473925);
      int sldY = svY + 120 + 30;
      this.drawColorSlider(gui, this.windowX + 18, sldY, "R", this.r, this.g, this.b, 0);
      this.drawColorSlider(gui, this.windowX + 18, sldY + 28, "G", this.g, this.r, this.b, 1);
      this.drawColorSlider(gui, this.windowX + 18, sldY + 56, "B", this.b, this.r, this.g, 2);
      this.drawColorSlider(gui, this.windowX + 18, sldY + 84, "A", this.a, 0, 0, 3);
      this.rgbInput.m_252865_(this.windowX + 18);
      this.rgbInput.m_253211_(this.windowY + this.windowHeight - 60);
      this.rgbInput.m_93674_(130);
      this.rgbInput.m_88315_(gui, mouseX, mouseY, delta);
      int btnW = 70;
      int btnH = 22;
      int btnR = 3;
      int btnY = this.windowY + this.windowHeight - 32;
      int saveX = this.windowX + 18;
      int cancelX = saveX + btnW + 16;
      boolean saveHov = mouseX >= saveX && mouseX <= saveX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
      int saveTop = saveHov ? -9805910 : -11713152;
      int saveBot = saveHov ? -11517824 : -13291440;
      this.fillRoundedRect(gui, saveX, btnY, saveX + btnW, btnY + btnH, btnR, -10858352);
      this.fillRoundedRectGradientV(gui, saveX + 1, btnY + 1, saveX + btnW - 1, btnY + btnH - 1, btnR - 1, saveTop, saveBot);
      this.drawCentered(gui, "保存", saveX + btnW / 2, btnY + 5, -1);
      boolean cancelHov = mouseX >= cancelX && mouseX <= cancelX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
      int canTop = cancelHov ? -8949641 : -11186091;
      int canBot = cancelHov ? -11186107 : -12568008;
      this.fillRoundedRect(gui, cancelX, btnY, cancelX + btnW, btnY + btnH, btnR, -10465192);
      this.fillRoundedRectGradientV(gui, cancelX + 1, btnY + 1, cancelX + btnW - 1, btnY + btnH - 1, btnR - 1, canTop, canBot);
      this.drawCentered(gui, "取消", cancelX + btnW / 2, btnY + 5, -1);
      int rainbowX = cancelX + btnW + 20;
      int rainbowW = 90;
      boolean rainHov = mouseX >= rainbowX && mouseX <= rainbowX + rainbowW && mouseY >= btnY && mouseY <= btnY + btnH;
      int rainTop = this.rainbow ? -2258910 : -12303292;
      int rainBot = this.rainbow ? -3381760 : -13421773;
      if (rainHov) {
         rainTop = this.rainbow ? -21948 : -11184811;
         rainBot = this.rainbow ? -1144781 : -12303292;
      }

      this.fillRoundedRect(gui, rainbowX, btnY, rainbowX + rainbowW, btnY + btnH, btnR, -10066330);
      this.fillRoundedRectGradientV(gui, rainbowX + 1, btnY + 1, rainbowX + rainbowW - 1, btnY + btnH - 1, btnR - 1, rainTop, rainBot);
      this.drawCentered(gui, this.rainbow ? "彩虹:开" : "彩虹:关", rainbowX + rainbowW / 2, btnY + 5, -1);
      this.fillRoundedRect(gui, this.windowX - 1, this.windowY - 1, this.windowX + this.windowWidth + 1, this.windowY + this.windowHeight + 1, 7, 843465326);
   }

   private void drawSVSquare(GuiGraphics gui, int x, int y, int size) {
      int hueRGB = java.awt.Color.HSBtoRGB(this.hue, 1.0F, 1.0F);
      int rH = hueRGB >> 16 & 255;
      int gH = hueRGB >> 8 & 255;
      int bH = hueRGB & 255;

      for(int row = 0; row < size; ++row) {
         float v = 1.0F - (float)row / (float)size;
         int rr = (int)((float)rH * v);
         int gg = (int)((float)gH * v);
         int bb = (int)((float)bH * v);
         int white = (int)(255.0F * v);
         int left = -16777216 | white << 16 | white << 8 | white;
         int right = -16777216 | rr << 16 | gg << 8 | bb;
         gui.m_280024_(x, y + row, x + size, y + row + 1, left, right);
      }

      gui.m_280509_(x, y, x + size, y + 1, -10066330);
      gui.m_280509_(x, y + size - 1, x + size, y + size, -10066330);
      gui.m_280509_(x, y, x + 1, y + size, -10066330);
      gui.m_280509_(x + size - 1, y, x + size, y + size, -10066330);
   }

   private void drawHueBar(GuiGraphics gui, int x, int y, int w, int h) {
      int stripH = 4;

      for(int row = 0; row < h; row += stripH) {
         int nextRow = Math.min(row + stripH, h);
         float hueTop = (float)row / (float)h;
         float hueBot = (float)nextRow / (float)h;
         int cTop = -16777216 | java.awt.Color.HSBtoRGB(hueTop, 1.0F, 1.0F) & 16777215;
         int cBot = -16777216 | java.awt.Color.HSBtoRGB(hueBot, 1.0F, 1.0F) & 16777215;
         gui.m_280024_(x, y + row, x + w, y + nextRow, cTop, cBot);
      }

      gui.m_280509_(x, y, x + w, y + 1, -10066330);
      gui.m_280509_(x, y + h - 1, x + w, y + h, -10066330);
      gui.m_280509_(x, y, x + 1, y + h, -10066330);
      gui.m_280509_(x + w - 1, y, x + w, y + h, -10066330);
   }

   private void drawColorSlider(GuiGraphics gui, int sx, int sy, String label, int val, int o1, int o2, int idx) {
      this.drawString(gui, label + ":", sx, sy + 2, -4473925);
      int trackX = sx + 18;
      int trackW = 200;
      int trackH = 14;
      int trackR = trackH / 2;
      int trackY = sy + 2;
      int trackBg = -13421760;
      int trackBd = -11184800;
      this.fillRoundedRect(gui, trackX, trackY, trackX + trackW, trackY + trackH, trackR, trackBd);
      this.fillRoundedRect(gui, trackX + 1, trackY + 1, trackX + trackW - 1, trackY + trackH - 1, trackR - 1, trackBg);
      int innerW = trackW - 2;
      int stripW = 4;

      int i;
      int next;
      int fc2;
      int cr;
      for(i = 0; i < innerW; i += stripW) {
         next = Math.min(i + stripW, innerW);
         float t1 = (float)i / (float)innerW;
         float t2 = (float)next / (float)innerW;
         int fc1;
         if (idx == 0) {
            cr = (int)(t1 * 255.0F);
            fc1 = -16777216 | cr << 16 | o1 << 8 | o2;
            cr = (int)(t2 * 255.0F);
            fc2 = -16777216 | cr << 16 | o1 << 8 | o2;
         } else if (idx == 1) {
            int cg = (int)(t1 * 255.0F);
            fc1 = -16777216 | o1 << 16 | cg << 8 | o2;
            cg = (int)(t2 * 255.0F);
            fc2 = -16777216 | o1 << 16 | cg << 8 | o2;
         } else if (idx == 2) {
            int cb = (int)(t1 * 255.0F);
            fc1 = -16777216 | o1 << 16 | o2 << 8 | cb;
            cb = (int)(t2 * 255.0F);
            fc2 = -16777216 | o1 << 16 | o2 << 8 | cb;
         } else {
            fc2 = -3618616;
            fc1 = -3618616;
         }

         gui.m_280024_(trackX + 1 + i, trackY + 1, trackX + 1 + next, trackY + trackH - 1, fc1, fc2);
      }

      i = trackX + (int)((float)val / 255.0F * (float)(trackW - 8));
      next = trackY - 3;
      int knobH = trackH + 6;
      int knobW = 8;
      boolean isActive = this.draggingSlider && this.activeSlider == idx;
      fc2 = isActive ? -1118482 : -5592406;
      cr = isActive ? -3355444 : -7829368;
      this.fillRoundedRect(gui, i, next, i + knobW, next + knobH, 3, fc2);
      this.fillRoundedRect(gui, i + 1, next + 1, i + knobW - 1, next + knobH - 1, 2, cr);
      this.drawString(gui, String.valueOf(val), trackX + trackW + 6, sy + 3, -3355444);
   }

   public boolean m_6375_(double mx, double my, int button) {
      int cx = this.windowX + this.windowWidth - 15;
      int cy = this.windowY + 13;
      if (mx >= (double)(cx - 7) && mx <= (double)(cx + 7) && my >= (double)(cy - 7) && my <= (double)(cy + 7)) {
         this.m_7379_();
         return true;
      } else if (button == 0 && mx >= (double)this.windowX && mx <= (double)(this.windowX + this.windowWidth) && my >= (double)this.windowY && my <= (double)(this.windowY + 30)) {
         this.draggingWindow = true;
         this.dragOffX = (int)(mx - (double)this.windowX);
         this.dragOffY = (int)(my - (double)this.windowY);
         return true;
      } else {
         int svX = this.windowX + 18;
         int svY = this.windowY + 42;
         if (mx >= (double)svX && mx <= (double)(svX + 120) && my >= (double)svY && my <= (double)(svY + 120)) {
            this.draggingSV = true;
            this.updateSV(mx, my);
            return true;
         } else {
            int hueX = svX + 120 + 8;
            if (mx >= (double)(hueX - 2) && mx <= (double)(hueX + 16 + 2) && my >= (double)svY && my <= (double)(svY + 120)) {
               this.draggingHue = true;
               this.updateHue(mx, my);
               return true;
            } else {
               int sldX = this.windowX + 18 + 18;
               int sldY = svY + 120 + 30;

               for(int i = 0; i < 4; ++i) {
                  int sy = sldY + i * 28 + 2;
                  if (mx >= (double)sldX && mx <= (double)(sldX + 200) && my >= (double)sy && my <= (double)(sy + 14)) {
                     this.draggingSlider = true;
                     this.activeSlider = i;
                     this.updateSlider(mx);
                     return true;
                  }
               }

               int btnW = 70;
               int btnH = 22;
               int btnY = this.windowY + this.windowHeight - 32;
               if (my >= (double)btnY && my <= (double)(btnY + btnH)) {
                  int saveX = this.windowX + 18;
                  if (mx >= (double)saveX && mx <= (double)(saveX + btnW)) {
                     this.save();
                     this.m_7379_();
                     return true;
                  }

                  int cancelX = saveX + btnW + 16;
                  if (mx >= (double)cancelX && mx <= (double)(cancelX + btnW)) {
                     this.m_7379_();
                     return true;
                  }

                  int rainbowX = cancelX + btnW + 20;
                  if (mx >= (double)rainbowX && mx <= (double)(rainbowX + 90)) {
                     this.toggleRainbow();
                     return true;
                  }
               }

               return super.m_6375_(mx, my, button);
            }
         }
      }
   }

   public boolean m_6348_(double mx, double my, int button) {
      this.draggingWindow = false;
      this.draggingSV = false;
      this.draggingHue = false;
      this.draggingSlider = false;
      this.activeSlider = -1;
      return super.m_6348_(mx, my, button);
   }

   public boolean m_7979_(double mx, double my, int button, double dx, double dy) {
      if (this.draggingWindow) {
         this.windowX = clamp((int)(mx - (double)this.dragOffX), 0, this.f_96543_ - this.windowWidth);
         this.windowY = clamp((int)(my - (double)this.dragOffY), 0, this.f_96544_ - this.windowHeight);
         return true;
      } else if (this.draggingSV) {
         this.updateSV(mx, my);
         return true;
      } else if (this.draggingHue) {
         this.updateHue(mx, my);
         return true;
      } else if (this.draggingSlider) {
         this.updateSlider(mx);
         return true;
      } else {
         return super.m_7979_(mx, my, button, dx, dy);
      }
   }

   private void updateSV(double mx, double my) {
      int svX = this.windowX + 18;
      int svY = this.windowY + 42;
      this.saturation = clamp((float)((mx - (double)svX) / 120.0), 0.0F, 1.0F);
      this.brightness = clamp(1.0F - (float)((my - (double)svY) / 120.0), 0.0F, 1.0F);
      this.updateRgbFromHSB();
   }

   private void updateHue(double mx, double my) {
      int svY = this.windowY + 42;
      this.hue = clamp((float)((my - (double)svY) / 120.0), 0.0F, 1.0F);
      this.updateRgbFromHSB();
   }

   private void updateSlider(double mx) {
      int sldX = this.windowX + 18 + 18;
      float t = clamp((float)((mx - (double)sldX) / 200.0), 0.0F, 1.0F);
      int v = (int)(t * 255.0F);
      switch (this.activeSlider) {
         case 0:
            this.r = v;
            break;
         case 1:
            this.g = v;
            break;
         case 2:
            this.b = v;
            break;
         case 3:
            this.a = v;
      }

      if (this.activeSlider < 3) {
         float[] hsb = java.awt.Color.RGBtoHSB(this.r, this.g, this.b, (float[])null);
         this.hue = hsb[0];
         this.saturation = hsb[1];
         this.brightness = hsb[2];
      }

      this.updateRgbInput();
   }

   private void save() {
      int packed = this.a << 24 | this.r << 16 | this.g << 8 | this.b;
      this.setting.setValue(packed);
      if (this.parent instanceof SettingsWindow) {
         ((SettingsWindow)this.parent).autoSave();
      }

   }

   private void toggleRainbow() {
      this.rainbow = !this.rainbow;
      String var10000 = this.setting.getHack().getName();
      String key = var10000 + "|" + this.setting.getName();
      if (this.rainbow) {
         rainbowMap.put(this.setting, (float)rainbowCount * 0.17F);
         ++rainbowCount;
         rainbowKeys.add(key);
      } else {
         rainbowMap.remove(this.setting);
         rainbowKeys.remove(key);
      }

      saveRainbowKeys();
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

   private void fillRoundedRect(GuiGraphics gui, int l, int t, int ri, int bot, int rad, int color) {
      int w = ri - l;
      int h = bot - t;
      if (rad > 0 && w >= rad * 2 && h >= rad * 2) {
         if (h > rad * 2) {
            gui.m_280509_(l, t + rad, ri, bot - rad, color);
         }

         for(int i = 0; i < rad; ++i) {
            double dy = (double)(rad - i) - 0.5;
            int dx = (int)Math.round((double)rad - Math.sqrt((double)(rad * rad) - dy * dy));
            gui.m_280509_(l + dx, t + i, ri - dx, t + i + 1, color);
            gui.m_280509_(l + dx, bot - i - 1, ri - dx, bot - i, color);
         }

      } else {
         gui.m_280509_(l, t, ri, bot, color);
      }
   }

   private void fillRoundedRectGradientV(GuiGraphics gui, int l, int t, int ri, int bot, int rad, int topC, int botC) {
      int w = ri - l;
      int h = bot - t;
      if (rad > 0 && w >= rad * 2 && h >= rad * 2) {
         if (h > rad * 2) {
            gui.m_280024_(l, t + rad, ri, bot - rad, lerpColor(topC, botC, (float)rad / (float)h), lerpColor(topC, botC, 1.0F - (float)rad / (float)h));
         }

         for(int i = 0; i < rad; ++i) {
            double dy = (double)(rad - i) - 0.5;
            int dx = (int)Math.round((double)rad - Math.sqrt((double)(rad * rad) - dy * dy));
            gui.m_280509_(l + dx, t + i, ri - dx, t + i + 1, lerpColor(topC, botC, (float)i / (float)h));
            gui.m_280509_(l + dx, bot - i - 1, ri - dx, bot - i, lerpColor(topC, botC, (float)(h - i - 1) / (float)h));
         }

      } else {
         gui.m_280024_(l, t, ri, bot, topC, botC);
      }
   }

   private static int lerpColor(int a, int b, float t) {
      int aa = a >> 24 & 255;
      int ar = a >> 16 & 255;
      int ag = a >> 8 & 255;
      int ab = a & 255;
      int ba = b >> 24 & 255;
      int br = b >> 16 & 255;
      int bg = b >> 8 & 255;
      int bb = b & 255;
      return (int)((float)aa + (float)(ba - aa) * t) << 24 | (int)((float)ar + (float)(br - ar) * t) << 16 | (int)((float)ag + (float)(bg - ag) * t) << 8 | (int)((float)ab + (float)(bb - ab) * t);
   }

   private void drawString(GuiGraphics gui, String text, int x, int y, int color) {
      gui.m_280488_(this.f_96547_, text, x, y, color);
   }

   private void drawCentered(GuiGraphics gui, String text, int cx, int y, int color) {
      gui.m_280488_(this.f_96547_, text, cx - this.f_96547_.m_92895_(text) / 2, y, color);
   }

   private static String hex(int v) {
      String h = Integer.toHexString(v & 255).toUpperCase();
      return h.length() == 1 ? "0" + h : h;
   }

   private static int clamp(int v, int min, int max) {
      return Math.max(min, Math.min(max, v));
   }

   private static float clamp(float v, float min, float max) {
      return Math.max(min, Math.min(max, v));
   }

   static {
      loadRainbowKeys();
      MinecraftForge.EVENT_BUS.register(new Object() {
         @SubscribeEvent
         public void onTick(TickEvent.ClientTickEvent event) {
            if (event.phase == Phase.END) {
               ColorSettingScreen.ensureRainbowRefs();
               if (!ColorSettingScreen.rainbowMap.isEmpty()) {
                  Iterator var2 = ColorSettingScreen.rainbowMap.entrySet().iterator();

                  while(var2.hasNext()) {
                     Map.Entry entry = (Map.Entry)var2.next();
                     Hack.Setting s = (Hack.Setting)entry.getKey();
                     float h = (Float)entry.getValue() + 0.004F;
                     if (h > 1.0F) {
                        --h;
                     }

                     entry.setValue(h);
                     int packed = java.awt.Color.HSBtoRGB(h, 1.0F, 1.0F);
                     int alpha = (Integer)s.getValue() >> 24 & 255;
                     if (alpha == 0) {
                        alpha = 255;
                     }

                     packed = alpha << 24 | packed & 16777215;
                     s.setValue(packed);
                  }

               }
            }
         }
      });
   }
}
