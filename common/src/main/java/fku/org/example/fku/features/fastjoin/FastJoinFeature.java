package fku.org.example.fku.features.fastjoin;

import fku.org.example.fku.Fku;
import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.features.fastjoin.FastJoinConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class FastJoinFeature {
    private static boolean recovering = false;
    private static int targetRd = 12;
    private static int tickCounter = 0;
    private static int lockedRd = -1;

    private static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    public static void init() {
        FastJoinConfig.load();
    }

    public static void toggleEnabled() {
        FastJoinFeature.setEnabled(!FastJoinFeature.isEnabled());
    }

    public static void setEnabled(boolean v) {
        FastJoinConfig cfg = FastJoinConfig.getInstance();
        cfg.enabled = v;
        cfg.save();
        if (!v) {
            recovering = false;
            tickCounter = 0;
            lockedRd = -1;
        }
    }

    public static boolean isEnabled() {
        return FastJoinConfig.getInstance().enabled;
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!FastJoinFeature.isEnabled()) {
            return;
        }
        FastJoinConfig cfg = FastJoinConfig.getInstance();
        if ("OFF".equals(cfg.mode)) {
            return;
        }
        recovering = true;
        tickCounter = 0;
        targetRd = Math.max(2, Math.min(32, cfg.targetRenderDistance));
        lockedRd = -1;
        FkuConfig.disableConnectionTimeout.set(true);
        FkuConfig.disableConnectionTimeout.save();
        int initialRd = switch (cfg.mode) {
            case "EXTREME" -> 1;
            case "SMOOTH" -> Math.max(1, targetRd / 2);
            default -> targetRd;
        };
        FastJoinFeature.setRd(initialRd);
        Fku.LOGGER.info("[FastJoin] mode={}, target={}, initial={}", new Object[]{cfg.mode, targetRd, initialRd});
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = FastJoinFeature.getMc();
        if (mc == null || !FastJoinFeature.isEnabled() || mc.player == null) {
            return;
        }
        FastJoinConfig cfg = FastJoinConfig.getInstance();
        int current = (Integer)mc.options.renderDistance().get();
        if (lockedRd > 0 && !recovering) {
            if (current != lockedRd) {
                FastJoinFeature.setRd(lockedRd);
            }
            return;
        }
        if (!recovering) {
            return;
        }
        if (current >= targetRd) {
            recovering = false;
            lockedRd = targetRd;
            if (cfg.showLoadingProgress && mc.player != null) {
                mc.player.displayClientMessage(Component.literal((String)("\u00a7a[FastJoin] \u00a77\u89c6\u8ddd\u5df2\u6062\u590d\u81f3 " + targetRd + " \u533a\u5757")), true);
            }
            return;
        }
        ++tickCounter;
        int speed = Math.max(1, Math.min(4, cfg.recoverSpeed));
        boolean doIncrease = false;
        switch (cfg.mode) {
            case "EXTREME": {
                if (tickCounter < 1) break;
                doIncrease = true;
                tickCounter = 0;
                break;
            }
            case "SMOOTH": {
                if (tickCounter < 40 / speed) break;
                doIncrease = true;
                tickCounter = 0;
                break;
            }
        }
        if (doIncrease) {
            int newRd = Math.min(current + speed, targetRd);
            FastJoinFeature.setRd(newRd);
            if (cfg.showLoadingProgress && mc.player != null) {
                mc.player.displayClientMessage(Component.literal((String)("\u00a76[FastJoin] \u00a77\u89c6\u8ddd: " + newRd + "/" + targetRd)), true);
            }
        }
    }

    private static void setRd(int rd) {
        Minecraft mc = FastJoinFeature.getMc();
        if (mc == null) {
            return;
        }
        mc.options.renderDistance().set(rd);
    }

    public static void fallbackToExtreme() {
        if (!FastJoinFeature.isEnabled()) {
            return;
        }
        FastJoinConfig cfg = FastJoinConfig.getInstance();
        if (!cfg.onTimeoutFallback) {
            return;
        }
        cfg.setMode("EXTREME");
        cfg.setEnabled(true);
        FastJoinFeature.setRd(1);
        recovering = true;
        tickCounter = 0;
        lockedRd = -1;
        targetRd = Math.max(2, cfg.targetRenderDistance);
        Minecraft mc = FastJoinFeature.getMc();
        if (mc != null && mc.player != null) {
            mc.player.displayClientMessage(Component.literal((String)"\u00a7c[FastJoin] \u8d85\u65f6\u56de\u9000\uff0c\u8bf7\u91cd\u65b0\u8fde\u63a5"), false);
        }
    }

    public static boolean isRecovering() {
        return recovering;
    }

    public static int getRecoveryProgress() {
        Minecraft mc = FastJoinFeature.getMc();
        if (mc == null || !recovering || targetRd <= 1) {
            return 100;
        }
        return Math.min(100, (Integer)mc.options.renderDistance().get() * 100 / targetRd);
    }
}

