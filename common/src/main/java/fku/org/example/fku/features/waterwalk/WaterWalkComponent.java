package fku.org.example.fku.features.waterwalk; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ToggleComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * WaterWalk（水上行走）移动菜单组件
 *
 * ★ 左键：开/关功能（状态写入 config/fku/waterwalk.json，重开自动恢复）
 * ★ 中键：绑定热键（继承自 ToggleComponent）
 * ★ 右键：打开配置界面（绕过反作弊发包 NCP Bypass 开关）
 *
 * 交互模式与 SprintComponent 保持一致。
 */
public class WaterWalkComponent extends ToggleComponent {

    public WaterWalkComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "水上行走");
    }

    @Override
    protected String getFeatureName() {
        return "水上行走";
    }

    @Override
    protected boolean isEnabled() {
        return WaterWalkConfig.getInstance().enabled;
    }

    @Override
    protected void toggle() {
        WaterWalkConfig.getInstance().enabled = !WaterWalkConfig.getInstance().enabled;
    }

    @Override
    protected void saveConfig() {
        WaterWalkConfig.save();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        if (renderHotkeyWait(g)) return;

        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();

        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled);

        String displayStr = hotkeyAppend(label + ": " + (enabled ? "开" : "关"));
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        g.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2, textColor);

        // 右侧 ">>" 提示可右键配置
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 右键：打开配置界面（左键开关 / 中键绑定热键交给基类处理）
        if (isHovered(mouseX, mouseY) && button == 1) {
            Minecraft.getInstance().setScreen(new WaterWalkConfigScreen());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
