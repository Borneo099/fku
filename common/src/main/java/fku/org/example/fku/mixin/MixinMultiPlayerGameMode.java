package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.arrowdmg.ArrowDmgFeature;
import fku.org.example.fku.features.autoattack.AutoAttackFeature;
import fku.org.example.fku.features.knockback.FakeRotationManager;
import fku.org.example.fku.features.knockback.KnockbackConfig;
import fku.org.example.fku.features.knockback.KnockbackDirectionCalculator;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
        // ★ 击杀图标：标记是否为爆头攻击（视线在目标头部）
        fku.org.example.fku.features.killicon.KillIconFeature.markHeadshot(
            target instanceof LivingEntity lt && player.getEyeY() >= lt.getY() + lt.getBbHeight() * 0.85);
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

    /**
     * ★ 自动攻击优化：长按左键且有已选目标时，禁止破坏方块。
     *   否则会顺带挖方块 → 自动切换工具/武器功能乱切、且武器攻击冷却被持续挖方块占用回不满。
     *   没有选中目标时放行，原版挖掘照常（见 AutoAttackFeature#isAttackingWithTarget）。
     */
    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void fkuBlockBreakCancelStart(BlockPos pPos, Direction pDirection, CallbackInfoReturnable<Boolean> cir) {
        if (AutoAttackFeature.isAttackingWithTarget()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void fkuBlockBreakCancelContinue(BlockPos pPos, Direction pDirection, CallbackInfoReturnable<Boolean> cir) {
        if (AutoAttackFeature.isAttackingWithTarget()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}