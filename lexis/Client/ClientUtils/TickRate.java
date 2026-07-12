package lexis.Client.ClientUtils;

import lexis.Hack.events.EventManager;
import lexis.Hack.events.PacketEvent;
import lexis.Hack.events.PacketReceiveListener;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;

public class TickRate implements PacketReceiveListener {
   private long lastTime = -1L;
   private float tps = 20.0F;

   public TickRate() {
      EventManager.add(PacketReceiveListener.class, this);
   }

   public void onPacketReceive(PacketEvent.Receive event) {
      if (event.packet instanceof ClientboundSetTimePacket) {
         long now = System.currentTimeMillis();
         if (this.lastTime != -1L) {
            long diff = now - this.lastTime;
            if (diff > 0L) {
               this.tps = Math.min(20.0F, 20000.0F / (float)diff);
            }
         }

         this.lastTime = now;
      }

   }

   public float getTps() {
      return this.tps;
   }
}
