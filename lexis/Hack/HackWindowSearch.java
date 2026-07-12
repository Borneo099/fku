package lexis.Hack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Utils.ThemeColors.ThemeColors;
import lexis.Hack.Utils.ThemeColors.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class HackWindowSearch {
   private static final Minecraft mc = Minecraft.m_91087_();
   private int x;
   private int y;
   private int width = 150;
   private int height;
   private boolean dragging;
   private int dragX;
   private int dragY;
   private boolean visible = true;
   private static final int TITLE_HEIGHT = 13;
   private static final int SEARCH_HEIGHT = 20;
   private static final int BUTTON_HEIGHT = 14;
   private static final int BUTTON_SPACING = 0;
   private static final int MAX_VISIBLE_BUTTONS = 12;
   private int scrollOffset = 0;
   private int maxScroll = 0;
   private boolean scrollbarHovered = false;
   private EditBox searchBox;
   private String searchText = "";
   private List searchResults = new ArrayList();
   private List resultButtons = new ArrayList();
   private HackGui parentGui;
   private boolean initialized = false;

   public HackWindowSearch(int x, int y, HackGui parentGui) {
      this.x = x;
      this.y = y;
      this.parentGui = parentGui;
   }

   private void initSearchBox() {
      if (mc.f_91062_ != null) {
         this.searchBox = new EditBox(mc.f_91062_, this.x + 5, this.y + 13 + 5, this.width - 10, 20, Component.m_237113_(""));
         this.searchBox.m_94199_(Integer.MAX_VALUE);
         this.searchBox.m_94182_(false);
         this.searchBox.m_94202_(16777215);
         this.searchBox.m_94151_((text) -> {
            this.searchText = text.toLowerCase();
            this.updateSearchResults();
         });
         this.searchBox.m_94190_(true);
         this.searchBox.m_94194_(true);
         this.initialized = true;
      }
   }

   private void updateSearchResults() {
      this.searchResults.clear();
      if (!this.searchText.isEmpty()) {
         Iterator var1 = HackManager.getInstance().getHacks().iterator();

         Hack hack;
         while(var1.hasNext()) {
            hack = (Hack)var1.next();
            if (hack.getName().toLowerCase().contains(this.searchText)) {
               this.searchResults.add(hack);
            }
         }

         this.resultButtons.clear();
         var1 = this.searchResults.iterator();

         while(var1.hasNext()) {
            hack = (Hack)var1.next();
            this.resultButtons.add(new HackButton(hack, this.parentGui));
         }

         int totalHeight = this.resultButtons.size() * 14;
         this.maxScroll = Math.max(0, (totalHeight - 168) / 14);
         this.scrollOffset = 0;
      }
   }

   public void setFocused(boolean focused) {
      if (this.searchBox != null) {
         this.searchBox.m_93692_(focused);
      }

   }

   public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
      if (this.visible) {
         if (!this.initialized) {
            this.initSearchBox();
         }

         if (mc.f_91062_ != null) {
            ThemeColors colors = ThemeManager.getColors();
            int contentHeight = this.searchText.isEmpty() ? 0 : Math.min(170, this.resultButtons.size() * 14 + 2);
            this.height = 38 + contentHeight;

            int alpha;
            for(int i = 1; i <= 4; ++i) {
               alpha = 10 * i;
               gui.m_280509_(this.x - i, this.y - i, this.x + this.width + i, this.y + 13 + i, alpha << 24 | 0);
               if (contentHeight > 0) {
                  gui.m_280509_(this.x - i, this.y + 13 + 20 + 5, this.x + this.width + i, this.y + 13 + 20 + 5 + contentHeight + i, alpha << 24 | 0);
               }
            }

            boolean dragHandleHovered = mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + 13;
            alpha = dragHandleHovered ? colors.titleBackgroundHovered.getPacked() : colors.titleBackground.getPacked();
            gui.m_280509_(this.x, this.y, this.x + this.width, this.y + 13, alpha);
            gui.m_280488_(mc.f_91062_, "搜索", this.x + 8, this.y + 2, colors.titleText.getPacked());
            gui.m_280509_(this.x, this.y, this.x + this.width, this.y + 1, colors.windowBorder.getPacked());
            gui.m_280509_(this.x, this.y + 13 - 1, this.x + this.width, this.y + 13, colors.windowBorder.getPacked());
            gui.m_280509_(this.x, this.y, this.x + 1, this.y + 13, colors.windowBorder.getPacked());
            gui.m_280509_(this.x + this.width - 1, this.y, this.x + this.width, this.y + 13, colors.windowBorder.getPacked());
            int searchBoxX = this.x + 2;
            int searchBoxY = this.y + 13 + 2;
            int searchBoxW = this.width - 4;
            int searchBoxH = 20;

            int startIndex;
            int endIndex;
            for(startIndex = 1; startIndex <= 2; ++startIndex) {
               endIndex = 15 * startIndex;
               gui.m_280509_(searchBoxX - startIndex, searchBoxY - startIndex, searchBoxX + searchBoxW + startIndex, searchBoxY - startIndex + 1, endIndex << 24 | 0);
               gui.m_280509_(searchBoxX - startIndex, searchBoxY + searchBoxH - 1, searchBoxX + searchBoxW + startIndex, searchBoxY + searchBoxH + startIndex, endIndex << 24 | 0);
               gui.m_280509_(searchBoxX - startIndex, searchBoxY - startIndex, searchBoxX - startIndex + 1, searchBoxY + searchBoxH + startIndex, endIndex << 24 | 0);
               gui.m_280509_(searchBoxX + searchBoxW - 1, searchBoxY - startIndex, searchBoxX + searchBoxW + startIndex, searchBoxY + searchBoxH + startIndex, endIndex << 24 | 0);
            }

            gui.m_280509_(searchBoxX, searchBoxY, searchBoxX + searchBoxW, searchBoxY + searchBoxH, -13421773);
            gui.m_280509_(searchBoxX, searchBoxY, searchBoxX + searchBoxW, searchBoxY + 1, colors.windowBorder.getPacked());
            gui.m_280509_(searchBoxX, searchBoxY + searchBoxH - 1, searchBoxX + searchBoxW, searchBoxY + searchBoxH, colors.windowBorder.getPacked());
            gui.m_280509_(searchBoxX, searchBoxY, searchBoxX + 1, searchBoxY + searchBoxH, colors.windowBorder.getPacked());
            gui.m_280509_(searchBoxX + searchBoxW - 1, searchBoxY, searchBoxX + searchBoxW, searchBoxY + searchBoxH, colors.windowBorder.getPacked());
            if (this.searchBox != null) {
               this.searchBox.m_252865_(searchBoxX + 3);
               this.searchBox.m_253211_(searchBoxY + 2);
               this.searchBox.m_93674_(searchBoxW - 6);
               this.searchBox.m_88315_(gui, mouseX, mouseY, partialTicks);
            }

            if (contentHeight > 0) {
               gui.m_280509_(this.x, this.y + 13 + 20 + 5, this.x + this.width, this.y + 13 + 20 + 5 + contentHeight, colors.windowBackground.getPacked());
               startIndex = this.scrollOffset;
               endIndex = Math.min(startIndex + 12, this.resultButtons.size());
               int buttonY = this.y + 13 + 20 + 6;

               int scrollbarX;
               for(scrollbarX = startIndex; scrollbarX < endIndex; ++scrollbarX) {
                  HackButton button = (HackButton)this.resultButtons.get(scrollbarX);
                  button.renderWithAlpha(gui, this.x + 1, buttonY, this.width - 2, 14, mouseX, mouseY, 1.0F);
                  buttonY += 14;
               }

               if (this.resultButtons.size() > 12) {
                  scrollbarX = this.x + this.width - 3;
                  int scrollbarY = this.y + 13 + 20 + 6;
                  int scrollbarHeight = contentHeight - 2;
                  gui.m_280509_(scrollbarX, scrollbarY, scrollbarX + 2, scrollbarY + scrollbarHeight, colors.scrollbarBg.getPacked());
                  float scrollPercent = (float)this.scrollOffset / (float)(this.resultButtons.size() - 12);
                  int sliderHeight = Math.max(20, (int)((float)scrollbarHeight * (12.0F / (float)this.resultButtons.size())));
                  int sliderY = scrollbarY + (int)(scrollPercent * (float)(scrollbarHeight - sliderHeight));
                  this.scrollbarHovered = mouseX >= scrollbarX && mouseX <= scrollbarX + 2 && mouseY >= sliderY && mouseY <= sliderY + sliderHeight;
                  int sliderColor = this.scrollbarHovered ? colors.scrollbarKnobHovered.getPacked() : colors.scrollbarKnob.getPacked();
                  gui.m_280509_(scrollbarX, sliderY, scrollbarX + 2, sliderY + sliderHeight, sliderColor);
               }

            }
         }
      }
   }

   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (this.visible) {
         if (!this.initialized) {
            this.initSearchBox();
         }

         boolean clickedSearch = false;
         int startIndex;
         int endIndex;
         int buttonY;
         int i;
         if (this.searchBox != null) {
            startIndex = this.x + 5;
            endIndex = this.y + 13 + 5;
            buttonY = this.width - 10;
            i = 20;
            if (mouseX >= (double)startIndex && mouseX <= (double)(startIndex + buttonY) && mouseY >= (double)endIndex && mouseY <= (double)(endIndex + i)) {
               clickedSearch = true;
               this.searchBox.m_6375_(mouseX, mouseY, button);
               this.searchBox.m_93692_(true);
            } else {
               this.searchBox.m_93692_(false);
            }
         }

         if (!clickedSearch) {
            if (button == 0 && mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY <= (double)(this.y + 13)) {
               this.dragging = true;
               this.dragX = (int)(mouseX - (double)this.x);
               this.dragY = (int)(mouseY - (double)this.y);
            } else if (!this.searchText.isEmpty()) {
               startIndex = this.scrollOffset;
               endIndex = Math.min(startIndex + 12, this.resultButtons.size());
               buttonY = this.y + 13 + 20 + 6;

               for(i = startIndex; i < endIndex; ++i) {
                  HackButton hackButton = (HackButton)this.resultButtons.get(i);
                  if (mouseX >= (double)(this.x + 1) && mouseX <= (double)(this.x + this.width - 1) && mouseY >= (double)buttonY && mouseY <= (double)(buttonY + 14)) {
                     hackButton.mouseClicked(mouseX, mouseY, button, this.x + 1, buttonY, this.width - 2);
                     return;
                  }

                  buttonY += 14;
               }

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
         if (this.searchBox != null) {
            this.searchBox.m_252865_(this.x + 5);
            this.searchBox.m_253211_(this.y + 13 + 5);
         }
      }

   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      if (this.visible && !this.searchText.isEmpty() && !this.resultButtons.isEmpty()) {
         if (mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)(this.y + 13 + 20 + 5) && mouseY <= (double)(this.y + 13 + 20 + 5 + this.height)) {
            int newScroll = (int)((double)this.scrollOffset - delta);
            this.scrollOffset = Math.max(0, Math.min(this.resultButtons.size() - 12, newScroll));
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.visible) {
         return false;
      } else {
         return this.searchBox != null && this.searchBox.m_93696_() ? this.searchBox.m_7933_(keyCode, scanCode, modifiers) : false;
      }
   }

   public boolean charTyped(char codePoint, int modifiers) {
      if (!this.visible) {
         return false;
      } else {
         return this.searchBox != null && this.searchBox.m_93696_() ? this.searchBox.m_5534_(codePoint, modifiers) : false;
      }
   }

   public void setPos(int x, int y) {
      this.x = x;
      this.y = y;
      if (this.searchBox != null) {
         this.searchBox.m_252865_(x + 5);
         this.searchBox.m_253211_(y + 13 + 5);
      }

   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }
}
