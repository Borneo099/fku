package fku.org.example.fku.features.arrowdmg;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ToggleComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.arrowdmg.ArrowDmgConfigScreen;
import fku.org.example.fku.features.arrowdmg.ArrowDmgFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class ArrowDmgComponent
extends ToggleComponent {
    @Override
    protected String getFeatureName() {
        return "32k\u5f13";
    }

    public ArrowDmgComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "32k\u5f13");
    }

    @Override
    protected boolean isEnabled() {
        return ArrowDmgFeature.isEnabled();
    }

    @Override
    protected void toggle() {
        ArrowDmgFeature.toggleEnabled();
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
        int c = en ? config.getTextColor() : 0xAAAAAA;
        g.drawString(Minecraft.getInstance().font, display, this.x + 5, this.y + (this.height - 8) / 2, c);
        g.drawString(Minecraft.getInstance().font, ">>", this.x + this.width - 18, this.y + (this.height - 8) / 2, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (this.isHovered(mx, my)) {
            if (btn == 0) {
                this.toggle();
                this.saveConfig();
                return true;
            }
            if (btn == 1) {
                Minecraft.getInstance().setScreen((Screen)new ArrowDmgConfigScreen());
                return true;
            }
            if (btn == 2) {
                return this.handleMiddleClick(mx, my, btn);
            }
        }
        return false;
    }
}

