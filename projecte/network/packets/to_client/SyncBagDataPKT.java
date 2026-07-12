package moze_intel.projecte.network.packets.to_client;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record SyncBagDataPKT(CompoundTag nbt) implements IPEPacket {
   public SyncBagDataPKT(CompoundTag nbt) {
      this.nbt = nbt;
   }

   public void handle(NetworkEvent.Context context) {
      if (Minecraft.m_91087_().f_91074_ != null) {
         Minecraft.m_91087_().f_91074_.getCapability(PECapabilities.ALCH_BAG_CAPABILITY).ifPresent((cap) -> {
            cap.deserializeNBT(this.nbt);
         });
      }

      PECore.debugLog("** RECEIVED BAGS CLIENTSIDE **");
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130079_(this.nbt);
   }

   public static SyncBagDataPKT decode(FriendlyByteBuf buffer) {
      return new SyncBagDataPKT(buffer.m_130260_());
   }

   public CompoundTag nbt() {
      return this.nbt;
   }
}
