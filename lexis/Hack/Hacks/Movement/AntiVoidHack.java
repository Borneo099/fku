package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;

public class AntiVoidHack extends Hack implements UpdateListener {
   private HackConfig config;
   private static final String CONFIG_KEY = "防虚空";
   private Mode mode;
   private boolean hasBounced;
   private int bounceTimer;

   public AntiVoidHack() {
      super("防虚空", "防止你掉入虚空", Hack.Category.MOVEMENT, true);
      this.mode = AntiVoidHack.Mode.JUMP;
      this.hasBounced = false;
      this.bounceTimer = 0;
      this.addSetting(new Hack.Setting("模式", "防虚空的方式", "跳跃", new String[]{"跳跃", "弹飞"}));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      String modeStr = this.config.getStringSetting("防虚空", "模式", "跳跃");
      this.mode = modeStr.equals("弹飞") ? AntiVoidHack.Mode.BOUNCE : AntiVoidHack.Mode.JUMP;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         if (setting.getName().equals("模式")) {
            setting.setValue(this.mode.toString());
            break;
         }
      }

   }

   public void onEnable() {
      EventManager.add(UpdateListener.class, this);
      this.hasBounced = false;
      this.bounceTimer = 0;
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
   }

   public String getDisplayName() {
      return String.format("%s [%s]", this.getName(), this.mode.toString());
   }

   public void onUpdate() {
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("模式")) {
            String newMode = setting.getString();
            Mode newModeEnum = newMode.equals("弹飞") ? AntiVoidHack.Mode.BOUNCE : AntiVoidHack.Mode.JUMP;
            if (newModeEnum != this.mode) {
               this.mode = newModeEnum;
               this.config.saveHackSettings("防虚空", this.getSettings());
            }
            break;
         }
      }

      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         int minY = mc.f_91073_.m_141937_();
         if (mc.f_91074_.m_20186_() < (double)minY) {
            if (this.mode == AntiVoidHack.Mode.JUMP) {
               mc.f_91074_.m_6135_();
            } else if (!this.hasBounced) {
               mc.f_91074_.m_20334_(mc.f_91074_.m_20184_().f_82479_, 1.4, mc.f_91074_.m_20184_().f_82481_);
               this.hasBounced = true;
               this.bounceTimer = 6;
            } else if (this.bounceTimer > 0) {
               --this.bounceTimer;
            } else {
               this.hasBounced = false;
            }
         } else if (this.hasBounced) {
            this.hasBounced = false;
            this.bounceTimer = 0;
         }

      }
   }

   public void onClick() {
      this.toggle();
   }

   public static enum Mode {
      JUMP("跳跃"),
      BOUNCE("弹飞");

      private final String displayName;

      private Mode(String name) {
         this.displayName = name;
      }

      public String toString() {
         return this.displayName;
      }

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{JUMP, BOUNCE};
      }
   }
}
