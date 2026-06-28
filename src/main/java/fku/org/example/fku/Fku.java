package fku.org.example.fku;

import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.features.healthtag.HealthTagConfig;
import fku.org.example.fku.config.MovementConfig;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.displaymodel.DisplayModelConfig;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerConfig;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerFeature;
import fku.org.example.fku.features.killfx.KillFXFeature;
import fku.org.example.fku.features.knockback.KnockbackFeature;
import fku.org.example.fku.features.sprint.SprintHandler;
import fku.org.example.fku.features.antilag.AntiLagFeature;
import fku.org.example.fku.features.quickswitch.QuickSwitchFeature;
import fku.org.example.fku.features.tpaura.TpAuraFeature;
import fku.org.example.fku.features.pearlphase.PearlPhaseFeature;
import fku.org.example.fku.features.fakeplayer.FakePlayerFeature;
import fku.org.example.fku.features.loot.LootConfig;
import fku.org.example.fku.features.loot.LootFeature;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // 注册模组通用设置事件，在这里面加载我们的配置
        modEventBus.addListener(this::commonSetup);

        // 注册各功能事件订阅器
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(KnockbackFeature.class);
        MinecraftForge.EVENT_BUS.register(AntiLagFeature.class);
        MinecraftForge.EVENT_BUS.register(TpAuraFeature.class);
        MinecraftForge.EVENT_BUS.register(LootFeature.class);
        MinecraftForge.EVENT_BUS.register(PearlPhaseFeature.class);
        MinecraftForge.EVENT_BUS.register(FakePlayerFeature.class);
        // KillFXFeature 使用 @Mod.EventBusSubscriber 自动注册，无需手动 register
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // 初始化配置文件，确保文件在游戏启动时被加载
        FkuConfig.init();
        HealthTagConfig.load();
        MovementConfig.load();
        GuiStyleConfig.load();
        DisplayModelConfig.load();
        BedrockBreakerConfig.load();
        BedrockBreakerFeature.init();
        KnockbackFeature.init();
        AntiLagFeature.init();
        TpAuraFeature.init();
        LootConfig.load();
        KillFXFeature.init();
        PearlPhaseFeature.init();
        SprintHandler.init();
        FakePlayerFeature.init();
        QuickSwitchFeature.init();
    }
}