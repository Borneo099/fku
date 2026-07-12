package lexis.Hack.events;

public interface PacketSentListener extends Listener {
   void onPacketSent(PacketEvent.Sent event);
}
