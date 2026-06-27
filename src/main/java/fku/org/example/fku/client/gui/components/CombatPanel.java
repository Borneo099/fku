package fku.org.example.fku.client.gui.components; /* water */

import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.features.knockback.KnockbackConfig;
import fku.org.example.fku.features.knockback.KnockbackConfigScreen;
import fku.org.example.fku.features.tpaura.TpAuraComponent;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 战斗面板
 * 仅保留自由击退方向的总开关，右键打开详细配置界面。
 * 所有模式选择、参数调节已迁移至 KnockbackConfigScreen。
 */
public class CombatPanel extends GuiPanel {

    public CombatPanel() {
        super("战斗", FkuConfig.combatPanelX.get(), FkuConfig.combatPanelY.get(), 130, 90);
    }

    @Override
    protected void init() {
        // ★ 击退方向功能总开关（左键开关，右键配置界面）
        addComponent(new ToggleComponent(0, 0, 110, 20, "击退方向") {
            @Override protected boolean isEnabled() { return KnockbackConfig.getInstance().enabled; }
            @Override protected void toggle() { KnockbackConfig cfg = KnockbackConfig.getInstance(); cfg.setEnabled(!cfg.enabled); }
            @Override protected void saveConfig() { KnockbackConfig.save(); }

            @Override
            public void render(GuiGraphics g, int mx, int my, float pt) {
                if (!visible) return;
                GuiStyleConfig config = GuiStyleConfig.getInstance();
                boolean enabled = isEnabled();
                GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled);
                String displayStr = label + ": " + (enabled ? "ON" : "OFF");
                int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
                g.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2, textColor);
                // ★ 右键打开配置提示
                g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2, 0x888888);
            }

            @Override
            public boolean mouseClicked(double mx, double my, int button) {
                if (!isHovered(mx, my)) return false;
                if (button == 0) {
                    toggle();
                    saveConfig();
                    return true;
                } else if (button == 1) {
                    Minecraft.getInstance().setScreen(new KnockbackConfigScreen());
                    return true;
                }
                return false;
            }
        });

        // ★ 如来神掌功能开关（左键开关，右键配置界面）
        addComponent(new TpAuraComponent(0, 0, 110, 25));
    }

    @Override
    protected void savePosition() {
        FkuConfig.combatPanelX.set(this.x);
        FkuConfig.combatPanelY.set(this.y);
    }
}