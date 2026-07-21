package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.antilag.AntiLagConfig;
import fku.org.example.fku.features.antilag.AntiLagScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class AntiLagComponent
extends GuiComponent {
    public AntiLagComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u9632\u62c9\u56de");
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
        if (!this.visible) {
            return;
        }
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = this.isEnabled();
        GuiRenderHelper.drawComponentBackground(guiGraphics, this.x, this.y, this.width, this.height, enabled);
        String status = enabled ? "ON" : "OFF";
        String displayStr = "\u9632\u62c9\u56de: " + status;
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
                Minecraft.getInstance().setScreen((Screen)new AntiLagScreen());
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

