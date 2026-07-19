package fku.org.example.fku.features.criticals; /* water */

import io.netty.channel.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 刀刀暴击 — 借鉴 Wurst 的 CriticalsHack
 *
 * 原理：攻击包发出前，通过 netty channel 先发一个"假离地"移动包，
 * 让服务端把这次攻击按暴击结算（服务端暴击判定依据 onGround=false 与 fallDistance）。
 * 攻击包发出后再恢复 onGround，避免服务端误判玩家处于下落状态而累计摔落伤害。
 *
 * 该方式对单人（内置服务端）与多人服均生效，且客户端暴击粒子由服务端回传，无需额外处理。
 */
@OnlyIn(Dist.CLIENT)
public class CriticalsFeature {

    public static void init() {
        CriticalsConfig.load();
    }

    public static boolean isEnabled() {
        return CriticalsConfig.getInstance().enabled;
    }

    public static void toggleEnabled() {
        CriticalsConfig cfg = CriticalsConfig.getInstance();
        cfg.enabled = !cfg.enabled;
        cfg.saveConfig(); // 始终静默持久化开关状态
        // 静默保存关闭时，给出切换提示
        if (!cfg.silentSave) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.sendSystemMessage(Component.literal(
                        "§a[刀刀暴击] " + (cfg.enabled ? "已开启" : "已关闭")));
            }
        }
    }

    /**
     * 攻击包发出前调用（由 MixinConnectionAttackInterceptor HEAD 注入）。
     * 发送假"离地"移动包，诱使服务端把这次攻击判定为暴击。
     */
    public static void onAttackPacket(Channel ch) {
        if (!isEnabled()) return;
        if (ch == null || !ch.isOpen()) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        String mode = CriticalsConfig.getInstance().mode;
        double x = player.getX(), y = player.getY(), z = player.getZ();

        try {
            if ("JITTER".equals(mode)) {
                ch.writeAndFlush(new ServerboundMovePlayerPacket.Pos(x, y + 1.0E-4, z, false));
                ch.writeAndFlush(new ServerboundMovePlayerPacket.Pos(x, y + 1.0E-4, z, false));
            } else if ("MINI_JUMP".equals(mode)) {
                ch.writeAndFlush(new ServerboundMovePlayerPacket.Pos(x, y + 0.0625, z, false));
            } else { // PACKET（默认）：只发一个离地状态包
                ch.writeAndFlush(new ServerboundMovePlayerPacket.StatusOnly(false));
            }

            // 攻击包由 interceptor 在 onAttackPacket 返回后正常发出；
            // 之后通过事件循环恢复原始位置与 onGround。
            ch.eventLoop().execute(() ->
                ch.writeAndFlush(new ServerboundMovePlayerPacket.Pos(x, y, z, true))
            );
        } catch (Exception ignored) {}
    }
}
