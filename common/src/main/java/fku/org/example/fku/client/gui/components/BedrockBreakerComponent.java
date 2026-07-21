package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerConfig;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerManager;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerScreen;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class BedrockBreakerComponent
extends GuiComponent {
    protected String getFeatureName() {
        return "\u57fa\u5ca9\u7834\u574f\u5668";
    }

    public BedrockBreakerComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u57fa\u5ca9\u7834\u574f\u5668");
        HotkeySystem.registerFeature("\u57fa\u5ca9\u7834\u574f\u5668", () -> BedrockBreakerManager.getInstance().process());
    }

    private boolean isEnabled() {
        return BedrockBreakerConfig.getInstance().enabled;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!this.visible) {
            return;
        }
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        if (HotkeySystem.isWaitingFor("\u57fa\u5ca9\u7834\u574f\u5668")) {
            GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, true);
            g.drawString(Minecraft.getInstance().font, "\u7ed1\u5b9a\u70ed\u952e\u4e2d. (Esc\u53d6\u6d88, Delete\u5220\u9664)", this.x + 5, this.y + (this.height - 8) / 2 - 4, 0xFFFF00);
            return;
        }
        boolean enabled = this.isEnabled();
        GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, enabled);
        String status = enabled ? "ON" : "OFF";
        String displayStr = "\u57fa\u5ca9\u7834\u574f\u5668: " + status;
        FeatureHotkeyManager.IHotkeyInterface hk = FeatureHotkeyManager.getInstance().getHotkey("\u57fa\u5ca9\u7834\u574f\u5668");
        if (hk.getHotkeyKey() >= 0) {
            displayStr = displayStr + " \u00a77[" + hk.getHotkeyName() + "]";
        }
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        g.drawString(Minecraft.getInstance().font, displayStr, this.x + 5, this.y + (this.height - 8) / 2 - 4, textColor);
        g.drawString(Minecraft.getInstance().font, ">>", this.x + this.width - 18, this.y + (this.height - 8) / 2 - 4, 0x888888);
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
            BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
            cfg.setEnabled(!cfg.enabled);
            if (!cfg.enabled) {
                BedrockBreakerManager.getInstance().stop();
            }
            return true;
        }
        if (button == 1) {
            if (HotkeySystem.isWaiting()) {
                return false;
            }
            Minecraft.getInstance().setScreen((Screen)new BedrockBreakerScreen());
            return true;
        }
        if (button == 2) {
            HotkeySystem.startBinding("\u57fa\u5ca9\u7834\u574f\u5668", () -> {});
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int k, int s, int m) {
        return false;
    }
}

