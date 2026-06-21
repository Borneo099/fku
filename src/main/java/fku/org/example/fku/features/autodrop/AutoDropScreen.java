package fku.org.example.fku.features.autodrop; /* water */

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 自动丢弃配置界面
 *
 * ★ 布局（3行）：
 *   行0：丢弃模式标签 + 按钮
 *   行1：扫描间隔标签 + 输入框 + 说明
 *   行2：重置黑名单按钮
 *   行3：保存按钮
 */
public class AutoDropScreen extends Screen {
    private static final int WIDTH = 270;
    private static final int HEIGHT = 160;
    private Button resetButton;
    private Button dropModeButton;
    private EditBox scanIntervalField;

    public AutoDropScreen() {
        super(Component.literal("自动丢配置"));
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;

        AutoDropConfig config = AutoDropConfig.getInstance();

        // ── 行0：丢弃模式按钮 ──
        dropModeButton = Button.builder(
                Component.literal(config.dropAsEntity ? "§a掉落物" : "§c直接消失"),
                btn -> {
                    AutoDropConfig cfg = AutoDropConfig.getInstance();
                    cfg.dropAsEntity = !cfg.dropAsEntity;
                    AutoDropConfig.save();
                    btn.setMessage(Component.literal(cfg.dropAsEntity ? "§a掉落物" : "§c直接消失"));
                })
                .bounds(x + 100, y + 30, 90, 20).build();
        addRenderableWidget(dropModeButton);

        // ── 行1：扫描间隔输入框 ──
        scanIntervalField = new EditBox(font, x + 120, y + 60, 40, 18, Component.literal("扫描间隔"));
        scanIntervalField.setValue(String.valueOf(config.scanInterval));
        scanIntervalField.setMaxLength(2);
        addRenderableWidget(scanIntervalField);

        // ── 行2：重置黑名单 ──
        resetButton = Button.builder(Component.literal("重置黑名单"), btn -> {
            AutoDropConfig cfg = AutoDropConfig.getInstance();
            cfg.clearBlacklist();
            AutoDropPanel.resetScroll();
            Minecraft.getInstance().player.displayClientMessage(
                Component.literal("§a自动丢弃黑名单已重置"), true);
        }).bounds(x + 10, y + 95, 100, 18).build();
        addRenderableWidget(resetButton);

        // ── 行3：保存 ──
        addRenderableWidget(Button.builder(
                Component.literal("保存"),
                btn -> saveConfig()
        ).bounds(x + 95, y + 130, 80, 20).build());
    }

    private void saveConfig() {
        AutoDropConfig config = AutoDropConfig.getInstance();
        try {
            config.setScanInterval(Integer.parseInt(scanIntervalField.getValue()));
        } catch (NumberFormatException ignored) {}
        onClose();
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);

        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;

        GuiRenderHelper.drawPanelBackground(g, x, y, WIDTH, HEIGHT, false);
        g.drawString(font, "自动丢配置", x + 10, y + 8, 0xFFFFFF);

        // 行0：丢弃模式标签
        g.drawString(font, "丢弃模式:", x + 10, y + 34, 0xAAAAAA);

        // 行1：扫描间隔标签
        g.drawString(font, "扫描间隔(tick):", x + 10, y + 64, 0xAAAAAA);
        g.drawString(font, "1~20，越小越快", x + 168, y + 64, 0x888888);

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}