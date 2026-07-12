package lexis.mixin.mixinc;

import lexis.Hack.Hacks.Combat.CrystalAuraHack;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class ExplosionPacketMixin {
   @Inject(
      method = {"handleExplosion"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onExplosion(ClientboundExplodePacket packet, CallbackInfo ci) {
      if (CrystalAuraHack.isNoExplosionParticlesEnabled()) {
         ci.cancel();
      }

   }
}
