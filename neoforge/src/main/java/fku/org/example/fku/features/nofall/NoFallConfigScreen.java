package fku.org.example.fku.features.nofall; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 防摔配置界面
 */
public class NoFallConfigScreen extends Screen {

    private static final int WIDTH = 260;
    private static final int HEIGHT = 140;

    private EditBox minDistInput;
    private Button immuneBtn, onlyFlyBtn;

    public NoFallConfigScreen() {
        super(Component.literal("防摔配置"));
    }

    @Override
    protected void init() {
        NoFallConfig cfg = NoFallConfig.getInstance();
        int cx = (width - WIDTH) / 2;
        int cy = (height - HEIGHT) / 2;

        // 最小触发高度
        addRenderableWidget(Button.builder(Component.literal("触发高度:"), b -> {}).bounds(cx + 5, cy + 10, 70, 18).build());
        minDistInput = new EditBox(font, cx + 80, cy + 10, 50, 16, Component.literal(""));
        minDistInput.setValue(String.valueOf(cfg.minFallDistance));
        minDistInput.setFilter(s -> s.matches("\\d*\\.?\\d*"));
        addWidget(minDistInput);

        // 完全免疫
        immuneBtn = addRenderableWidget(Button.builder(
            Component.literal("完全免疫: " + (cfg.immune ? "§a开" : "§c关")),
            btn -> { cfg.setImmune(!cfg.immune); btn.setMessage(Component.literal("完全免疫: " + (cfg.immune ? "§a开" : "§c关"))); }
        ).bounds(cx + 10, cy + 40, 140, 18).build());

        // 仅飞行保护
        onlyFlyBtn = addRenderableWidget(Button.builder(
            Component.literal("仅飞行: " + (cfg.onlyWhenFlying ? "§a开" : "§c关")),
            btn -> { cfg.setOnlyWhenFlying(!cfg.onlyWhenFlying); btn.setMessage(Component.literal("仅飞行: " + (cfg.onlyWhenFlying ? "§a开" : "§c关"))); }
        ).bounds(cx + 10, cy + 65, 140, 18).build());

        // 返回
        addRenderableWidget(Button.builder(
            Component.literal("§a返回"),
            btn -> saveAndClose()
        ).bounds(cx + WIDTH / 2 - 30, cy + HEIGHT - 30, 60, 18).build());
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);
        int cx = (width - WIDTH) / 2;
        int cy = (height - HEIGHT) / 2;
        GuiRenderHelper.drawPanelBackground(g, cx, cy, WIDTH, HEIGHT, false);
        super.render(g, mx, my, pt);
        if (minDistInput != null) minDistInput.render(g, mx, my, pt);
        g.drawString(font, "格（低于此不保护）", cx + 135, cy + 12, 0x666666);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (minDistInput != null) minDistInput.mouseClicked(mx, my, button);
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minDistInput != null && minDistInput.isFocused()) return minDistInput.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == 256) { saveAndClose(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (minDistInput != null && minDistInput.isFocused()) return minDistInput.charTyped(codePoint, modifiers);
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void onClose() { saveAndClose(); }

    private void saveAndClose() {
        NoFallConfig cfg = NoFallConfig.getInstance();
        try {
            double d = Double.parseDouble(minDistInput.getValue().trim());
            cfg.setMinFallDistance(d);
        } catch (NumberFormatException ignored) {}
        this.minecraft.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
