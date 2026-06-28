package fku.org.example.fku.features.quickswitch; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * QuickSwitch（鬼手秒切）战斗菜单开关组件
 *
 * ★ 职责：
 *   左键：切换启用/禁用
 *   右键：打开配置界面（QuickSwitchConfigScreen）
 *
 * ★ 该组件由赛博教员实现
 *
 * ★ 参考：
 *   AntiLagComponent 的渲染与交互模式
 */
public class QuickSwitchComponent extends GuiComponent {

    public QuickSwitchComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "鬼手秒切");
    }

    private boolean isEnabled() {
        return QuickSwitchConfig.getInstance().enabled;
    }

    private void toggle() {
        QuickSwitchFeature.setEnabled(!QuickSwitchFeature.isEnabled());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;

        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();

        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled);

        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();
        String modeLabel = switch (cfg.mode) {
            case "SILENT" -> "静默";
            case "NINE_SLOT" -> "九切";
            case "CUSTOM" -> "自定义";
            default -> "关闭";
        };
        String status = enabled ? "ON [" + modeLabel + "]" : "OFF";
        String displayStr = "鬼手秒切: " + status;
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        g.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2 - 4, textColor);
        // 右键配置提示箭头
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                toggle();
                return true;
            } else if (button == 1) {
                Minecraft.getInstance().setScreen(new QuickSwitchConfigScreen());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}