package fku.org.example.fku.features.antipush;

import fku.org.example.fku.features.antipush.AntiPushConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class AntiPushFeature {
    public static void init() {
        AntiPushConfig.load();
    }

    public static void toggleEnabled() {
        AntiPushFeature.setEnabled(!AntiPushFeature.isEnabled());
    }

    public static void setEnabled(boolean v) {
        AntiPushConfig cfg = AntiPushConfig.getInstance();
        cfg.enabled = v;
        cfg.save();
    }

    public static boolean isEnabled() {
        return AntiPushConfig.getInstance().enabled;
    }
}

