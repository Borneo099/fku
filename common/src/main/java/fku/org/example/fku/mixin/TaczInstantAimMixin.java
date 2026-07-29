package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.tacz.TaCZConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

/**
 * 瞬镜 — 跳过开镜动画，瞬间完成瞄准
 * 参考自 Lexis TaczInstantAimMixin
 * 该 Mixin 由赛博教员实现
 */
@Mixin(targets = {"com.tacz.guns.client.gameplay.LocalPlayerAim"}, remap = false)
public class TaczInstantAimMixin {

    private static Field dataField;
    private static Field clientIsAimingField;
    private static Field clientAimingProgressField;
    private static Field oldAimingProgressField;

    static {
        try {
            Class<?> aimClass = Class.forName("com.tacz.guns.client.gameplay.LocalPlayerAim");
            dataField = aimClass.getDeclaredField("data");
            dataField.setAccessible(true);
            Class<?> dataHolderClass = Class.forName("com.tacz.guns.client.gameplay.LocalPlayerDataHolder");
            clientIsAimingField = dataHolderClass.getDeclaredField("clientIsAiming");
            clientIsAimingField.setAccessible(true);
            clientAimingProgressField = dataHolderClass.getDeclaredField("clientAimingProgress");
            clientAimingProgressField.setAccessible(true);
            oldAimingProgressField = dataHolderClass.getDeclaredField("oldAimingProgress");
            oldAimingProgressField.setAccessible(true);
        } catch (Exception ignored) {}
    }

    @Inject(method = {"tickAimingProgress"}, at = @At("TAIL"), remap = false)
    private void onTickAimingProgress(CallbackInfo ci) {
        TaCZConfig cfg = TaCZConfig.getInstance();
        if (!cfg.masterEnabled || !cfg.instantAimEnabled) return;
        try {
            Object data = dataField.get(this);
            if (data == null) return;
            if ((Boolean) clientIsAimingField.get(data)) {
                clientAimingProgressField.setFloat(data, 1.0f);
                oldAimingProgressField.setFloat(data, 1.0f);
            }
        } catch (Exception ignored) {}
    }
}