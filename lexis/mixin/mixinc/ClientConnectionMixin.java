package lexis.mixin.mixinc;

import lexis.Hack.Hacks.Combat.AutoCriticalsHack;
import lexis.Hack.Hacks.Movement.AntiHungerHack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Connection.class})
public class ClientConnectionMixin {
   private static boolean isProcessing = false;

   @Inject(
      method = {"send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendPacket(Packet packet, PacketSendListener listener, CallbackInfo ci) {
      if (!isProcessing && AntiHungerHack.enabled) {
         if (packet instanceof ServerboundMovePlayerPacket) {
            ServerboundMovePlayerPacket oldPacket = (ServerboundMovePlayerPacket)packet;
            if (Minecraft.m_91087_().f_91074_ != null && Minecraft.m_91087_().f_91074_.m_20096_()) {
               isProcessing = true;

               try {
                  ServerboundMovePlayerPacket newPacket = this.createModifiedPacket(oldPacket);
                  Connection connection = (Connection)this;
                  connection.m_243124_(newPacket, listener);
                  ci.cancel();
               } finally {
                  isProcessing = false;
               }
            }
         }

      }
   }

   private ServerboundMovePlayerPacket createModifiedPacket(ServerboundMovePlayerPacket oldPacket) {
      double x = oldPacket.m_134129_(Double.MAX_VALUE);
      double y = oldPacket.m_134140_(Double.MAX_VALUE);
      double z = oldPacket.m_134146_(Double.MAX_VALUE);
      float yaw = oldPacket.m_134131_(Float.MAX_VALUE);
      float pitch = oldPacket.m_134142_(Float.MAX_VALUE);
      if (oldPacket.m_179683_() && oldPacket.m_179684_()) {
         return new ServerboundMovePlayerPacket.PosRot(x, y, z, yaw, pitch, false);
      } else if (oldPacket.m_179683_()) {
         return new ServerboundMovePlayerPacket.Pos(x, y, z, false);
      } else {
         return (ServerboundMovePlayerPacket)(oldPacket.m_179684_() ? new ServerboundMovePlayerPacket.Rot(yaw, pitch, false) : new ServerboundMovePlayerPacket.StatusOnly(false));
      }
   }

   @Inject(
      method = {"send*"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendPacket(Packet packet, CallbackInfo ci) {
      if (!isProcessing) {
         if (!AutoCriticalsHack.bypass) {
            if (AutoCriticalsHack.enabled && packet instanceof ServerboundInteractPacket) {
               ServerboundInteractPacket interactPacket = (ServerboundInteractPacket)packet;
               Minecraft mc = Minecraft.m_91087_();
               if (mc.f_91074_ == null || mc.f_91074_.f_108617_ == null) {
                  return;
               }

               if (AutoCriticalsHack.onlyOnGround && !mc.f_91074_.m_20096_()) {
                  return;
               }

               isProcessing = true;

               try {
                  ci.cancel();
                  if (AutoCriticalsHack.criticalMode == 0) {
                     double upY = mc.f_91074_.m_20186_() + 0.42;
                     double downY = mc.f_91074_.m_20186_();
                     mc.f_91074_.f_108617_.m_104955_(new ServerboundMovePlayerPacket.Pos(mc.f_91074_.m_20185_(), upY, mc.f_91074_.m_20189_(), false));
                     mc.f_91074_.f_108617_.m_104955_(new ServerboundMovePlayerPacket.Pos(mc.f_91074_.m_20185_(), downY, mc.f_91074_.m_20189_(), false));
                     mc.f_91074_.f_108617_.m_104955_(interactPacket);
                  } else {
                     AutoCriticalsHack.pendingPacket = interactPacket;
                     AutoCriticalsHack.jumpTime = System.currentTimeMillis();
                     mc.f_91074_.m_6135_();
                  }
               } finally {
                  isProcessing = false;
               }
            }

         }
      }
   }
}
