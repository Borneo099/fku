package moze_intel.projecte.network.packets.to_client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

public record SyncFuelMapperPKT(List items) implements IPEPacket {
   public SyncFuelMapperPKT(List items) {
      this.items = items;
   }

   public void handle(NetworkEvent.Context context) {
      FuelMapper.setFuelMap(this.items);
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130130_(this.items.size());
      Iterator var2 = this.items.iterator();

      while(var2.hasNext()) {
         Item item = (Item)var2.next();
         buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, item);
      }

   }

   public static SyncFuelMapperPKT decode(FriendlyByteBuf buffer) {
      int size = buffer.m_130242_();
      List items = new ArrayList(size);

      for(int i = 0; i < size; ++i) {
         items.add((Item)buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS));
      }

      return new SyncFuelMapperPKT(items);
   }

   public List items() {
      return this.items;
   }
}
