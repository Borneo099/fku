package fku.org.example.fku.features.clientop; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ToggleComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 客户端OP — 左键开关，右键打开配置面板，中键绑定热键
 * 视觉风格与 快捷指令/实体模型 一致（蓝底入口 + 悬停白边 + 开/关状态 + >> 标识）。
 */
public class ClientOPComponent extends ToggleComponent {

    public ClientOPComponent(int x, int y, int w, int h) {
        super(x, y, w, h, "客户端OP");
        hasConfigMenu = true; // 拥有配置菜单，由面板/组件统一绘制 >> 标识
    }

    @Override
    protected boolean isEnabled() {
        return ClientOPConfig.getInstance().enabled;
    }

    @Override
    protected void toggle() {
        ClientOPConfig.getInstance().setEnabled(!ClientOPConfig.getInstance().enabled);
    }

    @Override
    protected void saveConfig() {}

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible || currentAlpha <= 0.01f) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();

        // 统一模块背景（启用色/关闭灰底，与基岩破坏器一致，含呼吸光晕与悬停白边）
        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled, currentAlpha, this);

        // 标签 + 热键 + 开/关 状态提示
        int textAlpha = (int) (255 * currentAlpha);
        int textColor = enabled ? ((textAlpha << 24) | (config.getTextColor() & 0xFFFFFF)) : ((textAlpha << 24) | 0xAAAAAA);
        String labelText = hotkeyAppend(withState(label));
        int maxLabelW = width - 10;
        g.drawString(Minecraft.getInstance().font, truncate(labelText, maxLabelW), x + 5, y + (height - 8) / 2, textColor);
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2, (textAlpha << 24) | 0x888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (!isHovered(mx, my)) return false;
        if (btn == 0) {
            if (listeningForKey) return false;
            toggle();
            return true;
        }
        if (btn == 1) {
            Minecraft.getInstance().setScreen(new ClientOPScreen());
            return true;
        }
        if (btn == 2) return handleMiddleClick(mx, my, btn);
        return false;
    }

    @Override
    public String getFeatureName() {
        return "客户端OP";
    }
}
