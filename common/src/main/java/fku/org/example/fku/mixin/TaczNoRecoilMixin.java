package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.tacz.TaCZConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.ViewportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 无后座 — 完全消除相机后坐力效果
 * 参考自 Lexis TaczNoRecoilMixin
 * 该 Mixin 由赛博教员实现
 */
@Mixin(targets = {"com.tacz.guns.client.event.CameraSetupEvent"}, remap = false)
public class TaczNoRecoilMixin {

    private static float pitchBefore;
    private static float yawBefore;

    @Inject(method = {"applyCameraRecoil"}, at = @At("HEAD"), cancellable = true, remap = false)
    private static void onApplyCameraRecoilHead(ViewportEvent.ComputeCameraAngles event, CallbackInfo ci) {
        TaCZConfig cfg = TaCZConfig.getInstance();
        if (!cfg.masterEnabled || !cfg.noRecoilEnabled) return;
        float reduction = cfg.recoilReduction;
        if (reduction >= 1.0f) {
            ci.cancel();
        } else if (reduction > 0.0f) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                pitchBefore = player.getXRot();
                yawBefore = player.getYRot();
            }
        }
    }

    @Inject(method = {"applyCameraRecoil"}, at = @At("RETURN"), remap = false)
    private static void onApplyCameraRecoilReturn(ViewportEvent.ComputeCameraAngles event, CallbackInfo ci) {
        TaCZConfig cfg = TaCZConfig.getInstance();
        if (!cfg.masterEnabled || !cfg.noRecoilEnabled) return;
        float reduction = cfg.recoilReduction;
        if (reduction <= 0.0f || reduction >= 1.0f) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        float scale = 1.0f - reduction;
        float pitchAfter = player.getXRot();
        float pitchDelta = pitchAfter - pitchBefore;
        player.setXRot(pitchBefore + pitchDelta * scale);

        float yawAfter = player.getYRot();
        float yawDelta = yawAfter - yawBefore;
        player.setYRot(yawBefore + yawDelta * scale);
    }
}