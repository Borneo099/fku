package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.antilag.AntiLagFeature;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MixinClientPacketListener — 拦截服务端位置同步包处理入口
 *
 * ★ 职责：
 *   在 ClientPacketListener#handleMovePlayer HEAD 注入，
 *   当服务端强制拉回玩家位置时，通过 AntiLagFeature 决定是否拦截并生成假位置包。
 *
 * ★ 设计思想（抓主要矛盾——拦截时机）：
 *   PacketEvent.Receive 可能在处理之后才通知，无法阻止位置被修改。
 *   handleMovePlayer() 是服务端拉回包处理的最后关卡，注入 HEAD + cancel
 *   可在位置被应用之前完全阻止拉回。
 *
 * ★ 踩坑笔记：
 *   - 1.20.1 Mojang官方映射中该方法名是 handleMovePlayer，而非 handlePlayerPosition
 *     （SRG: handleMovePlayer(LClientboundPlayerPositionPacket;)V → m_5682_）
 *   - 写错方法名 → 编译期 refmap 无条目 → 运行时 MixinTransformerError 崩溃
 *
 * ★ 参考：
 *   AntiLag.java (Meteor Client) onPacketReceive → 改用 Mixin 确保拦截准确
 *
 * ★ 关于 QuickSwitch：
 *   v4 架构变更（2026-06-28）：移除了 send(Packet) 的 HEAD/RETURN 注入。
 *   根因：send(Packet) 方法定义在父类 ClientCommonPacketListenerImpl 中，
 *   @Mixin(ClientPacketListener.class) 对继承方法的注入静默失效，
 *   导致秒切逻辑从未执行。现改为由 TpAura 直接调用 QuickSwitchFeature 的
 *   beforeAttack() / afterAttack()，时序可控，100% 可靠。
 */
@OnlyIn(Dist.CLIENT)
@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

    /**
     * 在 handleMovePlayer HEAD 注入，由 AntiLagFeature 处理拦截逻辑
     */
    @Inject(
            method = "handleMovePlayer",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fku$onHandleMovePlayer(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        AntiLagFeature.onPlayerPositionPacket(packet, ci);
    }
}