package fku.org.example.fku.features.antiknockback; /* water */

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
 * 防击退配置界面
 *
 * 配置项：
 *   - 模式：完全免疫 / 按比例减弱
 *   - 减弱强度（REDUCE 模式，0.0~1.0，1.0 等同完全免疫）
 */
public class AntiKnockbackConfigScreen extends Screen {

    private static final int WIDTH = 250;
    private static final int HEIGHT = 220;

    private final AntiKnockbackConfig cfg = AntiKnockbackConfig.getInstance();

    private Button modeButton;
    private EditBox strengthInput;

    public AntiKnockbackConfigScreen() {
        super(Component.literal("防击退配置"));
    }

    @Override
    protected void init() {
        super.init();
        int cx = (width - WIDTH) / 2;

        // 模式
        String currentLabel = cfg.getMode().toString();
        modeButton = Button.builder(
                Component.literal("模式: " + currentLabel),
                btn -> {
                    AntiKnockbackConfig.Mode next = (cfg.getMode() == AntiKnockbackConfig.Mode.FULL)
                            ? AntiKnockbackConfig.Mode.REDUCE
                            : AntiKnockbackConfig.Mode.FULL;
                    cfg.setMode(next.name());
                    btn.setMessage(Component.literal("模式: " + next.toString()));
                    rebuildWidgets();
                }
        ).bounds(cx + 10, 50, 120, 18).build();
        addRenderableWidget(modeButton);

        // 减弱强度
        strengthInput = new EditBox(font, cx + 90, 96, 60, 14, Component.literal(""));
        strengthInput.setValue(String.format("%.2f", cfg.strength));
        strengthInput.setMaxLength(4);
        strengthInput.setFilter(s -> s.matches("0?(\\.\\d*)?|1(\\.0*)?"));
        strengthInput.setVisible(cfg.getMode() == AntiKnockbackConfig.Mode.REDUCE);
        addRenderableWidget(strengthInput);

        // 完成
        addRenderableWidget(Button.builder(
                Component.literal("完成"),
                btn -> { saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen()); }
        ).bounds(cx + 80, 170, 80, 18).build());
    }

    private void saveConfig() {
        try {
            float s = Float.parseFloat(strengthInput.getValue());
            cfg.setStrength(s);
        } catch (Exception ignored) {}
        AntiKnockbackConfig.save();
    }

    private int cy(int rowOffset) { return (height - HEIGHT) / 2 + rowOffset; }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int cx = (width - WIDTH) / 2;
        int cy = cy(0);
        GuiRenderHelper.drawPanelBackground(g, cx, cy, WIDTH, HEIGHT, false);
        g.drawString(font, "防击退配置", cx + 10, cy + 8, 0xFFFFFF);

        boolean isReduce = cfg.getMode() == AntiKnockbackConfig.Mode.REDUCE;
        if (isReduce) {
            g.drawString(font, "减弱强度:", cx + 10, cy(84), 0xAAAAAA);
            g.drawString(font, "§7(0.0~1.0, 越大剩余击退越少)", cx + 154, cy(96), 0x666666);
            g.drawString(font, "§7剩余击退 = " + String.format("%.0f", (1.0f - cfg.strength) * 100) + "%", cx + 10, cy(118), 0x888888);
        } else {
            g.drawString(font, "§7完全免疫：被攻击时不会后退/抬高", cx + 10, cy(84), 0x888888);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (strengthInput.mouseClicked(mouseX, mouseY, button)) {
            strengthInput.setFocused(true);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (strengthInput.isFocused() && strengthInput.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (keyCode == 256) { onClose(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (strengthInput.isFocused() && strengthInput.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        saveConfig();
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}
