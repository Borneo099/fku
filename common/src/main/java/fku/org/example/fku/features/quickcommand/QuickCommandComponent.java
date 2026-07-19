package fku.org.example.fku.features.quickcommand; /* water */

import fku.org.example.fku.client.gui.components.ToggleComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class QuickCommandComponent extends ToggleComponent {
    public QuickCommandComponent(int x, int y, int w, int h) { super(x, y, w, h, "快捷指令"); }
    @Override protected boolean isEnabled() { return QuickCommandConfig.getInstance().enabled; }
    @Override protected void toggle() { var c = QuickCommandConfig.getInstance(); c.setEnabled(!c.enabled); }
    @Override protected void saveConfig() {}

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible || currentAlpha <= 0.01f) return;
        super.render(g, mx, my, pt);
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 14, y + (height - 8) / 2, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (!isHovered(mx, my)) return false;
        if (btn == 0) { if (listeningForKey) return false; toggle(); return true; }
        if (btn == 1) { Minecraft.getInstance().setScreen(new QuickCommandScreen()); return true; }
        if (btn == 2) return handleMiddleClick(mx, my, btn);
        return false;
    }
    @Override public String getFeatureName() { return "快捷指令"; }
}
