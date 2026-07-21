package fku.org.example.fku.features.healthtag;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ToggleComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.healthtag.HealthTagConfig;
import fku.org.example.fku.features.healthtag.HealthTagConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class HealthTagComponent
extends ToggleComponent {
    @Override
    protected String getFeatureName() {
        return "\u8840\u91cf\u663e\u793a";
    }

    public HealthTagComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "HealthTag");
    }

    @Override
    protected boolean isEnabled() {
        return HealthTagConfig.getInstance().enabled;
    }

    @Override
    protected void toggle() {
        HealthTagConfig.getInstance().enabled = !HealthTagConfig.getInstance().enabled;
    }

    @Override
    protected void saveConfig() {
        HealthTagConfig.save();
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
        String displayStr = this.hotkeyAppend(this.label + ": " + (enabled ? "ON" : "OFF"));
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
                Minecraft.getInstance().setScreen((Screen)new HealthTagConfigScreen());
                return true;
            }
            if (button == 2) {
                return this.handleMiddleClick(mouseX, mouseY, button);
            }
        }
        return false;
    }
}

