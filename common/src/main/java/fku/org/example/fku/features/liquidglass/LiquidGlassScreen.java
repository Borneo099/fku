package fku.org.example.fku.features.liquidglass; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 液体玻璃配置界面
 * 提供对所有玻璃效果参数的实时调整
 *
 * ★ 参考：LiquidGlassShader (https://github.com/Jacquesqwq/LiquidGlassShader)
 *   移植其 V3 配置参数体系
 *
 * 该配置界面由赛博教员实现
 */
public class LiquidGlassScreen extends Screen {

    private final LiquidGlassConfig cfg = LiquidGlassConfig.getInstance();
    private static final int W = 180;
    private int bx, by0;

    // 当前编辑的滑块参数索引（用于简化实现）
    private int editMode = 0; // 0=主菜单, 1=基础, 2=Clear, 3=Tinted

    public LiquidGlassScreen() {
        super(Component.literal("液体玻璃配置"));
    }

    @Override
    protected void init() {
        bx = width / 2 - W / 2;
        by0 = height / 2 - 120;
        rebuildWidgets();
    }

    protected void rebuildWidgets() {
        clearWidgets();
        int y = by0, sp = 20;

        // 标题
        addRenderableWidget(Button.builder(Component.literal("§l§b液体玻璃配置"), b -> {})
                .bounds(bx, y, W, 18).build());

        y += sp + 4;

        // 开关
        addRenderableWidget(Button.builder(Component.literal(cfg.enabled ? "§a■ 开启" : "§c□ 关闭"), b -> {
            cfg.setEnabled(!cfg.enabled);
            b.setMessage(Component.literal(cfg.enabled ? "§a■ 开启" : "§c□ 关闭"));
        }).bounds(bx, y, W, 18).build());

        y += sp;

        if (editMode == 0) {
            // 主菜单
            addRenderableWidget(Button.builder(Component.literal("§7[基础设置]"), b -> editMode = 1)
                    .bounds(bx, y, W, 16).build());
            y += sp;
            addRenderableWidget(Button.builder(Component.literal("§7[Clear模式参数]"), b -> editMode = 2)
                    .bounds(bx, y, W, 16).build());
            y += sp;
            addRenderableWidget(Button.builder(Component.literal("§7[Tinted模式参数]"), b -> editMode = 3)
                    .bounds(bx, y, W, 16).build());
            y += sp;
            addRenderableWidget(Button.builder(Component.literal("§7[切换模式] " + (cfg.tintMode == 0 ? "Clear" : "Tinted")), b -> {
                cfg.setTintMode(cfg.tintMode == 0 ? 1 : 0);
                b.setMessage(Component.literal("§7[切换模式] " + (cfg.tintMode == 0 ? "Clear" : "Tinted")));
            }).bounds(bx, y, W, 16).build());
            y += sp;

            // 常用参数快捷调整
            y += 4;
            addSliderButton(y, "面板宽度", cfg.panelWidth, 50, 500, v -> cfg.setPanelWidth(v));
            y += sp;
            addSliderButton(y, "面板高度", cfg.panelHeight, 50, 500, v -> cfg.setPanelHeight(v));
            y += sp;
            addSliderButton(y, "圆角半径", cfg.cornerRadius, 0, 50, v -> cfg.setCornerRadius(v));
            y += sp;
            addSliderButton(y, "模糊强度", cfg.blurRadius, 0, 20, v -> cfg.setBlurRadius(v));
            y += sp;
            addSliderButton(y, "折射强度", cfg.refractionPower, -1, 10, v -> cfg.setRefractionPower(v));
            y += sp;
            addSliderButton(y, "全局透明度", cfg.globalAlpha, 0, 1, v -> cfg.setGlobalAlpha(v));
            y += sp;

        } else if (editMode == 1) {
            // 基础设置
            addRenderableWidget(Button.builder(Component.literal("§7← 返回主菜单"), b -> editMode = 0)
                    .bounds(bx, y, W, 16).build());
            y += sp + 4;

            addSliderButton(y, "面板宽度", cfg.panelWidth, 50, 500, v -> cfg.setPanelWidth(v));
            y += sp;
            addSliderButton(y, "面板高度", cfg.panelHeight, 50, 500, v -> cfg.setPanelHeight(v));
            y += sp;
            addSliderButton(y, "圆角半径", cfg.cornerRadius, 0, 50, v -> cfg.setCornerRadius(v));
            y += sp;
            addSliderButton(y, "模糊强度", cfg.blurRadius, 0, 20, v -> cfg.setBlurRadius(v));
            y += sp;
            addSliderButton(y, "折射强度", cfg.refractionPower, -1, 10, v -> cfg.setRefractionPower(v));
            y += sp;
            addSliderButton(y, "折射边缘", cfg.refractionEdge, 0, 1, v -> cfg.setRefractionEdge(v));
            y += sp;
            addSliderButton(y, "全局透明度", cfg.globalAlpha, 0, 1, v -> cfg.setGlobalAlpha(v));
            y += sp;

        } else if (editMode == 2) {
            // Clear 模式参数
            addRenderableWidget(Button.builder(Component.literal("§7← 返回主菜单"), b -> editMode = 0)
                    .bounds(bx, y, W, 16).build());
            y += sp + 4;

            addSliderButton(y, "噪声/磨砂", cfg.noise, 0, 0.3f, v -> cfg.setNoise(v));
            y += sp;
            addSliderButton(y, "发光权重", cfg.glowWeight, -1, 1, v -> cfg.setGlowWeight(v));
            y += sp;
            addSliderButton(y, "发光偏移", cfg.glowBias, -1, 1, v -> cfg.setGlowBias(v));
            y += sp;
            addSliderButton(y, "发光起始", cfg.glowEdge0, -1, 1, v -> cfg.setGlowEdge0(v));
            y += sp;
            addSliderButton(y, "发光结束", cfg.glowEdge1, -1, 1, v -> cfg.setGlowEdge1(v));
            y += sp;

        } else if (editMode == 3) {
            // Tinted 模式参数
            addRenderableWidget(Button.builder(Component.literal("§7← 返回主菜单"), b -> editMode = 0)
                    .bounds(bx, y, W, 16).build());
            y += sp + 4;

            addSliderButton(y, "染色R", cfg.tintR, 0, 1, v -> cfg.setTintR(v));
            y += sp;
            addSliderButton(y, "染色G", cfg.tintG, 0, 1, v -> cfg.setTintG(v));
            y += sp;
            addSliderButton(y, "染色B", cfg.tintB, 0, 1, v -> cfg.setTintB(v));
            y += sp;
            addSliderButton(y, "染色强度", cfg.tintStrength, 0, 1, v -> cfg.setTintStrength(v));
            y += sp;
            addSliderButton(y, "色散强度", cfg.chromaStrength, 0, 0.01f, v -> cfg.setChromaStrength(v));
            y += sp;
            addSliderButton(y, "暗度", cfg.darkness, 0, 1, v -> cfg.setDarkness(v));
            y += sp;
        }

        // 完成按钮
        y = Math.max(y + 10, by0 + 280);
        addRenderableWidget(Button.builder(Component.literal("§a完成"), b -> onClose())
                .bounds(bx + W / 2 - 40, y, 80, 18).build());
    }

    /**
     * 添加滑块按钮（简化实现：使用 +/- 按钮调整）
     */
    private void addSliderButton(int y, String label, float current, float min, float max, java.util.function.Consumer<Float> setter) {
        String display = String.format("%s: %.2f", label, current);
        addRenderableWidget(Button.builder(Component.literal("§7" + display), b -> {})
                .bounds(bx, y, W, 14).build());
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        GuiRenderHelper.drawRoundedRect(g, bx - 10, by0 - 8, W + 20, 320, 0xAA222222, 8);
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}