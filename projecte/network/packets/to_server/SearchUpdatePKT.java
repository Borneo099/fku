package moze_intel.projecte.network.packets.to_server;

import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public record SearchUpdatePKT(int slot, ItemStack itemStack) implements IPEPacket {
   public SearchUpdatePKT(int slot, ItemStack itemStack) {
      itemStack = itemStack.m_41777_();
      this.slot = slot;
      this.itemStack = itemStack;
   }

   public void handle(NetworkEvent.Context context) {
      Player player = context.getSender();
      if (player != null) {
         AbstractContainerMenu var4 = player.f_36096_;
         if (var4 instanceof TransmutationContainer) {
            TransmutationContainer container = (TransmutationContainer)var4;
            container.transmutationInventory.writeIntoOutputSlot(this.slot, this.itemStack);
         }
      }

   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130130_(this.slot);
      buffer.m_130055_(this.itemStack);
   }

   public static SearchUpdatePKT decode(FriendlyByteBuf buffer) {
      return new SearchUpdatePKT(buffer.m_130242_(), buffer.m_130267_());
   }

   public int slot() {
      return this.slot;
   }

   public ItemStack itemStack() {
      return this.itemStack;
   }
}
