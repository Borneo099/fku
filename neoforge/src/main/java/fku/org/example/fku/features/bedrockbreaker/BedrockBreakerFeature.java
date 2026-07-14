package fku.org.example.fku.features.bedrockbreaker; /* water */

import fku.org.example.fku.Fku;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * 基岩破坏器功能注册
 *
 * 职责：
 * - 在客户端 Tick 事件中驱动 BedrockBreakerManager 状态机
 * - 提供 /fku bedrockbreaker 命令（后续接入 FkuCommand）
 *
 * 设计思想：
 * - 纯客户端，无服务端逻辑
 * - 仅注册事件，不耦合 GUI
 */
@EventBusSubscriber(modid = Fku.MOD_ID, value = Dist.CLIENT)
public class BedrockBreakerFeature {

    private static boolean initialized = false;

    /**
     * 初始化（在 ClientSetup 中调用）
     */
    public static void init() {
        if (initialized) return;
        initialized = true;

        BedrockBreakerConfig.getInstance(); // 加载配置
        Fku.LOGGER.info("[BedrockBreaker] 功能已初始化");
    }

    /**
     * 客户端 Tick 结束事件 → 驱动状态机
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        BedrockBreakerManager.getInstance().tick();
    }
}