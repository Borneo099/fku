package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiPanel;
import fku.org.example.fku.client.gui.components.ToggleComponent;
import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.arrowdmg.ArrowDmgComponent;
import fku.org.example.fku.features.criticals.CriticalsComponent;
import fku.org.example.fku.features.killaura.KillAuraComponent;
import fku.org.example.fku.features.knockback.KnockbackConfig;
import fku.org.example.fku.features.knockback.KnockbackConfigScreen;
import fku.org.example.fku.features.quickswitch.QuickSwitchComponent;
import fku.org.example.fku.features.tpaura.TpAuraComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class CombatPanel
extends GuiPanel {
    public CombatPanel() {
        super("\u6218\u6597", (Integer)FkuConfig.combatPanelX.get(), (Integer)FkuConfig.combatPanelY.get(), 120, 120);
    }

    @Override
    protected void init() {
        this.addComponent(new ToggleComponent(0, 0, 110, 20, "\u51fb\u9000\u65b9\u5411"){

            @Override
            protected boolean isEnabled() {
                return KnockbackConfig.getInstance().enabled;
            }

            @Override
            protected void toggle() {
                KnockbackConfig cfg = KnockbackConfig.getInstance();
                cfg.setEnabled(!cfg.enabled);
            }

            @Override
            protected void saveConfig() {
                KnockbackConfig.save();
            }

            @Override
            public void render(GuiGraphics g, int mx, int my, float pt) {
                if (!this.visible) {
                    return;
                }
                GuiStyleConfig config = GuiStyleConfig.getInstance();
                boolean enabled = this.isEnabled();
                GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, enabled);
                String displayStr = this.label + ": " + (enabled ? "ON" : "OFF");
                int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
                g.drawString(Minecraft.getInstance().font, displayStr, this.x + 5, this.y + (this.height - 8) / 2, textColor);
                g.drawString(Minecraft.getInstance().font, ">>", this.x + this.width - 18, this.y + (this.height - 8) / 2, 0x888888);
            }

            @Override
            public boolean mouseClicked(double mx, double my, int button) {
                if (!this.isHovered(mx, my)) {
                    return false;
                }
                if (button == 0) {
                    this.toggle();
                    this.saveConfig();
                    return true;
                }
                if (button == 1) {
                    Minecraft.getInstance().setScreen((Screen)new KnockbackConfigScreen());
                    return true;
                }
                return false;
            }
        });
        this.addComponent(new TpAuraComponent(0, 0, 110, 25));
        this.addComponent(new QuickSwitchComponent(0, 0, 110, 20));
        this.addComponent(new ArrowDmgComponent(0, 0, 110, 25));
        this.addComponent(new KillAuraComponent(0, 0, 110, 22));
        this.addComponent(new CriticalsComponent(0, 0, 110, 22));
    }

    @Override
    protected void savePosition() {
        FkuConfig.combatPanelX.set(this.x);
        FkuConfig.combatPanelY.set(this.y);
    }
}

