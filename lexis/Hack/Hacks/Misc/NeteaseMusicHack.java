package lexis.Hack.Hacks.Misc;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.music.MusicScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class NeteaseMusicHack extends Hack {
   public NeteaseMusicHack() {
      super("网易云音乐", "在线搜索和播放网易云音乐", Hack.Category.MISC, false);
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
   }

   public void onClick() {
      Minecraft mc = Minecraft.m_91087_();
      mc.execute(() -> {
         Screen currentScreen = mc.f_91080_;
         mc.m_91152_(new MusicScreen(currentScreen));
      });
   }
}
