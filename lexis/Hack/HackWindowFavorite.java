package lexis.Hack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import lexis.Hack.Hackutil.FavoritesManager;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Utils.ThemeColors.ThemeColors;
import lexis.Hack.Utils.ThemeColors.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class HackWindowFavorite {
   private static final Minecraft mc = Minecraft.m_91087_();
   private int x;
   private int y;
   private int width = 120;
   private int height;
   private boolean dragging;
   private int dragX;
   private int dragY;
   private boolean visible = true;
   private static final int TITLE_HEIGHT = 13;
   private static final int BUTTON_HEIGHT = 14;
   private static final int BUTTON_SPACING = 0;
   private static final int MAX_VISIBLE_BUTTONS = 12;
   private int scrollOffset = 0;
   private int maxScroll = 0;
   private boolean scrollbarHovered = false;
   private List favoriteButtons = new ArrayList();
   private HackGui parentGui;
   private FavoritesManager favoritesManager;

   public HackWindowFavorite(int x, int y, HackGui parentGui) {
      this.x = x;
      this.y = y;
      this.parentGui = parentGui;
      this.favoritesManager = FavoritesManager.getInstance();
      this.favoritesManager.addListener(this::updateFavorites);
      this.updateFavorites();
   }

   private void updateFavorites() {
      this.favoriteButtons.clear();
      Set favoriteNames = this.favoritesManager.getFavorites();
      Iterator var2 = favoriteNames.iterator();

      while(true) {
         while(var2.hasNext()) {
            String name = (String)var2.next();
            Iterator var4 = HackManager.getInstance().getHacks().iterator();

            while(var4.hasNext()) {
               Hack hack = (Hack)var4.next();
               if (hack.getName().equals(name)) {
                  this.favoriteButtons.add(new HackButton(hack, this.parentGui));
                  break;
               }
            }
         }

         int totalButtonHeight = this.favoriteButtons.size() * 14;
         this.maxScroll = Math.max(0, (totalButtonHeight - 168) / 14);
         return;
      }
   }

   public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
      if (this.visible && !this.favoriteButtons.isEmpty()) {
         ThemeColors colors = ThemeManager.getColors();
         int contentHeight = Math.min(170, this.favoriteButtons.size() * 14 + 2);
         this.height = 13 + contentHeight;

         int titleColor;
         int startIndex;
         for(int i = 1; i <= 4; ++i) {
            titleColor = 10 * i;
            startIndex = titleColor << 24 | colors.windowShadow.getPacked() & 16777215;
            gui.m_280509_(this.x - i, this.y - i, this.x + this.width + i, this.y + 13 + i, startIndex);
            if (contentHeight > 0) {
               gui.m_280509_(this.x - i, this.y + 13, this.x + this.width + i, this.y + 13 + contentHeight + i, startIndex);
            }
         }

         boolean dragHandleHovered = mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + 13;
         titleColor = dragHandleHovered ? colors.titleBackgroundHovered.getPacked() : colors.titleBackground.getPacked();
         gui.m_280509_(this.x, this.y, this.x + this.width, this.y + 13, titleColor);
         gui.m_280488_(mc.f_91062_, "⭐ 收藏", this.x + 8, this.y + 2, colors.titleText.getPacked());
         if (contentHeight > 0) {
            gui.m_280509_(this.x, this.y + 13, this.x + this.width, this.y + 13 + contentHeight, colors.windowBackground.getPacked());
            startIndex = this.scrollOffset;
            int endIndex = Math.min(startIndex + 12, this.favoriteButtons.size());
            int buttonY = this.y + 13 + 1;

            int scrollbarX;
            for(scrollbarX = startIndex; scrollbarX < endIndex; ++scrollbarX) {
               HackButton button = (HackButton)this.favoriteButtons.get(scrollbarX);
               button.renderWithAlpha(gui, this.x + 1, buttonY, this.width - 2, 14, mouseX, mouseY, 1.0F);
               buttonY += 14;
            }

            if (this.favoriteButtons.size() > 12) {
               scrollbarX = this.x + this.width - 3;
               int scrollbarY = this.y + 13 + 1;
               int scrollbarHeight = contentHeight - 2;
               gui.m_280509_(scrollbarX, scrollbarY, scrollbarX + 2, scrollbarY + scrollbarHeight, colors.scrollbarBg.getPacked());
               float scrollPercent = (float)this.scrollOffset / (float)(this.favoriteButtons.size() - 12);
               int sliderHeight = Math.max(20, (int)((float)scrollbarHeight * (12.0F / (float)this.favoriteButtons.size())));
               int sliderY = scrollbarY + (int)(scrollPercent * (float)(scrollbarHeight - sliderHeight));
               this.scrollbarHovered = mouseX >= scrollbarX && mouseX <= scrollbarX + 2 && mouseY >= sliderY && mouseY <= sliderY + sliderHeight;
               int sliderColor = this.scrollbarHovered ? colors.scrollbarKnobHovered.getPacked() : colors.scrollbarKnob.getPacked();
               gui.m_280509_(scrollbarX, sliderY, scrollbarX + 2, sliderY + sliderHeight, sliderColor);
            }

         }
      }
   }

   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (this.visible && !this.favoriteButtons.isEmpty()) {
         if (mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY <= (double)(this.y + 13)) {
            if (button == 0) {
               this.dragging = true;
               this.dragX = (int)(mouseX - (double)this.x);
               this.dragY = (int)(mouseY - (double)this.y);
            }

         } else {
            int startIndex = this.scrollOffset;
            int endIndex = Math.min(startIndex + 12, this.favoriteButtons.size());
            int buttonY = this.y + 13 + 1;

            for(int i = startIndex; i < endIndex; ++i) {
               HackButton hackButton = (HackButton)this.favoriteButtons.get(i);
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
      if (this.visible && !this.favoriteButtons.isEmpty()) {
         if (mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)(this.y + 13) && mouseY <= (double)(this.y + this.height)) {
            int newScroll = (int)((double)this.scrollOffset - delta);
            this.scrollOffset = Math.max(0, Math.min(this.favoriteButtons.size() - 12, newScroll));
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void setPos(int x, int y) {
      this.x = x;
      this.y = y;
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
      return this.visible && !this.favoriteButtons.isEmpty();
   }
}
