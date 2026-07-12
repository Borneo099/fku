package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.network.protocol.Packet;

public class PacketLoggerEvent {
   public static class Receive extends CancellableEvent {
      public Packet packet;

      public Receive(Packet packet) {
         this.packet = packet;
      }

      public void fire(ArrayList listeners) {
         Iterator var2 = listeners.iterator();

         while(var2.hasNext()) {
            PacketLoggerListener listener = (PacketLoggerListener)var2.next();
            listener.onPacketReceive(this);
            if (this.isCancelled()) {
               break;
            }
         }

      }

      public Class getListenerType() {
         return PacketLoggerListener.class;
      }
   }

   public static class Send extends CancellableEvent {
      public Packet packet;

      public Send(Packet packet) {
         this.packet = packet;
      }

      public void fire(ArrayList listeners) {
         Iterator var2 = listeners.iterator();

         while(var2.hasNext()) {
            PacketLoggerListener listener = (PacketLoggerListener)var2.next();
            listener.onPacketSend(this);
            if (this.isCancelled()) {
               break;
            }
         }

      }

      public Class getListenerType() {
         return PacketLoggerListener.class;
      }
   }
}
