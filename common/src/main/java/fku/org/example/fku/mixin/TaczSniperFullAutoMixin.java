package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.tacz.TaCZConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

/**
 * 全狙自动 — 强制自动射击，忽略射击冷却
 * 参考自 Lexis TaczSniperFullAutoMixin
 * 将 ShootKey.lastTimeShootSuccess 设为 false，使系统允许继续射击
 * 该 Mixin 由赛博教员实现
 */
@Mixin(targets = {"com.tacz.guns.client.input.ShootKey"}, remap = false)
public class TaczSniperFullAutoMixin {

    private static Field lastTimeShootSuccessField;

    static {
        try {
            lastTimeShootSuccessField = Class.forName("com.tacz.guns.client.input.ShootKey")
                    .getDeclaredField("lastTimeShootSuccess");
            lastTimeShootSuccessField.setAccessible(true);
        } catch (Exception ignored) {}
    }

    @Inject(method = {"autoShoot"}, at = @At("HEAD"), remap = false)
    private static void onAutoShootHead(CallbackInfo ci) {
        TaCZConfig cfg = TaCZConfig.getInstance();
        if (!cfg.masterEnabled) return;
        if (!cfg.sniperFullAutoEnabled && !cfg.fullAutoEnabled) return;
        if (lastTimeShootSuccessField != null) {
            try {
                lastTimeShootSuccessField.setBoolean(null, false);
            } catch (Exception ignored) {}
        }
    }
}