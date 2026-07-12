package lexis.mixin;

import lexis.Client.OOCCommand.SignOOC.BlockDataQueryHandler;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class BlockDataQueryMixin {
   @Inject(
      method = {"handleTagQueryPacket"},
      at = {@At("HEAD")}
   )
   private void lexisOnTagQuery(ClientboundTagQueryPacket packet, CallbackInfo ci) {
      BlockDataQueryHandler.handleResponse(packet.m_133506_(), packet.m_133509_());
   }
}
