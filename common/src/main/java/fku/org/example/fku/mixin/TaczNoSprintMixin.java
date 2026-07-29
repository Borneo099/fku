package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.tacz.TaCZConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 疾跑不断 — 枪械开火时不中断疾跑状态
 * 参考自 Lexis TaczNoSprintMixin
 * 该 Mixin 由赛博教员实现
 */
@Mixin(targets = {"com.tacz.guns.client.gameplay.LocalPlayerSprint"}, remap = false)
public class TaczNoSprintMixin {

    @Inject(method = {"getProcessedSprintStatus"}, at = @At("HEAD"),
            cancellable = true, remap = false)
    private void onGetProcessedSprintStatus(boolean sprint, CallbackInfoReturnable<Boolean> cir) {
        TaCZConfig cfg = TaCZConfig.getInstance();
        if (!cfg.masterEnabled || !cfg.noSprintInterruptEnabled) return;
        cir.setReturnValue(sprint);
    }
}