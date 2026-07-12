package lexis.Hack.Hacks.Misc;

import lexis.Hack.Hack;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.PacketEvent;
import lexis.Hack.events.PacketReceiveListener;
import lexis.mixin.accessor.IClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

public class NoServerRotateHack extends Hack implements PacketReceiveListener {
   public NoServerRotateHack() {
      super("无服务器旋转", "阻止服务器强制修改玩家视角", Hack.Category.MISC, true);
   }

   public void onEnable() {
      EventManager.add(PacketReceiveListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(PacketReceiveListener.class, this);
   }

   public void onPacketReceive(PacketEvent.Receive event) {
      if (mc.f_91074_ != null) {
         Packet var3 = event.packet;
         if (var3 instanceof ClientboundPlayerPositionPacket) {
            ClientboundPlayerPositionPacket packet = (ClientboundPlayerPositionPacket)var3;
            IClientboundPlayerPositionPacket accessor = (IClientboundPlayerPositionPacket)packet;
            accessor.setYaw(mc.f_91074_.m_146908_());
            accessor.setPitch(mc.f_91074_.m_146909_());
         }

      }
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }
}
