package fku.org.example.fku.features.duplicator;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.duplicator.DuplicatorConfigScreen;
import fku.org.example.fku.features.duplicator.DuplicatorFeature;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class DuplicatorComponent
extends GuiComponent {
    public DuplicatorComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u4e09\u53c9\u621f\u590d\u5236");
        HotkeySystem.registerFeature("\u4e09\u53c9\u621f\u590d\u5236", () -> DuplicatorFeature.toggle());
    }

    protected String getFeatureName() {
        return "\u4e09\u53c9\u621f\u590d\u5236";
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!this.visible) {
            return;
        }
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        if (HotkeySystem.isWaitingFor("\u4e09\u53c9\u621f\u590d\u5236")) {
            GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, true);
            g.drawString(Minecraft.getInstance().font, "\u7ed1\u5b9a\u70ed\u952e\u4e2d. (Esc\u53d6\u6d88)", this.x + 5, this.y + (this.height - 8) / 2 - 4, 0xFFFF00);
            return;
        }
        boolean enabled = DuplicatorFeature.isEnabled();
        GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, enabled);
        String status = enabled ? "ON" : "OFF";
        FeatureHotkeyManager.IHotkeyInterface hk = FeatureHotkeyManager.getInstance().getHotkey("\u4e09\u53c9\u621f\u590d\u5236");
        String hkStr = hk.getHotkeyKey() >= 0 ? " \u00a77[" + hk.getHotkeyName() + "]" : "";
        g.drawString(Minecraft.getInstance().font, "\u4e09\u53c9\u621f\u590d\u5236: " + status + hkStr, this.x + 5, this.y + (this.height - 8) / 2 - 4, enabled ? config.getTextColor() : 0xAAAAAA);
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
            DuplicatorFeature.toggle();
            return true;
        }
        if (button == 1) {
            if (HotkeySystem.isWaiting()) {
                return false;
            }
            Minecraft.getInstance().setScreen((Screen)new DuplicatorConfigScreen());
            return true;
        }
        if (button == 2) {
            HotkeySystem.startBinding("\u4e09\u53c9\u621f\u590d\u5236", () -> {});
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}

