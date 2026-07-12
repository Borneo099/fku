package lexis.Hack.Hacks.TaCZ;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;

public class NoRecoilHack extends Hack implements UpdateListener {
   private static NoRecoilHack instance;
   public static boolean noRecoilActive = false;
   private HackConfig config;
   private static final String CONFIG_KEY = "无后座";
   private float recoilReduction = 1.0F;

   public NoRecoilHack() {
      super("无后座", "消除 TaCZ 枪械后座力", Hack.Category.TACZ, true);
      this.addSetting(new Hack.Setting("后座削减", "后座力削减比例 (1.0=完全消除)", 1.0, 0.0, 1.0));
      instance = this;
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.recoilReduction = (float)this.config.getDoubleSetting("无后座", "后座削减", 1.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if ("后座削减".equals(setting.getName())) {
            setting.setValue(this.recoilReduction);
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("无后座", this.getSettings());
   }

   public void onEnable() {
      noRecoilActive = true;
      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      noRecoilActive = false;
      EventManager.remove(UpdateListener.class, this);
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         if ("后座削减".equals(setting.getName()) && (float)setting.getDouble() != this.recoilReduction) {
            this.recoilReduction = (float)setting.getDouble();
            needSave = true;
         }
      }

      if (needSave) {
         this.saveConfig();
      }

   }

   public void onClick() {
      this.toggle();
   }

   public static NoRecoilHack getInstance() {
      return instance;
   }

   public static float getRecoilReduction() {
      return instance != null && noRecoilActive ? instance.recoilReduction : 0.0F;
   }
}
