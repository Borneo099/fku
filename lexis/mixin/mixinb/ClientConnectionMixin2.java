package lexis.mixin.mixinb;

import io.netty.channel.ChannelHandlerContext;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Misc.AntiPacketKickHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Hackutil.NotificationManager;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Connection.class})
public class ClientConnectionMixin2 {
   private long lastNotificationTime = 0L;
   private long lastResetTime = System.currentTimeMillis();
   private static final long NOTIFICATION_COOLDOWN = 3000L;

   @Inject(
      method = {"send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendPacket(Packet packet, PacketSendListener listener, CallbackInfo ci) {
      Iterator var4 = HackManager.getInstance().getHacks().iterator();

      while(var4.hasNext()) {
         Hack hack = (Hack)var4.next();
         if (hack instanceof AntiPacketKickHack apk && hack.isEnabled()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.lastResetTime > (long)AntiPacketKickHack.currentResetTime) {
               AntiPacketKickHack.packetCount = 0;
               AntiPacketKickHack.isLimited = false;
               this.lastResetTime = currentTime;
            }

            ++AntiPacketKickHack.packetCount;
            if (AntiPacketKickHack.isLimitEnabled && AntiPacketKickHack.packetCount > AntiPacketKickHack.currentLimit) {
               if (!AntiPacketKickHack.isLimited) {
                  if (currentTime - this.lastNotificationTime > 3000L) {
                     NotificationManager.warning("反数据包踢出", "§c发包过多！已经拦截！ I 当前: " + AntiPacketKickHack.packetCount + " 包/" + (double)AntiPacketKickHack.currentResetTime / 1000.0 + "秒", 6);
                     if (apk.logExceptions()) {
                        System.out.println("[反数据包踢出] 发包过多，已限制: " + AntiPacketKickHack.packetCount + " 包/" + (double)AntiPacketKickHack.currentResetTime / 1000.0 + "秒");
                     }

                     this.lastNotificationTime = currentTime;
                  }

                  AntiPacketKickHack.isLimited = true;
               }

               ci.cancel();
               return;
            }
            break;
         }
      }

   }

   @Inject(
      method = {"exceptionCaught"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onExceptionCaught(ChannelHandlerContext context, Throwable throwable, CallbackInfo ci) {
      Iterator var4 = HackManager.getInstance().getHacks().iterator();

      while(var4.hasNext()) {
         Hack hack = (Hack)var4.next();
         if (hack instanceof AntiPacketKickHack apk && hack.isEnabled()) {
            if (apk.catchExceptions()) {
               long currentTime = System.currentTimeMillis();
               if (currentTime - this.lastNotificationTime > 3000L) {
                  String errorMsg = throwable.getMessage();
                  if (errorMsg != null && errorMsg.length() > 50) {
                     errorMsg = errorMsg.substring(0, 47) + "...";
                  }

                  NotificationManager.warning("反数据包踢出", "§c检测到异常！\n§7" + (errorMsg != null ? errorMsg : "连接异常"), 3);
                  if (apk.logExceptions()) {
                     System.out.println("[反数据包踢出] Caught exception: " + String.valueOf(throwable));
                     throwable.printStackTrace();
                  }

                  this.lastNotificationTime = currentTime;
               }

               ci.cancel();
               return;
            }
         }
      }

   }
}
