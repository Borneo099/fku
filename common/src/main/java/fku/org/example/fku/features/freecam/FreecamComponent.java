package fku.org.example.fku.features.freecam; /* water */

import fku.org.example.fku.client.gui.components.ToggleComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 灵魂出窍 GUI开关组件 — 左键开关，右键打开配置界面
 * 该组件由赛博教员实现
 */
public class FreecamComponent extends ToggleComponent {

    @Override
    protected String getFeatureName() {
        return "灵魂出窍";
    }

    public FreecamComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "灵魂出窍");
    }

    @Override
    protected boolean isEnabled() {
        return FreecamConfig.getInstance().enabled;
    }

    @Override
    protected void toggle() {
        FreecamConfig cfg = FreecamConfig.getInstance();
        cfg.enabled = !cfg.enabled;
        // 立即生效
        FreecamFeature.setEnabled(cfg.enabled);
    }

    @Override
    protected void saveConfig() {
        FreecamConfig.save();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        if (renderHotkeyWait(g)) return;

        boolean enabled = isEnabled();
        fku.org.example.fku.client.gui.GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled);
        String displayStr = hotkeyAppend("灵魂出窍: " + (enabled ? "开" : "关"));
        int textColor = enabled ? fku.org.example.fku.config.GuiStyleConfig.getInstance().getTextColor() : 0xAAAAAA;
        g.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2, textColor);
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) return false;
        if (button == 0) {
            toggle();
            saveConfig();
            return true;
        } else if (button == 1) {
            Minecraft.getInstance().setScreen(new FreecamScreen());
            return true;
        } else if (button == 2) {
            return handleMiddleClick(mouseX, mouseY, button);
        }
        return false;
    }
}