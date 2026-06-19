package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.duplicator.DuplicatorConfig;
import fku.org.example.fku.features.duplicator.DuplicatorConfigScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 三叉戟复制开关组件
 * 左键切换启用/禁用
 */
public class DuplicatorComponent extends GuiComponent {

    public DuplicatorComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "三叉戟复制");
    }

    private boolean isEnabled() {
        return DuplicatorConfig.getInstance().enableTrident;
    }

    private void toggle() {
        DuplicatorConfig cfg = DuplicatorConfig.getInstance();
        cfg.enableTrident = !cfg.enableTrident;
        DuplicatorConfig.save();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;

        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();

        GuiRenderHelper.drawComponentBackground(guiGraphics, x, y, width, height, enabled);

        String displayStr = "三叉戟复制: " + (enabled ? "ON" : "OFF");
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        guiGraphics.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2 - 4, textColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                toggle();
                return true;
            } else if (button == 1) {
                Minecraft.getInstance().setScreen(new DuplicatorConfigScreen());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}