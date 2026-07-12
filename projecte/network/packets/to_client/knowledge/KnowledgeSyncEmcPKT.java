package moze_intel.projecte.network.packets.to_client.knowledge;

import java.math.BigInteger;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public record KnowledgeSyncEmcPKT(BigInteger emc) implements IPEPacket {
   public KnowledgeSyncEmcPKT(BigInteger emc) {
      this.emc = emc;
   }

   public void handle(NetworkEvent.Context context) {
      LocalPlayer player = Minecraft.m_91087_().f_91074_;
      if (player != null) {
         player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent((cap) -> {
            cap.setEmc(this.emc);
            AbstractContainerMenu patt842$temp = player.f_36096_;
            if (patt842$temp instanceof TransmutationContainer container) {
               container.transmutationInventory.updateClientTargets();
            }

         });
      }

      PECore.debugLog("** RECEIVED TRANSMUTATION EMC DATA CLIENTSIDE **");
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130070_(this.emc.toString());
   }

   public static KnowledgeSyncEmcPKT decode(FriendlyByteBuf buffer) {
      String emc = buffer.m_130277_();
      return new KnowledgeSyncEmcPKT(emc.isEmpty() ? BigInteger.ZERO : new BigInteger(emc));
   }

   public BigInteger emc() {
      return this.emc;
   }
}
