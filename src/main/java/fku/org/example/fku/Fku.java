package fku.org.example.fku;

import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.features.healthtag.HealthTagConfig;
import fku.org.example.fku.config.MovementConfig;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.displaymodel.DisplayModelConfig;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerConfig;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerFeature;
import fku.org.example.fku.features.loot.LootConfig;
import fku.org.example.fku.features.tpaura.TpAuraFeature;
import fku.org.example.fku.features.duplicator.DuplicatorFeature;
import fku.org.example.fku.features.duplicator.DuplicatorConfig;
import fku.org.example.fku.features.autodrop.AutoDropConfig;
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
        MinecraftForge.EVENT_BUS.register(this);
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

        // ★ v2.4 修复：补充所有未初始化的功能（源码存在但未注册到事件总线）
        LootConfig.load();                  // 一键取物配置
        TpAuraFeature.init();               // 如来神掌/瞬移攻击
        DuplicatorConfig.getInstance();     // 三叉戟/箭矢复制（加载配置）
        DuplicatorFeature.init();           // 三叉戟/箭矢复制（注册事件）
        AutoDropConfig.getInstance();       // 自动丢弃（加载配置）
        // ArrowDmgFlyHandler / AutoDropHandler / NoJumpDelayHandler / YPosOverlay / HealthTagEvents
        // 均使用 @Mod.EventBusSubscriber 注解，已由 Forge 自动注册，无需手动调用
        LOGGER.info("[Fku] 所有功能模块初始化完成");
    }
}