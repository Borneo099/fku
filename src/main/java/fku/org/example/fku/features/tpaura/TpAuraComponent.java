package fku.org.example.fku.features.tpaura; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * TpAura（如来神掌/瞬移攻击）战斗菜单开关组件
 *
 * 左键：切换启用/禁用
 * 右键：打开配置界面（TpAuraScreen）
 * 中键：绑定热键（按下后按键盘按键绑定）
 *
 * 渲染热键名称便于查看
 */
public class TpAuraComponent extends GuiComponent {

    /** 当前是否是热键等待状态（用于渲染提示） */
    private boolean isWaitingKeyBind = false;

    public TpAuraComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "如来神掌");
    }

    private boolean isEnabled() {
        return TpAuraFeature.isEnabled();
    }

    private void toggle() {
        TpAuraFeature.setEnabled(!TpAuraFeature.isEnabled());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;

        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();

        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled);

        // 热键等待状态：显示特殊提示
        if (isWaitingKeyBind) {
            String displayStr = "绑定热键中... (Esc取消)";
            int textColor = 0xFFFF00;
            g.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2 - 4, textColor);
            // 中键提示箭头
            g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0xFFFF00);
            return;
        }

        String status = enabled ? "ON" : "OFF";
        String displayStr = "如来神掌: " + status;
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        g.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2 - 4, textColor);

        // 显示已绑定的热键名称
        TpAuraConfig cfg = TpAuraConfig.getInstance();
        if (cfg.hotkeyKey >= 0 && !cfg.hotkeyName.isEmpty()) {
            g.drawString(Minecraft.getInstance().font, "[" + cfg.hotkeyName + "]", x + width - 45, y + (height - 8) / 2 - 4, 0x888888);
        }

        // ★ 右键配置提示箭头
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                // 左键：切换
                toggle();
                return true;
            } else if (button == 1) {
                // 右键：打开配置
                Minecraft.getInstance().setScreen(new TpAuraScreen());
                return true;
            } else if (button == 2) {
                // 中键：开始热键绑定
                startHotkeyBindProcess();
                return true;
            }
        }
        return false;
    }

    /** 启动热键绑定流程 */
    private void startHotkeyBindProcess() {
        isWaitingKeyBind = true;
        TpAuraFeature.startHotkeyBind(() -> {
            // 绑定完成回调：更新UI状态
            isWaitingKeyBind = false;
        });
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}