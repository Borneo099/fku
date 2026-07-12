package lexis.mixin;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Protect.ParticleProtectHack;
import lexis.Hack.Hacks.Render.TimeChangerHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class ClientPacketListenerMixin {
   @Shadow
   @Final
   private Minecraft f_104888_;
   private static int particleCounter = 0;
   private static long lastResetTime = 0L;

   @Inject(
      method = {"handleSetTime"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onHandleSetTime(ClientboundSetTimePacket packet, CallbackInfo ci) {
      Minecraft mc = Minecraft.m_91087_();
      Iterator var4 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var4.hasNext()) {
            return;
         }

         hack = (Hack)var4.next();
      } while(!(hack instanceof TimeChangerHack) || !hack.isEnabled());

      ci.cancel();
   }

   @Inject(
      method = {"handleParticleEvent"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onHandleParticleEvent(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
      Iterator var3 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var3.hasNext()) {
            return;
         }

         hack = (Hack)var3.next();
      } while(!(hack instanceof ParticleProtectHack) || !hack.isEnabled());

      if (ParticleProtectHack.shouldBlockParticle()) {
         ci.cancel();
      } else {
         if (ParticleProtectHack.shouldLimitCount()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastResetTime > 1000L) {
               particleCounter = 0;
               lastResetTime = currentTime;
            }

            int maxCount = ParticleProtectHack.getMaxParticleCount();
            int packetParticleCount = packet.m_132321_();
            if (particleCounter + packetParticleCount > maxCount) {
               ci.cancel();
               return;
            }

            particleCounter += packetParticleCount;
         }

      }
   }
}
