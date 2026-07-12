package lexis.mixin.mixinc;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Movement.SpringJumpHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LocalPlayer.class})
public class SpringJumpMixin {
   private boolean wasOnGround = false;
   private int cooldown = 0;

   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   private void onTick(CallbackInfo ci) {
      LocalPlayer player = (LocalPlayer)this;
      if (player != null) {
         if (this.cooldown > 0) {
            --this.cooldown;
         }

         SpringJumpHack springJump = null;
         Iterator var4 = HackManager.getInstance().getHacks().iterator();

         while(var4.hasNext()) {
            Hack hack = (Hack)var4.next();
            if (hack instanceof SpringJumpHack && hack.isEnabled()) {
               springJump = (SpringJumpHack)hack;
               break;
            }
         }

         if (springJump == null) {
            this.wasOnGround = player.m_20096_();
         } else {
            boolean onGround = player.m_20096_();
            if (onGround && !this.wasOnGround && this.cooldown == 0 && !player.m_20069_()) {
               double height = springJump.getJumpHeight();
               double velocityY = Math.sqrt(0.16 * height);
               player.m_20334_(player.m_20184_().f_82479_, velocityY, player.m_20184_().f_82481_);
               player.m_6853_(false);
               this.cooldown = springJump.getCooldownTicks();
            }

            this.wasOnGround = onGround;
         }
      }
   }
}
