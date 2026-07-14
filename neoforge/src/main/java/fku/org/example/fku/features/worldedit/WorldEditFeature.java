package fku.org.example.fku.features.worldedit; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.TickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * WorldEdit Lite 主控类
 *
 * 职责：
 * 1. 注册事件订阅（聊天指令、Tick、渲染、点击）
 * 2. 管理超远距离头盔交互
 * 3. 驱动任务队列
 * 4. 驱动选区渲染
 * 5. 非创造模式自动禁用
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WorldEditFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean initialized = false;

    /**
     * 初始化
     */
    public static void init() {
        if (initialized) return;
        initialized = true;

        WorldEditConfig.getInstance(); // 加载配置
        Fku.LOGGER.info("[WorldEdit] 功能已初始化");
    }

    /**
     * Tick 事件 — 驱动任务队列 + 安全检查
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        WorldEditConfig cfg = WorldEditConfig.getInstance();
        if (!cfg.enabled) return;
        if (mc.player == null || mc.level == null) return;

        // 非创造模式自动禁用
        if (!mc.player.getAbilities().instabuild && cfg.safeMode) {
            if (cfg.enabled) {
                autoDisable("§cWorldEdit 仅创造模式可用");
            }
            return;
        }

        // 驱动任务队列
        TaskQueue.getInstance().tick();
    }

    /**
     * 渲染事件 — 选区边框
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        WorldEditConfig cfg = WorldEditConfig.getInstance();
        if (!cfg.enabled || !cfg.renderSelection) return;
        if (mc.player == null || mc.level == null) return;

        SelectionManager.getInstance().renderSelection(event.getPoseStack(), event.getPartialTick());
    }

    /**
     * 鼠标点击事件 — 工具处理 + 超远交互
     */
    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        WorldEditConfig cfg = WorldEditConfig.getInstance();
        if (!cfg.enabled) return;
        if (mc.player == null || mc.level == null) return;

        // 非创造模式安全检查
        if (!mc.player.getAbilities().instabuild && cfg.safeMode) return;

        // 工具处理（选区等）
        int button = event.isAttack() ? 0 : (event.isUseItem() ? 1 : -1);
        if (button < 0) return;

        InteractionHand hand = event.getHand();
        boolean handled;

        // 先尝试工具处理
        if (ToolManager.getInstance().getCurrentTool().equals("wand")) {
            handled = ToolManager.getInstance().handleClick(button, hand);
        } else {
            // 再尝试超远交互
            if (SuperDistanceInteraction.getInstance().isHelmetEquipped()) {
                handled = SuperDistanceInteraction.getInstance().handleClick(button, hand);
                if (handled) {
                    event.setCanceled(true);
                    event.setSwingHand(false);
                    return;
                }
            }
            handled = ToolManager.getInstance().handleClick(button, hand);
        }

        if (handled) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    /**
     * 自动禁用
     */
    private static void autoDisable(String reason) {
        WorldEditConfig cfg = WorldEditConfig.getInstance();
        cfg.setEnabled(false);
        SuperDistanceInteraction.getInstance().disable();
        ToolManager.getInstance().disableAll();
        TaskQueue.getInstance().cancel();

        if (mc.player != null) {
            mc.player.displayClientMessage(
                    Component.literal("§c[WorldEdit] " + reason), true);
        }
    }
}
