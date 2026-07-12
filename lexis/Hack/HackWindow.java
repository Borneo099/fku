package lexis.Hack;

import com.mojang.math.Axis;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hackutil.config.ThemeConfig;
import lexis.Hack.Utils.ThemeColors.ThemeColors;
import lexis.Hack.Utils.ThemeColors.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class HackWindow {
   private String title;
   private int x;
   private int y;
   private int width;
   private int height;
   private boolean dragging;
   private int dragX;
   private int dragY;
   private boolean visible;
   private boolean collapsed;
   private double animProgress = 1.0;
   private static final double ANIM_SPEED = 12.0;
   private List buttons;
   private Hack.Category category;
   private HackGui parentGui;
   private static int titleColor = 0;
   private static ThemeConfig.ThemeType themeType;
   private static long rainbowStartTime;
   private static float flowOffset;
   private static final int TITLE_HEIGHT = 13;
   private static final int SHADOW_SIZE = 0;
   private static final int BUTTON_HEIGHT = 14;
   private static final int BUTTON_SPACING = 0;
   private static final int MAX_VISIBLE_BUTTONS = 25;
   private boolean needsWidthUpdate = true;
   private int scrollOffset = 0;
   private int maxScroll = 0;
   private boolean scrollbarHovered = false;
   private static final ResourceLocation TRIANGLE_TEXTURE;
   private long lastFrameTime = 0L;
   private static final int GLASS_BG_BASE = 858072357;
   private static final int GLASS_BG_TOP = 1077228853;
   private static final int GLASS_BG_BOTTOM = 857480220;
   private static final int GLASS_INNER_LIGHT = 419430399;
   private static final int GLASS_HIGHLIGHT = 1090519039;
   private static final int GLASS_BORDER = -2142220208;
   private static final int GLASS_BORDER_INNER = 822083583;
   private static final int TITLE_TEXT_COLOR = -1513240;
   private static final int LIQUID_WAVE_1 = 553648127;
   private static final int LIQUID_WAVE_2 = 419430399;
   private static final int LIQUID_WAVE_3 = 318767103;
   private static float liquidPhase;
   private static long lastLiquidTime;

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public boolean isDragging() {
      return this.dragging;
   }

   public int getDragX() {
      return this.dragX;
   }

   public int getDragY() {
      return this.dragY;
   }

   public HackWindow(String title, Hack.Category category, int x, int y, int width, HackGui parentGui) {
      this.title = title;
      this.category = category;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = 13;
      this.dragging = false;
      this.visible = true;
      this.collapsed = false;
      this.buttons = new ArrayList();
      this.parentGui = parentGui;
      this.animProgress = 1.0;
   }

   public void updateWidth() {
      Minecraft mc = Minecraft.m_91087_();
      if (mc != null && mc.f_91062_ != null) {
         int maxWidth = 110;
         Iterator var3 = this.buttons.iterator();

         while(var3.hasNext()) {
            HackButton button = (HackButton)var3.next();
            String displayName = button.getHack().getButtonName();
            int textWidth = mc.f_91062_.m_92895_(displayName);
            int buttonWidth = textWidth + 8 + 12 + 5 + 35 + 5;
            if (buttonWidth > maxWidth) {
               maxWidth = buttonWidth;
            }
         }

         if (this.width != maxWidth) {
            this.width = maxWidth;
         }

      }
   }

   private int getCurrentTitleColor() {
      switch (themeType) {
         case RAINBOW:
            long time = System.currentTimeMillis() - rainbowStartTime;
            float hue = (float)(time % 5000L) / 5000.0F;
            return -16777216 | Color.HSBtoRGB(hue, 0.8F, 1.0F);
         case RAINBOW_FLOW:
            float flowSpeed = (float)(System.currentTimeMillis() % 3000L) / 3000.0F;
            float waveOffset = ((float)this.x + flowOffset) / 50.0F;
            float flowHue = (flowSpeed + waveOffset) % 1.0F;
            return -16777216 | Color.HSBtoRGB(flowHue, 0.9F, 1.0F);
         case RAINBOW_SHIFT:
            long shiftTime = System.currentTimeMillis() - rainbowStartTime;
            float shiftHue = (float)(shiftTime % 8000L) / 8000.0F;
            return -16777216 | Color.HSBtoRGB(shiftHue, 0.7F, 1.0F);
         default:
            return titleColor != 0 ? titleColor : -1513240;
      }
   }

   private void updateAnimation() {
      if (this.lastFrameTime == 0L) {
         this.lastFrameTime = System.currentTimeMillis();
      } else {
         long currentTime = System.currentTimeMillis();
         float delta = (float)(currentTime - this.lastFrameTime) / 1000.0F;
         this.lastFrameTime = currentTime;
         delta = Math.min(delta, 0.05F);
         this.animProgress += (double)((float)(this.collapsed ? -1 : 1) * delta) * 12.0;
         this.animProgress = Mth.m_14008_(this.animProgress, 0.0, 1.0);
      }
   }

   private void updateLiquidAnimation() {
      long now = System.currentTimeMillis();
      float dt = (float)(now - lastLiquidTime) / 1000.0F;
      lastLiquidTime = now;
      liquidPhase += dt * 0.6F;
      if ((double)liquidPhase > 6.283185307179586) {
         liquidPhase -= 6.2831855F;
      }

   }

   private void drawLiquidEffect(GuiGraphics gui, int rx, int ry, int rw, int rh) {
      if (rh >= 17) {
         int innerY = ry + 13;
         int innerH = rh - 13;
         int wave1Y = innerY + (int)((Math.sin((double)liquidPhase) * 0.5 + 0.5) * (double)(innerH - 2));
         int wave2Y = innerY + (int)((Math.sin((double)liquidPhase + 2.0734512337608098) * 0.5 + 0.5) * (double)(innerH - 2));
         int wave3Y = innerY + (int)((Math.sin((double)liquidPhase + 4.178318364096955) * 0.5 + 0.5) * (double)(innerH - 2));
         int margin = 4;
         gui.m_280509_(rx + margin, wave1Y, rx + rw - margin, wave1Y + 1, 553648127);
         gui.m_280509_(rx + margin, wave2Y, rx + rw - margin, wave2Y + 1, 419430399);
         gui.m_280509_(rx + margin, wave3Y, rx + rw - margin, wave3Y + 1, 318767103);
      }
   }

   private void drawTriangle(GuiGraphics gui, int x, int y, int size, float rotation) {
      gui.m_280168_().m_85836_();
      gui.m_280168_().m_252880_((float)x, (float)y, 0.0F);
      gui.m_280168_().m_252781_(Axis.f_252403_.m_252977_(rotation));
      gui.m_280163_(TRIANGLE_TEXTURE, -size / 2, -size / 2, 0.0F, 0.0F, size, size, size, size);
      gui.m_280168_().m_85849_();
   }

   public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
      Minecraft mc = Minecraft.m_91087_();
      if (this.visible) {
         if (this.needsWidthUpdate) {
            this.updateWidth();
         }

         ThemeColors colors = ThemeManager.getColors();
         this.updateAnimation();
         this.updateLiquidAnimation();
         int contentFullHeight = Math.min(352, this.buttons.size() * 14 + 2);
         int currentContentHeight = (int)((double)contentFullHeight * this.animProgress);
         this.height = 13 + currentContentHeight;
         flowOffset += 0.02F;
         if (flowOffset > 100.0F) {
            flowOffset = 0.0F;
         }

         int totalButtonHeight = this.buttons.size() * 14;
         this.maxScroll = Math.max(0, (totalButtonHeight - 350) / 14);
         gui.m_280509_(this.x, this.y, this.x + this.width, this.y + this.height, 858072357);
         gui.m_280024_(this.x + 2, this.y + 2, this.x + this.width - 2, this.y + this.height - 2, 1077228853, 857480220);
         this.drawLiquidEffect(gui, this.x, this.y, this.width, this.height);
         gui.m_280509_(this.x + 6, this.y + 1, this.x + this.width - 6, this.y + 2, 1090519039);
         gui.m_280509_(this.x + 8, this.y + 2, this.x + this.width - 8, this.y + 3, 419430399);
         if (currentContentHeight > 0) {
            gui.m_280509_(this.x + 4, this.y + 13 - 1, this.x + this.width - 4, this.y + 13, 1090519039);
            gui.m_280509_(this.x + 4, this.y + 13, this.x + this.width - 4, this.y + 13 + 1, 1610612736);
         }

         gui.m_280509_(this.x, this.y, this.x + this.width, this.y + 1, -2142220208);
         gui.m_280509_(this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, -2142220208);
         gui.m_280509_(this.x, this.y, this.x + 1, this.y + this.height, -2142220208);
         gui.m_280509_(this.x + this.width - 1, this.y, this.x + this.width, this.y + this.height, -2142220208);
         gui.m_280509_(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + 2, 822083583);
         gui.m_280509_(this.x + 1, this.y + this.height - 2, this.x + this.width - 1, this.y + this.height - 1, 822083583);
         gui.m_280509_(this.x + 1, this.y + 1, this.x + 2, this.y + this.height - 1, 822083583);
         gui.m_280509_(this.x + this.width - 2, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, 822083583);
         int titleTextColor = this.getCurrentTitleColor();
         gui.m_280488_(mc.f_91062_, this.title, this.x + 8, this.y + 2, titleTextColor);
         float rotation = this.collapsed ? 90.0F : 180.0F;
         if (this.animProgress < 1.0 && this.animProgress > 0.0) {
            if (this.collapsed) {
               rotation = 180.0F - 90.0F * (float)this.animProgress;
            } else {
               rotation = 90.0F + 90.0F * (float)this.animProgress;
            }
         }

         this.drawTriangle(gui, this.x + this.width - 8, this.y + 6, 6, rotation);
         if (currentContentHeight > 0) {
            int startIndex = this.scrollOffset;
            int endIndex = Math.min(startIndex + 25, this.buttons.size());
            if (endIndex > startIndex) {
               int buttonY = this.y + 13 + 1;
               int visibleButtonCount = endIndex - startIndex;

               int scrollbarX;
               float delay;
               for(scrollbarX = startIndex; scrollbarX < endIndex; ++scrollbarX) {
                  HackButton button = (HackButton)this.buttons.get(scrollbarX);
                  float buttonIndex = (float)(scrollbarX - startIndex);
                  delay = buttonIndex / (float)visibleButtonCount * 0.6F;
                  float buttonAlpha;
                  float progressForButton;
                  if (this.collapsed) {
                     progressForButton = Math.max(0.0F, ((float)this.animProgress - delay) / (1.0F - delay));
                     buttonAlpha = Mth.m_14036_(progressForButton, 0.0F, 1.0F);
                     if (this.animProgress < (double)delay) {
                        buttonAlpha = 0.0F;
                     }
                  } else {
                     progressForButton = Math.max(0.0F, ((float)this.animProgress - delay) / (1.0F - delay));
                     buttonAlpha = Mth.m_14036_(progressForButton, 0.0F, 1.0F);
                  }

                  if (buttonAlpha > 0.01F) {
                     button.renderWithAlpha(gui, this.x + 1, buttonY, this.width - 2, 14, mouseX, mouseY, buttonAlpha);
                  }

                  buttonY += 14;
               }

               if (this.buttons.size() > 25 && this.animProgress > 0.10000000149011612) {
                  scrollbarX = this.x + this.width - 3;
                  int scrollbarY = this.y + 13 + 1;
                  int scrollbarHeight = currentContentHeight - 2;
                  gui.m_280509_(scrollbarX, scrollbarY, scrollbarX + 2, scrollbarY + scrollbarHeight, 1140850688);
                  delay = (float)this.scrollOffset / (float)(this.buttons.size() - 25);
                  int sliderHeight = Math.max(20, (int)((float)(scrollbarHeight * 25) / (float)this.buttons.size()));
                  int sliderY = scrollbarY + (int)(delay * (float)(scrollbarHeight - sliderHeight));
                  this.scrollbarHovered = mouseX >= scrollbarX && mouseX <= scrollbarX + 2 && mouseY >= sliderY && mouseY <= sliderY + sliderHeight;
                  int sliderColor = this.scrollbarHovered ? -5592406 : -7829368;
                  gui.m_280509_(scrollbarX, sliderY, scrollbarX + 2, sliderY + sliderHeight, sliderColor);
               }

            }
         }
      }
   }

   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (this.visible) {
         if (mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY <= (double)(this.y + 13)) {
            if (button == 0) {
               this.dragging = true;
               this.dragX = (int)(mouseX - (double)this.x);
               this.dragY = (int)(mouseY - (double)this.y);
            } else if (button == 1) {
               this.collapsed = !this.collapsed;
            }

         } else if (!(this.animProgress < 0.10000000149011612)) {
            int startIndex = this.scrollOffset;
            int endIndex = Math.min(startIndex + 25, this.buttons.size());
            int buttonY = this.y + 13 + 1;

            for(int i = startIndex; i < endIndex; ++i) {
               HackButton hackButton = (HackButton)this.buttons.get(i);
               if (mouseX >= (double)(this.x + 1) && mouseX <= (double)(this.x + this.width - 1) && mouseY >= (double)buttonY && mouseY <= (double)(buttonY + 14)) {
                  hackButton.mouseClicked(mouseX, mouseY, button, this.x + 1, buttonY, this.width - 2);
                  return;
               }

               buttonY += 14;
            }

         }
      }
   }

   public void mouseReleased() {
      this.dragging = false;
   }

   public void mouseDragged(double mouseX, double mouseY) {
      if (this.dragging) {
         this.x = (int)(mouseX - (double)this.dragX);
         this.y = (int)(mouseY - (double)this.dragY);
      }

   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      if (!this.visible) {
         return false;
      } else if (this.animProgress < 0.10000000149011612) {
         return false;
      } else if (mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)(this.y + 13) && mouseY <= (double)(this.y + this.height)) {
         int newScroll = (int)((double)this.scrollOffset - delta);
         this.scrollOffset = Math.max(0, Math.min(this.buttons.size() - 25, newScroll));
         return true;
      } else {
         return false;
      }
   }

   public void addButton(HackButton button) {
      this.buttons.add(button);
      this.needsWidthUpdate = true;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public Hack.Category getCategory() {
      return this.category;
   }

   public boolean isCollapsed() {
      return this.collapsed;
   }

   public void setCollapsed(boolean collapsed) {
      this.collapsed = collapsed;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public void setPos(int x, int y) {
      this.x = x;
      this.y = y;
   }

   public List getButtons() {
      return this.buttons;
   }

   public int getScrollOffset() {
      return this.scrollOffset;
   }

   static {
      themeType = ThemeConfig.ThemeType.SOLID;
      rainbowStartTime = System.currentTimeMillis();
      flowOffset = 0.0F;
      TRIANGLE_TEXTURE = new ResourceLocation("lexis", "gui/amc.png");
      liquidPhase = 0.0F;
      lastLiquidTime = System.currentTimeMillis();
   }
}
