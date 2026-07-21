package fku.org.example.fku.features.criticals;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ToggleComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.criticals.CriticalsConfigScreen;
import fku.org.example.fku.features.criticals.CriticalsFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class CriticalsComponent
extends ToggleComponent {
    @Override
    protected String getFeatureName() {
        return "\u5200\u5200\u66b4\u51fb";
    }

    public CriticalsComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u5200\u5200\u66b4\u51fb");
    }

    @Override
    protected boolean isEnabled() {
        return CriticalsFeature.isEnabled();
    }

    @Override
    protected void toggle() {
        CriticalsFeature.toggleEnabled();
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
        boolean en = this.isEnabled();
        GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, en);
        String display = this.hotkeyAppend(this.label + ": " + (en ? "\u5f00" : "\u5173"));
        int c = en ? GuiStyleConfig.getInstance().getTextColor() : 0xAAAAAA;
        g.drawString(Minecraft.getInstance().font, display, this.x + 5, this.y + (this.height - 8) / 2, c);
        g.drawString(Minecraft.getInstance().font, ">>", this.x + this.width - 18, this.y + (this.height - 8) / 2, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (this.isHovered(mx, my)) {
            if (button == 0) {
                this.toggle();
                this.saveConfig();
                return true;
            }
            if (button == 1) {
                Minecraft.getInstance().setScreen((Screen)new CriticalsConfigScreen());
                return true;
            }
            if (button == 2) {
                return this.handleMiddleClick(mx, my, button);
            }
        }
        return false;
    }
}

