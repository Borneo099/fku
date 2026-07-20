package fku.org.example.fku.features.liquidglass; /* water */

import fku.org.example.fku.Fku;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * 液体玻璃面板功能主类
 * 负责：
 * 1. 在游戏 HUD 上渲染液体玻璃面板
 * 2. 管理面板的显示/隐藏
 * 3. 处理按键事件打开配置界面
 * 4. 每帧更新 mipmap 模糊纹理
 *
 * ★ 参考：LiquidGlassShader (https://github.com/Jacquesqwq/LiquidGlassShader)
 *   移植其 V3 单通道片源着色器方案，适配 Forge 1.20.1
 *
 * 该功能由赛博教员实现
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = Dist.CLIENT)
public class LiquidGlassFeature {

    private static boolean initialized = false;
    private static boolean showPanel = false;
    private static boolean dragging = false;
    private static int dragStartX, dragStartY;
    private static int dragPanelStartX, dragPanelStartY;

    /**
     * 初始化液体玻璃功能
     */
    public static void init() {
        if (initialized) return;
        initialized = true;

        // 加载配置
        LiquidGlassConfig.load();

        // 注册事件订阅
        MinecraftForge.EVENT_BUS.register(LiquidGlassFeature.class);

        Fku.LOGGER.info("[LiquidGlass] 已初始化");
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().player == null) return;

        LiquidGlassConfig cfg = LiquidGlassConfig.getInstance();
        if (!cfg.enabled) return;

        // 每帧更新 mipmap 模糊纹理
        LiquidGlassRenderer.updateMipMapBlurTexture();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent event) {
        LiquidGlassConfig cfg = LiquidGlassConfig.getInstance();
        if (!cfg.enabled) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();

        // 检查是否在游戏内（非暂停菜单）
        if (mc.screen != null) return;

        // 渲染液体玻璃面板
        renderPanel(guiGraphics, cfg);
    }

    /**
     * 渲染液体玻璃面板
     */
    private static void renderPanel(GuiGraphics guiGraphics, LiquidGlassConfig cfg) {
        if (!showPanel) return;

        // 使用着色器渲染玻璃背景
        LiquidGlassRenderer.drawPanel(cfg);

        // 在着色器渲染的玻璃面板上绘制文字内容
        int x = cfg.panelX;
        int y = cfg.panelY;
        int w = (int) cfg.panelWidth;
        int h = (int) cfg.panelHeight;

        GuiStyleConfig style = GuiStyleConfig.getInstance();

        // 绘制标题文字
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                "§l液体玻璃",
                x + 8, y + 6,
                style.getTextColor(),
                true
        );

        // 绘制状态信息
        String mode = cfg.tintMode == 0 ? "Clear" : "Tinted";
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                "模式: " + mode,
                x + 8, y + 22,
                style.getTextColor(),
                true
        );

        // 绘制拖动提示
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                "§7拖拽移动 | 右键配置",
                x + 8, y + h - 14,
                0xAAAAAA,
                true
        );
    }

    /**
     * 处理鼠标拖动（从 Screen 传递）
     */
    public static void handleMouseDrag(int mouseX, int mouseY) {
        if (!dragging) return;
        LiquidGlassConfig cfg = LiquidGlassConfig.getInstance();
        int dx = mouseX - dragStartX;
        int dy = mouseY - dragStartY;
        cfg.setPanelX(dragPanelStartX + dx);
        cfg.setPanelY(dragPanelStartY + dy);
    }

    /**
     * 检查鼠标是否在面板区域内
     */
    public static boolean isMouseOverPanel(int mouseX, int mouseY) {
        LiquidGlassConfig cfg = LiquidGlassConfig.getInstance();
        return mouseX >= cfg.panelX && mouseX <= cfg.panelX + cfg.panelWidth
                && mouseY >= cfg.panelY && mouseY <= cfg.panelY + cfg.panelHeight;
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

    public static boolean isShowPanel() { return showPanel; }
    public static void setShowPanel(boolean v) { showPanel = v; }

    /**
     * 切换面板显示
     */
    public static void togglePanel() {
        showPanel = !showPanel;
    }
}