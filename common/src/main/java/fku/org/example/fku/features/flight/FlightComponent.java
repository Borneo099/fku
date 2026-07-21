package fku.org.example.fku.features.flight;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ToggleComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.flight.FlightConfigScreen;
import fku.org.example.fku.features.flight.FlightFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class FlightComponent
extends ToggleComponent {
    @Override
    protected String getFeatureName() {
        return "\u98de\u884c";
    }

    public FlightComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u98de\u884c");
    }

    @Override
    protected boolean isEnabled() {
        return FlightFeature.isEnabled();
    }

    @Override
    protected void toggle() {
        FlightFeature.toggleEnabled();
    }

    @Override
    protected void saveConfig() {
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!this.visible) {
            return;
        }
        if (this.renderHotkeyWait(g)) {
            return;
        }
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = this.isEnabled();
        GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, enabled);
        String displayStr = this.hotkeyAppend(this.label + ": " + (enabled ? "\u5f00" : "\u5173"));
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        g.drawString(Minecraft.getInstance().font, displayStr, this.x + 5, this.y + (this.height - 8) / 2, textColor);
        g.drawString(Minecraft.getInstance().font, ">>", this.x + this.width - 18, this.y + (this.height - 8) / 2, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovered(mouseX, mouseY)) {
            if (button == 0) {
                this.toggle();
                this.saveConfig();
                return true;
            }
            if (button == 1) {
                Minecraft.getInstance().setScreen((Screen)new FlightConfigScreen());
                return true;
            }
            if (button == 2) {
                return this.handleMiddleClick(mouseX, mouseY, button);
            }
        }
        return false;
    }
}

