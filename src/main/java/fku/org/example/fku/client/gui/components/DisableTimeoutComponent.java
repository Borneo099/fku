package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/**
 * 禁用连接超时开关组件
 * 左键切换开/关，开启后屏蔽 Connection#exceptionCaught 事件
 */
public class DisableTimeoutComponent extends GuiComponent {

    public DisableTimeoutComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "禁连超时");
    }

    private boolean isEnabled() {
        return FkuConfig.disableConnectionTimeout.get();
    }

    private void toggle() {
        FkuConfig.disableConnectionTimeout.set(!FkuConfig.disableConnectionTimeout.get());
        FkuConfig.disableConnectionTimeout.save();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;

        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();

        GuiRenderHelper.drawComponentBackground(guiGraphics, x, y, width, height, enabled);

        String displayStr = "禁连超时: " + (enabled ? "ON" : "OFF");
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
                Screen current = Minecraft.getInstance().screen;
                if (current != null) {
                    Minecraft.getInstance().setScreen(new DisableTimeoutConfigScreen());
                }
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