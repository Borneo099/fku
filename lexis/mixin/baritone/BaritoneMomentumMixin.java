package lexis.mixin.baritone;

import lexis.Hack.Hacks.Baritone.BaritoneSpeedHack;
import lexis.Hack.Utils.BaritoneBridge;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LocalPlayer.class})
public class BaritoneMomentumMixin {
   @Inject(
      method = {"tick"},
      at = {@At("TAIL")}
   )
   private void onTick(CallbackInfo ci) {
      if (BaritoneSpeedHack.enabled) {
         if (BaritoneBridge.isActive()) {
            LocalPlayer player = (LocalPlayer)this;
            if (!BaritoneSpeedHack.groundOnly || player.m_20096_()) {
               double factor = 1.0 / BaritoneSpeedHack.speedMultiplier;
               Vec3 vel = player.m_20184_();
               player.m_20334_(vel.f_82479_ * factor, vel.f_82480_, vel.f_82481_ * factor);
            }
         }
      }
   }
}
