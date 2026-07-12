package fku.org.example.fku.features.antipush; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import fku.org.example.fku.client.gui.components.ToggleComponent;

/**
 * 防推开关组件
 */
public class AntiPushComponent extends ToggleComponent {

    @Override
    protected String getFeatureName() { return "防推"; }

    public AntiPushComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "防推");
    }

    @Override protected boolean isEnabled() { return AntiPushFeature.isEnabled(); }
    @Override protected void toggle() { AntiPushFeature.toggleEnabled(); }
    @Override protected void saveConfig() {}

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        if (renderHotkeyWait(g)) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean en = isEnabled();
        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, en);
        String display = hotkeyAppend(label + ": " + (en ? "开" : "关"));
        g.drawString(Minecraft.getInstance().font, display, x + 5, y + (height - 8) / 2, en ? config.getTextColor() : 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (isHovered(mx, my)) {
            if (btn == 0) { toggle(); saveConfig(); return true; }
            else if (btn == 2) return handleMiddleClick(mx, my, btn);
        }
        return false;
    }
}
