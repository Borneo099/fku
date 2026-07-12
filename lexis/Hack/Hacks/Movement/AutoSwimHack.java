package lexis.Hack.Hacks.Movement;

import lexis.Hack.Hack;
import net.minecraft.client.player.LocalPlayer;

public class AutoSwimHack extends Hack {
   public AutoSwimHack() {
      super("自动游泳", "在水中自动冲刺", Hack.Category.MOVEMENT);
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
      LocalPlayer player = mc.f_91074_;
      if (player != null) {
         if (!player.m_20142_() && player.m_20069_() && player.f_20902_ > 0.0F) {
            player.m_6858_(true);
         }

      }
   }

   public void onClick() {
      this.toggle();
   }
}
