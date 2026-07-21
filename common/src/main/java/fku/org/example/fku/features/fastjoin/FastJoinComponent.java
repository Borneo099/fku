package fku.org.example.fku.features.fastjoin;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ToggleComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.fastjoin.FastJoinConfigScreen;
import fku.org.example.fku.features.fastjoin.FastJoinFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class FastJoinComponent
extends ToggleComponent {
    @Override
    protected String getFeatureName() {
        return "\u5feb\u901f\u52a0\u5165";
    }

    public FastJoinComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u5feb\u901f\u52a0\u8f7d");
    }

    @Override
    protected boolean isEnabled() {
        return FastJoinFeature.isEnabled();
    }

    @Override
    protected void toggle() {
        FastJoinFeature.toggleEnabled();
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
        g.drawString(Minecraft.getInstance().font, display, this.x + 5, this.y + (this.height - 8) / 2 - 4, en ? config.getTextColor() : 0xAAAAAA);
        g.drawString(Minecraft.getInstance().font, ">>", this.x + this.width - 18, this.y + (this.height - 8) / 2 - 4, 0x888888);
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
                Minecraft.getInstance().setScreen((Screen)new FastJoinConfigScreen());
                return true;
            }
            if (btn == 2) {
                return this.handleMiddleClick(mx, my, btn);
            }
        }
        return false;
    }
}

