package lexis.Hack.Hackutil.HUD;

import lexis.Hack.Hacks.Lexis.BindsDisplayHack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MoveBindsDisplayScreen extends Screen {
   private final BindsDisplayHack hack;
   private final BindsDisplayWidget widget;
   private final Screen previousScreen;

   public MoveBindsDisplayScreen(BindsDisplayHack hack, Screen previousScreen) {
      super(Component.m_237113_("移动按键显示"));
      this.hack = hack;
      this.widget = hack.getWidget();
      this.previousScreen = previousScreen;
   }

   public void m_88315_(GuiGraphics gui, int mouseX, int mouseY, float delta) {
      gui.m_280509_(0, 0, this.f_96543_, this.f_96544_, -2013265920);
      String tip = "拖动标题栏移动按键显示窗口";
      int tipWidth = this.f_96547_.m_92895_(tip);
      int tipX = (this.f_96543_ - tipWidth) / 2;
      int tipY = this.f_96544_ / 2 - 10;
      gui.m_280488_(this.f_96547_, tip, tipX, tipY, 16777215);
      gui.m_280488_(this.f_96547_, "按 ESC 键保存位置并退出", tipX, tipY + 12, 11184810);
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      return this.widget != null && this.widget.mouseClicked(mouseX, mouseY, button) ? true : true;
   }

   public boolean m_6348_(double mouseX, double mouseY, int button) {
      if (this.widget != null) {
         this.widget.mouseReleased();
      }

      return true;
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (this.widget != null) {
         this.widget.mouseDragged(mouseX, mouseY);
      }

      return true;
   }

   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         Minecraft.m_91087_().m_91152_(this.previousScreen);
         this.hack.setMovingMode(false);
         return true;
      } else {
         return true;
      }
   }

   public boolean m_7043_() {
      return false;
   }

   public void m_7861_() {
      this.hack.setMovingMode(false);
   }
}
