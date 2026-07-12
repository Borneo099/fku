package lexis.Hack.Hacks.Render;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class NoBackgroundHack extends Hack {
   public boolean allGuis = false;
   private HackConfig config;

   public NoBackgroundHack() {
      super("无界面背景", new String[]{"移除GUI背景的", "§c§l警告：别的MOD(客户端/服务端)修改gui了开启这功能打开其地gui可能会崩闪退！"}, Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("所有界面", "移除所有界面的背景，不只是容器界面", false));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.allGuis = this.config.getBooleanSetting("无界面背景", "所有界面", false);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("所有界面")) {
            setting.setValue(this.allGuis);
            break;
         }
      }

   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("所有界面")) {
            this.allGuis = setting.getBoolean();
            break;
         }
      }

   }

   public void onClick() {
      this.toggle();
   }

   public boolean shouldCancelBackground(Screen screen) {
      if (!this.isEnabled()) {
         return false;
      } else if (mc.f_91073_ == null) {
         return false;
      } else {
         return this.allGuis || screen instanceof AbstractContainerScreen;
      }
   }
}
