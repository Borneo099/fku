package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.antilag.AntiLagFeature;
import fku.org.example.fku.features.tpaura.TpAuraConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MixinClientPacketListener — 拦截服务端协议包处理入口
 *
 * 职责：
 * - handleMovePlayer HEAD：防位置拉回 (AntiLagFeature)
 * - handlePlayerAbilities TAIL：TpAura 生存模式飞行保持
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

    /**
     * ★ TpAura 生存模式自动飞行保持
     * 在 handlePlayerAbilities TAIL 注入，服务端通过 abilities 包禁飞后，立即恢复。
     * 注意：不设 mayfly 以避免与 FlightFeature 的 hasCreativeFlight 检测冲突。
     */
    @Inject(
            method = "handlePlayerAbilities",
            at = @At("TAIL")
    )
    private void fku$onHandlePlayerAbilities(ClientboundPlayerAbilitiesPacket packet, CallbackInfo ci) {
        TpAuraConfig cfg = TpAuraConfig.getInstance();
        if (cfg.autoFlight && cfg.enabled) {
            var p = Minecraft.getInstance().player;
            if (p != null) {
                p.getAbilities().flying = true;
            }
        }
    }
}