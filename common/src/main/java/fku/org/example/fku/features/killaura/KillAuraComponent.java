package fku.org.example.fku.features.killaura;

import fku.org.example.fku.client.gui.components.ToggleComponent;
import fku.org.example.fku.features.killaura.KillAuraConfig;
import fku.org.example.fku.features.killaura.KillAuraScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class KillAuraComponent
extends ToggleComponent {
    public KillAuraComponent(int x, int y, int w, int h) {
        super(x, y, w, h, "\u6740\u622e\u5149\u73af");
    }

    @Override
    protected boolean isEnabled() {
        return KillAuraConfig.getInstance().enabled;
    }

    @Override
    protected void toggle() {
        KillAuraConfig c = KillAuraConfig.getInstance();
        c.setEnabled(!c.enabled);
    }

    @Override
    protected void saveConfig() {
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!this.visible || this.currentAlpha <= 0.01f) {
            return;
        }
        super.render(g, mx, my, pt);
        g.drawString(Minecraft.getInstance().font, ">>", this.x + this.width - 14, this.y + (this.height - 8) / 2, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (!this.isHovered(mx, my)) {
            return false;
        }
        if (btn == 0) {
            if (this.HotkeyWatcher()) {
                return true;
            }
            this.toggle();
            return true;
        }
        if (btn == 1) {
            Minecraft.getInstance().setScreen((Screen)new KillAuraScreen());
            return true;
        }
        if (btn == 2) {
            return this.handleMiddleClick(mx, my, btn);
        }
        return false;
    }

    private boolean HotkeyWatcher() {
        return this.listeningForKey;
    }

    @Override
    public String getFeatureName() {
        return "\u6740\u622e\u5149\u73af";
    }
}

