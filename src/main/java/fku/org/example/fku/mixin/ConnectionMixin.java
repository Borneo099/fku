package fku.org.example.fku.mixin;

import fku.org.example.fku.config.FkuConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.TimeUnit;

/**
 * ConnectionMixin — 根治连接超时断开
 *
 * ★ 核心策略（四层防护）：
 *   1) channelActive → 替换 ReadTimeoutHandler(30秒) 为 ReadTimeoutHandler(3600秒)
 *      防止 Netty 因30秒无数据收到而强行关闭通道（治本）。
 *   2) exceptionCaught → 阻止 ReadTimeoutException 传播（备胎）。
 *   3) channelInactive → 阻止断线弹窗（最后防线）。
 *   4) disconnect → 阻止 Connection.disconnect 的传播，避免被踢回主菜单。
 *
 *   配合 KeepAliveHandler（每0.25秒发送位置/旋转包）从根源防止服务端超时踢出。
 *
 * ★ 原理（矛盾定性）：
 *   主要矛盾是 Netty 的 ReadTimeoutHandler(30秒) 在无数据接收时直接关闭通道，
 *   而非 Minecraft 自身的 keep-alive 超时。拦截事件通知只是治标，
 *   替换超时时间 + 高频发包才是治本。
 */
@Mixin(Connection.class)
public abstract class ConnectionMixin {

    @Unique
    private static final Logger fkuLogger = LoggerFactory.getLogger("FKU_Connection");

    @Inject(
            method = "channelActive",
            at = @At(value = "TAIL")
    )
    public void onChannelActive(ChannelHandlerContext ctx, CallbackInfo ci) {
        if (!FkuConfig.disableConnectionTimeout.get()) return;

        // ★ 替换 ReadTimeoutHandler：30秒 → 3600秒（1小时）
        //   ReadTimeoutHandler 在空闲(无数据)30秒后会调用 ctx.close() 关闭通道
        //   这是"连接中断"的根源，拦截 exceptionCaught/channelInactive 无法阻止通道被关闭
        ChannelPipeline pipeline = ctx.pipeline();
        if (pipeline.get("timeout") != null) {
            pipeline.replace("timeout", "timeout", new ReadTimeoutHandler(3600, TimeUnit.SECONDS));
            fkuLogger.warn("[FKU] ReadTimeoutHandler 已替换为 3600 秒（禁连超时模式）");
        }
    }

    @Inject(
            method = "exceptionCaught",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void onExceptionCaught(ChannelHandlerContext context, Throwable ex, CallbackInfo ci) {
        if (FkuConfig.disableConnectionTimeout.get()) {
            ci.cancel();
        }
    }

    /**
     * 通道关闭事件的最后防线
     *
     * 当 ReadTimeoutHandler 已被替换为3600秒时，正常情况下 channelInactive 不应触发。
     * 此处仅保留作为兜底（防止服务端主动断开等意外情况）。
     */
    @Inject(
            method = "channelInactive",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void onChannelInactive(ChannelHandlerContext context, CallbackInfo ci) {
        if (FkuConfig.disableConnectionTimeout.get()) {
            ci.cancel();
        }
    }

    /**
     * 拦截连接断开请求（第4层防线）
     *
     * 当服务端踢出玩家或连接异常时，Connection.disconnect(Component) 会被调用。
     * 此拦截器阻止断开传播，让玩家保持在游戏中，同时 KeepAliveHandler 继续发包维持连接。
     *
     * 注意：理论上不应该触发此拦截（前三层应已阻止），但作为兜底策略保留。
     */
    @Inject(
            method = "disconnect",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void onDisconnect(Component component, CallbackInfo ci) {
        if (FkuConfig.disableConnectionTimeout.get()) {
            fkuLogger.warn("[FKU] 拦截 disconnect: {}", component.getString());
            ci.cancel();
        }
    }
}