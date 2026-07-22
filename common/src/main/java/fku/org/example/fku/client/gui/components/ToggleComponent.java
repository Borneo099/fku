package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public abstract class ToggleComponent
extends GuiComponent {
    protected String label;

    protected String getFeatureName() {
        return null;
    }

    public ToggleComponent(int x, int y, int width, int height, String label) {
        super(x, y, width, height, label);
        this.label = label;
        String fn = this.getFeatureName();
        if (fn != null) {
            HotkeySystem.registerFeature(fn, () -> {
                this.toggle();
                this.saveConfig();
            });
        }
    }

    protected abstract boolean isEnabled();

    protected abstract void toggle();

    protected abstract void saveConfig();

    protected boolean renderHotkeyWait(GuiGraphics g) {
        String fn = this.getFeatureName();
        if (fn != null && HotkeySystem.isWaitingFor(fn)) {
            GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, true);
            g.drawString(Minecraft.getInstance().font, "\u7ed1\u5b9a\u70ed\u952e\u4e2d. (Esc\u53d6\u6d88)", this.x + 5, this.y + (this.height - 8) / 2, 0xFFFF00);
            return true;
        }
        return false;
    }

    protected String hotkeyAppend(String text) {
        FeatureHotkeyManager.IHotkeyInterface hk;
        String fn = this.getFeatureName();
        if (fn != null && (hk = FeatureHotkeyManager.getInstance().getHotkey(fn)).getHotkeyKey() >= 0) {
            text = text + " \u00a77[" + hk.getHotkeyName() + "]";
        }
        return text;
    }

    protected boolean handleMiddleClick(double mx, double my, int button) {
        if (button == 2 && this.isHovered(mx, my)) {
            String fn = this.getFeatureName();
            if (fn != null) {
                HotkeySystem.startBinding(fn, () -> {});
            }
            return true;
        }
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!this.visible || this.currentAlpha <= 0.01f) {
            return;
        }
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        if (this.renderHotkeyWait(g)) {
            return;
        }
        boolean enabled = this.isEnabled();
        GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, enabled, this.currentAlpha);
        String displayStr = this.hotkeyAppend(this.label + ": " + (enabled ? "ON" : "OFF"));
        int textAlpha = (int)(255.0f * this.currentAlpha);
        int textColor = enabled ? textAlpha << 24 | config.getTextColor() & 0xFFFFFF : textAlpha << 24 | 0xAAAAAA;
        g.drawString(Minecraft.getInstance().font, displayStr, this.x + 5, this.y + (this.height - 8) / 2, textColor);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!this.isHovered(mx, my)) {
            return false;
        }
        if (button == 0) {
            if (HotkeySystem.isWaiting()) {
                return false;
            }
            this.toggle();
            this.saveConfig();
            return true;
        }
        if (button == 2) {
            return this.handleMiddleClick(mx, my, button);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}

