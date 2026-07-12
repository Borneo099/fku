package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class SpiderHack extends Hack implements UpdateListener {
   private HackConfig config;
   private static final String CONFIG_KEY = "蜘蛛侠";
   private double climbSpeed = 0.2;

   public SpiderHack() {
      super("蜘蛛侠", "像蜘蛛一样在墙上爬行", Hack.Category.MOVEMENT, true);
      this.addSetting(new Hack.Setting("爬升速度", "爬墙时的向上速度", 0.2, 0.05, 4.0, Hack.ValueDisplay.DECIMAL));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.climbSpeed = this.config.getDoubleSetting("蜘蛛侠", "爬升速度", 0.2);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("爬升速度")) {
            setting.setValue(this.climbSpeed);
            break;
         }
      }

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
         if (setting.getName().equals("爬升速度")) {
            double newSpeed = setting.getDouble();
            if (newSpeed != this.climbSpeed) {
               this.climbSpeed = newSpeed;
               needSave = true;
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("蜘蛛侠", this.getSettings());
      }

      if (mc.f_91074_ != null) {
         Player player = mc.f_91074_;
         boolean onWall = player.f_19862_ && !player.m_20096_();
         if (onWall) {
            Vec3 velocity = player.m_20184_();
            if (velocity.f_82480_ < this.climbSpeed) {
               player.m_20334_(velocity.f_82479_, this.climbSpeed, velocity.f_82481_);
            }
         }

      }
   }

   public void onClick() {
      this.toggle();
   }
}
