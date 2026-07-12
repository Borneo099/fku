package lexis.mixin.mixina;

import java.util.UUID;
import lexis.Hack.Hacks.Fun.ImitatePlayerHack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class ClientPacketListenerImitateMixin {
   @Inject(
      method = {"handlePlayerChat"},
      at = {@At("HEAD")}
   )
   private void onHandlePlayerChat(ClientboundPlayerChatPacket packet, CallbackInfo ci) {
      try {
         ImitatePlayerHack hack = ImitatePlayerHack.instance;
         if (hack == null || !hack.isEnabled()) {
            return;
         }

         String target = hack.getTargetPlayer();
         if (target.isEmpty()) {
            return;
         }

         UUID senderUUID = packet.f_243918_();
         ClientPacketListener listener = (ClientPacketListener)this;
         PlayerInfo info = listener.m_104949_(senderUUID);
         if (info == null) {
            return;
         }

         String senderName = info.m_105312_().getName();
         if (senderName == null) {
            return;
         }

         if (!senderName.equalsIgnoreCase(target)) {
            return;
         }

         Component contentComp = packet.f_243686_();
         if (contentComp == null) {
            return;
         }

         String content = contentComp.getString();
         if (content.isEmpty()) {
            return;
         }

         Minecraft mc = Minecraft.m_91087_();
         if (mc.f_91074_ == null || mc.f_91074_.f_108617_ == null) {
            return;
         }

         mc.f_91074_.f_108617_.m_246175_(content);
      } catch (Exception var12) {
      }

   }
}
