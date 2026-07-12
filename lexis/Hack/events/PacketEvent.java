package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;

public class PacketEvent {
   public static class Sent extends Event {
      public Packet packet;
      public Connection connection;

      public Sent(Packet packet, Connection connection) {
         this.packet = packet;
         this.connection = connection;
      }

      public void fire(ArrayList listeners) {
         Iterator var2 = listeners.iterator();

         while(var2.hasNext()) {
            PacketSentListener listener = (PacketSentListener)var2.next();
            listener.onPacketSent(this);
         }

      }

      public Class getListenerType() {
         return PacketSentListener.class;
      }
   }

   public static class Send extends CancellableEvent {
      public Packet packet;
      public Connection connection;

      public Send(Packet packet, Connection connection) {
         this.packet = packet;
         this.connection = connection;
      }

      public void fire(ArrayList listeners) {
         Iterator var2 = listeners.iterator();

         while(var2.hasNext()) {
            PacketSendListener listener = (PacketSendListener)var2.next();
            listener.onPacketSend(this);
            if (this.isCancelled()) {
               break;
            }
         }

      }

      public Class getListenerType() {
         return PacketSendListener.class;
      }
   }

   public static class Receive extends CancellableEvent {
      public Packet packet;
      public Connection connection;

      public Receive(Packet packet, Connection connection) {
         this.packet = packet;
         this.connection = connection;
      }

      public void fire(ArrayList listeners) {
         Iterator var2 = listeners.iterator();

         while(var2.hasNext()) {
            PacketReceiveListener listener = (PacketReceiveListener)var2.next();
            listener.onPacketReceive(this);
            if (this.isCancelled()) {
               break;
            }
         }

      }

      public Class getListenerType() {
         return PacketReceiveListener.class;
      }
   }
}
