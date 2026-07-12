package lexis.Hack;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.awt.Button;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hackutil.FavoritesManager;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Hackutil.settings.BlockListScreen;
import lexis.Hack.Hackutil.settings.BlockListSetting;
import lexis.Hack.Hackutil.settings.ItemListScreen;
import lexis.Hack.Hackutil.settings.ItemListSetting;
import lexis.Hack.Utils.Colors.ColorSettingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class SettingsWindow extends Screen {
   private final Hack hack;
   private final Screen parent;
   private final HackConfig config;
   private Button keyBindButton;
   private boolean bindingKey = false;
   private int windowX;
   private int windowY;
   private int windowWidth = 500;
   private int windowHeight = 400;
   private boolean dragging = false;
   private int dragX;
   private int dragY;
   private boolean dragHandleHovered = false;
   private float targetScrollOffset = 0.0F;
   private float currentScrollOffset = 0.0F;
   private long lastScrollTime = System.currentTimeMillis();
   private static final float SCROLL_SPEED = 0.3F;
   private int scrollOffset = 0;
   private int maxScroll = 0;
   private static final int ITEM_HEIGHT = 40;
   private static final int PADDING = 15;
   private float startOffset;
   private float targetSlideOffset;
   private long animStartTime;
   private boolean animating = false;
   private boolean isClosing = false;
   private static final long ANIM_DURATION = 250L;
   private boolean initialized = false;
   private List widgets = new ArrayList();
   private EditBox focusedTextBox = null;
   private static final ResourceLocation STAR_EMPTY = new ResourceLocation("lexis", "gui/favorite_no.png");
   private static final ResourceLocation STAR_FILLED = new ResourceLocation("lexis", "gui/favorite_yes.png");
   private int hoveredWidgetIndex = -1;
   private long hoverStartTime = 0L;
   private float slideOffset;
   private static final int TOOLTIP_DELAY = 500;
   private Minecraft mc = Minecraft.m_91087_();
   private static final int WIN_BG = -434891746;
   private static final int TITLE_BG = -14145486;
   private static final int TITLE_BG_HOVERED = -12829616;
   private static final int WIN_BORDER = -11513756;
   private static final int SCROLL_TRACK = -935576506;
   private static final int SCROLL_SLIDER = -8882046;
   private static final int SCROLL_SLIDER_HOVERED = -4934456;
   private static final int EDITBOX_BG = -14145486;
   private static final int EDITBOX_BORDER = -12171696;
   private static final int SLIDER_TRACK_C = -13487556;
   private static final int SLIDER_FILL = -12156216;
   private static final int SLIDER_KNOB = -7566176;
   private static final int SLIDER_KNOB_DRAGGING = -4934456;
   private static final int MODE_BG = -12829626;
   private static final int MODE_BG_HOVERED = -12171696;
   private static final int MODE_BORDER = -11513766;
   private static final int WIDGET_BG = -12829616;
   private static final int WIDGET_BORDER = -10197896;
   private static final int BTN_BG = -12171686;

   public int getWindowX() {
      return this.windowX;
   }

   public int getWindowY() {
      return this.windowY;
   }

   public SettingsWindow(Hack hack, Screen parent) {
      super(Component.m_237113_(hack.getName() + " 设置"));
      this.hack = hack;
      this.parent = parent;
      this.config = HackConfig.getInstance();
      this.startOffset = (float)this.f_96543_;
      this.targetSlideOffset = 0.0F;
      this.animStartTime = System.currentTimeMillis();
      this.animating = true;
      this.isClosing = false;
      this.slideOffset = (float)this.f_96543_;
   }

   protected void m_7856_() {
      super.m_7856_();
      this.windowX = (this.f_96543_ - this.windowWidth) / 2;
      this.windowY = (this.f_96544_ - this.windowHeight) / 2;
      this.widgets.clear();
      int index = 0;

      for(Iterator var2 = this.hack.getSettings().iterator(); var2.hasNext(); ++index) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         int relY = 45 + index * 40;
         SettingWidget widget = this.createWidget(setting, 15, relY, this.windowWidth - 30 - 30, 20);
         this.widgets.add(widget);
      }

      int contentHeight = index * 40;
      this.maxScroll = Math.max(0, contentHeight - (this.windowHeight - 80));
      if (!this.initialized) {
         this.startOffset = (float)this.f_96543_;
         this.targetSlideOffset = 0.0F;
         this.animStartTime = System.currentTimeMillis();
         this.animating = true;
         this.isClosing = false;
         this.slideOffset = (float)this.f_96543_;
         this.initialized = true;
      }

   }

   private SettingWidget createWidget(Hack.Setting setting, int x, int y, int width, int height) {
      switch (setting.getType()) {
         case BOOLEAN:
            return new BooleanWidget(setting, x, y, width, height, this);
         case INTEGER:
         case DOUBLE:
            return new SliderWidget(setting, x, y, width, height, this);
         case MODE:
            return new ModeWidget(setting, x, y, width, height, this);
         case COLOR:
            return new ColorWidget(setting, x, y, width, height, this);
         case STRING:
            return new StringWidget(setting, x, y, width, height, this);
         case BUTTON:
            return new ButtonWidget(setting, x, y, width, height, this);
         case ITEM_LIST:
            return new ItemListWidget(setting, x, y, width, height, this);
         case BLOCK_LIST:
            return new BlockListWidget(setting, x, y, width, height, this);
         default:
            return null;
      }
   }

   public void autoSave() {
      this.config.saveHackSettings(this.hack.getName(), this.hack.getSettings());
   }

   public void m_88315_(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
      this.updateScrollAnimation();
      if (this.animating) {
         long elapsed = System.currentTimeMillis() - this.animStartTime;
         float progress = Math.min(1.0F, (float)elapsed / 250.0F);
         this.slideOffset = this.startOffset + (this.targetSlideOffset - this.startOffset) * progress;
         if (elapsed >= 250L) {
            this.slideOffset = this.targetSlideOffset;
            this.animating = false;
            if (this.isClosing) {
               if (this.parent instanceof HackGui) {
                  ((HackGui)this.parent).startSlideIn();
               }

               Minecraft.m_91087_().m_91152_(this.parent);
               return;
            }
         }
      }

      gui.m_280168_().m_85836_();

      try {
         gui.m_280168_().m_252880_(this.slideOffset, 0.0F, 0.0F);
         gui.m_280509_(this.windowX, this.windowY, this.windowX + this.windowWidth, this.windowY + this.windowHeight, -434891746);
         this.dragHandleHovered = mouseX >= this.windowX && mouseX <= this.windowX + this.windowWidth && mouseY >= this.windowY && mouseY <= this.windowY + 30;
         int titleColor = this.dragHandleHovered ? -12829616 : -14145486;
         gui.m_280509_(this.windowX, this.windowY, this.windowX + this.windowWidth, this.windowY + 30, titleColor);
         gui.m_280488_(this.f_96547_, "§l" + this.hack.getName() + " 设置", this.windowX + 12, this.windowY + 8, -1);
         boolean isFavorite = FavoritesManager.getInstance().isFavorite(this.hack.getName());
         int starX = this.windowX + this.windowWidth - 45;
         int starY = this.windowY + 5;
         int starSize = 16;
         boolean starHovered = mouseX >= starX && mouseX <= starX + starSize && mouseY >= starY && mouseY <= starY + starSize;
         gui.m_280168_().m_85836_();
         gui.m_280168_().m_252880_((float)starX, (float)starY, 0.0F);
         RenderSystem.setShader(GameRenderer::m_172817_);
         RenderSystem.setShaderTexture(0, isFavorite ? STAR_FILLED : STAR_EMPTY);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         Matrix4f matrix = gui.m_280168_().m_85850_().m_252922_();
         BufferBuilder buffer = Tesselator.m_85913_().m_85915_();
         buffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85817_);
         buffer.m_252986_(matrix, 0.0F, 0.0F, 0.0F).m_7421_(0.0F, 0.0F).m_5752_();
         buffer.m_252986_(matrix, (float)starSize, 0.0F, 0.0F).m_7421_(1.0F, 0.0F).m_5752_();
         buffer.m_252986_(matrix, (float)starSize, (float)starSize, 0.0F).m_7421_(1.0F, 1.0F).m_5752_();
         buffer.m_252986_(matrix, 0.0F, (float)starSize, 0.0F).m_7421_(0.0F, 1.0F).m_5752_();
         BufferUploader.m_231202_(buffer.m_231175_());
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         gui.m_280168_().m_85849_();
         if (starHovered) {
            List tooltip = new ArrayList();
            tooltip.add(Component.m_237113_(isFavorite ? "§e点击取消收藏" : "§e点击收藏"));
            gui.m_280666_(this.f_96547_, tooltip, mouseX, mouseY);
         }

         boolean closeHovered = mouseX >= this.windowX + this.windowWidth - 25 && mouseX <= this.windowX + this.windowWidth - 10 && mouseY >= this.windowY + 5 && mouseY <= this.windowY + 25;
         gui.m_280488_(this.f_96547_, "✕", this.windowX + this.windowWidth - 18, this.windowY + 8, closeHovered ? -43691 : -5592406);
         int index;
         int y;
         int tooltipX;
         if (this.maxScroll > 0) {
            index = this.windowX + this.windowWidth - 15;
            int scrollbarY = this.windowY + 35;
            int scrollbarHeight = this.windowHeight - 70;
            gui.m_280509_(index, scrollbarY, index + 6, scrollbarY + scrollbarHeight, -935576506);
            float progress = (float)this.scrollOffset / (float)this.maxScroll;
            y = Math.max(20, scrollbarHeight * (this.windowHeight - 70) / (this.maxScroll + this.windowHeight - 70));
            int sliderY = scrollbarY + (int)(progress * (float)(scrollbarHeight - y));
            boolean scrollHovered = mouseX >= index && mouseX <= index + 6 && mouseY >= sliderY && mouseY <= sliderY + y;
            tooltipX = scrollHovered ? -4934456 : -8882046;
            gui.m_280509_(index, sliderY, index + 6, sliderY + y, tooltipX);
         }

         gui.m_280588_(this.windowX + 2, this.windowY + 32, this.windowX + this.windowWidth - 20, this.windowY + this.windowHeight - 10);
         index = 0;
         boolean foundHovered = false;

         for(Iterator var32 = this.hack.getSettings().iterator(); var32.hasNext(); ++index) {
            Hack.Setting setting = (Hack.Setting)var32.next();
            y = this.windowY + 45 + index * 40 - this.scrollOffset;
            if (y >= this.windowY + 35 && y <= this.windowY + this.windowHeight - 20) {
               gui.m_280056_(this.f_96547_, setting.getName() + ":", this.windowX + 15, y + 5, 13421772, false);
               if (index < this.widgets.size()) {
                  ((SettingWidget)this.widgets.get(index)).render(gui, mouseX, mouseY, partialTick, this.scrollOffset);
               }

               if (!foundHovered && mouseX >= this.windowX + 15 && mouseX <= this.windowX + this.windowWidth - 30 && mouseY >= y && mouseY <= y + 20) {
                  if (this.hoveredWidgetIndex != index) {
                     this.hoveredWidgetIndex = index;
                     this.hoverStartTime = System.currentTimeMillis();
                  }

                  foundHovered = true;
                  boolean isDragging = false;
                  if (index < this.widgets.size()) {
                     SettingWidget widget = (SettingWidget)this.widgets.get(index);
                     if (widget instanceof SliderWidget) {
                        isDragging = ((SliderWidget)widget).isDragging();
                     }
                  }

                  if (!isDragging && System.currentTimeMillis() - this.hoverStartTime > 500L) {
                     List tooltip = new ArrayList();
                     String[] var37 = setting.getDescriptionLines();
                     int tooltipY = var37.length;

                     for(int var23 = 0; var23 < tooltipY; ++var23) {
                        String line = var37[var23];
                        tooltip.add(Component.m_237113_("§7" + line));
                     }

                     tooltipX = mouseX + 10;
                     tooltipY = mouseY + 10;
                     if (tooltipX + 200 > this.f_96543_) {
                        tooltipX = mouseX - 210;
                     }

                     if (tooltipY + tooltip.size() * 10 + 10 > this.f_96544_) {
                        tooltipY = mouseY - tooltip.size() * 10 - 20;
                     }

                     gui.m_280666_(this.f_96547_, tooltip, tooltipX, tooltipY);
                  }
               }
            }
         }

         gui.m_280618_();
         this.drawBorder(gui, this.windowX, this.windowY, this.windowX + this.windowWidth, this.windowY + this.windowHeight, 1, -11513756);
      } finally {
         gui.m_280168_().m_85849_();
      }
   }

   private void updateScrollAnimation() {
      if (Math.abs(this.currentScrollOffset - this.targetScrollOffset) > 0.01F) {
         this.currentScrollOffset += (this.targetScrollOffset - this.currentScrollOffset) * 0.3F;
         this.scrollOffset = Math.round(this.currentScrollOffset);
      } else {
         this.currentScrollOffset = this.targetScrollOffset;
         this.scrollOffset = (int)this.targetScrollOffset;
      }

   }

   private void drawBorder(GuiGraphics gui, int left, int top, int right, int bottom, int thickness, int color) {
      this.fill(gui, left, top, right, top + thickness, color);
      this.fill(gui, left, bottom - thickness, right, bottom, color);
      this.fill(gui, left, top, left + thickness, bottom, color);
      this.fill(gui, right - thickness, top, right, bottom, color);
   }

   private void fill(GuiGraphics gui, int left, int top, int right, int bottom, int color) {
      if (left < right && top < bottom) {
         gui.m_280509_(left, top, right, bottom, color);
      }

   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (this.animating) {
         return false;
      } else {
         int starX = this.windowX + this.windowWidth - 45;
         int starY = this.windowY + 5;
         int starSize = 16;
         if (mouseX >= (double)starX && mouseX <= (double)(starX + starSize) && mouseY >= (double)starY && mouseY <= (double)(starY + starSize)) {
            FavoritesManager.getInstance().toggleFavorite(this.hack.getName());
            return true;
         } else if (mouseX >= (double)(this.windowX + this.windowWidth - 25) && mouseX <= (double)(this.windowX + this.windowWidth - 10) && mouseY >= (double)(this.windowY + 5) && mouseY <= (double)(this.windowY + 25)) {
            this.startCloseAnimation();
            return true;
         } else if (button == 0 && mouseX >= (double)this.windowX && mouseX <= (double)(this.windowX + this.windowWidth) && mouseY >= (double)this.windowY && mouseY <= (double)(this.windowY + 30)) {
            this.dragging = true;
            this.dragX = (int)(mouseX - (double)this.windowX);
            this.dragY = (int)(mouseY - (double)this.windowY);
            return true;
         } else {
            Iterator var9 = this.widgets.iterator();

            SettingWidget widget;
            do {
               if (!var9.hasNext()) {
                  return super.m_6375_(mouseX, mouseY, button);
               }

               widget = (SettingWidget)var9.next();
            } while(!widget.mouseClicked(mouseX, mouseY, button, this.scrollOffset));

            return true;
         }
      }
   }

   private void startCloseAnimation() {
      this.startOffset = this.slideOffset;
      this.targetSlideOffset = (float)this.f_96543_;
      this.animStartTime = System.currentTimeMillis();
      this.animating = true;
      this.isClosing = true;
   }

   public boolean m_6348_(double mouseX, double mouseY, int button) {
      this.dragging = false;
      Iterator var6 = this.widgets.iterator();

      while(var6.hasNext()) {
         SettingWidget widget = (SettingWidget)var6.next();
         if (widget instanceof SliderWidget) {
            ((SliderWidget)widget).mouseReleased();
         }
      }

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
         return super.m_7979_(mouseX, mouseY, button, dragX, dragY);
      }
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      if (mouseX >= (double)this.windowX && mouseX <= (double)(this.windowX + this.windowWidth) && mouseY >= (double)this.windowY && mouseY <= (double)(this.windowY + this.windowHeight)) {
         int newScroll = (int)Math.max(0.0, Math.min((double)this.maxScroll, (double)this.scrollOffset - delta * 20.0));
         this.targetScrollOffset = (float)newScroll;
         this.lastScrollTime = System.currentTimeMillis();
         return true;
      } else {
         return false;
      }
   }

   public void setFocusedTextBox(EditBox box) {
      if (this.focusedTextBox != null && this.focusedTextBox != box) {
         this.focusedTextBox.m_93692_(false);
      }

      this.focusedTextBox = box;
      if (this.focusedTextBox != null) {
         this.focusedTextBox.m_93692_(true);
      }

   }

   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      if (this.animating) {
         return false;
      } else if (this.focusedTextBox != null) {
         if (keyCode != 257 && keyCode != 335) {
            return this.focusedTextBox.m_7933_(keyCode, scanCode, modifiers);
         } else {
            this.focusedTextBox.m_93692_(false);
            this.setFocusedTextBox((EditBox)null);
            return true;
         }
      } else if (keyCode == 256) {
         this.startCloseAnimation();
         return true;
      } else {
         return super.m_7933_(keyCode, scanCode, modifiers);
      }
   }

   public boolean m_5534_(char codePoint, int modifiers) {
      return this.focusedTextBox != null ? this.focusedTextBox.m_5534_(codePoint, modifiers) : super.m_5534_(codePoint, modifiers);
   }

   public void m_7379_() {
      if (this.parent != null) {
         Minecraft.m_91087_().m_91152_(this.parent);
      } else {
         Minecraft.m_91087_().m_91152_((Screen)null);
      }

   }

   public boolean m_7043_() {
      return false;
   }

   private interface SettingWidget {
      void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick, int scrollOffset);

      boolean mouseClicked(double mouseX, double mouseY, int button, int scrollOffset);
   }

   private class BooleanWidget implements SettingWidget {
      private final Hack.Setting setting;
      private final int x;
      private final int y;
      private final int width;
      private final int height;
      private final SettingsWindow screen;
      private static final ResourceLocation SWITCH_TRACK = new ResourceLocation("lexis", "gui/amcs2.png");
      private static final int TRACK_WIDTH = 44;
      private static final int TRACK_HEIGHT = 20;
      private static final int KNOB_SIZE = 18;
      private float knobPosition = 0.0F;
      private float targetKnobPosition = 0.0F;
      private long lastUpdateTime = System.currentTimeMillis();
      private static final float ANIM_SPEED = 0.15F;

      public BooleanWidget(Hack.Setting setting, int x, int y, int width, int height, SettingsWindow screen) {
         this.setting = setting;
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.screen = screen;
         this.targetKnobPosition = setting.getBoolean() ? 1.0F : 0.0F;
         this.knobPosition = this.targetKnobPosition;
      }

      public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick, int scrollOffset) {
         int baseX = this.screen.windowX + this.x;
         int baseY = this.screen.windowY + this.y - scrollOffset;
         if (baseY >= this.screen.windowY + 35 && baseY + this.height <= this.screen.windowY + SettingsWindow.this.windowHeight - 20) {
            boolean value = this.setting.getBoolean();
            this.targetKnobPosition = value ? 1.0F : 0.0F;
            long currentTime = System.currentTimeMillis();
            float delta = (float)(currentTime - this.lastUpdateTime) / 1000.0F;
            this.lastUpdateTime = currentTime;
            float diff = this.targetKnobPosition - this.knobPosition;
            if (Math.abs(diff) > 0.001F) {
               this.knobPosition += diff * Math.min(1.0F, 0.15F * delta * 60.0F);
               this.knobPosition = Math.max(0.0F, Math.min(1.0F, this.knobPosition));
            } else {
               this.knobPosition = this.targetKnobPosition;
            }

            int trackX = baseX + 200 + 13;
            int trackY = baseY + (this.height - 20) / 2;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            gui.m_280163_(SWITCH_TRACK, trackX, trackY, 0.0F, 0.0F, 44, 20, 44, 20);
            int knobX = trackX + (int)(26.0F * this.knobPosition);
            int knobY = trackY + 1;
            int cx = knobX + 9;
            int cy = knobY + 9;
            int radius = 9;
            RenderSystem.setShader(GameRenderer::m_172811_);
            Tesselator tesselator = Tesselator.m_85913_();
            BufferBuilder buffer = tesselator.m_85915_();
            Matrix4f matrix = gui.m_280168_().m_85850_().m_252922_();
            buffer.m_166779_(Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.f_85815_);
            long time = System.currentTimeMillis();
            float baseHue = 0.75F + (float)time / 1500.0F % 0.2F;
            if (baseHue > 1.0F) {
               --baseHue;
            }

            float alpha = value ? 1.0F : 0.6F;
            int segments = 48;

            for(int i = 0; i <= segments; ++i) {
               double angle = 6.283185307179586 * (double)i / (double)segments;
               float offset = (float)Math.sin(angle * 2.0 + (double)time * 0.008) * 0.08F;
               float hue = baseHue + offset;
               if (hue > 1.0F) {
                  --hue;
               }

               if (hue < 0.0F) {
                  ++hue;
               }

               int rgb = Color.HSBtoRGB(hue, 0.9F, 1.0F);
               float r = (float)(rgb >> 16 & 255) / 255.0F;
               float g = (float)(rgb >> 8 & 255) / 255.0F;
               float b = (float)(rgb & 255) / 255.0F;
               float px = (float)((double)cx + (double)radius * Math.cos(angle));
               float py = (float)((double)cy + (double)radius * Math.sin(angle));
               buffer.m_252986_(matrix, px, py, 0.0F).m_85950_(r, g, b, alpha).m_5752_();
            }

            tesselator.m_85914_();
            RenderSystem.disableBlend();
         }
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button, int scrollOffset) {
         int baseX = this.screen.windowX + this.x;
         int baseY = this.screen.windowY + this.y - scrollOffset;
         if (baseY >= this.screen.windowY + 35 && baseY + this.height <= this.screen.windowY + SettingsWindow.this.windowHeight - 20) {
            int trackLeft = baseX + 200 + 13;
            int trackRight = trackLeft + 44;
            if (mouseX >= (double)trackLeft && mouseX <= (double)trackRight && mouseY >= (double)baseY && mouseY <= (double)(baseY + this.height)) {
               this.setting.setValue(!this.setting.getBoolean());
               this.screen.autoSave();
               this.targetKnobPosition = this.setting.getBoolean() ? 1.0F : 0.0F;
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   private class SliderWidget implements SettingWidget {
      private final Hack.Setting setting;
      private final int x;
      private final int y;
      private final int width;
      private final int height;
      private final SettingsWindow screen;
      private final boolean isInteger;
      private final double min;
      private final double max;
      private boolean dragging = false;
      private CustomEditBox valueBox;
      private boolean isEditing = false;
      private boolean isUpdating = false;

      public SliderWidget(Hack.Setting setting, int x, int y, int width, int height, SettingsWindow screen) {
         this.setting = setting;
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.screen = screen;
         this.isInteger = setting.getType() == Hack.Setting.SettingType.INTEGER;
         Object minObj = setting.getMin();
         Object maxObj = setting.getMax();
         this.min = minObj instanceof Number ? ((Number)minObj).doubleValue() : 0.0;
         this.max = maxObj instanceof Number ? ((Number)maxObj).doubleValue() : 100.0;
         this.valueBox = SettingsWindow.this.new CustomEditBox(SettingsWindow.this.f_96547_, screen.windowX + x + 300, screen.windowY + y, 60, height - 2, Component.m_237113_(""));
         this.valueBox.m_94199_(10);
         this.valueBox.m_94144_(this.getValueText());
         this.valueBox.m_94194_(true);
         this.valueBox.m_94190_(true);
         this.valueBox.m_94151_((text) -> {
            if (this.isEditing && !this.isUpdating) {
               if (!text.isEmpty()) {
                  if (text.equals("-")) {
                     if (!(this.min < 0.0)) {
                        this.valueBox.m_94144_(this.getValueText());
                     }
                  } else {
                     if (this.isInteger) {
                        if (!text.matches("-?\\d*")) {
                           this.valueBox.m_94144_(this.getValueText());
                        }
                     } else if (!text.matches("-?\\d*\\.?\\d*")) {
                        this.valueBox.m_94144_(this.getValueText());
                     }

                  }
               }
            }
         });
         this.valueBox.m_94153_((text) -> {
            if (text.isEmpty()) {
               return true;
            } else if (text.equals("-")) {
               return this.min < 0.0;
            } else {
               return this.isInteger ? text.matches("-?\\d*") : text.matches("-?\\d*\\.?\\d*");
            }
         });
      }

      public boolean isDragging() {
         return this.dragging;
      }

      private String getValueText() {
         double value = this.isInteger ? (double)this.setting.getInt() : this.setting.getDouble();
         Hack.ValueDisplay display = this.setting.getDisplayFormat();
         if (display == null) {
            display = Hack.ValueDisplay.DECIMAL;
         }

         return display.format(value);
      }

      private void applyValue() {
         String text = this.valueBox.m_94155_();
         if (text.isEmpty()) {
            if (this.isInteger) {
               this.setting.setValue((int)this.min);
               this.valueBox.m_94144_(String.valueOf((int)this.min));
            } else {
               this.setting.setValue(this.min);
               this.valueBox.m_94144_(this.getValueText());
            }

            this.screen.autoSave();
         } else if (text.equals("-")) {
            if (this.isInteger) {
               this.setting.setValue(-1);
               this.valueBox.m_94144_("-1");
            } else {
               this.setting.setValue(-0.1);
               this.valueBox.m_94144_("-0.1");
            }

            this.screen.autoSave();
         } else {
            try {
               if (this.isInteger) {
                  int val = Integer.parseInt(text);
                  if ((double)val < this.min) {
                     val = (int)this.min;
                  }

                  if ((double)val > this.max) {
                     val = (int)this.max;
                  }

                  this.setting.setValue(val);
                  this.valueBox.m_94144_(String.valueOf(val));
               } else {
                  double valx = Double.parseDouble(text);
                  if (valx < this.min) {
                     valx = this.min;
                  }

                  if (valx > this.max) {
                     valx = this.max;
                  }

                  this.setting.setValue(valx);
                  this.valueBox.m_94144_(this.getValueText());
               }

               this.screen.autoSave();
            } catch (NumberFormatException var4) {
               this.valueBox.m_94144_(this.getValueText());
            }

         }
      }

      public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick, int scrollOffset) {
         int baseX = this.screen.windowX + this.x;
         int baseY = this.screen.windowY + this.y - scrollOffset;
         if (baseY >= this.screen.windowY + 35 && baseY + this.height <= this.screen.windowY + SettingsWindow.this.windowHeight - 20) {
            double value = this.isInteger ? (double)this.setting.getInt() : this.setting.getDouble();
            int sliderX = baseX + 150;
            int sliderWidth = 130;
            SettingsWindow.this.fill(gui, sliderX, baseY + 6, sliderX + sliderWidth, baseY + this.height - 6, -13487556);
            double percent = (value - this.min) / (this.max - this.min);
            int progressWidth = (int)((double)sliderWidth * percent);
            SettingsWindow.this.fill(gui, sliderX, baseY + 6, sliderX + progressWidth, baseY + this.height - 6, -12156216);
            int knobX = sliderX + (int)((double)(sliderWidth - 8) * percent);
            int knobColor = this.dragging ? -4934456 : -7566176;
            SettingsWindow.this.fill(gui, knobX, baseY + 4, knobX + 8, baseY + this.height - 4, knobColor);
            int inputY = baseY + (this.height - 8) / 2 - 2;
            this.valueBox.m_252865_(this.screen.windowX + this.x + 300);
            this.valueBox.m_253211_(inputY);
            this.valueBox.m_93674_(60);
            this.valueBox.m_88315_(gui, mouseX, mouseY, partialTick);
            if (this.dragging) {
               double newPercent = (double)(mouseX - sliderX) / (double)sliderWidth;
               newPercent = Math.max(0.0, Math.min(1.0, newPercent));
               double newValue = this.min + (this.max - this.min) * newPercent;
               if (this.isInteger) {
                  newValue = (double)Math.round(newValue);
                  this.setting.setValue((int)newValue);
                  this.valueBox.m_94144_(String.valueOf((int)newValue));
               } else {
                  String minStr = String.valueOf(this.min);
                  int decimalPlaces = 0;
                  if (minStr.contains(".")) {
                     decimalPlaces = minStr.split("\\.")[1].length();
                  }

                  double factor = Math.pow(10.0, (double)decimalPlaces);
                  newValue = (double)Math.round(newValue * factor) / factor;
                  this.setting.setValue(newValue);
                  this.valueBox.m_94144_(this.getValueText());
               }

               this.screen.autoSave();
            }

         }
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button, int scrollOffset) {
         int baseX = this.screen.windowX + this.x;
         int baseY = this.screen.windowY + this.y - scrollOffset;
         if (baseY >= this.screen.windowY + 35 && baseY + this.height <= this.screen.windowY + SettingsWindow.this.windowHeight - 20) {
            int sliderX = baseX + 150;
            int sliderWidth = 130;
            if (mouseX >= (double)sliderX && mouseX <= (double)(sliderX + sliderWidth) && mouseY >= (double)baseY && mouseY <= (double)(baseY + this.height)) {
               this.dragging = true;
               this.isEditing = false;
               this.valueBox.m_93692_(false);
               this.screen.setFocusedTextBox((EditBox)null);
               return true;
            } else {
               this.valueBox.m_252865_(this.screen.windowX + this.x + 300);
               this.valueBox.m_253211_(baseY + (this.height - 8) / 2 - 2);
               if (this.valueBox.m_6375_(mouseX, mouseY, button)) {
                  this.isEditing = true;
                  this.valueBox.m_93692_(true);
                  this.valueBox.m_94208_(this.valueBox.m_94155_().length());
                  this.screen.setFocusedTextBox(this.valueBox);
                  return true;
               } else {
                  return false;
               }
            }
         } else {
            return false;
         }
      }

      public void mouseReleased() {
         this.dragging = false;
         this.applyValue();
      }
   }

   private class ModeWidget implements SettingWidget {
      private final Hack.Setting setting;
      private final int x;
      private final int y;
      private final int width;
      private final int height;
      private final SettingsWindow screen;
      private final String[] options;

      public ModeWidget(Hack.Setting setting, int x, int y, int width, int height, SettingsWindow screen) {
         this.setting = setting;
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.screen = screen;
         this.options = setting.getOptions() != null ? setting.getOptions() : new String[]{"默认"};
      }

      public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick, int scrollOffset) {
         int baseX = this.screen.windowX + this.x;
         int baseY = this.screen.windowY + this.y - scrollOffset;
         if (baseY >= this.screen.windowY + 35 && baseY + this.height <= this.screen.windowY + SettingsWindow.this.windowHeight - 20) {
            int btnX = baseX + 150;
            int btnWidth = 210;
            boolean hovered = mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= baseY && mouseY <= baseY + this.height;
            boolean leftHovered = mouseX >= btnX && mouseX <= btnX + 20;
            boolean rightHovered = mouseX >= btnX + btnWidth - 20 && mouseX <= btnX + btnWidth;
            int bgColor = hovered ? -12171696 : -12829626;
            SettingsWindow.this.fill(gui, btnX, baseY, btnX + btnWidth, baseY + this.height, bgColor);
            SettingsWindow.this.drawBorder(gui, btnX, baseY, btnX + btnWidth, baseY + this.height, 1, -11513766);
            String current = this.setting.getString();
            gui.m_280488_(SettingsWindow.this.f_96547_, current, btnX + 25, baseY + 5, 16777215);
            gui.m_280488_(SettingsWindow.this.f_96547_, "<", btnX + 8, baseY + 5, leftHovered ? 16777215 : 11184810);
            gui.m_280488_(SettingsWindow.this.f_96547_, ">", btnX + btnWidth - 15, baseY + 5, rightHovered ? 16777215 : 11184810);
         }
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button, int scrollOffset) {
         int baseX = this.screen.windowX + this.x;
         int baseY = this.screen.windowY + this.y - scrollOffset;
         if (baseY >= this.screen.windowY + 35 && baseY + this.height <= this.screen.windowY + SettingsWindow.this.windowHeight - 20) {
            int btnX = baseX + 150;
            int btnWidth = 210;
            if (mouseX >= (double)btnX && mouseX <= (double)(btnX + btnWidth) && mouseY >= (double)baseY && mouseY <= (double)(baseY + this.height)) {
               String current = this.setting.getString();
               int index = 0;

               for(int i = 0; i < this.options.length; ++i) {
                  if (this.options[i].equals(current)) {
                     index = i;
                     break;
                  }
               }

               if (mouseX <= (double)(btnX + 20)) {
                  index = (index - 1 + this.options.length) % this.options.length;
               } else {
                  index = (index + 1) % this.options.length;
               }

               this.setting.setValue(this.options[index]);
               this.screen.autoSave();
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   private class ColorWidget implements SettingWidget {
      private final Hack.Setting setting;
      private final int x;
      private final int y;
      private final int width;
      private final int height;
      private final SettingsWindow screen;
      private int color;

      public ColorWidget(Hack.Setting setting, int x, int y, int width, int height, SettingsWindow screen) {
         this.setting = setting;
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.screen = screen;
         this.color = (Integer)setting.getValue();
      }

      public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick, int scrollOffset) {
         int baseX = this.screen.windowX + this.x;
         int baseY = this.screen.windowY + this.y - scrollOffset;
         if (baseY >= this.screen.windowY + 35 && baseY + this.height <= this.screen.windowY + SettingsWindow.this.windowHeight - 20) {
            int currentColor = (Integer)this.setting.getValue();
            int btnX = baseX + 200;
            int btnWidth = 100;
            boolean hovered = mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= baseY && mouseY <= baseY + this.height;
            int bgColor = hovered ? -11513756 : -12829616;
            SettingsWindow.this.fill(gui, btnX, baseY, btnX + btnWidth, baseY + this.height, bgColor);
            SettingsWindow.this.drawBorder(gui, btnX, baseY, btnX + btnWidth, baseY + this.height, 1, -10197896);
            int previewSize = this.height - 6;
            SettingsWindow.this.fill(gui, btnX + 3, baseY + 3, btnX + 3 + previewSize, baseY + 3 + previewSize, currentColor);
            gui.m_280488_(SettingsWindow.this.f_96547_, "选择颜色", btnX + previewSize + 8, baseY + 5, 16777215);
            int r = currentColor >> 16 & 255;
            int g = currentColor >> 8 & 255;
            int b = currentColor & 255;
            String rgbText = "" + r + "," + g + "," + b;
            gui.m_280488_(SettingsWindow.this.f_96547_, rgbText, btnX + previewSize + 8, baseY + 15, 11184810);
         }
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button, int scrollOffset) {
         int baseX = this.screen.windowX + this.x;
         int baseY = this.screen.windowY + this.y - scrollOffset;
         if (baseY >= this.screen.windowY + 35 && baseY + this.height <= this.screen.windowY + SettingsWindow.this.windowHeight - 20) {
            int btnX = baseX + 200;
            int btnWidth = 100;
            if (mouseX >= (double)btnX && mouseX <= (double)(btnX + btnWidth) && mouseY >= (double)baseY && mouseY <= (double)(baseY + this.height)) {
               Minecraft.m_91087_().m_91152_(new ColorSettingScreen(this.setting, this.screen));
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   private class StringWidget implements SettingWidget {
      private final Hack.Setting setting;
      private final int x;
      private final int y;
      private final int width;
      private final int height;
      private final SettingsWindow screen;
      private final CustomEditBox textBox;
      private boolean isUpdating = false;

      public StringWidget(Hack.Setting setting, int x, int y, int width, int height, SettingsWindow screen) {
         this.setting = setting;
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.screen = screen;
         this.textBox = SettingsWindow.this.new CustomEditBox(SettingsWindow.this.f_96547_, screen.windowX + x + 200, screen.windowY + y, width - 200, height - 2, Component.m_237113_(""));
         this.textBox.m_94199_(32767);
         this.textBox.m_94144_((String)setting.getValue());
         this.textBox.m_94151_((text) -> {
            if (!this.isUpdating) {
               this.isUpdating = true;
               setting.setValue(text);
               screen.autoSave();
               this.isUpdating = false;
            }
         });
         this.textBox.m_94153_((text) -> {
            return true;
         });
      }

      public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick, int scrollOffset) {
         int var10000 = this.screen.windowX + this.x;
         int baseY = this.screen.windowY + this.y - scrollOffset;
         if (baseY >= this.screen.windowY + 35 && baseY + this.height <= this.screen.windowY + SettingsWindow.this.windowHeight - 20) {
            this.textBox.m_252865_(this.screen.windowX + this.x + 200);
            this.textBox.m_253211_(baseY);
            this.textBox.m_93674_(this.width - 200);
            this.textBox.m_88315_(gui, mouseX, mouseY, partialTick);
         }
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button, int scrollOffset) {
         int var10000 = this.screen.windowX + this.x;
         int baseY = this.screen.windowY + this.y - scrollOffset;
         if (baseY >= this.screen.windowY + 35 && baseY + this.height <= this.screen.windowY + SettingsWindow.this.windowHeight - 20) {
            this.textBox.m_252865_(this.screen.windowX + this.x + 200);
            this.textBox.m_253211_(baseY);
            if (this.textBox.m_6375_(mouseX, mouseY, button)) {
               this.screen.setFocusedTextBox(this.textBox);
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   private class ButtonWidget implements SettingWidget {
      private final Hack.Setting setting;
      private final int x;
      private final int y;
      private final int width;
      private final int height;
      private final SettingsWindow screen;

      public ButtonWidget(Hack.Setting setting, int x, int y, int width, int height, SettingsWindow screen) {
         this.setting = setting;
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.screen = screen;
      }

      public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick, int scrollOffset) {
         int baseX = this.screen.windowX + this.x;
         int baseY = this.screen.windowY + this.y - scrollOffset;
         if (baseY >= this.screen.windowY + 35 && baseY + this.height <= this.screen.windowY + SettingsWindow.this.windowHeight - 20) {
            int btnX = baseX + 200;
            int btnWidth = 120;
            boolean hovered = mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= baseY && mouseY <= baseY + this.height;
            int bgColor = hovered ? -11513756 : -12171686;
            SettingsWindow.this.fill(gui, btnX, baseY, btnX + btnWidth, baseY + this.height, bgColor);
            SettingsWindow.this.drawBorder(gui, btnX, baseY, btnX + btnWidth, baseY + this.height, 1, -10197896);
            String text = this.setting.getString();
            int textWidth = SettingsWindow.this.f_96547_.m_92895_(text);
            int textX = btnX + (btnWidth - textWidth) / 2;
            gui.m_280488_(SettingsWindow.this.f_96547_, text, textX, baseY + 5, 16777215);
         }
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button, int scrollOffset) {
         int baseX = this.screen.windowX + this.x;
         int baseY = this.screen.windowY + this.y - scrollOffset;
         if (baseY >= this.screen.windowY + 35 && baseY + this.height <= this.screen.windowY + SettingsWindow.this.windowHeight - 20) {
            int btnX = baseX + 200;
            int btnWidth = 120;
            if (mouseX >= (double)btnX && mouseX <= (double)(btnX + btnWidth) && mouseY >= (double)baseY && mouseY <= (double)(baseY + this.height)) {
               if (this.setting.getAction() != null) {
                  this.setting.getAction().run();
               }

               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   private class ItemListWidget implements SettingWidget {
      private final ItemListSetting setting;
      private final int x;
      private final int y;
      private final int width;
      private final int height;
      private final SettingsWindow screen;

      public ItemListWidget(Hack.Setting setting, int x, int y, int width, int height, SettingsWindow screen) {
         this.setting = (ItemListSetting)setting;
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.screen = screen;
      }

      public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick, int scrollOffset) {
         int baseX = this.screen.windowX + this.x;
         int baseY = this.screen.windowY + this.y - scrollOffset;
         if (baseY >= this.screen.windowY + 35 && baseY + this.height <= this.screen.windowY + SettingsWindow.this.windowHeight - 20) {
            int btnX = baseX + 200;
            int btnWidth = 100;
            boolean hovered = mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= baseY && mouseY <= baseY + this.height;
            int bgColor = hovered ? -11513756 : -12829616;
            SettingsWindow.this.fill(gui, btnX, baseY, btnX + btnWidth, baseY + this.height, bgColor);
            SettingsWindow.this.drawBorder(gui, btnX, baseY, btnX + btnWidth, baseY + this.height, 1, -10197896);
            int listSize = this.setting.getItemNames().size();
            gui.m_280488_(SettingsWindow.this.f_96547_, "编辑物品 (" + listSize + ")", btnX + 5, baseY + 5, 16777215);
            gui.m_280488_(SettingsWindow.this.f_96547_, "点击打开", btnX + 5, baseY + 15, 11184810);
         }
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button, int scrollOffset) {
         int baseX = this.screen.windowX + this.x;
         int baseY = this.screen.windowY + this.y - scrollOffset;
         if (baseY >= this.screen.windowY + 35 && baseY + this.height <= this.screen.windowY + SettingsWindow.this.windowHeight - 20) {
            int btnX = baseX + 200;
            int btnWidth = 100;
            if (mouseX >= (double)btnX && mouseX <= (double)(btnX + btnWidth) && mouseY >= (double)baseY && mouseY <= (double)(baseY + this.height)) {
               SettingsWindow.this.mc.m_91152_(new ItemListScreen(this.setting, this.screen));
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   private class BlockListWidget implements SettingWidget {
      private final BlockListSetting setting;
      private final int x;
      private final int y;
      private final int width;
      private final int height;
      private final SettingsWindow screen;

      public BlockListWidget(Hack.Setting setting, int x, int y, int width, int height, SettingsWindow screen) {
         this.setting = (BlockListSetting)setting;
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.screen = screen;
      }

      public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick, int scrollOffset) {
         int baseX = this.screen.windowX + this.x;
         int baseY = this.screen.windowY + this.y - scrollOffset;
         if (baseY >= this.screen.windowY + 35 && baseY + this.height <= this.screen.windowY + SettingsWindow.this.windowHeight - 20) {
            int btnX = baseX + 200;
            int btnWidth = 100;
            boolean hovered = mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= baseY && mouseY <= baseY + this.height;
            int bgColor = hovered ? -11513756 : -12829616;
            SettingsWindow.this.fill(gui, btnX, baseY, btnX + btnWidth, baseY + this.height, bgColor);
            SettingsWindow.this.drawBorder(gui, btnX, baseY, btnX + btnWidth, baseY + this.height, 1, -10197896);
            int listSize = this.setting.getBlockNames().size();
            gui.m_280488_(SettingsWindow.this.f_96547_, "编辑方块 (" + listSize + ")", btnX + 5, baseY + 5, 16777215);
            gui.m_280488_(SettingsWindow.this.f_96547_, "点击打开", btnX + 5, baseY + 15, 11184810);
         }
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button, int scrollOffset) {
         int baseX = this.screen.windowX + this.x;
         int baseY = this.screen.windowY + this.y - scrollOffset;
         if (baseY >= this.screen.windowY + 35 && baseY + this.height <= this.screen.windowY + SettingsWindow.this.windowHeight - 20) {
            int btnX = baseX + 200;
            int btnWidth = 100;
            if (mouseX >= (double)btnX && mouseX <= (double)(btnX + btnWidth) && mouseY >= (double)baseY && mouseY <= (double)(baseY + this.height)) {
               SettingsWindow.this.mc.m_91152_(new BlockListScreen(this.setting, this.screen));
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   private class CustomEditBox extends EditBox {
      private static final int[] FLOW_COLORS = new int[]{-2461482, -2252579, -1146130, -18751, -38476};
      private float targetCursorPos;
      private float currentCursorPos;
      private long lastUpdateTime = System.currentTimeMillis();
      private static final float CURSOR_SMOOTHING = 0.3F;

      public CustomEditBox(Font font, int x, int y, int width, int height, Component message) {
         super(font, x, y, width, height, message);
         this.m_94182_(false);
         this.m_94202_(-1);
         this.targetCursorPos = 0.0F;
         this.currentCursorPos = 0.0F;
      }

      public void m_94144_(String text) {
         super.m_94144_(text);
         this.targetCursorPos = (float)this.m_94207_();
      }

      public void m_94196_(int pos) {
         super.m_94196_(pos);
         this.targetCursorPos = (float)pos;
      }

      public void m_94208_(int pos) {
         super.m_94208_(pos);
         this.targetCursorPos = (float)pos;
      }

      public void m_87963_(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
         long now = System.currentTimeMillis();
         float delta = (float)(now - this.lastUpdateTime) / 1000.0F * 60.0F;
         this.lastUpdateTime = now;
         if (Math.abs(this.currentCursorPos - this.targetCursorPos) > 0.01F) {
            this.currentCursorPos += (this.targetCursorPos - this.currentCursorPos) * Math.min(1.0F, 0.3F * delta);
         } else {
            this.currentCursorPos = this.targetCursorPos;
         }

         int x = this.m_252754_();
         int y = this.m_252907_();
         int w = this.m_5711_();
         int h = this.m_93694_();
         int bgColor = -14145486;
         int borderColor = -12171696;
         gui.m_280509_(x, y, x + w, y + h, bgColor);
         int i;
         if (this.m_93696_()) {
            long time = System.currentTimeMillis();
            float offset = (float)(time % 3000L) / 3000.0F;

            float progress;
            int color;
            for(i = 0; i < w; ++i) {
               progress = ((float)i / (float)w + offset) % 1.0F;
               color = this.getGradientColor(progress);
               gui.m_280509_(x + i, y, x + i + 1, y + 1, color);
            }

            for(i = 0; i < w; ++i) {
               progress = (1.0F - (float)i / (float)w + offset) % 1.0F;
               color = this.getGradientColor(progress);
               gui.m_280509_(x + i, y + h - 1, x + i + 1, y + h, color);
            }

            for(i = 0; i < h; ++i) {
               progress = ((float)i / (float)h + offset) % 1.0F;
               color = this.getGradientColor(progress);
               gui.m_280509_(x, y + i, x + 1, y + i + 1, color);
            }

            for(i = 0; i < h; ++i) {
               progress = (1.0F - (float)i / (float)h + offset) % 1.0F;
               color = this.getGradientColor(progress);
               gui.m_280509_(x + w - 1, y + i, x + w, y + i + 1, color);
            }
         } else {
            gui.m_280509_(x, y, x + w, y + 1, borderColor);
            gui.m_280509_(x, y + h - 1, x + w, y + h, borderColor);
            gui.m_280509_(x, y, x + 1, y + h, borderColor);
            gui.m_280509_(x + w - 1, y, x + w, y + h, borderColor);
         }

         super.m_87963_(gui, mouseX, mouseY, partialTick);
         if (this.m_93696_() && this.m_94207_() >= 0) {
            String fullText = this.m_94155_();
            int pos = this.m_94207_();
            String beforeCursor = fullText.substring(0, Math.min(pos, fullText.length()));
            i = x + 4 + SettingsWindow.this.f_96547_.m_92895_(beforeCursor);
            int cursorY = y + (h - 8) / 2;
            gui.m_280509_(i - 2, cursorY, i + 3, cursorY + 8, bgColor);
         }

      }

      private int getGradientColor(float progress) {
         int index = (int)(progress * (float)(FLOW_COLORS.length - 1));
         float blend = progress * (float)(FLOW_COLORS.length - 1) - (float)index;
         return index >= FLOW_COLORS.length - 1 ? FLOW_COLORS[FLOW_COLORS.length - 1] : this.interpolateColor(FLOW_COLORS[index], FLOW_COLORS[index + 1], blend);
      }

      private int interpolateColor(int color1, int color2, float blend) {
         int r1 = color1 >> 16 & 255;
         int g1 = color1 >> 8 & 255;
         int b1 = color1 & 255;
         int r2 = color2 >> 16 & 255;
         int g2 = color2 >> 8 & 255;
         int b2 = color2 & 255;
         int r = (int)((float)r1 + (float)(r2 - r1) * blend);
         int g = (int)((float)g1 + (float)(g2 - g1) * blend);
         int b = (int)((float)b1 + (float)(b2 - b1) * blend);
         return -16777216 | r << 16 | g << 8 | b;
      }
   }
}
