package lexis.Hack.Hacks.Render;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class ColorCodeHack extends Hack {
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "颜色代码";

   public ColorCodeHack() {
      super("颜色代码", new String[]{"允许在聊天和输入框中使用 §1颜§2色§3代§4码 §f格式代码", "命令方块 和 书 和 其地等等可以使用这颜色代码", "§c§l注意：如果你是客户端 这使用在聊天会被踢出", "§6§l可用：需要房主安装Lexismod 开启这功能 客户端的lexis可以发送颜色代码了不会被踢出"}, Hack.Category.RENDER, true);
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
