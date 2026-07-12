package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.network.protocol.Packet;

public interface PacketOutputListener extends Listener {
   void onPacketOutput(PacketOutputEvent event);

   public static class PacketOutputEvent extends CancellableEvent {
      private Packet packet;

      public PacketOutputEvent(Packet packet) {
         this.packet = packet;
      }

      public Packet getPacket() {
         return this.packet;
      }

      public void setPacket(Packet packet) {
         this.packet = packet;
      }

      public void fire(ArrayList listeners) {
         Iterator var2 = listeners.iterator();

         while(var2.hasNext()) {
            PacketOutputListener listener = (PacketOutputListener)var2.next();
            listener.onPacketOutput(this);
            if (this.isCancelled()) {
               break;
            }
         }

      }

      public Class getListenerType() {
         return PacketOutputListener.class;
      }
   }
}
