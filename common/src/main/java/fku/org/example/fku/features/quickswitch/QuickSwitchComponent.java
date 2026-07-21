package fku.org.example.fku.features.quickswitch;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.quickswitch.QuickSwitchConfig;
import fku.org.example.fku.features.quickswitch.QuickSwitchConfigScreen;
import fku.org.example.fku.features.quickswitch.QuickSwitchFeature;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class QuickSwitchComponent
extends GuiComponent {
    public QuickSwitchComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u9b3c\u624b\u79d2\u5207");
        HotkeySystem.registerFeature("\u9b3c\u624b\u79d2\u5207", () -> QuickSwitchFeature.setEnabled(!QuickSwitchFeature.isEnabled()));
    }

    protected String getFeatureName() {
        return "\u9b3c\u624b\u79d2\u5207";
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!this.visible) {
            return;
        }
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        if (HotkeySystem.isWaitingFor("\u9b3c\u624b\u79d2\u5207")) {
            GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, true);
            g.drawString(Minecraft.getInstance().font, "\u7ed1\u5b9a\u70ed\u952e\u4e2d. (Esc\u53d6\u6d88)", this.x + 5, this.y + (this.height - 8) / 2 - 4, 0xFFFF00);
            return;
        }
        boolean enabled = QuickSwitchConfig.getInstance().enabled;
        GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, enabled);
        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();
        String modeLabel = switch (cfg.mode) {
            case "SMART" -> "\u667a\u80fd";
            case "CUSTOM" -> "\u81ea\u5b9a\u4e49";
            default -> "\u5173\u95ed";
        };
        Object status = enabled ? "ON [" + modeLabel + "]" : "OFF";
        FeatureHotkeyManager.IHotkeyInterface hk = FeatureHotkeyManager.getInstance().getHotkey("\u9b3c\u624b\u79d2\u5207");
        if (hk.getHotkeyKey() >= 0) {
            status = (String)status + " \u00a77[" + hk.getHotkeyName() + "]";
        }
        g.drawString(Minecraft.getInstance().font, "\u9b3c\u624b\u79d2\u5207: " + (String)status, this.x + 5, this.y + (this.height - 8) / 2 - 4, enabled ? config.getTextColor() : 0xAAAAAA);
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
            QuickSwitchFeature.setEnabled(!QuickSwitchFeature.isEnabled());
            return true;
        }
        if (button == 1) {
            if (HotkeySystem.isWaiting()) {
                return false;
            }
            Minecraft.getInstance().setScreen((Screen)new QuickSwitchConfigScreen());
            return true;
        }
        if (button == 2) {
            HotkeySystem.startBinding("\u9b3c\u624b\u79d2\u5207", () -> {});
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int k, int s, int m) {
        return false;
    }
}

