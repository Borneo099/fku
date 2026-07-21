package fku.org.example.fku.features.antipush;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ToggleComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.antipush.AntiPushFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class AntiPushComponent
extends ToggleComponent {
    @Override
    protected String getFeatureName() {
        return "\u9632\u63a8";
    }

    public AntiPushComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u9632\u63a8");
    }

    @Override
    protected boolean isEnabled() {
        return AntiPushFeature.isEnabled();
    }

    @Override
    protected void toggle() {
        AntiPushFeature.toggleEnabled();
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
        boolean en = this.isEnabled();
        GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, en);
        String display = this.hotkeyAppend(this.label + ": " + (en ? "\u5f00" : "\u5173"));
        g.drawString(Minecraft.getInstance().font, display, this.x + 5, this.y + (this.height - 8) / 2, en ? config.getTextColor() : 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (this.isHovered(mx, my)) {
            if (btn == 0) {
                this.toggle();
                this.saveConfig();
                return true;
            }
            if (btn == 2) {
                return this.handleMiddleClick(mx, my, btn);
            }
        }
        return false;
    }
}

