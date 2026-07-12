package lexis.mixin.mixina;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.TimeoutException;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Misc.AntiPacketKickHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.PacketEvent;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Connection.class})
public abstract class ClientConnectionMixin {
   @Inject(
      method = {"channelRead0*"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V",
   shift = Shift.BEFORE
)},
      cancellable = true
   )
   private void onChannelRead(ChannelHandlerContext context, Packet packet, CallbackInfo ci) {
      if (packet instanceof ClientboundBundlePacket bundle) {
         Iterator it = bundle.m_264216_().iterator();

         while(it.hasNext()) {
            PacketEvent.Receive event = new PacketEvent.Receive((Packet)it.next(), (Connection)this);
            EventManager.fire(event);
            if (event.isCancelled()) {
               it.remove();
            }
         }
      } else {
         PacketEvent.Receive event = new PacketEvent.Receive(packet, (Connection)this);
         EventManager.fire(event);
         if (event.isCancelled()) {
            ci.cancel();
         }
      }

   }

   @Inject(
      method = {"send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendPacket(Packet packet, @Nullable PacketSendListener listener, CallbackInfo ci) {
      PacketEvent.Send event = new PacketEvent.Send(packet, (Connection)this);
      EventManager.fire(event);
      if (event.isCancelled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V"},
      at = {@At("TAIL")}
   )
   private void onSendPacketTail(Packet packet, @Nullable PacketSendListener listener, CallbackInfo ci) {
      PacketEvent.Sent event = new PacketEvent.Sent(packet, (Connection)this);
      EventManager.fire(event);
   }

   @Inject(
      method = {"exceptionCaught"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onExceptionCaught(ChannelHandlerContext context, Throwable throwable, CallbackInfo ci) {
      if (!(throwable instanceof TimeoutException)) {
         Iterator var4 = HackManager.getInstance().getHacks().iterator();

         while(var4.hasNext()) {
            Hack hack = (Hack)var4.next();
            if (hack instanceof AntiPacketKickHack && hack.isEnabled()) {
               AntiPacketKickHack apk = (AntiPacketKickHack)hack;
               if (apk.catchExceptions()) {
                  if (apk.logExceptions()) {
                     System.out.println("[AntiPacketKick] 捕获异常: " + String.valueOf(throwable));
                     throwable.printStackTrace();
                  }

                  ci.cancel();
                  return;
               }
            }
         }

      }
   }
}
