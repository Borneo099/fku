package fku.org.example.fku.mixin;

import fku.org.example.fku.config.FkuConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.util.concurrent.TimeUnit;
import net.minecraft.network.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Connection.class})
public abstract class ConnectionMixin {
    @Unique
    private static final Logger fkuLogger = LoggerFactory.getLogger((String)"FKU_Connection");

    @Inject(method={"channelActive"}, at={@At(value="TAIL")})
    public void onChannelActive(ChannelHandlerContext ctx, CallbackInfo ci) {
        if (!((Boolean)FkuConfig.disableConnectionTimeout.get()).booleanValue()) {
            return;
        }
        ChannelPipeline pipeline = ctx.pipeline();
        if (pipeline.get("timeout") != null) {
            pipeline.replace("timeout", "timeout", (ChannelHandler)new ReadTimeoutHandler(3600L, TimeUnit.SECONDS));
            fkuLogger.warn("[FKU] ReadTimeoutHandler \u5df2\u66ff\u6362\u4e3a 3600 \u79d2\uff08\u7981\u8fde\u8d85\u65f6\u6a21\u5f0f\uff09");
        }
    }

    @Inject(method={"exceptionCaught"}, at={@At(value="HEAD")}, cancellable=true)
    public void onExceptionCaught(ChannelHandlerContext context, Throwable ex, CallbackInfo ci) {
        if (((Boolean)FkuConfig.disableConnectionTimeout.get()).booleanValue()) {
            ci.cancel();
        }
    }
}

