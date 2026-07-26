package fku.org.example.fku.features.tacz; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 疾跑不断 — 每 tick 检查并恢复疾跑状态
 * 移植自 Lexis NoSprintInterruptHack
 * 该功能由赛博教员实现
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = {Dist.CLIENT})
public class NoSprintInterruptFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;
        MinecraftForge.EVENT_BUS.register(NoSprintInterruptFeature.class);
        Fku.LOGGER.info("[NoSprintInterruptFeature] 疾跑不断已初始化");
    }

    public static boolean isEnabled() {
        TaCZConfig cfg = TaCZConfig.getInstance();
        return cfg.masterEnabled && cfg.noSprintInterruptEnabled;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled() || mc.player == null) return;
        // 每 tick 设置疾跑，防止被射击/换弹打断
        if (mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
    }
}