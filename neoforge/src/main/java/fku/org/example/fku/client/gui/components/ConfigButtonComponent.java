package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class ConfigButtonComponent extends GuiComponent {
    
    private final String label;
    private final Runnable onClick;

    protected String getFeatureName() { return null; }

    public ConfigButtonComponent(int x, int y, int width, int height, String label, Runnable onClick) {
        super(x, y, width, height, label);
        this.label = label;
        this.onClick = onClick;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();

        String fn = getFeatureName();
        boolean waiting = fn != null && HotkeySystem.isWaitingFor(fn);

        if (waiting) {
            int bgColor = config.getPrimaryColorWithAlpha(180);
            GuiRenderHelper.drawRoundedRect(g, x, y, width, height, bgColor, Math.max(2, config.cornerRadius / 2));
            g.drawString(Minecraft.getInstance().font, "绑定热键中... (Esc取消)",
                    x + 5, y + (height - 8) / 2, 0xFFFFFF00);
            return;
        }

        int bgColor = config.getPrimaryColorWithAlpha(180);
        GuiRenderHelper.drawRoundedRect(g, x, y, width, height, bgColor, Math.max(2, config.cornerRadius / 2));
        int borderColor = config.getPrimaryColor() | (255 << 24);
        GuiRenderHelper.drawRoundedOutline(g, x, y, width, height, borderColor, Math.max(2, config.cornerRadius / 2), 1);

        String display = label;
        if (fn != null) {
            var hk = FeatureHotkeyManager.getInstance().getHotkey(fn);
            if (hk.getHotkeyKey() >= 0) display += " §7[" + hk.getHotkeyName() + "]";
        }
        g.drawString(Minecraft.getInstance().font, display, x + 5, y + (height - 8) / 2, config.getTextColor());
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2, 0xFF888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!isHovered(mx, my)) return false;
        if (button == 0) {
            if (HotkeySystem.isWaiting()) return false;
            onClick.run();
            return true;
        } else if (button == 2) {
            String fn = getFeatureName();
            if (fn != null) HotkeySystem.startBinding(fn, () -> {});
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}
