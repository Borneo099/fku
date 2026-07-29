package fku.org.example.fku;

import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.features.healthtag.HealthTagConfig;
import fku.org.example.fku.config.MovementConfig;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.displaymodel.DisplayModelConfig;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerConfig;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerFeature;
import fku.org.example.fku.features.killfx.KillFXConfig;
import fku.org.example.fku.features.killfx.KillFXFeature;
import fku.org.example.fku.features.knockback.KnockbackFeature;
import fku.org.example.fku.features.sprint.SprintHandler;
import fku.org.example.fku.features.antilag.AntiLagFeature;
import fku.org.example.fku.features.quickswitch.QuickSwitchFeature;
import fku.org.example.fku.features.tpaura.TpAuraFeature;
import fku.org.example.fku.features.standattack.StandAttackFeature;
import fku.org.example.fku.features.pearlphase.PearlPhaseFeature;
import fku.org.example.fku.features.fakeplayer.FakePlayerFeature;
import fku.org.example.fku.features.loot.LootConfig;
import fku.org.example.fku.features.loot.LootFeature;
import fku.org.example.fku.features.worldedit.WorldEditFeature;
import fku.org.example.fku.features.structure_locator.StructureLocatorConfig;
import fku.org.example.fku.features.baritone.BaritoneConfig;
import fku.org.example.fku.features.selfdamage.SelfDamageFeature;
import fku.org.example.fku.features.killicon.KillIconConfig;
import fku.org.example.fku.features.killaura.KillAuraConfig;
import fku.org.example.fku.features.teleport.TeleportConfig;
import fku.org.example.fku.features.quickcommand.QuickCommandConfig;
import fku.org.example.fku.features.waterwalk.WaterWalkConfig;
import fku.org.example.fku.features.crashmonitor.CrashMonitor;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Fku.MOD_ID)
public class Fku
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "fku";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public Fku()
    {
        // ★ 崩溃监控必须最早初始化
        CrashMonitor.init(new java.io.File(FMLPaths.GAMEDIR.get().toFile(), "fku"));
        CrashMonitor.startPhase("FKU 构造器");

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // 注册模组通用设置事件，在这里面加载我们的配置
        modEventBus.addListener(this::commonSetup);

        // 注册各功能事件订阅器
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(KnockbackFeature.class);
        MinecraftForge.EVENT_BUS.register(AntiLagFeature.class);
        MinecraftForge.EVENT_BUS.register(TpAuraFeature.class);
        MinecraftForge.EVENT_BUS.register(StandAttackFeature.class);
        MinecraftForge.EVENT_BUS.register(LootFeature.class);
        MinecraftForge.EVENT_BUS.register(PearlPhaseFeature.class);
        MinecraftForge.EVENT_BUS.register(FakePlayerFeature.class);

        CrashMonitor.endPhase("FKU 构造器");
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        CrashMonitor.startPhase("加载配置");

        // 初始化配置文件，确保文件在游戏启动时被加载
        FkuConfig.init();
        HealthTagConfig.load();
        MovementConfig.load();
        GuiStyleConfig.load();
        DisplayModelConfig.load();
        BedrockBreakerConfig.load();

        CrashMonitor.setStage("初始化功能模块");

        BedrockBreakerFeature.init();
        KnockbackFeature.init();
        AntiLagFeature.init();
        TpAuraFeature.init();
        StandAttackFeature.init();
        LootConfig.load();
        KillFXFeature.init();
        PearlPhaseFeature.init();
        SprintHandler.init();
        FakePlayerFeature.init();
        QuickSwitchFeature.init();
        WorldEditFeature.init();
        StructureLocatorConfig.load();
        BaritoneConfig.load();
        SelfDamageFeature.init();
        KillIconConfig.load();
        KillAuraConfig.load();
        TeleportConfig.load();
        QuickCommandConfig.load();
        WaterWalkConfig.load();

        // ★ 热键互联：有独立 Config 的功能 ↔ 全局热键系统
        var tpa = fku.org.example.fku.features.tpaura.TpAuraConfig.getInstance();
        FeatureHotkeyManager.linkConfig("如来神掌", new FeatureHotkeyManager.LinkedConfig(
            () -> tpa.hotkeyKey, v -> tpa.setHotkeyKey(v),
            () -> tpa.hotkeyName, v -> tpa.setHotkeyName(v),
            () -> fku.org.example.fku.features.tpaura.TpAuraConfig.save()));

        var sa = fku.org.example.fku.features.standattack.StandAttackConfig.getInstance();
        FeatureHotkeyManager.linkConfig("替身攻击", new FeatureHotkeyManager.LinkedConfig(
            () -> sa.hotkeyKey, v -> sa.setHotkeyKey(v),
            () -> sa.hotkeyName, v -> sa.setHotkeyName(v),
            () -> fku.org.example.fku.features.standattack.StandAttackConfig.save()));

        var loot = fku.org.example.fku.features.loot.LootConfig.getInstance();
        FeatureHotkeyManager.linkConfig("一键取物", new FeatureHotkeyManager.LinkedConfig(
            () -> loot.hotkeyKey, v -> loot.setHotkeyKey(v),
            () -> loot.hotkeyName, v -> loot.setHotkeyName(v),
            () -> fku.org.example.fku.features.loot.LootConfig.save()));

        var sd = fku.org.example.fku.features.selfdamage.SelfDamageConfig.getInstance();
        FeatureHotkeyManager.linkConfig("自伤", new FeatureHotkeyManager.LinkedConfig(
            () -> sd.hotkeyKey, v -> { sd.hotkeyKey = v; sd.save(); },
            () -> sd.hotkeyName, v -> { sd.hotkeyName = v; sd.save(); },
            () -> fku.org.example.fku.features.selfdamage.SelfDamageConfig.save()));

        // ★ 基岩破坏器：桥接 BedrockBreakerConfig.triggerKey(String) ↔ 全局热键系统(int)
        var bb = fku.org.example.fku.features.bedrockbreaker.BedrockBreakerConfig.getInstance();
        FeatureHotkeyManager.linkConfig("基岩破坏器", new FeatureHotkeyManager.LinkedConfig(
            () -> {
                String tk = bb.triggerKey;
                if (tk == null || tk.isEmpty()) return -1;
                var ik = com.mojang.blaze3d.platform.InputConstants.getKey(tk);
                return ik.getValue();
            },
            key -> {
                var ik = com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(key);
                fku.org.example.fku.client.KeyBindings.updateBedrockBreakerKey(ik);
            },
            () -> {
                String tk = bb.triggerKey;
                if (tk == null || tk.isEmpty()) return "";
                String[] parts = tk.split("\\.");
                return parts.length > 0 ? parts[parts.length - 1].toUpperCase() : "";
            },
            name -> {
                var ik = com.mojang.blaze3d.platform.InputConstants.getKey(
                    "key.keyboard." + name.toLowerCase());
                if (ik != com.mojang.blaze3d.platform.InputConstants.UNKNOWN)
                    fku.org.example.fku.client.KeyBindings.updateBedrockBreakerKey(ik);
            },
            () -> fku.org.example.fku.features.bedrockbreaker.BedrockBreakerConfig.save()));

        // ★ 提前注册所有热键触发动作，确保 GUI 打开前也能响应
        HotkeySystem.registerFeature("防推", () -> fku.org.example.fku.features.antipush.AntiPushFeature.toggleEnabled());
        HotkeySystem.registerFeature("32k弓", () -> fku.org.example.fku.features.arrowdmg.ArrowDmgFeature.toggleEnabled());
        HotkeySystem.registerFeature("快速加入", () -> fku.org.example.fku.features.fastjoin.FastJoinFeature.toggleEnabled());
        HotkeySystem.registerFeature("飞行", () -> fku.org.example.fku.features.flight.FlightFeature.toggleEnabled());
        HotkeySystem.registerFeature("血量显示", () -> { var c = HealthTagConfig.getInstance(); c.enabled = !c.enabled; c.save(); });
        HotkeySystem.registerFeature("击杀特效", () -> { var c = KillFXConfig.getInstance(); c.enabled = !c.enabled; c.save(); });
        HotkeySystem.registerFeature("防摔", () -> fku.org.example.fku.features.nofall.NoFallFeature.toggleEnabled());
        HotkeySystem.registerFeature("无跳跃延迟", () -> MovementConfig.getInstance().setNoJumpDelayEnabled(!MovementConfig.getInstance().noJumpDelayEnabled));
        HotkeySystem.registerFeature("强制疾跑", () -> SprintHandler.setEnabled(!SprintHandler.isEnabled()));
        HotkeySystem.registerFeature("Y坐标显示", () -> MovementConfig.getInstance().setYPosOverlayEnabled(!MovementConfig.getInstance().yPosOverlayEnabled));
        HotkeySystem.registerFeature("基岩破坏器", () -> fku.org.example.fku.features.bedrockbreaker.BedrockBreakerManager.getInstance().process());
        HotkeySystem.registerFeature("一键取物", () -> {
            var c = loot;
            c.setEnabled(!c.enabled);
            if (c.enabled) LootFeature.start();
        });
        HotkeySystem.registerFeature("自伤", () -> SelfDamageFeature.applyDamage());
        HotkeySystem.registerFeature("如来神掌", () -> TpAuraFeature.setEnabled(!TpAuraFeature.isEnabled()));
        HotkeySystem.registerFeature("替身攻击", () -> StandAttackFeature.setEnabled(!StandAttackFeature.isEnabled()));
        HotkeySystem.registerFeature("假人", () -> FakePlayerFeature.toggle());
    }
}