package lexis.Hack.Hacks.Misc;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.gui.screens.PacketCancellerScreen;

public class PacketCancellerHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "数据包取消器";

   public PacketCancellerHack() {
      super("数据包取消器", "取消特定数据包的发送/接收", Hack.Category.MISC, true);
      this.addSetting(new Hack.Setting("打开S2C设置", "打开服务端→客户端数据包设置", "打开S2C", () -> {
         if (mc != null) {
            mc.m_91152_(new PacketCancellerScreen(this, mc.f_91080_, PacketCancellerScreen.Tab.S2C));
         }

      }));
      this.addSetting(new Hack.Setting("打开C2S设置", "打开客户端→服务端数据包设置", "打开C2S", () -> {
         if (mc != null) {
            mc.m_91152_(new PacketCancellerScreen(this, mc.f_91080_, PacketCancellerScreen.Tab.C2S));
         }

      }));
      this.config = HackConfig.getInstance();
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
