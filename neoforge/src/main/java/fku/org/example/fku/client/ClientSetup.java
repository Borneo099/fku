package fku.org.example.fku.client;

import fku.org.example.fku.client.KeyBindings;
import fku.org.example.fku.features.antipush.AntiPushFeature;
import fku.org.example.fku.features.arrowdmg.ArrowDmgFeature;
import fku.org.example.fku.features.fastjoin.FastJoinFeature;
import fku.org.example.fku.features.flight.FlightFeature;
import fku.org.example.fku.features.nofall.NoFallFeature;
import fku.org.example.fku.features.displaymodel.DisplayModelFeature;
import fku.org.example.fku.features.displaymodel.DisplayModelConfig;
import fku.org.example.fku.features.displaymodel.DisplayModelManager;
import fku.org.example.fku.features.duplicator.DuplicatorFeature;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "fku", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 注册按键输入监听
        NeoForge.EVENT_BUS.register(KeyBindings.class);
        // 注册实体模型展示功能的指令
        DisplayModelFeature.init();
        // ★ 注册 DisplayModelManager 实例事件处理器（确保 ClientTickEvent 可靠触发）
        DisplayModelManager.registerEventHandlers();
        // ★ 初始化三叉戟/箭矢复制工具
        DuplicatorFeature.init();
        // ★ 从配置文件静默恢复飞行开关状态
        FlightFeature.init();
        // ★ 从配置文件静默恢复防摔开关状态
        NoFallFeature.init();
        // ★ 从配置文件静默恢复快速加载开关状态
        FastJoinFeature.init();
        // ★ 从配置文件静默恢复32k弓开关状态
        ArrowDmgFeature.init();
        // ★ 从配置文件静默恢复防推开关状态
        AntiPushFeature.init();
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        KeyBindings.register(event);
    }
}
