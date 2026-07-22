package fku.org.example.fku.features.waterwalk;

import fku.org.example.fku.features.waterwalk.WaterWalkConfig;
import net.minecraft.client.Minecraft;

public class WaterWalkFeature {
    private static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    public static void init() {
        WaterWalkConfig.getInstance();
    }

    public static boolean isActive() {
        WaterWalkConfig cfg = WaterWalkConfig.getInstance();
        if (!cfg.enabled) {
            return false;
        }
        Minecraft mc = WaterWalkFeature.getMc();
        if (mc == null || mc.player == null) {
            return false;
        }
        return !mc.options.keyShift.isDown();
    }
}

