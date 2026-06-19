package fku.org.example.fku;

import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.config.HealthTagConfig;
import fku.org.example.fku.config.MovementConfig;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.displaymodel.DisplayModelConfig;
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
    }
}