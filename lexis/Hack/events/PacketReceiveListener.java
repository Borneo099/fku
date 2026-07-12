package lexis.Hack.events;

public interface PacketReceiveListener extends Listener {
   void onPacketReceive(PacketEvent.Receive event);
}
