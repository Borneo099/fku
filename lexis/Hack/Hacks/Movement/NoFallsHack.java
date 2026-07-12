package lexis.Hack.Hacks.Movement;

import lexis.Hack.Hack;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class NoFallsHack extends Hack implements UpdateListener {
   public NoFallsHack() {
      super("无摔伤", "下落速度过快会受伤！ 不如使用无摔伤2呢", Hack.Category.MOVEMENT, true);
   }

   public void onEnable() {
      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
   }

   public void onUpdate() {
      LocalPlayer player = mc.f_91074_;
      if (player != null) {
         if (!player.m_20069_() && !player.m_20077_()) {
            if (!player.m_150110_().f_35935_) {
               if (!(player.f_19789_ <= 2.0F)) {
                  if (player.f_108617_ != null) {
                     player.f_108617_.m_104955_(new ServerboundMovePlayerPacket.StatusOnly(true));
                  }

               }
            }
         }
      }
   }

   public void onClick() {
      this.toggle();
   }
}
