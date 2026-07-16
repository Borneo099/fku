package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerConfig;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerManager;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerScreen;
import fku.org.example.fku.client.KeyBindings;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class BedrockBreakerComponent extends GuiComponent {

    protected String getFeatureName() { return "基岩破坏器"; }

    public BedrockBreakerComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "基岩破坏器");
        // ★ 热键触发状态机（非开关功能），与 BedrockBreakerScreen 配置的热键行为一致
        HotkeySystem.registerFeature("基岩破坏器", () -> BedrockBreakerManager.getInstance().process());
    }

    private boolean isEnabled() {
        return BedrockBreakerConfig.getInstance().enabled;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();

        if (HotkeySystem.isWaitingFor("基岩破坏器")) {
            GuiRenderHelper.drawComponentBackground(g, x, y, width, height, true);
            g.drawString(Minecraft.getInstance().font, "绑定热键中... (Esc取消, Delete删除)",
                    x + 5, y + (height - 8) / 2 - 4, 0xFFFFFF00);
            return;
        }

        boolean enabled = isEnabled();
        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled);

        String status = enabled ? "ON" : "OFF";
        String displayStr = "基岩破坏器: " + status;
        var hk = FeatureHotkeyManager.getInstance().getHotkey("基岩破坏器");
        if (hk.getHotkeyKey() >= 0) displayStr += " §7[" + hk.getHotkeyName() + "]";
        int textColor = enabled ? config.getTextColor() : 0xFFAAAAAA;
        g.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2 - 4, textColor);
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0xFF888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!isHovered(mx, my)) return false;
        if (button == 0) {
            if (HotkeySystem.isWaiting()) return false;
            BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
            cfg.setEnabled(!cfg.enabled);
            if (!cfg.enabled) BedrockBreakerManager.getInstance().stop();
            return true;
        } else if (button == 1) {
            if (HotkeySystem.isWaiting()) return false;
            Minecraft.getInstance().setScreen(new BedrockBreakerScreen());
            return true;
        } else if (button == 2) {
            HotkeySystem.startBinding("基岩破坏器", () -> {});
            return true;
        }
        return false;
    }

    @Override public boolean keyPressed(int k, int s, int m) { return false; }
}
