package fku.org.example.fku.mixin;

import fku.org.example.fku.features.arrowdmg.ArrowDmgFeature;
import fku.org.example.fku.features.killicon.KillIconFeature;
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
@Mixin(value={MultiPlayerGameMode.class})
public abstract class MixinMultiPlayerGameMode {
    /*
     * Unable to fully structure code
     */
    @Inject(method={"attack"}, at={@At(value="HEAD")})
    public void onAttackHead(Player player, Entity target, CallbackInfo ci) {
        config = KnockbackConfig.getInstance();
        if (config.enabled && target instanceof LivingEntity) {
            livingTarget = (LivingEntity)target;
            targetYaw = KnockbackDirectionCalculator.calculateYaw((LivingEntity)player, livingTarget, config.mode);
            FakeRotationManager.setPending(livingTarget, targetYaw);
        }
        if (!(target instanceof LivingEntity)) ** GOTO lbl-1000
        lt = (LivingEntity)target;
        if (player.m_20188_() >= lt.getY() + lt.getBbHeight() * 0.85) {
            v0 = true;
        } else lbl-1000:
        // 2 sources

        {
            v0 = false;
        }
        KillIconFeature.markHeadshot(v0);
    }

    @Inject(method={"releaseUsingItem"}, at={@At(value="HEAD")}, cancellable=true)
    public void onReleaseUsingItem(CallbackInfo ci) {
        if (ArrowDmgFeature.handleManualRelease()) {
            ci.cancel();
        }
    }
}

