package fku.org.example.fku.features.arrowdmg; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import fku.org.example.fku.client.gui.components.ToggleComponent;

/**
 * ArrowDmg（32k弓）开关组件
 */
public class ArrowDmgComponent extends ToggleComponent {

    public ArrowDmgComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "32k弓");
    }

    @Override protected boolean isEnabled() { return ArrowDmgFeature.isEnabled(); }
    @Override protected void toggle() { ArrowDmgFeature.toggleEnabled(); }
    @Override protected void saveConfig() {}

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean en = isEnabled();
        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, en);
        String display = label + ": " + (en ? "开" : "关");
        int c = en ? config.getTextColor() : 0xAAAAAA;
        g.drawString(Minecraft.getInstance().font, display, x + 5, y + (height - 8) / 2, c);
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (isHovered(mx, my)) {
            if (btn == 0) { toggle(); saveConfig(); return true; }
            else if (btn == 1) { Minecraft.getInstance().setScreen(new ArrowDmgConfigScreen()); return true; }
        }
        return false;
    }
}
