package lexis.Hack.events;

public class PacketCancelListener implements PacketSendListener {
   private static PacketCancelListener instance;

   public static void register() {
      if (instance == null) {
         instance = new PacketCancelListener();
         EventManager.add(PacketSendListener.class, instance);
      }

   }

   public static void unregister() {
      if (instance != null) {
         EventManager.remove(PacketSendListener.class, instance);
         instance = null;
      }

   }

   public void onPacketSend(PacketEvent.Send event) {
      String className = event.packet.getClass().getSimpleName();
      if (PacketCancelManager.shouldCancel(className)) {
         event.cancel();
      }

   }
}
