package fku.org.example.fku.features.entitycontrol;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.components.ToggleComponent;
import fku.org.example.fku.features.entitycontrol.EntityControlConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 实体控制开关组件
 * 左键切换启用/禁用，右键打开配置菜单，中键绑定热键
 */
public class EntityControlComponent extends ToggleComponent {

    @Override
    protected String getFeatureName() { return "实体控制"; }

    public EntityControlComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "实体控制");
    }

    @Override
    protected boolean isEnabled() {
        return EntityControlFeature.isEnabled();
    }

    @Override
    protected void toggle() {
        EntityControlFeature.toggleEnabled();
    }

    @Override
    protected void saveConfig() {
        // EntityControlFeature 内部已自动保存配置
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        if (renderHotkeyWait(g)) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();
        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled, currentAlpha);
        String displayStr = hotkeyAppend(label + ": " + (enabled ? "ON" : "OFF"));
        int textAlpha = (int)(255 * currentAlpha);
        int textColor = enabled ? ((textAlpha << 24) | (config.getTextColor() & 0xFFFFFF)) : ((textAlpha << 24) | 0xAAAAAA);
        g.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2, textColor);
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (isHovered(mx, my)) {
            if (button == 0) {
                if (fku.org.example.fku.util.HotkeySystem.isWaiting()) return false;
                toggle();
                saveConfig();
                return true;
            } else if (button == 1) {
                Minecraft.getInstance().setScreen(new EntityControlConfigScreen());
                return true;
            } else if (button == 2) {
                return handleMiddleClick(mx, my, button);
            }
        }
        return false;
    }
}
