package fku.org.example.fku.features.sprint; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import fku.org.example.fku.client.gui.components.ToggleComponent;

/**
 * 强制疾跑（Sprint）开关组件
 *
 * ★ 职责：
 *   左键切换启用/禁用，右键打开配置菜单。
 *   遵循 FKU 现有 ToggleComponent 交互模式。
 *
 * ★ 参考来源：
 *   KillFXComponent 的右键配置菜单交互模式
 */
public class SprintComponent extends ToggleComponent {

    public SprintComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "强制疾跑");
    }

    @Override
    protected boolean isEnabled() {
        return SprintHandler.isEnabled();
    }

    @Override
    protected void toggle() {
        SprintHandler.setEnabled(!SprintHandler.isEnabled());
    }

    @Override
    protected void saveConfig() {
        // SprintHandler 内部已自动保存，无需额外操作
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
                Minecraft.getInstance().setScreen(new SprintConfigScreen());
                return true;
            }
        }
        return false;
    }
}