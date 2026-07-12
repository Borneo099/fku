package lexis.mixin;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.BlockEspHack;
import lexis.Hack.Hacks.Render.PortalEspHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class NetworkPacketMixin {
   private static final Minecraft mc = Minecraft.m_91087_();

   @Inject(
      method = {"handleBlockUpdate"},
      at = {@At("HEAD")}
   )
   private void onHandleBlockUpdate(ClientboundBlockUpdatePacket packet, CallbackInfo ci) {
      BlockPos pos = packet.m_131749_();
      BlockState state = packet.m_131746_();
      Iterator var5 = HackManager.getInstance().getHacks().iterator();

      while(var5.hasNext()) {
         Hack hack = (Hack)var5.next();
         if (hack instanceof BlockEspHack && hack.isEnabled()) {
            ((BlockEspHack)hack).updateBlock(pos, state);
         }

         if (hack instanceof PortalEspHack && hack.isEnabled()) {
            ((PortalEspHack)hack).updateBlock(pos, state);
         }
      }

   }

   @Inject(
      method = {"handleChunkBlocksUpdate"},
      at = {@At("HEAD")}
   )
   private void onHandleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket packet, CallbackInfo ci) {
      packet.m_132992_((pos, state) -> {
         Iterator var2 = HackManager.getInstance().getHacks().iterator();

         while(var2.hasNext()) {
            Hack hack = (Hack)var2.next();
            if (hack instanceof BlockEspHack && hack.isEnabled()) {
               ((BlockEspHack)hack).updateBlock(pos, state);
            }

            if (hack instanceof PortalEspHack && hack.isEnabled()) {
               ((PortalEspHack)hack).updateBlock(pos, state);
            }
         }

      });
   }
}
