package moze_intel.projecte.network.packets.to_client.knowledge;

import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class UpdateTransmutationTargetsPkt implements IPEPacket {
   public void handle(NetworkEvent.Context context) {
      LocalPlayer player = Minecraft.m_91087_().f_91074_;
      if (player != null) {
         AbstractContainerMenu var4 = player.f_36096_;
         if (var4 instanceof TransmutationContainer) {
            TransmutationContainer container = (TransmutationContainer)var4;
            container.transmutationInventory.updateClientTargets();
         }
      }

   }

   public void encode(FriendlyByteBuf buffer) {
   }

   public static UpdateTransmutationTargetsPkt decode(FriendlyByteBuf buffer) {
      return new UpdateTransmutationTargetsPkt();
   }
}
