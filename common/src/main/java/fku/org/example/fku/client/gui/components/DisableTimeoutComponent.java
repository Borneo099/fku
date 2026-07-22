package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.DisableTimeoutConfigScreen;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class DisableTimeoutComponent
extends GuiComponent {
    public DisableTimeoutComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u7981\u8fde\u8d85\u65f6");
    }

    private boolean isEnabled() {
        return (Boolean)FkuConfig.disableConnectionTimeout.get();
    }

    private void toggle() {
        FkuConfig.disableConnectionTimeout.set(!(Boolean)FkuConfig.disableConnectionTimeout.get());
        FkuConfig.disableConnectionTimeout.save();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = this.isEnabled();
        GuiRenderHelper.drawComponentBackground(guiGraphics, this.x, this.y, this.width, this.height, enabled);
        String displayStr = "\u7981\u8fde\u8d85\u65f6: " + (enabled ? "ON" : "OFF");
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        guiGraphics.drawString(Minecraft.getInstance().font, displayStr, this.x + 5, this.y + (this.height - 8) / 2 - 4, textColor);
        guiGraphics.drawString(Minecraft.getInstance().font, ">>", this.x + this.width - 18, this.y + (this.height - 8) / 2 - 4, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovered(mouseX, mouseY)) {
            if (button == 0) {
                this.toggle();
                return true;
            }
            if (button == 1) {
                Screen current = Minecraft.getInstance().screen;
                if (current != null) {
                    Minecraft.getInstance().setScreen((Screen)new DisableTimeoutConfigScreen());
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

