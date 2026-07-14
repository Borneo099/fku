package fku.org.example.fku.features.fastjoin; /* water */

import fku.org.example.fku.Fku;
import fku.org.example.fku.config.FkuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.tick.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * FastJoinFeature — 快速加载 (修复版)
 *
 * ★ 修复：视距恢复后持续锁定目标值，防止游戏自动拉回
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FastJoinFeature {

    private static final Minecraft mc = Minecraft.getInstance();

    private static boolean recovering = false;
    private static int targetRd = 12;
    private static int tickCounter = 0;
    /** ★ 锁定目标视距，每 Tick 强制保持，防游戏自动拉回 */
    private static int lockedRd = -1;

    public static void init() {
        FastJoinConfig.load();
    }

    public static void toggleEnabled() { setEnabled(!isEnabled()); }
    public static void setEnabled(boolean v) {
        FastJoinConfig cfg = FastJoinConfig.getInstance();
        cfg.enabled = v;
        cfg.save();
        if (!v) { recovering = false; tickCounter = 0; lockedRd = -1; }
    }
    public static boolean isEnabled() { return FastJoinConfig.getInstance().enabled; }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!isEnabled()) return;
        FastJoinConfig cfg = FastJoinConfig.getInstance();
        if ("OFF".equals(cfg.mode)) return;

        recovering = true;
        tickCounter = 0;
        targetRd = Math.max(2, Math.min(32, cfg.targetRenderDistance));
        lockedRd = -1;

        FkuConfig.disableConnectionTimeout.set(true);
        FkuConfig.disableConnectionTimeout.save();

        int initialRd;
        switch (cfg.mode) {
            case "EXTREME" -> initialRd = 1;
            case "SMOOTH" -> initialRd = Math.max(1, targetRd / 2);
            default -> initialRd = targetRd;
        }
        setRd(initialRd);
        Fku.LOGGER.info("[FastJoin] mode={}, target={}, initial={}", cfg.mode, targetRd, initialRd);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled() || mc.player == null) return;

        FastJoinConfig cfg = FastJoinConfig.getInstance();
        int current = mc.options.renderDistance().get();

        // ★ 锁定阶段：每 Tick 强制保持目标值
        if (lockedRd > 0 && !recovering) {
            if (current != lockedRd) {
                setRd(lockedRd);
            }
            return;
        }

        if (!recovering) return;

        // 恢复阶段
        if (current >= targetRd) {
            recovering = false;
            lockedRd = targetRd; // ★ 进入锁定阶段
            if (cfg.showLoadingProgress && mc.player != null)
                mc.player.displayClientMessage(
                    Component.literal("§a[FastJoin] §7视距已恢复至 " + targetRd + " 区块"), true);
            return;
        }

        tickCounter++;
        int speed = Math.max(1, Math.min(4, cfg.recoverSpeed));
        boolean doIncrease = false;

        switch (cfg.mode) {
            case "EXTREME" -> { if (tickCounter >= 1) { doIncrease = true; tickCounter = 0; } }
            case "SMOOTH" -> { if (tickCounter >= 40 / speed) { doIncrease = true; tickCounter = 0; } }
            default -> {}
        }

        if (doIncrease) {
            int newRd = Math.min(current + speed, targetRd);
            setRd(newRd);
            if (cfg.showLoadingProgress && mc.player != null)
                mc.player.displayClientMessage(
                    Component.literal("§6[FastJoin] §7视距: " + newRd + "/" + targetRd), true);
        }
    }

    private static void setRd(int rd) {
        mc.options.renderDistance().set(rd);
    }

    public static void fallbackToExtreme() {
        if (!isEnabled()) return;
        FastJoinConfig cfg = FastJoinConfig.getInstance();
        if (!cfg.onTimeoutFallback) return;
        cfg.setMode("EXTREME"); cfg.setEnabled(true);
        setRd(1); recovering = true; tickCounter = 0; lockedRd = -1;
        targetRd = Math.max(2, cfg.targetRenderDistance);
        if (mc.player != null) mc.player.displayClientMessage(
            Component.literal("§c[FastJoin] 超时回退，请重新连接"), false);
    }

    public static boolean isRecovering() { return recovering; }
    public static int getRecoveryProgress() {
        if (!recovering || targetRd <= 1) return 100;
        return Math.min(100, mc.options.renderDistance().get() * 100 / targetRd);
    }
}
