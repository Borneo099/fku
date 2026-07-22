package fku.org.example.fku.features.worldedit;

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.worldedit.SelectionManager;
import fku.org.example.fku.features.worldedit.SuperDistanceInteraction;
import fku.org.example.fku.features.worldedit.TaskQueue;
import fku.org.example.fku.features.worldedit.ToolManager;
import fku.org.example.fku.features.worldedit.WorldEditConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class WorldEditFeature {
    private static boolean initialized = false;

    private static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        WorldEditConfig.getInstance();
        Fku.LOGGER.info("[WorldEdit] \u529f\u80fd\u5df2\u521d\u59cb\u5316");
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        WorldEditConfig cfg = WorldEditConfig.getInstance();
        Minecraft mc = WorldEditFeature.getMc();
        if (mc == null || !cfg.enabled) {
            return;
        }
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (!mc.player.getAbilities().instabuild && cfg.safeMode) {
            if (cfg.enabled) {
                WorldEditFeature.autoDisable("\u00a7cWorldEdit \u4ec5\u521b\u9020\u6a21\u5f0f\u53ef\u7528");
            }
            return;
        }
        TaskQueue.getInstance().tick();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        WorldEditConfig cfg = WorldEditConfig.getInstance();
        Minecraft mc = WorldEditFeature.getMc();
        if (mc == null || !cfg.enabled || !cfg.renderSelection) {
            return;
        }
        if (mc.player == null || mc.level == null) {
            return;
        }
        SelectionManager.getInstance().renderSelection(event.getPoseStack(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        boolean handled;
        int button;
        WorldEditConfig cfg = WorldEditConfig.getInstance();
        Minecraft mc = WorldEditFeature.getMc();
        if (mc == null || !cfg.enabled) {
            return;
        }
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (!mc.player.getAbilities().instabuild && cfg.safeMode) {
            return;
        }
        int n = event.isAttack() ? 0 : (button = event.isUseItem() ? 1 : -1);
        if (button < 0) {
            return;
        }
        InteractionHand hand = event.getHand();
        if (ToolManager.getInstance().getCurrentTool().equals("wand")) {
            handled = ToolManager.getInstance().handleClick(button, hand);
        } else {
            if (SuperDistanceInteraction.getInstance().isHelmetEquipped() && (handled = SuperDistanceInteraction.getInstance().handleClick(button, hand))) {
                event.setCanceled(true);
                event.setSwingHand(false);
                return;
            }
            handled = ToolManager.getInstance().handleClick(button, hand);
        }
        if (handled) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    private static void autoDisable(String reason) {
        WorldEditConfig cfg = WorldEditConfig.getInstance();
        cfg.setEnabled(false);
        SuperDistanceInteraction.getInstance().disable();
        ToolManager.getInstance().disableAll();
        TaskQueue.getInstance().cancel();
        Minecraft mc = WorldEditFeature.getMc();
        if (mc != null && mc.player != null) {
            mc.player.displayClientMessage(Component.literal((String)("\u00a7c[WorldEdit] " + reason)), true);
        }
    }
}

