package fku.org.example.fku.features.fakeplayer;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class FakePlayerComponent extends GuiComponent {

    public FakePlayerComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "假人");
        HotkeySystem.registerFeature("假人", () -> FakePlayerFeature.toggle());
    }

    protected String getFeatureName() { return "假人"; }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();

        if (HotkeySystem.isWaitingFor("假人")) {
            GuiRenderHelper.drawComponentBackground(g, x, y, width, height, true);
            g.drawString(Minecraft.getInstance().font, "绑定热键中... (Esc取消)", x + 5, y + (height - 8) / 2 - 4, 0xFFFFFF00);
            return;
        }

        boolean enabled = FakePlayerConfig.getInstance().enabled;
        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled);
        String displayStr = "假人: " + (enabled ? "ON" : "OFF");
        var hk = FeatureHotkeyManager.getInstance().getHotkey("假人");
        if (hk.getHotkeyKey() >= 0) displayStr += " §7[" + hk.getHotkeyName() + "]";
        g.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2 - 4, enabled ? config.getTextColor() : 0xFFAAAAAA);
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0xFF888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!isHovered(mx, my)) return false;
        if (button == 0) {
            if (HotkeySystem.isWaiting()) return false;
            FakePlayerFeature.toggle(); return true;
        } else if (button == 2) {
            HotkeySystem.startBinding("假人", () -> {});
            return true;
        }
        return false;
    }

    @Override public boolean keyPressed(int k, int s, int m) { return false; }
}
