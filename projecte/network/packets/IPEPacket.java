package moze_intel.projecte.network.packets;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public interface IPEPacket {
   void handle(NetworkEvent.Context var1);

   void encode(FriendlyByteBuf var1);

   static void handle(IPEPacket message, Supplier ctx) {
      NetworkEvent.Context context = (NetworkEvent.Context)ctx.get();
      context.enqueueWork(() -> {
         message.handle(context);
      });
      context.setPacketHandled(true);
   }
}
