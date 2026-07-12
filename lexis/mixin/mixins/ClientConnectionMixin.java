package lexis.mixin.mixins;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Movement.NoFallHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Connection.class})
public class ClientConnectionMixin {
   private static final Minecraft mc = Minecraft.m_91087_();

   @Inject(
      method = {"send*"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendPacket(Packet packet, CallbackInfo ci) {
      boolean noFallEnabled = false;
      Iterator var4 = HackManager.getInstance().getHacks().iterator();

      while(var4.hasNext()) {
         Hack hack = (Hack)var4.next();
         if (hack instanceof NoFallHack && hack.isEnabled()) {
            noFallEnabled = true;
            break;
         }
      }

      if (noFallEnabled) {
         if (packet instanceof ServerboundMovePlayerPacket) {
            ServerboundMovePlayerPacket oldPacket = (ServerboundMovePlayerPacket)packet;
            if (mc.f_91074_ != null && !mc.f_91074_.m_21255_() && mc.f_91074_.m_20184_().f_82480_ < -0.5) {
               double x = oldPacket.m_179683_() ? oldPacket.m_134129_(mc.f_91074_.m_20185_()) : mc.f_91074_.m_20185_();
               double y = oldPacket.m_179683_() ? oldPacket.m_134140_(mc.f_91074_.m_20186_()) : mc.f_91074_.m_20186_();
               double z = oldPacket.m_179683_() ? oldPacket.m_134146_(mc.f_91074_.m_20189_()) : mc.f_91074_.m_20189_();
               float yaw = oldPacket.m_179684_() ? oldPacket.m_134131_(mc.f_91074_.m_146908_()) : mc.f_91074_.m_146908_();
               float pitch = oldPacket.m_179684_() ? oldPacket.m_134142_(mc.f_91074_.m_146909_()) : mc.f_91074_.m_146909_();
               Object newPacket;
               if (oldPacket.m_179683_() && oldPacket.m_179684_()) {
                  newPacket = new ServerboundMovePlayerPacket.PosRot(x, y, z, yaw, pitch, true);
               } else if (oldPacket.m_179683_()) {
                  newPacket = new ServerboundMovePlayerPacket.Pos(x, y, z, true);
               } else if (oldPacket.m_179684_()) {
                  newPacket = new ServerboundMovePlayerPacket.Rot(yaw, pitch, true);
               } else {
                  newPacket = new ServerboundMovePlayerPacket.StatusOnly(true);
               }

               ((Connection)this).m_243124_((Packet)newPacket, (PacketSendListener)null);
               ci.cancel();
            }

         }
      }
   }
}
