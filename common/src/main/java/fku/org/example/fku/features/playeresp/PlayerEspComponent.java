package fku.org.example.fku.features.playeresp; /* water */

import fku.org.example.fku.client.gui.components.ToggleComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 玩家ESP GUI开关组件 — 左键开关，右键打开详细配置界面
 * 该组件由赛博教员实现
 */
public class PlayerEspComponent extends ToggleComponent {

    @Override
    protected String getFeatureName() {
        return "玩家ESP";
    }

    public PlayerEspComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "玩家ESP");
    }

    @Override
    protected boolean isEnabled() {
        return PlayerEspConfig.getInstance().enabled;
    }

    @Override
    protected void toggle() {
        PlayerEspConfig cfg = PlayerEspConfig.getInstance();
        cfg.enabled = !cfg.enabled;
    }

    @Override
    protected void saveConfig() {
        PlayerEspConfig.save();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        if (renderHotkeyWait(g)) return;

        boolean enabled = isEnabled();
        fku.org.example.fku.client.gui.GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled);
        String displayStr = hotkeyAppend("玩家ESP: " + (enabled ? "开" : "关"));
        int textColor = enabled ? fku.org.example.fku.config.GuiStyleConfig.getInstance().getTextColor() : 0xAAAAAA;
        g.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2, textColor);
        // 右键打开配置提示
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
            Minecraft.getInstance().setScreen(new PlayerEspScreen());
            return true;
        } else if (button == 2) {
            return handleMiddleClick(mouseX, mouseY, button);
        }
        return false;
    }
}