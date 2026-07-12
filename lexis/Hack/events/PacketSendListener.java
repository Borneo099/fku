package lexis.Hack.events;

public interface PacketSendListener extends Listener {
   void onPacketSend(PacketEvent.Send event);
}
