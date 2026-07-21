package fku.org.example.fku.features.waterwalk;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ToggleComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.waterwalk.WaterWalkConfig;
import fku.org.example.fku.features.waterwalk.WaterWalkConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class WaterWalkComponent
extends ToggleComponent {
    public WaterWalkComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u6c34\u4e0a\u884c\u8d70");
    }

    @Override
    protected String getFeatureName() {
        return "\u6c34\u4e0a\u884c\u8d70";
    }

    @Override
    protected boolean isEnabled() {
        return WaterWalkConfig.getInstance().enabled;
    }

    @Override
    protected void toggle() {
        WaterWalkConfig.getInstance().enabled = !WaterWalkConfig.getInstance().enabled;
    }

    @Override
    protected void saveConfig() {
        WaterWalkConfig.save();
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
        if (this.isHovered(mouseX, mouseY) && button == 1) {
            Minecraft.getInstance().setScreen((Screen)new WaterWalkConfigScreen());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

