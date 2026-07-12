package lexis.Hack.Hacks.Misc;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.NotificationManager;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.Action;

public class IpHack extends Hack {
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "读取IP";

   public IpHack() {
      super("读取IP", new String[]{"读取当前服务器的IP地址", "聊天中点击IP可直接复制"}, Hack.Category.MISC, false);
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
      this.showIpInChat();
   }

   private String getCurrentIp() {
      Minecraft mc = Minecraft.m_91087_();
      ServerData serverData = mc.m_91089_();
      if (serverData != null) {
         String ip = serverData.f_105363_;
         if (!ip.contains(":")) {
            ip = ip + ":25565";
         }

         return ip;
      } else {
         return "127.0.0.1:25565";
      }
   }

   private void showIpInChat() {
      Minecraft mc = Minecraft.m_91087_();
      String ip = this.getCurrentIp();
      if (mc.f_91074_ != null) {
         Component message = Component.m_237113_("§d[§6Lexis§d] §fIP：").m_7220_(Component.m_237113_(ip).m_6270_(Style.f_131099_.m_131162_(true).m_178520_(5635925).m_131142_(new ClickEvent(Action.COPY_TO_CLIPBOARD, ip)).m_131144_(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.f_130831_, Component.m_237113_("点击复制IP到剪贴板")))));
         mc.f_91074_.m_5661_(message, false);
      }

      NotificationManager.info("读取IP", "已经转发在聊天！去看聊天！", 2);
   }
}
