package fku.org.example.fku.features.fakeplayer;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.fakeplayer.FakePlayerConfig;
import fku.org.example.fku.features.fakeplayer.FakePlayerFeature;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class FakePlayerComponent
extends GuiComponent {
    public FakePlayerComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u5047\u4eba");
        HotkeySystem.registerFeature("\u5047\u4eba", () -> FakePlayerFeature.toggle());
    }

    protected String getFeatureName() {
        return "\u5047\u4eba";
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!this.visible) {
            return;
        }
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        if (HotkeySystem.isWaitingFor("\u5047\u4eba")) {
            GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, true);
            g.drawString(Minecraft.getInstance().font, "\u7ed1\u5b9a\u70ed\u952e\u4e2d. (Esc\u53d6\u6d88)", this.x + 5, this.y + (this.height - 8) / 2 - 4, 0xFFFF00);
            return;
        }
        boolean enabled = FakePlayerConfig.getInstance().enabled;
        GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, enabled);
        String displayStr = "\u5047\u4eba: " + (enabled ? "ON" : "OFF");
        FeatureHotkeyManager.IHotkeyInterface hk = FeatureHotkeyManager.getInstance().getHotkey("\u5047\u4eba");
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
            FakePlayerFeature.toggle();
            return true;
        }
        if (button == 2) {
            HotkeySystem.startBinding("\u5047\u4eba", () -> {});
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int k, int s, int m) {
        return false;
    }
}

