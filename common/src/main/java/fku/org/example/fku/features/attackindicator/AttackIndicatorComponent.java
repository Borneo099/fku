package fku.org.example.fku.features.attackindicator;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ToggleComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.attackindicator.AttackIndicatorConfig;
import fku.org.example.fku.features.attackindicator.AttackIndicatorConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class AttackIndicatorComponent
extends ToggleComponent {
    @Override
    protected String getFeatureName() {
        return "\u653b\u51fb\u6307\u793a\u5668";
    }

    public AttackIndicatorComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u653b\u51fb\u6307\u793a\u5668");
    }

    @Override
    protected boolean isEnabled() {
        return AttackIndicatorConfig.getInstance().enabled;
    }

    @Override
    protected void toggle() {
        AttackIndicatorConfig cfg = AttackIndicatorConfig.getInstance();
        cfg.enabled = !cfg.enabled;
    }

    @Override
    protected void saveConfig() {
        AttackIndicatorConfig.save();
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
        boolean enabled = this.isEnabled();
        GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, enabled);
        String displayStr = this.hotkeyAppend("\u653b\u51fb\u6307\u793a: " + (enabled ? "\u5f00" : "\u5173"));
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        g.drawString(Minecraft.getInstance().font, displayStr, this.x + 5, this.y + (this.height - 8) / 2, textColor);
        g.drawString(Minecraft.getInstance().font, ">>", this.x + this.width - 18, this.y + (this.height - 8) / 2, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovered(mouseX, mouseY)) {
            if (button == 0) {
                this.toggle();
                this.saveConfig();
                return true;
            }
            if (button == 1) {
                Minecraft.getInstance().setScreen(new AttackIndicatorConfigScreen());
                return true;
            }
            if (button == 2) {
                return this.handleMiddleClick(mouseX, mouseY, button);
            }
        }
        return false;
    }
}

