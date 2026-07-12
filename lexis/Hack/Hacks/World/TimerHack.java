package lexis.Hack.Hacks.World;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.client.Timer;

public class TimerHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "变速";
   private double speed = 1.0;
   private Timer originalTimer;

   public TimerHack() {
      super("变速", "改变游戏速度", Hack.Category.WORLD, true);
      this.addSetting(new Hack.Setting("速度", "游戏速度倍数 (0.01 - 100)", 1.0, 0.01, 100.0, Hack.ValueDisplay.DECIMAL));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.speed = this.config.getDoubleSetting("变速", "速度", 1.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("速度")) {
            setting.setValue(this.speed);
            break;
         }
      }

   }

   public void onEnable() {
      if (mc != null && mc.f_90991_ != null) {
         this.originalTimer = mc.f_90991_;
         this.applySpeed();
      }

   }

   public void onDisable() {
      if (this.originalTimer != null && mc != null) {
         mc.f_90991_ = this.originalTimer;
         this.originalTimer = null;
      } else if (mc != null && mc.f_90991_ != null) {
         mc.f_90991_ = new Timer(20.0F, 0L);
      }

   }

   public String getDisplayName() {
      return String.format("%s [%.1f倍率]", this.getName(), this.speed);
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         if (setting.getName().equals("速度")) {
            double newSpeed = setting.getDouble();
            if (newSpeed != this.speed) {
               this.speed = newSpeed;
               needSave = true;
               if (this.isEnabled()) {
                  this.applySpeed();
               }
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("变速", this.getSettings());
      }

   }

   private void applySpeed() {
      if (mc != null && mc.f_90991_ != null) {
         mc.f_90991_ = new Timer(20.0F * (float)this.speed, 0L);
      }

   }

   public void onClick() {
      this.toggle();
   }
}
