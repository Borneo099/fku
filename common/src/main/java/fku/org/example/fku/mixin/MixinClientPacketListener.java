package fku.org.example.fku.mixin;

import fku.org.example.fku.features.antilag.AntiLagFeature;
import fku.org.example.fku.features.tpaura.TpAuraConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(value={ClientPacketListener.class})
public abstract class MixinClientPacketListener {
    @Inject(method={"handleMovePlayer"}, at={@At(value="HEAD")}, cancellable=true)
    private void fku$onHandleMovePlayer(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        AntiLagFeature.onPlayerPositionPacket(packet, ci);
    }

    @Inject(method={"handlePlayerAbilities"}, at={@At(value="TAIL")})
    private void fku$onHandlePlayerAbilities(ClientboundPlayerAbilitiesPacket packet, CallbackInfo ci) {
        LocalPlayer p;
        TpAuraConfig cfg = TpAuraConfig.getInstance();
        if (cfg.autoFlight && cfg.enabled && (p = Minecraft.getInstance().player) != null) {
            p.getAbilities().flying = true;
        }
    }
}

