package lexis.mixin.tacz;

import lexis.Hack.Hacks.TaCZ.NoRecoilHack;
import lexis.Hack.Hacks.TaCZ.NoRecoilState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.ViewportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   targets = {"com.tacz.guns.client.event.CameraSetupEvent"},
   remap = false
)
public class TaczNoRecoilMixin {
   @Inject(
      method = {"applyCameraRecoil"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private static void onApplyCameraRecoil(ViewportEvent.ComputeCameraAngles event, CallbackInfo ci) {
      if (NoRecoilHack.noRecoilActive) {
         float reduction = NoRecoilHack.getRecoilReduction();
         if (reduction >= 1.0F) {
            ci.cancel();
         } else {
            LocalPlayer player = Minecraft.m_91087_().f_91074_;
            if (player != null) {
               NoRecoilState.pitchBefore = (double)player.m_146909_();
               NoRecoilState.yawBefore = (double)player.m_146908_();
            }

         }
      }
   }

   @Inject(
      method = {"applyCameraRecoil"},
      at = {@At("RETURN")},
      remap = false
   )
   private static void onApplyCameraRecoilReturn(ViewportEvent.ComputeCameraAngles event, CallbackInfo ci) {
      if (NoRecoilHack.noRecoilActive) {
         float reduction = NoRecoilHack.getRecoilReduction();
         if (!(reduction >= 1.0F) && !(reduction <= 0.0F)) {
            LocalPlayer player = Minecraft.m_91087_().f_91074_;
            if (player != null) {
               float scale = 1.0F - reduction;
               float pitchAfter = player.m_146909_();
               float pitchDelta = pitchAfter - (float)NoRecoilState.pitchBefore;
               player.m_146926_((float)NoRecoilState.pitchBefore + pitchDelta * scale);
               float yawAfter = player.m_146908_();
               float yawDelta = yawAfter - (float)NoRecoilState.yawBefore;
               player.m_146922_((float)NoRecoilState.yawBefore + yawDelta * scale);
            }
         }
      }
   }
}
