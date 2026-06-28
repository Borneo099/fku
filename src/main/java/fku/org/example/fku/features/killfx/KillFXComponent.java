package fku.org.example.fku.features.killfx; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import fku.org.example.fku.client.gui.components.ToggleComponent;

/**
 * KillFX（击杀特效）开关组件
 * 左键切换开关，右键打开配置菜单
 *
 * ★ 参考：HealthTagComponent 的交互模式
 */
public class KillFXComponent extends ToggleComponent {

    public KillFXComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "击杀特效");
    }

    @Override
    protected boolean isEnabled() {
        return KillFXConfig.getInstance().enabled;
    }

    @Override
    protected void toggle() {
        KillFXConfig cfg = KillFXConfig.getInstance();
        cfg.enabled = !cfg.enabled;
    }

    @Override
    protected void saveConfig() {
        KillFXConfig.save();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();
        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled);
        String displayStr = label + ": " + (enabled ? "开" : "关");
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        g.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2, textColor);
        // 右键打开配置提示
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
                Minecraft.getInstance().setScreen(new KillFXConfigScreen());
                return true;
            }
        }
        return false;
    }
}