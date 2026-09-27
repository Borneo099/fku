package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.criticals.CriticalsFeature;
import fku.org.example.fku.features.knockback.FakeRotationManager;
import fku.org.example.fku.features.quickswitch.QuickSwitchFeature;
import fku.org.example.fku.features.spear.SpearChargeFeature;
import fku.org.example.fku.util.PacketAttackDetector;
import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;

/**
 * MixinConnectionAttackInterceptor — 在 Connection.send() 层拦截攻击包与 Blink 移动包
 *
 * ★ 职责：
 *   1. 攻击包发出前，通过 netty channel 发送假旋转包和秒切换包。
 *   2. Blink 模式蓄力矛时，拦截并缓存玩家自身发出的 ServerboundMovePlayerPacket，
 *      松手时由 SpearKillFeature 一次性 flush，制造"瞬移突刺"。
 *
 * ★ 设计思想：
 *   channel.writeAndFlush 确保切换包在攻击包之前写入 netty 管道。
 *   攻击包由 Connection.send() 正常发送。
 *   Blink 包由 SpearKillFeature.onClientMoveSent() 缓存并 ci.cancel() 取消发送。
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
     */
    @Inject(
            method = "send(Lnet/minecraft/network/protocol/Packet;)V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void fku$onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (fku$sendingPending) return;

        if (!(packet instanceof ServerboundInteractPacket)) return;

        // ★ 区分攻击与右键交互：仅攻击类型才触发秒切和暴击，右键使用物品不触发
        boolean isAttack = PacketAttackDetector.isAttack((ServerboundInteractPacket) packet);

        boolean hasRotation = FakeRotationManager.hasPending();
        // ★ 状态机：仅 IDLE 状态下且为攻击包时才走秒切
        boolean hasQuickSwitch = isAttack && QuickSwitchFeature.isIdle() && QuickSwitchFeature.isEnabled();
        boolean hasCriticals = isAttack && CriticalsFeature.isEnabled();

        if (!hasRotation && !hasQuickSwitch && !hasCriticals) return;

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
                // ★ 刀刀暴击：攻击包前发假离地移动包
                if (hasCriticals) {
                    CriticalsFeature.onAttackPacket(ch);
                }
            }
        } catch (Exception ignored) {
        } finally {
            fku$sendingPending = false;
        }
    }

    /**
     * 矛之冲锋：发包前把 Pos/PosRot 包的位置叠加 boostOffset（服务端侧位移突进，参考 Meteor SpearPacketMixin）。
     * ★ x/z 为 final 字段，直接用 Accessor 写会抛 IllegalAccessError，故重建等价包。
     * 与 NoFallPacketMixin 的 @ModifyVariable 叠加在同一 send 入参上，二者互相组合互不冲突。
     */
    @ModifyVariable(
        method = "send(Lnet/minecraft/network/protocol/Packet;)V",
        at = @At(value = "HEAD"),
        index = 1,
        argsOnly = true
    )
    private Packet<?> fku$modifySpearPacket(Packet<?> packet) {
        Vec3 offset = SpearChargeFeature.getBoostOffset();
        if (offset == null) return packet;
        if (packet instanceof ServerboundMovePlayerPacket.Pos p) {
            return new ServerboundMovePlayerPacket.Pos(p.getX(0.0) + offset.x, p.getY(0.0), p.getZ(0.0) + offset.z, p.isOnGround());
        } else if (packet instanceof ServerboundMovePlayerPacket.PosRot pr) {
            return new ServerboundMovePlayerPacket.PosRot(pr.getX(0.0) + offset.x, pr.getY(0.0), pr.getZ(0.0) + offset.z, pr.getYRot(0.0f), pr.getXRot(0.0f), pr.isOnGround());
        }
        return packet;
    }
}
