package lexis.Hack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.BaritoneBridge;
import lexis.Hack.Utils.TaczBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

public class HackGui extends Screen {
   private static final Minecraft mc = Minecraft.m_91087_();
   private List windows = new ArrayList();
   private HackWindowFavorite favoriteWindow;
   private boolean guiVisible = false;
   private HackConfig config = HackConfig.getInstance();
   private HackButton currentBindingButton = null;
   private HackWindowSearch searchWindow;
   private Map windowSnapshots = new HashMap();
   private HackWindow draggedWindow = null;
   private float slideOffset = 0.0F;
   private float startOffset = 0.0F;
   private float targetSlideOffset = 0.0F;
   private long animStartTime = 0L;
   private boolean animating = false;
   private Screen pendingScreen = null;
   private static final long ANIM_DURATION = 250L;

   public void startSlideOut(Screen newScreen) {
      this.startOffset = this.slideOffset;
      this.targetSlideOffset = (float)(-this.f_96543_);
      this.animStartTime = System.currentTimeMillis();
      this.animating = true;
      this.pendingScreen = newScreen;
   }

   public void startSlideIn() {
      this.startOffset = this.slideOffset;
      this.targetSlideOffset = 0.0F;
      this.animStartTime = System.currentTimeMillis();
      this.animating = true;
   }

   public HackGui() {
      super(Component.m_237113_("Lexis Hack GUI"));
      int startX = 50;
      int startY = 50;
      int windowWidth = 120;
      int windowSpacing = 140;
      int categoriesPerRow = 5;
      int row = 0;
      int col = 0;
      int favX = this.config.getWindowX("收藏");
      int favY = this.config.getWindowY("收藏");
      if (favX == -1 || favY == -1) {
         favX = startX;
         favY = startY;
      }

      this.favoriteWindow = new HackWindowFavorite(favX, favY, this);
      int searchX = this.config.getWindowX("搜索");
      int searchY = this.config.getWindowY("搜索");
      if (searchX == -1 || searchY == -1) {
         searchX = startX + 250;
         searchY = startY;
      }

      this.searchWindow = new HackWindowSearch(searchX, searchY, this);
      Hack.Category[] var12 = Hack.Category.values();
      int var13 = var12.length;

      for(int var14 = 0; var14 < var13; ++var14) {
         Hack.Category category = var12[var14];
         if ((category != Hack.Category.BARITONE || BaritoneBridge.isAvailable()) && (category != Hack.Category.TACZ || TaczBridge.isAvailable()) && (category != Hack.Category.TACZ_SERVER || TaczBridge.isAvailable()) && (category != Hack.Category.CATACLYSM || ModList.get().isLoaded("cataclysm"))) {
            int savedX = this.config.getWindowX(category.name());
            int savedY = this.config.getWindowY(category.name());
            int windowX;
            int windowY;
            if (savedX != -1 && savedY != -1) {
               windowX = savedX;
               windowY = savedY;
            } else {
               windowX = startX + col * windowSpacing;
               windowY = startY + row * 250;
               ++col;
               if (col >= categoriesPerRow) {
                  col = 0;
                  ++row;
               }
            }

            HackWindow window = new HackWindow(category.displayName, category, windowX, windowY, windowWidth, this);
            boolean savedCollapsed = this.config.isWindowCollapsed(category.name());
            if (savedCollapsed) {
               window.setCollapsed(true);
            }

            Iterator var22 = HackManager.getInstance().getHacksByCategory(category).iterator();

            while(var22.hasNext()) {
               Hack hack = (Hack)var22.next();
               window.addButton(new HackButton(hack, this));
            }

            window.updateWidth();
            this.windows.add(window);
         }
      }

   }

   private void drawFixedText(GuiGraphics gui, String text, int x, int y, int color) {
      gui.m_280488_(mc.f_91062_, text, x, y, color);
   }

   public void m_88315_(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
      if (this.animating) {
         long elapsed = System.currentTimeMillis() - this.animStartTime;
         float progress = Math.min(1.0F, (float)elapsed / 250.0F);
         this.slideOffset = this.startOffset + (this.targetSlideOffset - this.startOffset) * progress;
         if (elapsed >= 250L) {
            this.slideOffset = this.targetSlideOffset;
            this.animating = false;
            if (this.targetSlideOffset != 0.0F && this.pendingScreen != null) {
               mc.m_91152_(this.pendingScreen);
               this.pendingScreen = null;
            }
         }
      }

      gui.m_280168_().m_85836_();
      gui.m_280168_().m_252880_(this.slideOffset, 0.0F, 0.0F);
      Iterator var8 = this.windows.iterator();

      while(var8.hasNext()) {
         HackWindow window = (HackWindow)var8.next();
         window.render(gui, mouseX, mouseY, partialTicks);
      }

      if (this.favoriteWindow != null) {
         this.favoriteWindow.render(gui, mouseX, mouseY, partialTicks);
      }

      if (this.searchWindow != null) {
         this.searchWindow.render(gui, mouseX, mouseY, partialTicks);
      }

      gui.m_280168_().m_85849_();
      this.renderAllTooltips(gui, mouseX, mouseY);
      String bindMsg;
      if (this.currentBindingButton != null) {
         bindMsg = "§e正在绑定按键: " + this.currentBindingButton.getHack().getName() + " §7(点击其他位置取消)";
         this.drawFixedText(gui, bindMsg, (this.f_96543_ - mc.f_91062_.m_92895_(bindMsg)) / 2, this.f_96544_ - 20, 16777215);
      } else {
         bindMsg = "左键:开关 X 右键:设置 X 中键:按键绑定 X 右键标题:公开/收起 X Delete:解除按键绑定";
         this.drawFixedText(gui, bindMsg, 5, 5, 16777215);
      }

   }

   private void renderAllTooltips(GuiGraphics gui, int mouseX, int mouseY) {
      Iterator var4 = this.windows.iterator();

      while(true) {
         HackWindow window;
         List btns;
         do {
            do {
               do {
                  do {
                     if (!var4.hasNext()) {
                        return;
                     }

                     window = (HackWindow)var4.next();
                  } while(!window.isVisible());
               } while(window.isCollapsed());

               btns = window.getButtons();
            } while(btns == null);
         } while(btns.isEmpty());

         int start = window.getScrollOffset();
         int end = Math.min(start + 25, btns.size());
         int buttonStartY = window.getY() + 13 + 1;

         for(int i = start; i < end; ++i) {
            HackButton btn = (HackButton)btns.get(i);
            int btnX = window.getX() + 1;
            int btnW = window.getWidth() - 2;
            int btnH = 14;
            int btnY = buttonStartY + (i - start) * 14;
            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
               if (!btn.isStarHovered()) {
                  btn.renderTooltip(gui, mouseX, mouseY);
               }

               return;
            }
         }
      }
   }

   public boolean m_5534_(char codePoint, int modifiers) {
      return this.searchWindow.charTyped(codePoint, modifiers);
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (this.currentBindingButton != null) {
         this.currentBindingButton.cancelBinding();
         this.currentBindingButton = null;
         return true;
      } else {
         this.favoriteWindow.mouseClicked(mouseX, mouseY, button);
         this.searchWindow.mouseClicked(mouseX, mouseY, button);
         this.windowSnapshots.clear();
         Iterator var6 = this.windows.iterator();

         HackWindow window;
         while(var6.hasNext()) {
            window = (HackWindow)var6.next();
            this.windowSnapshots.put(window, new Rectangle(window.getX(), window.getY(), window.getWidth(), window.getHeight()));
         }

         for(int i = this.windows.size() - 1; i >= 0; --i) {
            window = (HackWindow)this.windows.get(i);
            window.mouseClicked(mouseX, mouseY, button);
            if (window.isDragging()) {
               this.draggedWindow = window;
               this.windows.remove(i);
               this.windows.add(window);
               break;
            }
         }

         return super.m_6375_(mouseX, mouseY, button);
      }
   }

   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      if (this.searchWindow.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else {
         if (this.currentBindingButton != null) {
            if (keyCode == 256) {
               this.currentBindingButton.cancelBinding();
               this.currentBindingButton = null;
               return true;
            }

            if (this.currentBindingButton.keyPressed(keyCode)) {
               this.currentBindingButton = null;
               return true;
            }
         }

         if (keyCode == 256) {
            this.guiVisible = false;
            mc.m_91152_((Screen)null);
            return true;
         } else {
            return super.m_7933_(keyCode, scanCode, modifiers);
         }
      }
   }

   public boolean m_6348_(double mouseX, double mouseY, int button) {
      this.favoriteWindow.mouseReleased();
      this.searchWindow.mouseReleased();
      if (this.draggedWindow != null) {
         this.draggedWindow.mouseReleased();
         this.draggedWindow = null;
         this.windowSnapshots.clear();
      }

      Iterator var6 = this.windows.iterator();

      HackWindow window;
      while(var6.hasNext()) {
         window = (HackWindow)var6.next();
         window.mouseReleased();
      }

      var6 = this.windows.iterator();

      while(var6.hasNext()) {
         window = (HackWindow)var6.next();
         this.config.setWindowPos(window.getCategory().name(), window.getX(), window.getY());
         this.config.setWindowCollapsed(window.getCategory().name(), window.isCollapsed());
      }

      this.config.setWindowPos("收藏", this.favoriteWindow.getX(), this.favoriteWindow.getY());
      this.config.save();
      this.config.setWindowPos("搜索", this.searchWindow.getX(), this.searchWindow.getY());
      return super.m_6348_(mouseX, mouseY, button);
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (this.draggedWindow != null && this.draggedWindow.isDragging()) {
         this.draggedWindow.setPos((int)(mouseX - (double)this.draggedWindow.getDragX()), (int)(mouseY - (double)this.draggedWindow.getDragY()));
         return true;
      } else {
         this.favoriteWindow.mouseDragged(mouseX, mouseY);
         this.searchWindow.mouseDragged(mouseX, mouseY);
         return super.m_7979_(mouseX, mouseY, button, dragX, dragY);
      }
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      if (this.searchWindow.mouseScrolled(mouseX, mouseY, delta)) {
         return true;
      } else if (this.favoriteWindow.mouseScrolled(mouseX, mouseY, delta)) {
         return true;
      } else {
         Iterator var7 = this.windows.iterator();

         HackWindow window;
         do {
            if (!var7.hasNext()) {
               return false;
            }

            window = (HackWindow)var7.next();
         } while(!window.mouseScrolled(mouseX, mouseY, delta));

         return true;
      }
   }

   public boolean m_7043_() {
      return false;
   }

   public boolean isGuiVisible() {
      return this.guiVisible;
   }

   public void setGuiVisible(boolean visible) {
      this.guiVisible = visible;
      Iterator var2 = this.windows.iterator();

      while(var2.hasNext()) {
         HackWindow window = (HackWindow)var2.next();
         window.setVisible(visible);
      }

      if (this.searchWindow != null) {
         this.searchWindow.setFocused(false);
      }

   }

   public void setCurrentBindingButton(HackButton button) {
      if (this.currentBindingButton != null) {
         this.currentBindingButton.cancelBinding();
      }

      this.currentBindingButton = button;
   }

   private static class Rectangle {
      int x;
      int y;
      int width;
      int height;

      Rectangle(int x, int y, int width, int height) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
      }

      boolean intersects(Rectangle other) {
         return this.x < other.x + other.width && this.x + this.width > other.x && this.y < other.y + other.height && this.y + this.height > other.y;
      }
   }
}
