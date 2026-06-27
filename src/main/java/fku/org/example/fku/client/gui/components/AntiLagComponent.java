package fku.org.example.fku.client.gui.components; /* water */

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.antilag.AntiLagConfig;
import fku.org.example.fku.features.antilag.AntiLagScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * AntiLag 防拉回开关组件
 *
 * 左键：切换启用/禁用
 * 右键：打开配置界面（AntiLagScreen）
 *
 * 渲染风格与 BedrockBreakerComponent 一致
 */
public class AntiLagComponent extends GuiComponent {

    public AntiLagComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "防拉回");
    }

    private boolean isEnabled() {
        return AntiLagConfig.getInstance().enabled;
    }

    private void toggle() {
        AntiLagConfig cfg = AntiLagConfig.getInstance();
        cfg.setEnabled(!cfg.enabled);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;

        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();

        GuiRenderHelper.drawComponentBackground(guiGraphics, x, y, width, height, enabled);

        String status = enabled ? "ON" : "OFF";
        String displayStr = "防拉回: " + status;
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        guiGraphics.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2 - 4, textColor);
        // ★ 右键配置提示箭头
        guiGraphics.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                toggle();
                return true;
            } else if (button == 1) {
                Minecraft.getInstance().setScreen(new AntiLagScreen());
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