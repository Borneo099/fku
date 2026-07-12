package moze_intel.projecte.network.packets.to_client.knowledge;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public record KnowledgeSyncPKT(CompoundTag nbt) implements IPEPacket {
   public KnowledgeSyncPKT(CompoundTag nbt) {
      this.nbt = nbt;
   }

   public void handle(NetworkEvent.Context context) {
      LocalPlayer player = Minecraft.m_91087_().f_91074_;
      if (player != null) {
         player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent((cap) -> {
            cap.deserializeNBT(this.nbt);
            AbstractContainerMenu patt857$temp = player.f_36096_;
            if (patt857$temp instanceof TransmutationContainer container) {
               container.transmutationInventory.updateClientTargets();
            }

         });
      }

      PECore.debugLog("** RECEIVED TRANSMUTATION DATA CLIENTSIDE **");
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130079_(this.nbt);
   }

   public static KnowledgeSyncPKT decode(FriendlyByteBuf buffer) {
      return new KnowledgeSyncPKT(buffer.m_130260_());
   }

   public CompoundTag nbt() {
      return this.nbt;
   }
}
