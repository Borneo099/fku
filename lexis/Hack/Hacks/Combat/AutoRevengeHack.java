package lexis.Hack.Hacks.Combat;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class AutoRevengeHack extends Hack {
   private static boolean active = false;
   private static long cooldown = 200L;
   public static long lastAttack = 0L;
   private HackConfig config = HackConfig.getInstance();

   public AutoRevengeHack() {
      super("自动反击", new String[]{"自动反击回去实体"}, Hack.Category.COMBAT, true);
      this.addSetting(new Hack.Setting("反击冷却", "攻击后就多久", 200, 0, 1000, Hack.ValueDisplay.INTEGER));
      this.load();
   }

   private void load() {
      cooldown = (long)this.config.getDoubleSetting("自动反击", "反击冷却", 200.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         if (s.getName().equals("反击冷却")) {
            s.setValue((double)cooldown);
         }
      }

   }

   private void save() {
      this.config.saveHackSettings("自动反击", this.getSettings());
   }

   public void onEnable() {
      active = true;
      this.load();
   }

   public void onDisable() {
      active = false;
   }

   public void onUpdate() {
      boolean need = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         if (s.getName().equals("反击冷却")) {
            long v = (long)s.getDouble();
            if (v != cooldown) {
               cooldown = v;
               need = true;
            }
            break;
         }
      }

      if (need) {
         this.save();
      }

   }

   public void onClick() {
      this.toggle();
   }

   public static boolean isActive() {
      return active;
   }

   public static long getCooldown() {
      return cooldown;
   }
}
