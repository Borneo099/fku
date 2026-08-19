package fku.org.example.fku.features.antiknockback; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import fku.org.example.fku.client.gui.components.ToggleComponent;

/**
 * 防击退开关组件
 */
public class AntiKnockbackComponent extends ToggleComponent {

    @Override
    protected String getFeatureName() { return "防击退"; }

    public AntiKnockbackComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "防击退");
    }

    @Override protected boolean isEnabled() { return AntiKnockbackFeature.isEnabled(); }
    @Override protected void toggle() { AntiKnockbackFeature.toggleEnabled(); }
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
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (isHovered(mx, my)) {
            if (btn == 0) { toggle(); saveConfig(); return true; }
            else if (btn == 1) { Minecraft.getInstance().setScreen(new AntiKnockbackConfigScreen()); return true; }
            else if (btn == 2) return handleMiddleClick(mx, my, btn);
        }
        return false;
    }
}
