package fku.org.example.fku; /* water */

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fku.org.example.fku.config.FkuConfig;

@Mod(Fku.MOD_ID)
public class Fku {
    public static final String MOD_ID = "fku";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public Fku(IEventBus modBus) {
        LOGGER.info("[FKU] NeoForge 1.21.8 启动");

        // 注册客户端配置：NeoForge 会在模组加载阶段把 FkuConfig.SPEC 加载并绑定，
        // 这样 GUI 面板在打开时调用 FkuConfig.xxx.get() 不会抛
        // "Cannot get config value before config is loaded"。
        ModLoadingContext.get().getActiveContainer()
                .registerConfig(ModConfig.Type.CLIENT, FkuConfig.SPEC);

        if (FMLEnvironment.dist.isClient()) {
            modBus.addListener(ClientSetup::onClientSetup);
        }
    }
}
