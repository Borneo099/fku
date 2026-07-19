package fku.org.example.fku.features.killaura; /* water */

import fku.org.example.fku.client.gui.components.ToggleComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class KillAuraComponent extends ToggleComponent {
    public KillAuraComponent(int x, int y, int w, int h) { super(x, y, w, h, "杀戮光环"); }
    @Override protected boolean isEnabled() { return KillAuraConfig.getInstance().enabled; }
    @Override protected void toggle() { var c = KillAuraConfig.getInstance(); c.setEnabled(!c.enabled); }
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
        if (btn == 0) { if (HotkeyWatcher()) return true; toggle(); return true; }
        if (btn == 1) { Minecraft.getInstance().setScreen(new KillAuraScreen()); return true; }
        if (btn == 2) return handleMiddleClick(mx, my, btn);
        return false;
    }

    private boolean HotkeyWatcher() { return listeningForKey; }
    @Override public String getFeatureName() { return "杀戮光环"; }
}
