package lexis.Hack.Hackutil.ServerGUtils;

import java.util.Arrays;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.GameJoinedEvent;
import lexis.Hack.events.GameJoinedListener;
import lexis.Hack.events.PacketEvent;
import lexis.Hack.events.PacketReceiveListener;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.util.Mth;

public class TickRate implements PacketReceiveListener, GameJoinedListener {
   public static TickRate INSTANCE = new TickRate();
   private final float[] tickRates = new float[20];
   private int nextIndex = 0;
   private long timeLastTimeUpdate = -1L;
   private long timeGameJoined;

   private TickRate() {
      EventManager.add(PacketReceiveListener.class, this);
      EventManager.add(GameJoinedListener.class, this);
   }

   public void onPacketReceive(PacketEvent.Receive event) {
      if (event.packet instanceof ClientboundSetTimePacket) {
         long now = System.currentTimeMillis();
         float timeElapsed = (float)(now - this.timeLastTimeUpdate) / 1000.0F;
         this.tickRates[this.nextIndex] = Mth.m_14036_(20.0F / timeElapsed, 0.0F, 20.0F);
         this.nextIndex = (this.nextIndex + 1) % this.tickRates.length;
         this.timeLastTimeUpdate = now;
      }

   }

   public void onGameJoined(GameJoinedEvent event) {
      Arrays.fill(this.tickRates, 0.0F);
      this.nextIndex = 0;
      this.timeGameJoined = this.timeLastTimeUpdate = System.currentTimeMillis();
   }

   public float getTickRate() {
      if (System.currentTimeMillis() - this.timeGameJoined < 4000L) {
         return 20.0F;
      } else {
         int numTicks = 0;
         float sumTickRates = 0.0F;
         float[] var3 = this.tickRates;
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            float tickRate = var3[var5];
            if (tickRate > 0.0F) {
               sumTickRates += tickRate;
               ++numTicks;
            }
         }

         return sumTickRates / (float)numTicks;
      }
   }

   public float getTimeSinceLastTick() {
      long now = System.currentTimeMillis();
      return now - this.timeGameJoined < 4000L ? 0.0F : (float)(now - this.timeLastTimeUpdate) / 1000.0F;
   }
}
