package lexis.mixin.mixins;

import io.netty.channel.ChannelHandlerContext;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.PacketLoggerEvent;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Connection.class})
public class PacketLoggerMixin {
   @Inject(
      method = {"send(Lnet/minecraft/network/protocol/Packet;)V"},
      at = {@At("HEAD")}
   )
   private void onSendPacket(Packet packet, CallbackInfo ci) {
      EventManager.fire(new PacketLoggerEvent.Send(packet));
   }

   @Inject(
      method = {"channelRead0*"},
      at = {@At("HEAD")}
   )
   private void onChannelRead(ChannelHandlerContext context, Packet packet, CallbackInfo ci) {
      EventManager.fire(new PacketLoggerEvent.Receive(packet));
   }
}
