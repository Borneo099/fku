package fku.org.example.fku.features.quickswitch;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class QuickSwitchComponent extends GuiComponent {

    public QuickSwitchComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "鬼手秒切");
        HotkeySystem.registerFeature("鬼手秒切", () -> QuickSwitchFeature.setEnabled(!QuickSwitchFeature.isEnabled()));
    }

    protected String getFeatureName() { return "鬼手秒切"; }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();

        if (HotkeySystem.isWaitingFor("鬼手秒切")) {
            GuiRenderHelper.drawComponentBackground(g, x, y, width, height, true);
            g.drawString(Minecraft.getInstance().font, "绑定热键中... (Esc取消)", x + 5, y + (height - 8) / 2 - 4, 0xFFFFFF00);
            return;
        }

        boolean enabled = QuickSwitchConfig.getInstance().enabled;
        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled);
        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();
        String modeLabel = switch (cfg.mode) { case "SMART" -> "智能"; case "CUSTOM" -> "自定义"; default -> "关闭"; };
        String status = enabled ? "ON [" + modeLabel + "]" : "OFF";
        var hk = FeatureHotkeyManager.getInstance().getHotkey("鬼手秒切");
        if (hk.getHotkeyKey() >= 0) status += " §7[" + hk.getHotkeyName() + "]";
        g.drawString(Minecraft.getInstance().font, "鬼手秒切: " + status, x + 5, y + (height - 8) / 2 - 4, enabled ? config.getTextColor() : 0xFFAAAAAA);
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0xFF888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!isHovered(mx, my)) return false;
        if (button == 0) {
            if (HotkeySystem.isWaiting()) return false;
            QuickSwitchFeature.setEnabled(!QuickSwitchFeature.isEnabled()); return true;
        } else if (button == 1) {
            if (HotkeySystem.isWaiting()) return false;
            Minecraft.getInstance().setScreen(new QuickSwitchConfigScreen()); return true;
        } else if (button == 2) {
            HotkeySystem.startBinding("鬼手秒切", () -> {});
            return true;
        }
        return false;
    }

    @Override public boolean keyPressed(int k, int s, int m) { return false; }
}
