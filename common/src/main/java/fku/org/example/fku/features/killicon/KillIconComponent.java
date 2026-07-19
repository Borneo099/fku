package fku.org.example.fku.features.killicon; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import fku.org.example.fku.client.gui.components.ToggleComponent;

/**
 * 击杀图标开关组件 — 左键开关，右键打开配置
 */
public class KillIconComponent extends ToggleComponent {

    @Override
    protected String getFeatureName() { return "击杀图标"; }

    public KillIconComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "击杀图标");
    }

    @Override
    protected boolean isEnabled() { return KillIconConfig.getInstance().enabled; }

    @Override
    protected void toggle() {
        KillIconConfig cfg = KillIconConfig.getInstance();
        cfg.enabled = !cfg.enabled;
    }

    @Override
    protected void saveConfig() { KillIconConfig.save(); }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        if (renderHotkeyWait(g)) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();
        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled);
        String displayStr = hotkeyAppend(label + ": " + (enabled ? "开" : "关"));
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        g.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2, textColor);
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) { toggle(); saveConfig(); return true; }
            else if (button == 1) { Minecraft.getInstance().setScreen(new KillIconScreen()); return true; }
            else if (button == 2) { return handleMiddleClick(mouseX, mouseY, button); }
        }
        return false;
    }
}
