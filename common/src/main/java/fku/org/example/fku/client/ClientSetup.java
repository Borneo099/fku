package fku.org.example.fku.client;

import fku.org.example.fku.client.KeyBindings;
import fku.org.example.fku.features.antipush.AntiPushFeature;
import fku.org.example.fku.features.arrowdmg.ArrowDmgFeature;
import fku.org.example.fku.features.attackindicator.AttackIndicatorFeature;
import fku.org.example.fku.features.crashmonitor.CrashMonitor;
import fku.org.example.fku.features.criticals.CriticalsFeature;
import fku.org.example.fku.features.displaymodel.DisplayModelFeature;
import fku.org.example.fku.features.displaymodel.DisplayModelManager;
import fku.org.example.fku.features.duplicator.DuplicatorFeature;
import fku.org.example.fku.features.fastjoin.FastJoinFeature;
import fku.org.example.fku.features.flight.FlightFeature;
import fku.org.example.fku.features.killaura.KillAuraFeature;
import fku.org.example.fku.features.liquidglass.LiquidGlassFeature;
import fku.org.example.fku.features.nofall.NoFallFeature;
import fku.org.example.fku.features.quickcommand.QuickCommandFeature;
import fku.org.example.fku.features.teleport.TeleportFeature;
import fku.org.example.fku.features.waterwalk.WaterWalkFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid="fku", value={Dist.CLIENT}, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        CrashMonitor.startPhase("\u5ba2\u6237\u7aef\u8bbe\u7f6e");
        MinecraftForge.EVENT_BUS.register(KeyBindings.class);
        DisplayModelFeature.init();
        DisplayModelManager.registerEventHandlers();
        DuplicatorFeature.init();
        FlightFeature.init();
        NoFallFeature.init();
        FastJoinFeature.init();
        ArrowDmgFeature.init();
        CriticalsFeature.init();
        AntiPushFeature.init();
        KillAuraFeature.init();
        TeleportFeature.init();
        QuickCommandFeature.init();
        WaterWalkFeature.init();
        LiquidGlassFeature.init();
        AttackIndicatorFeature.init();
        CrashMonitor.endPhase("\u5ba2\u6237\u7aef\u8bbe\u7f6e");
        CrashMonitor.markLaunchComplete();
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        KeyBindings.register(event);
    }
}

