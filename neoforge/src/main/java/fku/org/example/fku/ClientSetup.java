package fku.org.example.fku; /* water */

import fku.org.example.fku.features.antipush.AntiPushFeature;
import fku.org.example.fku.features.arrowdmg.ArrowDmgFeature;
import fku.org.example.fku.features.fastjoin.FastJoinFeature;
import fku.org.example.fku.features.flight.FlightFeature;
import fku.org.example.fku.features.nofall.NoFallFeature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ClientSetup {
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 注册事件监听（NeoForge 通过 NeoForge.EVENT_BUS 注册）
        var bus = NeoForge.EVENT_BUS;

        // 初始化各功能（与 Forge 版一致）
        FlightFeature.init();
        NoFallFeature.init();
        FastJoinFeature.init();
        ArrowDmgFeature.init();
        AntiPushFeature.init();

        Fku.LOGGER.info("[FKU] 客户端初始化完成");
    }
}
