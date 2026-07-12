package lexis.Hack.Hackutil.HUD;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Lexis.BindsDisplayHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Utils.Colors.SettingColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class BindsDisplayWidget {
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final ResourceLocation KEYBOARD_ICON = new ResourceLocation("lexis", "gui/djp.png");
   private int x = 5;
   private int y = 5;
   private int width = 170;
   private int height;
   private boolean visible = false;
   private boolean dragging = false;
   private int dragX;
   private int dragY;
   private SettingColor backgroundColor;
   private SettingColor textColor;
   private BindsDisplayHack hack;
   private static final int TITLE_HEIGHT = 18;
   private static final int LINE_HEIGHT = 10;
   private static final int PADDING = 4;
   private static final int ICON_WIDTH = 20;
   private static final int ICON_HEIGHT = 12;
   private static final int[] GRADIENT_COLORS = new int[]{-2461482, -2252579, -1146130, -18751, -38476};

   public BindsDisplayWidget(BindsDisplayHack hack) {
      this.hack = hack;
      this.backgroundColor = hack.getBackgroundColor();
      this.textColor = hack.getTextColor();
   }

   public void setVisible(boolean v) {
      this.visible = v;
   }

   public void setBackgroundColor(SettingColor color) {
      this.backgroundColor = color;
   }

   public void setTextColor(SettingColor color) {
      this.textColor = color;
   }

   public void setPosition(int x, int y) {
      this.x = x;
      this.y = y;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
      if (this.visible) {
         List entries = this.collectEntries();
         if (!entries.isEmpty()) {
            int contentHeight = entries.size() * 10 + 8;
            this.height = 18 + contentHeight;
            gui.m_280509_(this.x, this.y, this.x + this.width, this.y + this.height, this.backgroundColor.getPacked());
            gui.m_280488_(mc.f_91062_, "Binds (绑定)", this.x + 4, this.y + 5, 14540253);
            int iconX = this.x + this.width - 20 - 4;
            int iconY = this.y + 3;
            gui.m_280168_().m_85836_();
            gui.m_280168_().m_252880_((float)iconX, (float)iconY, 0.0F);
            RenderSystem.setShader(GameRenderer::m_172817_);
            RenderSystem.setShaderTexture(0, KEYBOARD_ICON);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            Matrix4f matrix = gui.m_280168_().m_85850_().m_252922_();
            BufferBuilder buffer = Tesselator.m_85913_().m_85915_();
            buffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85817_);
            buffer.m_252986_(matrix, 0.0F, 0.0F, 0.0F).m_7421_(0.0F, 0.0F).m_5752_();
            buffer.m_252986_(matrix, 20.0F, 0.0F, 0.0F).m_7421_(1.0F, 0.0F).m_5752_();
            buffer.m_252986_(matrix, 20.0F, 12.0F, 0.0F).m_7421_(1.0F, 1.0F).m_5752_();
            buffer.m_252986_(matrix, 0.0F, 12.0F, 0.0F).m_7421_(0.0F, 1.0F).m_5752_();
            BufferUploader.m_231202_(buffer.m_231175_());
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            gui.m_280168_().m_85849_();
            int textY = this.y + 18 + 4;

            for(Iterator var12 = entries.iterator(); var12.hasNext(); textY += 10) {
               Entry entry = (Entry)var12.next();
               String var10000 = entry.hack.getName();
               String line = "hack." + var10000 + ".bind." + entry.keyName;
               gui.m_280488_(mc.f_91062_, line, this.x + 4, textY, this.textColor.getPacked());
            }

            this.drawGradientBorder(gui, this.x, this.y, this.x + this.width, this.y + this.height);
            if (this.hack.isMovingMode()) {
               this.drawOuterGlow(gui, this.x, this.y, this.x + this.width, this.y + this.height);
            }

            if (this.dragging) {
               this.updateDragPosition();
            }

         }
      }
   }

   private void updateDragPosition() {
      double mouseXReal = mc.f_91067_.m_91589_() * (double)mc.m_91268_().m_85445_() / (double)mc.m_91268_().m_85443_();
      double mouseYReal = mc.f_91067_.m_91594_() * (double)mc.m_91268_().m_85446_() / (double)mc.m_91268_().m_85444_();
      this.x = (int)(mouseXReal - (double)this.dragX);
      this.y = (int)(mouseYReal - (double)this.dragY);
      int sw = mc.m_91268_().m_85445_();
      int sh = mc.m_91268_().m_85446_();
      this.x = Math.max(0, Math.min(this.x, sw - this.width));
      this.y = Math.max(0, Math.min(this.y, sh - this.height));
   }

   private void drawOuterGlow(GuiGraphics gui, int left, int top, int right, int bottom) {
      int glowColor = -1;
      gui.m_280509_(left - 1, top - 1, right + 1, top, glowColor);
      gui.m_280509_(left - 1, bottom, right + 1, bottom + 1, glowColor);
      gui.m_280509_(left - 1, top, left, bottom, glowColor);
      gui.m_280509_(right, top, right + 1, bottom, glowColor);
   }

   private void drawGradientBorder(GuiGraphics gui, int left, int top, int right, int bottom) {
      int w = right - left;
      int h = bottom - top;
      long time = System.currentTimeMillis();
      float offset = (float)(time % 3000L) / 3000.0F;

      int i;
      float p;
      int color;
      for(i = 0; i < w; ++i) {
         p = ((float)i / (float)w + offset) % 1.0F;
         color = this.interpolateColor(GRADIENT_COLORS, p);
         gui.m_280509_(left + i, top, left + i + 1, top + 1, color);
         p = (1.0F - (float)i / (float)w + offset) % 1.0F;
         color = this.interpolateColor(GRADIENT_COLORS, p);
         gui.m_280509_(left + i, bottom - 1, left + i + 1, bottom, color);
      }

      for(i = 0; i < h; ++i) {
         p = ((float)i / (float)h + offset) % 1.0F;
         color = this.interpolateColor(GRADIENT_COLORS, p);
         gui.m_280509_(left, top + i, left + 1, top + i + 1, color);
         p = (1.0F - (float)i / (float)h + offset) % 1.0F;
         color = this.interpolateColor(GRADIENT_COLORS, p);
         gui.m_280509_(right - 1, top + i, right, top + i + 1, color);
      }

   }

   private int interpolateColor(int[] colors, float progress) {
      int idx = (int)(progress * (float)(colors.length - 1));
      float blend = progress * (float)(colors.length - 1) - (float)idx;
      if (idx >= colors.length - 1) {
         return colors[colors.length - 1];
      } else {
         int c1 = colors[idx];
         int c2 = colors[idx + 1];
         int r = (int)((float)(c1 >> 16 & 255) * (1.0F - blend) + (float)(c2 >> 16 & 255) * blend);
         int g = (int)((float)(c1 >> 8 & 255) * (1.0F - blend) + (float)(c2 >> 8 & 255) * blend);
         int b = (int)((float)(c1 & 255) * (1.0F - blend) + (float)(c2 & 255) * blend);
         return -16777216 | r << 16 | g << 8 | b;
      }
   }

   private List collectEntries() {
      List list = new ArrayList();
      Iterator var2 = HackManager.getInstance().getHacks().iterator();

      while(var2.hasNext()) {
         Hack hack = (Hack)var2.next();
         int key = hack.getKeyBind();
         if (key != -1) {
            list.add(new Entry(hack, this.getKeyName(key)));
         }
      }

      return list;
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

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.visible) {
         return false;
      } else if (mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY <= (double)(this.y + 18) && button == 0) {
         this.dragging = true;
         this.dragX = (int)(mouseX - (double)this.x);
         this.dragY = (int)(mouseY - (double)this.y);
         return true;
      } else {
         return false;
      }
   }

   public void mouseDragged(double mouseX, double mouseY) {
      if (this.dragging) {
         this.updateDragPosition();
      }

   }

   public void mouseReleased() {
      this.dragging = false;
      this.hack.autoSavePosition(this.x, this.y);
   }

   private static class Entry {
      Hack hack;
      String keyName;

      Entry(Hack h, String k) {
         this.hack = h;
         this.keyName = k;
      }
   }
}
