package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.knockback.FakeRotationManager;
import fku.org.example.fku.features.quickswitch.QuickSwitchFeature;
import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;

/**
 * MixinConnectionAttackInterceptor — 在 Connection.send() 层拦截攻击包
 *
 * ★ 职责：
 *   攻击包发出前，通过 netty channel 发送假旋转包和秒切换包。
 *
 * ★ v6 变更（状态机版）：
 *   - 秒切调用 QuickSwitchFeature.onAttackPacket(channel) 状态机入口
 *   - 不再有 RETURN 注入（状态机由 ClientTick 驱动）
 *
 * ★ 设计思想：
 *   channel.writeAndFlush 确保切换包在攻击包之前写入 netty 管道。
 *   攻击包由 Connection.send() 正常发送。
 *   切回包由状态机在延迟后通过 connection.send() 发送。
 */
@OnlyIn(Dist.CLIENT)
@Mixin(Connection.class)
public abstract class MixinConnectionAttackInterceptor {

    @Shadow
    private Channel channel;

    @Unique
    private static boolean fku$sendingPending = false;

    /**
     * HEAD 注入：攻击包发出前，写入假旋转/秒切换包
     *
     * ★ 冗余发送优化（抗网络延迟）：
     *   假旋转 PosRot 包发送 2~3 份（相同角度），即使某份因网络抖动延迟，
     *   备份份仍能覆盖攻击包到达的时间窗口，提高击退方向控制成功率。
     */
    @Inject(
            method = "send(Lnet/minecraft/network/protocol/Packet;)V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void fku$onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (fku$sendingPending) return;
        if (!(packet instanceof ServerboundInteractPacket)) return;

        boolean hasRotation = FakeRotationManager.hasPending();
        // ★ 状态机：仅 IDLE 状态下才走秒切
        boolean hasQuickSwitch = QuickSwitchFeature.isIdle() && QuickSwitchFeature.isEnabled();

        if (!hasRotation && !hasQuickSwitch) return;

        fku$sendingPending = true;
        try {
            Channel ch = this.channel;
            if (ch != null && ch.isOpen()) {
                if (hasRotation) {
                    // ★ 冗余发送：2~3 份相同角度的假旋转包
                    int burstCount = 2 + ThreadLocalRandom.current().nextInt(2); // 2 或 3
                    for (int i = 0; i < burstCount; i++) {
                        ch.writeAndFlush(FakeRotationManager.createPendingPacket());
                    }
                    FakeRotationManager.clearPending();
                }
                // ★ 状态机入口：channel 传入，在内部发切换包
                if (hasQuickSwitch) {
                    QuickSwitchFeature.onAttackPacket(ch);
                }
            }
        } catch (Exception ignored) {
        } finally {
            fku$sendingPending = false;
        }
    }
}
