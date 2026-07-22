package fku.org.example.fku.features.criticals;

import fku.org.example.fku.features.criticals.CriticalsConfig;
import io.netty.channel.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CriticalsFeature {
    public static void init() {
        CriticalsConfig.load();
    }

    public static boolean isEnabled() {
        return CriticalsConfig.getInstance().enabled;
    }

    public static void toggleEnabled() {
        LocalPlayer player;
        CriticalsConfig cfg = CriticalsConfig.getInstance();
        cfg.enabled = !cfg.enabled;
        cfg.saveConfig();
        if (!cfg.silentSave && (player = Minecraft.getInstance().player) != null) {
            player.sendSystemMessage(Component.literal((String)("\u00a7a[\u5200\u5200\u66b4\u51fb] " + (cfg.enabled ? "\u5df2\u5f00\u542f" : "\u5df2\u5173\u95ed"))));
        }
    }

    public static void onAttackPacket(Channel ch) {
        if (!CriticalsFeature.isEnabled()) {
            return;
        }
        if (ch == null || !ch.isOpen()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        String mode = CriticalsConfig.getInstance().mode;
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        try {
            if ("JITTER".equals(mode)) {
                ch.writeAndFlush(new ServerboundMovePlayerPacket.Pos(x, y + 1.0E-4, z, false));
                ch.writeAndFlush(new ServerboundMovePlayerPacket.Pos(x, y + 1.0E-4, z, false));
            } else if ("MINI_JUMP".equals(mode)) {
                ch.writeAndFlush(new ServerboundMovePlayerPacket.Pos(x, y + 0.0625, z, false));
            } else {
                ch.writeAndFlush(new ServerboundMovePlayerPacket.StatusOnly(false));
            }
            ch.eventLoop().execute(() -> ch.writeAndFlush(new ServerboundMovePlayerPacket.Pos(x, y, z, true)));
        }
        catch (Exception exception) {
            // ignored
        }
    }
}

