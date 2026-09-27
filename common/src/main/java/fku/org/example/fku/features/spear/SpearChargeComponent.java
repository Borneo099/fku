package fku.org.example.fku.features.spear; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 矛之冲锋 UI 组件 — 战斗面板中显示
 * 左键开关 / 右键配置 / 中键热键（移植自 SpearExploit）
 */
public class SpearChargeComponent extends GuiComponent {

    public SpearChargeComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "矛之冲锋");
        HotkeySystem.registerFeature("矛之冲锋", () -> SpearChargeFeature.setEnabled(!SpearChargeFeature.isEnabled()));
    }

    protected String getFeatureName() { return "矛之冲锋"; }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();

        if (HotkeySystem.isWaitingFor("矛之冲锋")) {
            GuiRenderHelper.drawComponentBackground(g, x, y, width, height, true);
            g.drawString(Minecraft.getInstance().font, "绑定热键中... (Esc取消)", x + 5, y + (height - 8) / 2 - 4, 0xFFFF00);
            return;
        }

        boolean enabled = SpearChargeFeature.isEnabled();
        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled);
        String displayStr = "矛之冲锋: " + (enabled ? "开" : "关");
        var hk = FeatureHotkeyManager.getInstance().getHotkey("矛之冲锋");
        if (hk.getHotkeyKey() >= 0) displayStr += " §7[" + hk.getHotkeyName() + "]";
        g.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2 - 4, enabled ? config.getTextColor() : 0xAAAAAA);
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!isHovered(mx, my)) return false;
        if (button == 0) {
            if (HotkeySystem.isWaiting()) return false;
            SpearChargeFeature.setEnabled(!SpearChargeFeature.isEnabled()); return true;
        } else if (button == 1) {
            if (HotkeySystem.isWaiting()) return false;
            Minecraft.getInstance().setScreen(new SpearChargeScreen()); return true;
        } else if (button == 2) {
            HotkeySystem.startBinding("矛之冲锋", () -> {});
            return true;
        }
        return false;
    }

    @Override public boolean keyPressed(int k, int s, int m) { return false; }
}
