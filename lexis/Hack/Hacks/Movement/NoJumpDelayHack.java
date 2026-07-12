package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.mixin.accessor.ILivingEntityAccessor;

public class NoJumpDelayHack extends Hack {
   private int delay = 1;
   private HackConfig config;
   private static final String CONFIG_KEY = "无跳跃延迟";

   public NoJumpDelayHack() {
      super("无跳跃延迟", "减少连续跳跃的冷却时间", Hack.Category.MOVEMENT, true);
      this.addSetting(new Hack.Setting("冷却(tick)", "跳跃后必须等延迟(0 = 原版 1 = 几乎无延迟)", 1, 0, 4, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.delay = this.config.getIntSetting("无跳跃延迟", "冷却(tick)", 1);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         if (s.getName().equals("冷却(tick)")) {
            s.setValue((double)this.delay);
            break;
         }
      }

   }

   public void onEnable() {
      this.loadConfig();
   }

   public void onDisable() {
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         if (s.getName().equals("冷却(tick)")) {
            int newDelay = (int)s.getDouble();
            if (newDelay != this.delay) {
               this.delay = newDelay;
               needSave = true;
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("无跳跃延迟", this.getSettings());
      }

      if (mc.f_91074_ != null) {
         ILivingEntityAccessor accessor = (ILivingEntityAccessor)mc.f_91074_;
         if (accessor.getNoJumpDelay() > this.delay) {
            accessor.setNoJumpDelay(this.delay);
         }

      }
   }

   public void onClick() {
      this.toggle();
   }
}
