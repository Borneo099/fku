package lexis.Hack.Hacks.Baritone;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.BaritoneBridge;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class BaritoneSpeedHack extends Hack {
   public static boolean enabled = false;
   public static double speedMultiplier = 1.5;
   public static boolean groundOnly = true;
   private static final double VANILLA_SPEED = 0.1;
   private boolean wasActive = false;
   private final HackConfig config;

   public BaritoneSpeedHack() {
      super("Baritone加速", "寻路时加速和无惯性，精准移动", Hack.Category.BARITONE, true);
      this.addSetting(new Hack.Setting("速度倍率", "原版速度的倍数", 1.5, 1.0, 32.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("仅地面", "仅在地面时加速", true));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   public void onEnable() {
      enabled = true;
      this.loadConfig();
   }

   public void onDisable() {
      enabled = false;
      this.restoreSpeed();
   }

   public void onUpdate() {
      if (enabled && mc.f_91074_ != null && mc.f_91073_ != null) {
         boolean needSave = false;
         Iterator var2 = this.getSettings().iterator();

         while(var2.hasNext()) {
            Hack.Setting s = (Hack.Setting)var2.next();
            if ("速度倍率".equals(s.getName()) && s.getDouble() != speedMultiplier) {
               speedMultiplier = s.getDouble();
               needSave = true;
            }

            if ("仅地面".equals(s.getName()) && s.getBoolean() != groundOnly) {
               groundOnly = s.getBoolean();
               needSave = true;
            }
         }

         if (needSave) {
            this.config.saveHackSettings(this.getName(), this.getSettings());
         }

         boolean active = BaritoneBridge.isActive();
         if (active) {
            if (groundOnly && !mc.f_91074_.m_20096_()) {
               this.restoreSpeed();
               this.wasActive = true;
               return;
            }

            mc.f_91074_.m_21051_(Attributes.f_22279_).m_22100_(0.1 * speedMultiplier);
         }

         if (this.wasActive && !active) {
            this.restoreSpeed();
         }

         this.wasActive = active;
      }
   }

   private void restoreSpeed() {
      if (mc.f_91074_ != null) {
         mc.f_91074_.m_21051_(Attributes.f_22279_).m_22100_(0.1);
      }

   }

   public void onClick() {
      this.toggle();
   }

   public String getDisplayName() {
      boolean active = BaritoneBridge.isActive();
      String var10000 = this.getName();
      return var10000 + String.format(" [%.1fx%s]", speedMultiplier, active ? "*" : "");
   }

   private void loadConfig() {
      speedMultiplier = this.config.getDoubleSetting(this.getName(), "速度倍率", 1.5);
      groundOnly = this.config.getBooleanSetting(this.getName(), "仅地面", true);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         if ("速度倍率".equals(s.getName())) {
            s.setValue(speedMultiplier);
         }

         if ("仅地面".equals(s.getName())) {
            s.setValue(groundOnly);
         }
      }

   }
}
