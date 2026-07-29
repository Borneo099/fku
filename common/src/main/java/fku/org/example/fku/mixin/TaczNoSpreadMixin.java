package fku.org.example.fku.mixin; /* water */

import com.tacz.guns.entity.shooter.ShooterDataHolder;
import fku.org.example.fku.features.tacz.TaCZConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

/**
 * 无扩散 — 子弹发射时强制将扩散值设为 0
 * 参考自 Lexis TaczNoSpreadMixin
 * 该 Mixin 由赛博教员实现
 */
@Mixin(targets = {"com.tacz.guns.api.item.gun.AbstractGunItem"}, remap = false)
public class TaczNoSpreadMixin {

    private static Method shoot6Method;

    @Inject(method = {"doBulletSpread"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void onDoBulletSpread(ShooterDataHolder dataHolder, ItemStack stack,
                                   LivingEntity shooter, Projectile projectile,
                                   int seed, float velocity, float inaccuracy,
                                   float pitch, float yaw, CallbackInfo ci) {
        TaCZConfig cfg = TaCZConfig.getInstance();
        if (!cfg.masterEnabled || !cfg.noSpreadEnabled) return;
        if (shooter == null || projectile == null) return;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            if (!shooter.equals(mc.player)) return;

            if (shoot6Method == null) {
                shoot6Method = Projectile.class.getMethod("shoot",
                        net.minecraft.world.entity.Entity.class,
                        float.class, float.class, float.class,
                        float.class, float.class, float.class);
                shoot6Method.setAccessible(true);
            }
            // 以 0 扩散发射子弹
            shoot6Method.invoke(projectile, shooter, pitch, yaw, 0.0f, velocity, 0.0f);
            ci.cancel();
        } catch (Exception ignored) {}
    }
}