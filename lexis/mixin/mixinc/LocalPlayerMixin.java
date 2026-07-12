package lexis.mixin.mixinc;

import lexis.Hack.Hacks.Movement.NoMomentumHack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LocalPlayer.class})
public class LocalPlayerMixin {
   @Inject(
      method = {"tick"},
      at = {@At("TAIL")}
   )
   private void onTick(CallbackInfo ci) {
      if (NoMomentumHack.isEnabled) {
         LocalPlayer player = (LocalPlayer)this;
         if (!NoMomentumHack.onlyOnGround || player.m_20096_()) {
            boolean hasInput = player.f_20900_ != 0.0F || player.f_20902_ != 0.0F;
            if (!NoMomentumHack.onlyWhenNoInput || !hasInput) {
               Vec3 vel = player.m_20184_();
               double factor = 1.0 - NoMomentumHack.deceleration;
               player.m_20334_(vel.f_82479_ * factor, vel.f_82480_, vel.f_82481_ * factor);
            }
         }
      }
   }
}
