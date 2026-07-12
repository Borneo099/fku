package lexis.Hack.Hacks.Lexis;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.HUD.TabGui;
import lexis.Hack.Hackutil.config.HackConfig;

public class TabGuiHack extends Hack {
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "TabGui";
   private static TabGui tabGui;

   public TabGuiHack() {
      super("TabGui", "键盘控制的选项卡菜单", Hack.Category.LEXIS, true);
      this.loadConfig();
   }

   private void loadConfig() {
   }

   public void onEnable() {
      if (tabGui == null) {
         tabGui = new TabGui();
      }

   }

   public void onDisable() {
      tabGui = null;
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }

   public static TabGui getTabGui() {
      return tabGui;
   }
}
