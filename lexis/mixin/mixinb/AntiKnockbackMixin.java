package lexis.mixin.mixinb;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Movement.AntiKnockbackHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientboundSetEntityMotionPacket.class})
public class AntiKnockbackMixin {
   @Inject(
      method = {"handle*"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onHandle(ClientGamePacketListener listener, CallbackInfo ci) {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null) {
         ClientboundSetEntityMotionPacket packet = (ClientboundSetEntityMotionPacket)this;
         if (packet.m_133192_() == mc.f_91074_.m_19879_()) {
            Iterator var5 = HackManager.getInstance().getHacks().iterator();

            while(var5.hasNext()) {
               Hack hack = (Hack)var5.next();
               if (hack instanceof AntiKnockbackHack && hack.isEnabled()) {
                  ci.cancel();
                  return;
               }
            }
         }

      }
   }
}
