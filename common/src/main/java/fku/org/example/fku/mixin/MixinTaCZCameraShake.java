package fku.org.example.fku.mixin; /* water */

import com.tacz.guns.api.client.event.BeforeRenderHandEvent;
import com.tacz.guns.client.event.CameraSetupEvent;
import fku.org.example.fku.features.tacz.TaCZConfig;
import net.minecraftforge.client.event.ViewportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MixinTaCZCameraShake — 防抖功能（关闭相机后坐力动画）
 * 参考自 NoSpread 02 的 MixinCameraSetupEvent
 * 当 TaCZConfig.antiShakeEnabled 开启时，取消相机后坐力抖动和动画
 * 该 Mixin 由赛博教员实现
 */
@Mixin(value = CameraSetupEvent.class, remap = false)
public class MixinTaCZCameraShake {

    @Inject(method = "applyCameraRecoil", at = @At("HEAD"), cancellable = true)
    private static void onApplyCameraRecoil(ViewportEvent.ComputeCameraAngles event, CallbackInfo ci) {
        if (TaCZConfig.getInstance().antiShakeEnabled) {
            ci.cancel();
        }
    }

    @Inject(method = "applyLevelCameraAnimation", at = @At("HEAD"), cancellable = true)
    private static void onApplyLevelCameraAnimation(ViewportEvent.ComputeCameraAngles event, CallbackInfo ci) {
        if (TaCZConfig.getInstance().antiShakeEnabled) {
            ci.cancel();
        }
    }

    @Inject(method = "applyItemInHandCameraAnimation", at = @At("HEAD"), cancellable = true)
    private static void onApplyItemInHandCameraAnimation(BeforeRenderHandEvent event, CallbackInfo ci) {
        if (TaCZConfig.getInstance().antiShakeEnabled) {
            ci.cancel();
        }
    }
}