package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.arrowdmg.ArrowDmgFeature;
import fku.org.example.fku.features.knockback.FakeRotationManager;
import fku.org.example.fku.features.knockback.KnockbackConfig;
import fku.org.example.fku.features.knockback.KnockbackDirectionCalculator;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode {

    @Inject(method = "attack", at = @At("HEAD"))
    public void onAttackHead(Player player, Entity target, CallbackInfo ci) {
        KnockbackConfig config = KnockbackConfig.getInstance();
        if (config.enabled && target instanceof LivingEntity livingTarget) {
            float targetYaw = KnockbackDirectionCalculator.calculateYaw(player, livingTarget, config.mode);
            FakeRotationManager.setPending(livingTarget, targetYaw);
        }
    }

    /**
     * ★ ArrowDmg 手动释放（连射关闭时）：拦截原包 → VClip + 瞄准 + RELEASE
     *   连射开启时由 ClientTick 处理，此处不拦截
     */
    @Inject(method = "releaseUsingItem", at = @At("HEAD"), cancellable = true)
    public void onReleaseUsingItem(CallbackInfo ci) {
        if (ArrowDmgFeature.handleManualRelease()) {
            ci.cancel();
        }
    }
}