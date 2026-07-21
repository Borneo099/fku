package fku.org.example.fku.features.bedrockbreaker;

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerConfig;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class BedrockBreakerFeature {
    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        BedrockBreakerConfig.getInstance();
        Fku.LOGGER.info("[BedrockBreaker] \u529f\u80fd\u5df2\u521d\u59cb\u5316");
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        BedrockBreakerManager.getInstance().tick();
    }
}

