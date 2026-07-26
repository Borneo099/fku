package fku.org.example.fku.features.tacz; /* water */

import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * TaCZ 枪械辅助 — 战斗菜单组件
 * 左键：开关主开关（静默保存），右键：打开配置面板，中键：绑定热键
 * 该组件由赛博教员实现
 */
public class TaCZComponent extends GuiComponent {

    public TaCZComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "TaCZ");
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible || currentAlpha <= 0.01f) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean isHovered = mx >= x && mx <= x + width && my >= y && my <= y + height;
        int bgAlpha = (int)(180 * currentAlpha);
        TaCZConfig cfg = TaCZConfig.getInstance();
        boolean on = cfg.masterEnabled;
        int bgColor;
        if (on) {
            bgColor = config.getPrimaryColorWithAlpha(bgAlpha);
        } else {
            int gray = 0x40 << 16 | 0x40 << 8 | 0x40;
            bgColor = (bgAlpha << 24) | gray;
        }
        GuiRenderHelper.drawRoundedRect(g, x, y, width, height, bgColor, 3);
        if (isHovered && currentAlpha > 0.5f) {
            GuiRenderHelper.drawRoundedOutline(g, x, y, width, height, config.getPrimaryColorWithAlpha((int)(200 * currentAlpha)), 3, 1);
        }
        int textAlpha = (int)(255 * currentAlpha);
        String display = "TaCZ 枪械: " + (on ? "§a开" : "§c关");
        g.drawString(Minecraft.getInstance().font, display, x + 5, y + (height - 8) / 2, (textAlpha << 24) | 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!visible) return false;
        if (mx < x || mx > x + width || my < y || my > y + height) return false;
        if (button == 0) {
            if (HotkeySystem.isWaiting()) return false;
            TaCZConfig cfg = TaCZConfig.getInstance();
            cfg.masterEnabled = !cfg.masterEnabled;
            TaCZConfig.save();
            return true;
        } else if (button == 1) {
            Minecraft.getInstance().setScreen(new TaCZScreen());
            return true;
        } else if (button == 2) {
            HotkeySystem.startBinding("TaCZ", () -> {});
            return true;
        }
        return false;
    }
}