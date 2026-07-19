package fku.org.example.fku.features.criticals; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import fku.org.example.fku.client.gui.components.ToggleComponent;

/**
 * 刀刀暴击开关组件（左键开关，右键配置界面）
 */
public class CriticalsComponent extends ToggleComponent {

    @Override
    protected String getFeatureName() { return "刀刀暴击"; }

    public CriticalsComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "刀刀暴击");
    }

    @Override
    protected boolean isEnabled() { return CriticalsFeature.isEnabled(); }

    @Override
    protected void toggle() { CriticalsFeature.toggleEnabled(); }

    @Override
    protected void saveConfig() {}

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        if (renderHotkeyWait(g)) return;
        boolean en = isEnabled();
        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, en);
        String display = hotkeyAppend(label + ": " + (en ? "开" : "关"));
        int c = en ? GuiStyleConfig.getInstance().getTextColor() : 0xAAAAAA;
        g.drawString(Minecraft.getInstance().font, display, x + 5, y + (height - 8) / 2, c);
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (isHovered(mx, my)) {
            if (button == 0) { toggle(); saveConfig(); return true; }
            else if (button == 1) { Minecraft.getInstance().setScreen(new CriticalsConfigScreen()); return true; }
            else if (button == 2) { return handleMiddleClick(mx, my, button); }
        }
        return false;
    }
}
