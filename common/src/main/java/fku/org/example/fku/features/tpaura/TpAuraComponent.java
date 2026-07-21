package fku.org.example.fku.features.tpaura;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.tpaura.TpAuraFeature;
import fku.org.example.fku.features.tpaura.TpAuraScreen;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class TpAuraComponent
extends GuiComponent {
    public TpAuraComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u5982\u6765\u795e\u638c");
        HotkeySystem.registerFeature("\u5982\u6765\u795e\u638c", () -> TpAuraFeature.setEnabled(!TpAuraFeature.isEnabled()));
    }

    protected String getFeatureName() {
        return "\u5982\u6765\u795e\u638c";
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!this.visible) {
            return;
        }
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        if (HotkeySystem.isWaitingFor("\u5982\u6765\u795e\u638c")) {
            GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, true);
            g.drawString(Minecraft.getInstance().font, "\u7ed1\u5b9a\u70ed\u952e\u4e2d. (Esc\u53d6\u6d88)", this.x + 5, this.y + (this.height - 8) / 2 - 4, 0xFFFF00);
            return;
        }
        boolean enabled = TpAuraFeature.isEnabled();
        GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, enabled);
        String displayStr = "\u5982\u6765\u795e\u638c: " + (enabled ? "ON" : "OFF");
        FeatureHotkeyManager.IHotkeyInterface hk = FeatureHotkeyManager.getInstance().getHotkey("\u5982\u6765\u795e\u638c");
        if (hk.getHotkeyKey() >= 0) {
            displayStr = displayStr + " \u00a77[" + hk.getHotkeyName() + "]";
        }
        g.drawString(Minecraft.getInstance().font, displayStr, this.x + 5, this.y + (this.height - 8) / 2 - 4, enabled ? config.getTextColor() : 0xAAAAAA);
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
            TpAuraFeature.setEnabled(!TpAuraFeature.isEnabled());
            return true;
        }
        if (button == 1) {
            if (HotkeySystem.isWaiting()) {
                return false;
            }
            Minecraft.getInstance().setScreen((Screen)new TpAuraScreen());
            return true;
        }
        if (button == 2) {
            HotkeySystem.startBinding("\u5982\u6765\u795e\u638c", () -> {});
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int k, int s, int m) {
        return false;
    }
}

