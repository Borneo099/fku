package moze_intel.projecte.network.packets.to_client.knowledge;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

public record KnowledgeSyncChangePKT(ItemInfo change, boolean learned) implements IPEPacket {
   public KnowledgeSyncChangePKT(ItemInfo change, boolean learned) {
      this.change = change;
      this.learned = learned;
   }

   public void handle(NetworkEvent.Context context) {
      LocalPlayer player = Minecraft.m_91087_().f_91074_;
      if (player != null) {
         player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent((cap) -> {
            if (this.learned) {
               if (!cap.hasKnowledge(this.change) && cap.addKnowledge(this.change)) {
                  AbstractContainerMenu patt985$temp = player.f_36096_;
                  if (patt985$temp instanceof TransmutationContainer) {
                     TransmutationContainer containerx = (TransmutationContainer)patt985$temp;
                     containerx.transmutationInventory.itemLearned();
                  }
               }
            } else if (cap.hasKnowledge(this.change) && cap.removeKnowledge(this.change)) {
               AbstractContainerMenu patt1188$temp = player.f_36096_;
               if (patt1188$temp instanceof TransmutationContainer) {
                  TransmutationContainer container = (TransmutationContainer)patt1188$temp;
                  container.transmutationInventory.itemUnlearned();
               }
            }

         });
      }

      PECore.debugLog("** RECEIVED TRANSMUTATION KNOWLEDGE CHANGE DATA CLIENTSIDE **");
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, this.change.getItem());
      buffer.m_130079_(this.change.getNBT());
      buffer.writeBoolean(this.learned);
   }

   public static KnowledgeSyncChangePKT decode(FriendlyByteBuf buffer) {
      return new KnowledgeSyncChangePKT(ItemInfo.fromItem((ItemLike)buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS), buffer.m_130260_()), buffer.readBoolean());
   }

   public ItemInfo change() {
      return this.change;
   }

   public boolean learned() {
      return this.learned;
   }
}
