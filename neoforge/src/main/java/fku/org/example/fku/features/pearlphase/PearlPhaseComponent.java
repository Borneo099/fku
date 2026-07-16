package fku.org.example.fku.features.pearlphase;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class PearlPhaseComponent extends GuiComponent {

    public PearlPhaseComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "珍珠卡墙");
        HotkeySystem.registerFeature("珍珠卡墙", () -> {
            var c = PearlPhaseConfig.getInstance(); c.setEnabled(!c.enabled);
        });
    }

    protected String getFeatureName() { return "珍珠卡墙"; }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();

        if (HotkeySystem.isWaitingFor("珍珠卡墙")) {
            GuiRenderHelper.drawComponentBackground(g, x, y, width, height, true);
            g.drawString(Minecraft.getInstance().font, "绑定热键中... (Esc取消)", x + 5, y + (height - 8) / 2 - 4, 0xFFFFFF00);
            return;
        }

        boolean enabled = PearlPhaseConfig.getInstance().enabled;
        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled);
        String status = enabled ? "ON" : "OFF";
        var hk = FeatureHotkeyManager.getInstance().getHotkey("珍珠卡墙");
        String hkStr = hk.getHotkeyKey() >= 0 ? " §7[" + hk.getHotkeyName() + "]" : "";
        g.drawString(Minecraft.getInstance().font, "珍珠卡墙: " + status + hkStr, x + 5, y + (height - 8) / 2 - 4, enabled ? config.getTextColor() : 0xFFAAAAAA);
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0xFF888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!isHovered(mx, my)) return false;
        if (button == 0) {
            if (HotkeySystem.isWaiting()) return false;
            var c = PearlPhaseConfig.getInstance(); c.setEnabled(!c.enabled); return true;
        } else if (button == 1) {
            if (HotkeySystem.isWaiting()) return false;
            Minecraft.getInstance().setScreen(new PearlPhaseConfigScreen()); return true;
        } else if (button == 2) {
            HotkeySystem.startBinding("珍珠卡墙", () -> {});
            return true;
        }
        return false;
    }

    @Override public boolean keyPressed(int k, int s, int m) { return false; }
}
