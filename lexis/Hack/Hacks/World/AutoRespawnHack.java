package lexis.Hack.Hacks.World;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.client.gui.screens.Screen;

public class AutoRespawnHack extends Hack {
   private HackConfig config = HackConfig.getInstance();

   public AutoRespawnHack() {
      super("自动重生", "在死亡后自动重生", Hack.Category.WORLD, true);
      this.loadConfig();
   }

   private void loadConfig() {
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
      if (mc.f_91074_ != null) {
         if (!mc.f_91074_.m_6084_() && mc.f_91074_.f_20919_ > 0) {
            mc.f_91074_.m_7583_();
            mc.m_91152_((Screen)null);
         }

      }
   }

   public void onClick() {
      this.toggle();
   }
}
