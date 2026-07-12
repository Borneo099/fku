package lexis.Hack.Hacks.Protect;

import lexis.Hack.Hack;

public class PreventGameCloseHack extends Hack {
   private static PreventGameCloseHack instance;
   private static final String CONFIG_KEY = "阻止关闭游戏";

   public PreventGameCloseHack() {
      super("阻止关闭游戏", new String[]{"防止误关闭游戏(拦截 Alt+F4 和窗口关闭按钮(X) )", "§c§l警告：你开启这功能了 在主菜单中 在退出游戏 可能退出不了"}, Hack.Category.PROTECT, true);
      instance = this;
      this.loadConfig();
   }

   private void loadConfig() {
   }

   public static boolean isActive() {
      return instance != null && instance.isEnabled();
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }
}
