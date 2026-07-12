package lexis.mixin;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.World.AntiKickMineHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Connection.class})
public class AntiKickMineMixin {
   @Inject(
      method = {"send*"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendPacket(Packet packet, CallbackInfo ci) {
      if (packet instanceof ServerboundPlayerActionPacket actionPacket) {
         Iterator var4 = HackManager.getInstance().getHacks().iterator();

         while(var4.hasNext()) {
            Hack hack = (Hack)var4.next();
            if (hack instanceof AntiKickMineHack antiKick && hack.isEnabled()) {
               if (actionPacket.m_134285_() == Action.START_DESTROY_BLOCK && antiKick.shouldCancelBreak(actionPacket.m_134281_())) {
                  ci.cancel();
                  return;
               }
            }
         }
      }

   }
}
