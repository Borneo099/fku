package fku.org.example.fku.features.duplicator;

import fku.org.example.fku.Fku;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

/**
 * 三叉戟/箭矢复制工具 — 功能注册
 *
 * 职责：
 * - 在 ClientSetup 阶段调用注册事件处理器
 * - 提供启用/禁用开关（后续可通过 GUI 控制）
 *
 * 设计思想：
 * - 独立模块，通过 DuplicatorManager 状态机驱动
 * - 不与任何 Screen 耦合，后台运行
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DuplicatorFeature {

    private static boolean initialized = false;

    /**
     * 初始化（在 ClientSetup 中调用一次）
     */
    public static void init() {
        if (initialized) return;
        initialized = true;

        // 注册事件处理器
        DuplicatorManager.registerEventHandlers();

        // 加载配置
        DuplicatorConfig.getInstance();
        Fku.LOGGER.info("[Duplicator] 功能已初始化");
    }
}