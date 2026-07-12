package lexis.mixin.mixiny;

import lexis.Hack.Hacks.Combat.FreezePlayerHack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class FreezePlayerMixin {
   @Inject(
      method = {"handleMoveEntity"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onHandleMoveEntity(ClientboundMoveEntityPacket packet, CallbackInfo ci) {
      if (FreezePlayerHack.isFrozen()) {
         Entity entity = packet.m_132519_(Minecraft.m_91087_().f_91073_);
         if (entity != null && entity != Minecraft.m_91087_().f_91074_) {
            ci.cancel();
         }

      }
   }

   @Inject(
      method = {"handleTeleportEntity"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onHandleTeleportEntity(ClientboundTeleportEntityPacket packet, CallbackInfo ci) {
      if (FreezePlayerHack.isFrozen()) {
         int entityId = packet.m_133545_();
         Entity entity = Minecraft.m_91087_().f_91073_.m_6815_(entityId);
         if (entity != null && entity != Minecraft.m_91087_().f_91074_) {
            ci.cancel();
         }

      }
   }
}
