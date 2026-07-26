package fku.org.example.fku.client;

import fku.org.example.fku.client.KeyBindings;
import fku.org.example.fku.features.antipush.AntiPushFeature;
import fku.org.example.fku.features.arrowdmg.ArrowDmgFeature;
import fku.org.example.fku.features.crashmonitor.CrashMonitor;
import fku.org.example.fku.features.criticals.CriticalsFeature;
import fku.org.example.fku.features.fastjoin.FastJoinFeature;
import fku.org.example.fku.features.flight.FlightFeature;
import fku.org.example.fku.features.nofall.NoFallFeature;
import fku.org.example.fku.features.displaymodel.DisplayModelFeature;
import fku.org.example.fku.features.displaymodel.DisplayModelConfig;
import fku.org.example.fku.features.displaymodel.DisplayModelManager;
import fku.org.example.fku.features.duplicator.DuplicatorFeature;
import fku.org.example.fku.features.killaura.KillAuraFeature;
import fku.org.example.fku.features.teleport.TeleportFeature;
import fku.org.example.fku.features.quickcommand.QuickCommandFeature;
import fku.org.example.fku.features.waterwalk.WaterWalkFeature;
import fku.org.example.fku.features.attackindicator.AttackIndicatorFeature;
import fku.org.example.fku.features.trail.TrailFeature;
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
        CrashMonitor.startPhase("客户端设置");

        // 注册按键输入监听
        MinecraftForge.EVENT_BUS.register(KeyBindings.class);
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
        // ★ 从配置文件静默恢复刀刀暴击开关状态
        CriticalsFeature.init();
        // ★ 从配置文件静默恢复防推开关状态
        AntiPushFeature.init();
        // ★ 初始化攻击指示器
        AttackIndicatorFeature.init();
        // ★ 初始化拖尾特效
        TrailFeature.init();
        // ★ 初始化 HUD 信息显示
        KillAuraFeature.init();
        TeleportFeature.init();
        QuickCommandFeature.init();
        WaterWalkFeature.init();

        // ★ 客户端设置完成 = 启动完成
        CrashMonitor.endPhase("客户端设置");
        CrashMonitor.markLaunchComplete();
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        KeyBindings.register(event);
    }
}
