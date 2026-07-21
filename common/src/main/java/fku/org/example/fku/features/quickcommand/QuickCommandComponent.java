package fku.org.example.fku.features.quickcommand;

import fku.org.example.fku.client.gui.components.ToggleComponent;
import fku.org.example.fku.features.quickcommand.QuickCommandConfig;
import fku.org.example.fku.features.quickcommand.QuickCommandScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class QuickCommandComponent
extends ToggleComponent {
    public QuickCommandComponent(int x, int y, int w, int h) {
        super(x, y, w, h, "\u5feb\u6377\u6307\u4ee4");
    }

    @Override
    protected boolean isEnabled() {
        return QuickCommandConfig.getInstance().enabled;
    }

    @Override
    protected void toggle() {
        QuickCommandConfig c = QuickCommandConfig.getInstance();
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
            if (this.listeningForKey) {
                return false;
            }
            this.toggle();
            return true;
        }
        if (btn == 1) {
            Minecraft.getInstance().setScreen((Screen)new QuickCommandScreen());
            return true;
        }
        if (btn == 2) {
            return this.handleMiddleClick(mx, my, btn);
        }
        return false;
    }

    @Override
    public String getFeatureName() {
        return "\u5feb\u6377\u6307\u4ee4";
    }
}

