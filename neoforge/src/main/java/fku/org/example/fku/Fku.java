package fku.org.example.fku; /* water */

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Fku.MOD_ID)
public class Fku {
    public static final String MOD_ID = "fku";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public Fku(IEventBus modBus) {
        LOGGER.info("[FKU] NeoForge 1.21.8 启动");

        if (FMLEnvironment.dist.isClient()) {
            modBus.addListener(ClientSetup::onClientSetup);
        }
    }
}
