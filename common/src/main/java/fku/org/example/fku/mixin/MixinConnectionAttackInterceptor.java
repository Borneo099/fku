package fku.org.example.fku.mixin;

import fku.org.example.fku.features.criticals.CriticalsFeature;
import fku.org.example.fku.features.knockback.FakeRotationManager;
import fku.org.example.fku.features.quickswitch.QuickSwitchFeature;
import io.netty.channel.Channel;
import java.util.concurrent.ThreadLocalRandom;
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

@OnlyIn(Dist.CLIENT)
@Mixin(value={Connection.class})
public abstract class MixinConnectionAttackInterceptor {
    @Shadow
    private Channel channel;
    @Unique
    private static boolean fku$sendingPending = false;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"send(Lnet/minecraft/network/protocol/Packet;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void fku$onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (fku$sendingPending) {
            return;
        }
        if (!(packet instanceof ServerboundInteractPacket)) {
            return;
        }
        boolean hasRotation = FakeRotationManager.hasPending();
        boolean hasQuickSwitch = QuickSwitchFeature.isIdle() && QuickSwitchFeature.isEnabled();
        boolean hasCriticals = CriticalsFeature.isEnabled();
        if (!(hasRotation || hasQuickSwitch || hasCriticals)) {
            return;
        }
        fku$sendingPending = true;
        try {
            Channel ch = this.channel;
            if (ch != null && ch.isOpen()) {
                if (hasRotation) {
                    int burstCount = 2 + ThreadLocalRandom.current().nextInt(2);
                    for (int i = 0; i < burstCount; ++i) {
                        ch.writeAndFlush(FakeRotationManager.createPendingPacket());
                    }
                    FakeRotationManager.clearPending();
                }
                if (hasQuickSwitch) {
                    QuickSwitchFeature.onAttackPacket(ch);
                }
                if (hasCriticals) {
                    CriticalsFeature.onAttackPacket(ch);
                }
            }
        }
        catch (Exception exception) {
            // ignored
        }
        finally {
            fku$sendingPending = false;
        }
    }
}

