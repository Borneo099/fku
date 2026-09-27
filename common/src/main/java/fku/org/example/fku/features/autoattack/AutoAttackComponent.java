package fku.org.example.fku.features.autoattack; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 自动攻击 UI 组件 — 战斗面板中显示
 * 左键开关 / 右键配置 / 中键热键
 */
public class AutoAttackComponent extends GuiComponent {

    public AutoAttackComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "自动攻击");
        HotkeySystem.registerFeature("自动攻击", () -> AutoAttackFeature.setEnabled(!AutoAttackFeature.isEnabled()));
    }

    protected String getFeatureName() { return "自动攻击"; }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();

        if (HotkeySystem.isWaitingFor("自动攻击")) {
            GuiRenderHelper.drawComponentBackground(g, x, y, width, height, true);
            g.drawString(Minecraft.getInstance().font, "绑定热键中... (Esc取消)", x + 5, y + (height - 8) / 2 - 4, 0xFFFF00);
            return;
        }

        boolean enabled = AutoAttackFeature.isEnabled();
        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled);
        String displayStr = "自动攻击: " + (enabled ? "开" : "关");
        var hk = FeatureHotkeyManager.getInstance().getHotkey("自动攻击");
        if (hk.getHotkeyKey() >= 0) displayStr += " §7[" + hk.getHotkeyName() + "]";
        g.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2 - 4, enabled ? config.getTextColor() : 0xAAAAAA);
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!isHovered(mx, my)) return false;
        if (button == 0) {
            if (HotkeySystem.isWaiting()) return false;
            AutoAttackFeature.setEnabled(!AutoAttackFeature.isEnabled()); return true;
        } else if (button == 1) {
            if (HotkeySystem.isWaiting()) return false;
            Minecraft.getInstance().setScreen(new AutoAttackScreen()); return true;
        } else if (button == 2) {
            HotkeySystem.startBinding("自动攻击", () -> {});
            return true;
        }
        return false;
    }

    @Override public boolean keyPressed(int k, int s, int m) { return false; }
}
