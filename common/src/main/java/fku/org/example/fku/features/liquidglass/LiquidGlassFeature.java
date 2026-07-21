package fku.org.example.fku.features.liquidglass;

import fku.org.example.fku.Fku;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.liquidglass.LiquidGlassConfig;
import fku.org.example.fku.features.liquidglass.LiquidGlassRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="fku", value={Dist.CLIENT})
public class LiquidGlassFeature {
    private static boolean initialized = false;
    private static boolean showPanel = false;
    private static boolean dragging = false;
    private static int dragStartX;
    private static int dragStartY;
    private static int dragPanelStartX;
    private static int dragPanelStartY;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        LiquidGlassConfig.load();
        MinecraftForge.EVENT_BUS.register(LiquidGlassFeature.class);
        Fku.LOGGER.info("[LiquidGlass] \u5df2\u521d\u59cb\u5316");
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (Minecraft.getInstance().player == null) {
            return;
        }
        LiquidGlassConfig cfg = LiquidGlassConfig.getInstance();
        if (!cfg.enabled) {
            return;
        }
        LiquidGlassRenderer.updateMipMapBlurTexture();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent event) {
        LiquidGlassConfig cfg = LiquidGlassConfig.getInstance();
        if (!cfg.enabled) {
            return;
        }
        GuiGraphics guiGraphics = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            return;
        }
        LiquidGlassFeature.renderPanel(guiGraphics, cfg);
    }

    private static void renderPanel(GuiGraphics guiGraphics, LiquidGlassConfig cfg) {
        if (!showPanel) {
            return;
        }
        LiquidGlassRenderer.drawPanel(cfg);
        int x = cfg.panelX;
        int y = cfg.panelY;
        int w = cfg.panelWidth;
        int h = cfg.panelHeight;
        GuiStyleConfig style = GuiStyleConfig.getInstance();
        guiGraphics.drawString(Minecraft.getInstance().font, "\u00a7l\u6db2\u4f53\u73bb\u7483", x + 8, y + 6, style.getTextColor(), true);
        String mode = cfg.tintMode == 0 ? "Clear" : "Tinted";
        guiGraphics.drawString(Minecraft.getInstance().font, "\u6a21\u5f0f: " + mode, x + 8, y + 22, style.getTextColor(), true);
        guiGraphics.drawString(Minecraft.getInstance().font, "\u00a77\u62d6\u62fd\u79fb\u52a8 | \u53f3\u952e\u914d\u7f6e", x + 8, y + h - 14, 0xAAAAAA, true);
    }

    public static void handleMouseDrag(int mouseX, int mouseY) {
        if (!dragging) {
            return;
        }
        LiquidGlassConfig cfg = LiquidGlassConfig.getInstance();
        int dx = mouseX - dragStartX;
        int dy = mouseY - dragStartY;
        cfg.setPanelX(dragPanelStartX + dx);
        cfg.setPanelY(dragPanelStartY + dy);
    }

    public static boolean isMouseOverPanel(int mouseX, int mouseY) {
        LiquidGlassConfig cfg = LiquidGlassConfig.getInstance();
        return mouseX >= cfg.panelX && mouseX <= cfg.panelX + cfg.panelWidth && mouseY >= cfg.panelY && mouseY <= cfg.panelY + cfg.panelHeight;
    }

    public static void startDrag(int mouseX, int mouseY) {
        LiquidGlassConfig cfg = LiquidGlassConfig.getInstance();
        dragging = true;
        dragStartX = mouseX;
        dragStartY = mouseY;
        dragPanelStartX = cfg.panelX;
        dragPanelStartY = cfg.panelY;
    }

    public static void stopDrag() {
        dragging = false;
    }

    public static boolean isShowPanel() {
        return showPanel;
    }

    public static void setShowPanel(boolean v) {
        showPanel = v;
    }

    public static void togglePanel() {
        showPanel = !showPanel;
    }
}

