package fku.org.example.fku.features.tacz; /* water */

import fku.org.example.fku.Fku;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 全枪自动 — 状态管理类
 * 核心射击逻辑由 TaczClientShootMixin + TaczSniperFullAutoMixin 实现
 * 该功能由赛博教员实现
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = Dist.CLIENT)
public class FullAutoFeature {

    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;
        Fku.LOGGER.info("[FullAutoFeature] 全枪自动已初始化（由 Mixin 驱动）");
    }

    public static boolean isEnabled() {
        TaCZConfig cfg = TaCZConfig.getInstance();
        return cfg.masterEnabled && cfg.fullAutoEnabled;
    }
}