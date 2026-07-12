package lexis.Hack.Hacks.Lexis;

import lexis.Hack.Hack;
import lexis.Hack.Utils.ThemeColors.ThemeManager;
import lexis.Hack.Utils.ThemeColors.ThemeSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class ThemeSettingsHack extends Hack {
   public ThemeSettingsHack() {
      super("主题设置", "自定义所有界面颜色", Hack.Category.LEXIS, false);
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
   }

   public void onClick() {
      ThemeManager.load();
      Minecraft.m_91087_().m_91152_(new ThemeSettingsScreen((Screen)null));
   }
}
