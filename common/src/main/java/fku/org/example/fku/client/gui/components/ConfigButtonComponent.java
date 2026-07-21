package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class ConfigButtonComponent
extends GuiComponent {
    private final String label;
    private final Runnable onClick;

    protected String getFeatureName() {
        return null;
    }

    public ConfigButtonComponent(int x, int y, int width, int height, String label, Runnable onClick) {
        super(x, y, width, height, label);
        this.label = label;
        this.onClick = onClick;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        FeatureHotkeyManager.IHotkeyInterface hk;
        boolean waiting;
        if (!this.visible || this.currentAlpha <= 0.01f) {
            return;
        }
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        String fn = this.getFeatureName();
        boolean bl = waiting = fn != null && HotkeySystem.isWaitingFor(fn);
        if (waiting) {
            int bgColor = config.getPrimaryColorWithAlpha((180.0f * this.currentAlpha));
            GuiRenderHelper.drawRoundedRect(g, this.x, this.y, this.width, this.height, bgColor, Math.max(2, config.cornerRadius / 2));
            g.drawString(Minecraft.getInstance().font, "\u7ed1\u5b9a\u70ed\u952e\u4e2d. (Esc\u53d6\u6d88)", this.x + 5, this.y + (this.height - 8) / 2, 0xFFFF00);
            return;
        }
        int alpha = (180.0f * this.currentAlpha);
        int bgColor = config.getPrimaryColorWithAlpha(alpha);
        GuiRenderHelper.drawRoundedRect(g, this.x, this.y, this.width, this.height, bgColor, Math.max(2, config.cornerRadius / 2));
        int borderAlpha = (255.0f * this.currentAlpha);
        int borderColor = borderAlpha << 24 | config.getPrimaryColor() & 0xFFFFFF;
        GuiRenderHelper.drawRoundedOutline(g, this.x, this.y, this.width, this.height, borderColor, Math.max(2, config.cornerRadius / 2), 1);
        Object display = this.label;
        if (fn != null && (hk = FeatureHotkeyManager.getInstance().getHotkey(fn)).getHotkeyKey() >= 0) {
            display = (String)display + " \u00a77[" + hk.getHotkeyName() + "]";
        }
        int textAlpha = (255.0f * this.currentAlpha);
        int textColor = textAlpha << 24 | config.getTextColor() & 0xFFFFFF;
        g.drawString(Minecraft.getInstance().font, (String)display, this.x + 5, this.y + (this.height - 8) / 2, textColor);
        g.drawString(Minecraft.getInstance().font, ">>", this.x + this.width - 18, this.y + (this.height - 8) / 2, textAlpha << 24 | 0x888888);
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
            this.onClick.run();
            return true;
        }
        if (button == 2) {
            String fn = this.getFeatureName();
            if (fn != null) {
                HotkeySystem.startBinding(fn, () -> {});
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}

