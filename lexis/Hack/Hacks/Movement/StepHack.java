package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.client.player.LocalPlayer;

public class StepHack extends Hack implements UpdateListener {
   private HackConfig config;
   private static final String CONFIG_KEY = "快速上楼";
   private int height = 2;

   public StepHack() {
      super("快速上楼", "自动上楼梯", Hack.Category.MOVEMENT, true);
      this.addSetting(new Hack.Setting("高度", "可以上的台阶高度 (格)", 2, 1, 10));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.height = (int)this.config.getDoubleSetting("快速上楼", "高度", 2.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("高度")) {
            setting.setValue((double)this.height);
            break;
         }
      }

   }

   public float getHeight() {
      return (float)this.height;
   }

   public void onEnable() {
      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         if (setting.getName().equals("高度")) {
            int newHeight = (int)setting.getDouble();
            if (newHeight != this.height) {
               this.height = newHeight;
               needSave = true;
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("快速上楼", this.getSettings());
      }

      LocalPlayer player = mc.f_91074_;
      if (player != null) {
         ;
      }
   }

   public void onClick() {
      this.toggle();
   }
}
