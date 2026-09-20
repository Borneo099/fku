package fku.org.example.fku.features.tacz; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * TaCZ 枪械辅助 — 战斗菜单组件
 * 左键：开关主开关（静默保存），右键：打开配置面板，中键：绑定热键
 * 视觉统一为蓝底入口风格（同 实体模型），悬停白边选中，开/关状态文字提示
 */
public class TaCZComponent extends GuiComponent {

    public TaCZComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "TaCZ");
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible || currentAlpha <= 0.01f) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        TaCZConfig cfg = TaCZConfig.getInstance();
        boolean on = cfg.masterEnabled;

        // 统一模块背景（启用色/关闭灰底，与基岩破坏器一致，含呼吸光晕与悬停白边）
        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, on, currentAlpha, this);

        int textAlpha = (int)(255 * currentAlpha);
        int textColor = on ? ((textAlpha << 24) | (config.getTextColor() & 0xFFFFFF)) : ((textAlpha << 24) | 0xAAAAAA);
        String display = "TaCZ 枪械: " + (on ? "开" : "关");
        int maxLabelW = width - 10;
        g.drawString(Minecraft.getInstance().font, truncate(display, maxLabelW), x + 5, y + (height - 8) / 2, textColor);
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2, (textAlpha << 24) | 0x888888);
    }

    private String truncate(String s, int maxWidth) {
        var font = Minecraft.getInstance().font;
        if (font.width(s) <= maxWidth) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() > 1 && font.width(sb.toString() + "…") > maxWidth) sb.setLength(sb.length() - 1);
        return sb.toString() + "…";
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
