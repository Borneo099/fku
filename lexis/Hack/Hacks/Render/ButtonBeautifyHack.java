package lexis.Hack.Hacks.Render;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class ButtonBeautifyHack extends Hack {
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "美化所有原版按钮";

   public ButtonBeautifyHack() {
      super("美化所有原版按钮", "替换为黑色半透明背景 + 梦幻紫粉边框", Hack.Category.RENDER, true);
      this.loadConfig();
   }

   private void loadConfig() {
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
