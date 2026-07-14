package fku.org.example.fku.features.selfdamage;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class SelfDamageComponent extends GuiComponent {

    public SelfDamageComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "自伤");
        HotkeySystem.registerFeature("自伤", () -> SelfDamageFeature.applyDamage());
    }

    protected String getFeatureName() { return "自伤"; }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();

        if (HotkeySystem.isWaitingFor("自伤")) {
            GuiRenderHelper.drawComponentBackground(g, x, y, width, height, true);
            g.drawString(Minecraft.getInstance().font, "绑定热键中... (Esc取消)", x + 5, y + (height - 8) / 2 - 4, 0xFFFF00);
            return;
        }

        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, true);
        var cfg = SelfDamageConfig.getInstance();
        String display = "自伤: " + cfg.damageAmount + "❤";
        var hk = FeatureHotkeyManager.getInstance().getHotkey("自伤");
        if (hk.getHotkeyKey() >= 0) display += " §7[" + hk.getHotkeyName() + "]";
        g.drawString(Minecraft.getInstance().font, display, x + 5, y + (height - 8) / 2 - 4, config.getTextColor());
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!isHovered(mx, my)) return false;
        if (button == 0) {
            if (HotkeySystem.isWaiting()) return false;
            SelfDamageFeature.applyDamage(); return true;
        } else if (button == 1) {
            if (HotkeySystem.isWaiting()) return false;
            Minecraft.getInstance().setScreen(new SelfDamageScreen()); return true;
        } else if (button == 2) {
            HotkeySystem.startBinding("自伤", () -> {});
            return true;
        }
        return false;
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
}
