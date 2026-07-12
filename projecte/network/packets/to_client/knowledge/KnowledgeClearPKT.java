package moze_intel.projecte.network.packets.to_client.knowledge;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class KnowledgeClearPKT implements IPEPacket {
   public void handle(NetworkEvent.Context context) {
      LocalPlayer player = Minecraft.m_91087_().f_91074_;
      if (player != null) {
         player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent((cap) -> {
            cap.clearKnowledge();
            AbstractContainerMenu patt764$temp = player.f_36096_;
            if (patt764$temp instanceof TransmutationContainer container) {
               container.transmutationInventory.updateClientTargets();
            }

         });
      }

   }

   public void encode(FriendlyByteBuf buffer) {
   }

   public static KnowledgeClearPKT decode(FriendlyByteBuf buffer) {
      return new KnowledgeClearPKT();
   }
}
