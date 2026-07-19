package fku.org.example.fku.mixin; /* water */

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 绕过 OpMod 反作弊的模组检测器。
 *
 * @Pseudo + targets = 仅当目标类存在时生效，不存在则静默跳过。
 * 拦截 CheatModDetector.runCheck() 使其不执行任何检测。
 */
@Pseudo
@Mixin(targets = "lbxrman.mymod.opmod.features.anticheat.CheatModDetector", remap = false)
public class MixinCheatModDetector {

    @Inject(method = "runCheck", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onRunCheck(CallbackInfo ci) {
        ci.cancel();
    }
}
