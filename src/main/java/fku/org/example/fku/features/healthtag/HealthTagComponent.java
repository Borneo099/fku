package fku.org.example.fku.features.healthtag; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import fku.org.example.fku.client.gui.components.ToggleComponent;

/**
 * HealthTag开关组件
 * 左键切换开关，右键打开配置菜单
 */
public class HealthTagComponent extends ToggleComponent {

    public HealthTagComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "HealthTag");
    }

    @Override
    protected boolean isEnabled() {
        return HealthTagConfig.getInstance().enabled;
    }

    @Override
    protected void toggle() {
        HealthTagConfig.getInstance().enabled = !HealthTagConfig.getInstance().enabled;
    }

    @Override
    protected void saveConfig() {
        HealthTagConfig.save();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();
        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled);
        String displayStr = label + ": " + (enabled ? "ON" : "OFF");
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        g.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2, textColor);
        // ★ 右键打开配置提示
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                toggle();
                saveConfig();
                return true;
            } else if (button == 1) {
                Minecraft.getInstance().setScreen(new HealthTagConfigScreen());
                return true;
            }
        }
        return false;
    }
}