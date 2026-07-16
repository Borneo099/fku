package fku.org.example.fku.mixin;

import fku.org.example.fku.config.FkuConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.TimeUnit;

/**
 * ConnectionMixin — 防止连接超时断开
 *
 * ★ 核心策略：
 *   1) channelActive → 替换 ReadTimeoutHandler(30秒) 为 ReadTimeoutHandler(3600秒)
 *      防止 Netty 因30秒无数据收到而强行关闭通道（治本）。
 *   2) exceptionCaught → 阻止 ReadTimeoutException 弹窗（治标）。
 *
 * ★ 注意（矛盾定性：冻结与解冻）：
 *   不能拦截 channelInactive 和 disconnect，否则退出游戏/世界时连接无法正常关闭，
 *   导致"保存世界中"界面卡死。只拦截 exceptionCaught 阻止超时弹窗即可。
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
}