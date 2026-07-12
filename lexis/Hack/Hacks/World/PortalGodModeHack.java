package lexis.Hack.Hacks.World;

import lexis.Hack.Hack;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.PacketEvent;
import lexis.Hack.events.PacketSendListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.world.level.block.Blocks;

public class PortalGodModeHack extends Hack implements PacketSendListener {
   private long lastPortalContactTime = 0L;
   private static final long ACTIVE_WINDOW_MS = 5000L;

   public PortalGodModeHack() {
      super("传送门无敌", "在你进入传送门 几秒 会成你无敌了，别人无法打死你", Hack.Category.WORLD, true);
   }

   public void onEnable() {
      EventManager.add(PacketSendListener.class, this);
      this.lastPortalContactTime = 0L;
   }

   public void onDisable() {
      EventManager.remove(PacketSendListener.class, this);
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         BlockPos center = mc.f_91074_.m_20183_();
         boolean nearPortal = false;

         label38:
         for(int x = -2; x <= 2; ++x) {
            for(int y = -2; y <= 2; ++y) {
               for(int z = -2; z <= 2; ++z) {
                  if (mc.f_91073_.m_8055_(center.m_7918_(x, y, z)).m_60734_() == Blocks.f_50142_) {
                     nearPortal = true;
                     break label38;
                  }
               }
            }
         }

         if (nearPortal) {
            this.lastPortalContactTime = System.currentTimeMillis();
         }

      }
   }

   public void onPacketSend(PacketEvent.Send event) {
      if (event.packet instanceof ServerboundAcceptTeleportationPacket) {
         if (System.currentTimeMillis() - this.lastPortalContactTime < 5000L) {
            event.cancel();
         }

      }
   }

   public void onClick() {
      this.toggle();
   }
}
