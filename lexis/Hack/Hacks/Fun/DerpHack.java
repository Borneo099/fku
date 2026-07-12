package lexis.Hack.Hacks.Fun;

import java.util.Iterator;
import java.util.Random;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Combat.KillauraHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class DerpHack extends Hack {
   private final Random random = new Random();
   private HackConfig config = HackConfig.getInstance();

   public DerpHack() {
      super("乱舞旋转", "随机旋转头，第三人称可以看到自己旋转", Hack.Category.FUN, true);
   }

   public void onEnable() {
   }

   public void onDisable() {
      if (!this.isAnyHeadRotationActive()) {
         HeadOnlyLook.stopRotation();
      }

   }

   private boolean isAnyHeadRotationActive() {
      Iterator var1 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         do {
            do {
               if (!var1.hasNext()) {
                  return false;
               }

               hack = (Hack)var1.next();
            } while(!hack.isEnabled());
         } while(hack == this);
      } while(!(hack instanceof KillauraHack) && !(hack instanceof StareAtPlayerHack));

      return true;
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.m_91403_() != null) {
         float yaw = mc.f_91074_.m_146908_() + this.random.nextFloat() * 360.0F - 180.0F;
         float pitch = this.random.nextFloat() * 180.0F - 90.0F;
         mc.m_91403_().m_104955_(new ServerboundMovePlayerPacket.Rot(yaw, pitch, mc.f_91074_.m_20096_()));
         if (!HeadOnlyLook.isRotating()) {
            HeadOnlyLook.startRotation(yaw, pitch);
         } else {
            HeadOnlyLook.updateRotation(yaw, pitch);
         }

      }
   }

   public void onClick() {
      this.toggle();
   }
}
