package lexis.Hack.events;

public interface PacketLoggerListener extends Listener {
   void onPacketSend(PacketLoggerEvent.Send event);

   void onPacketReceive(PacketLoggerEvent.Receive event);
}
