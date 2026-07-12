package lexis.Hack.Hacks.Misc;

import lexis.Hack.Hack;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.PacketEvent;
import lexis.Hack.events.PacketSendListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action;

public class GhostHack extends Hack implements PacketSendListener {
   private boolean bypass = false;

   public GhostHack() {
      super("幽灵模式", new String[]{"要死亡后 就会进入 幽灵模式，关闭功能就直接重生了", "§c§l注意：进入幽灵模式 可能看不到 实体 和 玩家 的渲染"}, Hack.Category.MISC, true);
   }

   public void onEnable() {
      this.bypass = false;
      EventManager.add(PacketSendListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(PacketSendListener.class, this);
      if (mc.f_91074_ != null && mc.m_91403_() != null) {
         mc.m_91403_().m_104955_(new ServerboundClientCommandPacket(Action.PERFORM_RESPAWN));
      }

      this.bypass = false;
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         if (mc.f_91074_.m_21223_() <= 0.0F) {
            mc.f_91074_.m_21153_(20.0F);
            this.bypass = true;
            mc.m_91152_((Screen)null);
            mc.f_91074_.m_6034_(mc.f_91074_.m_20185_(), mc.f_91074_.m_20186_(), mc.f_91074_.m_20189_());
            mc.f_91074_.m_213846_(Component.m_237113_("§6[§bLexis§6] §d[§7幽灵模式§d] 你已经死亡，功能有阻止了重生，如果你想要重生就在关闭功能就重生！"));
         }

      }
   }

   public void onPacketSend(PacketEvent.Send event) {
      if (this.bypass && event.packet instanceof ServerboundMovePlayerPacket) {
         event.cancel();
      }

   }

   public void onClick() {
      this.toggle();
   }
}
