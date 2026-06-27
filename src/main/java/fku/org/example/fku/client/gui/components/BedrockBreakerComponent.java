package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerConfig;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerManager;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 基岩破坏器开关组件
 * 左键切换启用/禁用，右键打开配置界面
 */
public class BedrockBreakerComponent extends GuiComponent {

    public BedrockBreakerComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "基岩破坏器");
    }

    private boolean isEnabled() {
        return BedrockBreakerConfig.getInstance().enabled;
    }

    private void toggle() {
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        cfg.setEnabled(!cfg.enabled);
        if (!cfg.enabled) {
            BedrockBreakerManager.getInstance().stop();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;

        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();

        GuiRenderHelper.drawComponentBackground(guiGraphics, x, y, width, height, enabled);

        String status = enabled ? "ON" : "OFF";
        String displayStr = "基岩破坏器: " + status;
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        guiGraphics.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2 - 4, textColor);
        // ★ 右键打开配置提示
        guiGraphics.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                toggle();
                return true;
            } else if (button == 1) {
                Minecraft.getInstance().setScreen(new BedrockBreakerScreen());
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