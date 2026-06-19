package fku.org.example.fku.client;

import fku.org.example.fku.client.KeyBindings;
import fku.org.example.fku.features.displaymodel.DisplayModelFeature;
import fku.org.example.fku.features.displaymodel.DisplayModelConfig;
import fku.org.example.fku.features.displaymodel.DisplayModelManager;
import fku.org.example.fku.features.duplicator.DuplicatorFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "fku", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 注册按键输入监听
        MinecraftForge.EVENT_BUS.register(KeyBindings.class);
        // 注册实体模型展示功能的指令
        DisplayModelFeature.init();
        // ★ 注册 DisplayModelManager 实例事件处理器（确保 ClientTickEvent 可靠触发）
        DisplayModelManager.registerEventHandlers();
        // ★ 初始化三叉戟/箭矢复制工具
        DuplicatorFeature.init();
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        KeyBindings.register(event);
    }
}