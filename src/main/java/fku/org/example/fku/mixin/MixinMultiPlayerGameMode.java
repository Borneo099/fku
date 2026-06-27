package fku.org.example.fku.mixin; /* water */

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

/**
 * MixinMultiPlayerGameMode — 拦截客户端攻击调用
 *
 * ★ 职责：
 *   在 MultiPlayerGameMode#attack() 执行前注入假旋转包，
 *   使服务端在处理攻击时使用修改后的 Yaw 计算击退方向。
 *
 * ★ 设计思想（抓主要矛盾：时序精度）：
 *   核心矛盾是"旋转包必须在攻击包之前到达服务端"。
 *   在 attack() 的 HEAD 注入确保：
 *     1. 假旋转包先发送
 *     2. 原始 attack() 紧接着发送攻击包（ServerboundInteractPacket）
 *     3. 服务端按接收顺序处理：先更新 Yaw，再处理攻击 → 击退使用新 Yaw
 *
 * ★ 兼容性：
 *   无论攻击来自玩家手动操作还是杀戮光环模组，最终都会经过
 *   MultiPlayerGameMode#attack()，因此本 Mixin 对所有攻击方式生效。
 *
 * ★ 参考：
 *   Litematica Printer 的 Fake Rotation System 设计思路
 */
@OnlyIn(Dist.CLIENT)
@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode {

    @Inject(
            method = "attack",
            at = @At(value = "HEAD")
    )
    public void onAttackHead(Player player, Entity target, CallbackInfo ci) {
        KnockbackConfig config = KnockbackConfig.getInstance();
        if (!config.enabled) return;
        if (!(target instanceof LivingEntity livingTarget)) return;

        // ★ 计算目标旋转角度
        float targetYaw = KnockbackDirectionCalculator.calculateYaw(player, livingTarget, config.mode);

        // ★ 设置待发送状态（不立即发送，由 MixinConnectionAttackInterceptor 在攻击包发出前通过 channel 直发）
        //   旧方案在 attack() HEAD 直接发 PosRot，但跨服时攻击包可能先到服务端。
        //   新方案推迟到 Connection.send() 层拦截攻击包，确保 PosRot 紧贴攻击包发出。
        FakeRotationManager.setPending(livingTarget, targetYaw);
    }
}