package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.knockback.FakeRotationManager;
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

/**
 * MixinConnectionAttackInterceptor — 在 Connection.send() 层拦截攻击包
 *
 * ★ 职责：
 *   当 ServerboundInteractPacket（攻击包）即将发送到网络时，
 *   通过 netty channel 直接发送 pending 的假旋转+假位置包，
 *   确保 PosRot 包在攻击包之前到达服务端。
 *
 * ★ 设计思想（抓主要矛盾：时序精度）：
 *   旧方案（MixinMultiPlayerGameMode at HEAD）虽然先发 PosRot 再 attack()，
 *   但在跨服场景下，两个包可能因网络排队/反代重新排序而乱序。
 *
 *   新方案在 Connection.send() 的 HEAD 注入，这是包离开客户端前的最后一道关卡。
 *   通过 channel.writeAndFlush() 直接将 PosRot 写入 netty 管道，
 *   确保物理顺序上 PosRot 紧贴攻击包前发出，显著提高跨服可靠性。
 *
 * ★ 注意（否定之否定）：
 *   不能使用 connection.send() 发送 PosRot 包（会触发本 Mixin 递归），
 *   必须通过 @Shadow channel 直接 writeAndFlush。
 *
 * ★ 参考：
 *   Connection 类的 send() 方法源码
 *   net.minecraft.network.Connection#send(Packet)
 */
@OnlyIn(Dist.CLIENT)
@Mixin(Connection.class)
public abstract class MixinConnectionAttackInterceptor {

    @Shadow
    private Channel channel;

    /**
     * 内部递归保护锁
     * 防止 writeAndFlush(pendingPosRot) → channel.write → pipeline → send() → 再次触发本 Mixin
     */
    @Unique
    private static boolean fku$sendingPending = false;

    /**
     * 在 Connection.send(Packet) HEAD 注入
     *
     * 检测到攻击包 + 有 pending 假旋转 → 通过 channel 直发假旋转
     */
    @Inject(
            method = "send(Lnet/minecraft/network/protocol/Packet;)V",
            at = @At(value = "HEAD")
    )
    private void fku$onSendPacket(Packet<?> packet, CallbackInfo ci) {
        // ★ 递归保护
        if (fku$sendingPending) return;
        if (!FakeRotationManager.hasPending()) return;
        // ★ 只拦截 ServerboundInteractPacket（攻击/交互包），其他包放行
        if (!(packet instanceof ServerboundInteractPacket)) return;

        // ★ 通过 netty channel 直接发送 pending 的假旋转包（绕过本 Mixin 防递归）
        fku$sendingPending = true;
        try {
            Channel ch = this.channel;
            if (ch != null && ch.isOpen()) {
                ch.writeAndFlush(FakeRotationManager.createPendingPacket());
            }
        } catch (Exception ignored) {
            // channel 异常不影响攻击包正常发送
        } finally {
            FakeRotationManager.clearPending();
            fku$sendingPending = false;
        }
    }
}