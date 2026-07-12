package moze_intel.projecte.network.packets.to_client;

import moze_intel.projecte.PECore;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class SyncEmcPKT implements IPEPacket {
   private final EmcPKTInfo[] data;

   public SyncEmcPKT(EmcPKTInfo[] data) {
      this.data = data;
   }

   public void handle(NetworkEvent.Context context) {
      PECore.LOGGER.info("Receiving EMC data from server.");
      EMCMappingHandler.fromPacket(this.data);
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130130_(this.data.length);
      EmcPKTInfo[] var2 = this.data;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         EmcPKTInfo info = var2[var4];
         buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, info.item);
         buffer.m_130079_(info.nbt());
         buffer.m_130103_(info.emc());
      }

   }

   public static SyncEmcPKT decode(FriendlyByteBuf buffer) {
      int size = buffer.m_130242_();
      EmcPKTInfo[] data = new EmcPKTInfo[size];

      for(int i = 0; i < size; ++i) {
         data[i] = new EmcPKTInfo((Item)buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS), buffer.m_130260_(), buffer.m_130258_());
      }

      return new SyncEmcPKT(data);
   }

   public static record EmcPKTInfo(Item item, @Nullable CompoundTag nbt, long emc) {
      public EmcPKTInfo(Item item, @Nullable CompoundTag nbt, long emc) {
         this.item = item;
         this.nbt = nbt;
         this.emc = emc;
      }

      public Item item() {
         return this.item;
      }

      public @Nullable CompoundTag nbt() {
         return this.nbt;
      }

      public long emc() {
         return this.emc;
      }
   }
}
