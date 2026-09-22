package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.antilag.AntiLagFeature;
import fku.org.example.fku.features.clientop.ClientOPFeature;
import fku.org.example.fku.features.clientop.OpCommandDB;
import fku.org.example.fku.features.tpaura.TpAuraConfig;
import fku.org.example.fku.features.dynamicisland.DynamicIslandDamageHandler;
import fku.org.example.fku.mixin.ServerboundInteractPacketAccessor;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MixinClientPacketListener — 拦截服务端协议包处理入口
 *
 * 职责：
 * - handleMovePlayer HEAD：防位置拉回 (AntiLagFeature)
 * - handlePlayerAbilities TAIL：TpAura 生存模式飞行保持
 * - handleCommands TAIL：把“需 OP 权限”的指令节点注入客户端本地指令树，
 *   让原版补全像真有权限一样工作（/game 不再爆红、/tell 不会混入 OP 指令）
 */
@OnlyIn(Dist.CLIENT)
@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

    @Shadow
    public CommandDispatcher<ClientSuggestionProvider> commands;

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

    /**
     * ★ 客户端OP：把 OP 指令节点注入本地指令树，使原版补全/解析像有权限一样。
     * 仅注入 OP 指令（非 OP 的 msg/tell 等保持服务端原始节点，不再被串味）。
     * 已存在的指令（真有权限时服务端已下发）跳过，避免重复。
     */
    @Inject(method = "handleCommands", at = @At("TAIL"))
    private void fku$injectOpCommands(ClientboundCommandsPacket packet, CallbackInfo ci) {
        if (!ClientOPFeature.isEnabled()) return;
        try {
            RootCommandNode<ClientSuggestionProvider> root = this.commands.getRoot();
            for (OpCommandDB.OpCommand c : OpCommandDB.COMMANDS) {
                if (!c.op) continue;
                if (root.getChild(c.name) != null) continue;
                LiteralCommandNode<ClientSuggestionProvider> node = LiteralArgumentBuilder.<ClientSuggestionProvider>literal(c.name)
                        .then(RequiredArgumentBuilder.<ClientSuggestionProvider, String>argument("args", StringArgumentType.greedyString())
                                .executes(ctx -> 0))
                        .build();
                root.addChild(node);
            }
        } catch (Exception ignored) {}
    }

    @Inject(
            method = {"send(Lnet/minecraft/network/protocol/Packet;)V"},
            at = {@At("HEAD")}
    )
    private void fku$onSendPacket(Packet packet, CallbackInfo ci) {
        if (packet instanceof final ServerboundInteractPacket ip) {
            ip.dispatch(new ServerboundInteractPacket.Handler() {
                public void onAttack() {
                    DynamicIslandDamageHandler.notifyAttack(((ServerboundInteractPacketAccessor)ip).getEntityId());
                }
                public void onInteraction(InteractionHand hand) {}
                public void onInteraction(InteractionHand hand, Vec3 pos) {}
            });
        }
    }
}