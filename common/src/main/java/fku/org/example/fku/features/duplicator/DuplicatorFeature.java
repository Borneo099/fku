package fku.org.example.fku.features.duplicator;

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.duplicator.DuplicatorConfig;
import fku.org.example.fku.features.duplicator.DuplicatorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class DuplicatorFeature {
    private static boolean initialized = false;
    private static boolean enabled = false;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        DuplicatorManager.registerEventHandlers();
        DuplicatorConfig.getInstance();
        Fku.LOGGER.info("[Duplicator] \u529f\u80fd\u5df2\u521d\u59cb\u5316");
    }

    public static void toggle() {
        DuplicatorFeature.setEnabled(!enabled);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean v) {
        enabled = v;
        if (v) {
            Minecraft mc = Minecraft.getInstance();
            mc.player.m_5661_(Component.literal((String)"\u00a77[\u590d\u5236] \u00a7a\u4e09\u53c9\u621f\u590d\u5236\u5df2\u5f00\u542f\uff0c\u8bf7\u5c06\u4e09\u53c9\u621f\u653e\u5165\u70ed\u680f"), false);
        } else {
            DuplicatorManager.getInstance().reset();
        }
    }

    public static boolean isRunning() {
        return DuplicatorManager.getInstance().isRunning();
    }
}

