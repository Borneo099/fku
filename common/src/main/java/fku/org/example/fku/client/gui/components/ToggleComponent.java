package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.dynamicisland.NotificationCenter;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 开关组件基类 — 左键切换，中键绑定热键
 */
public abstract class ToggleComponent extends GuiComponent {

    protected String label;

    /** 子类返回功能名称（用于热键注册），返回 null 则不启用热键 */
    protected String getFeatureName() { return null; }

    public ToggleComponent(int x, int y, int width, int height, String label) {
        super(x, y, width, height, label);
        this.label = label;
        String fn = getFeatureName();
        if (fn != null) HotkeySystem.registerFeature(fn, () -> { toggle(); saveConfig(); });
    }

    protected abstract boolean isEnabled();
    protected abstract void toggle();
    protected abstract void saveConfig();

    // ═══════ 热键辅助方法（子类 render/mouseClicked 中调用） ═══════

    /** 在 render 开头调用：如果正在等待绑定则显示提示并返回 true */
    protected boolean renderHotkeyWait(GuiGraphics g) {
        String fn = getFeatureName();
        if (fn != null && HotkeySystem.isWaitingFor(fn)) {
            GuiRenderHelper.drawComponentBackground(g, x, y, width, height, true);
            g.drawString(Minecraft.getInstance().font, "绑定热键中... (Esc取消)",
                    x + 5, y + (height - 8) / 2, 0xFFFF00);
            return true;
        }
        return false;
    }

    /** 追加热键名称后缀到显示文字 */
    protected String hotkeyAppend(String text) {
        String fn = getFeatureName();
        if (fn != null) {
            var hk = FeatureHotkeyManager.getInstance().getHotkey(fn);
            if (hk.getHotkeyKey() >= 0) text += " §7[" + hk.getHotkeyName() + "]";
        }
        return text;
    }

    /** mouseClicked 中处理中键绑定，子类在按钮链末尾调用 */
    protected boolean handleMiddleClick(double mx, double my, int button) {
        if (button == 2 && isHovered(mx, my)) {
            String fn = getFeatureName();
            if (fn != null) HotkeySystem.startBinding(fn, () -> {});
            return true;
        }
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible || currentAlpha <= 0.01f) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();

        if (renderHotkeyWait(g)) return;

        boolean enabled = isEnabled();
        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled, currentAlpha, this);

        // 标签（带 开/关 状态，悬停白边由 Helper 统一处理）
        int textAlpha = (int)(255 * currentAlpha);
        int textColor = enabled ? ((textAlpha << 24) | (config.getTextColor() & 0xFFFFFF)) : ((textAlpha << 24) | 0xAAAAAA);
        String labelText = hotkeyAppend(withState(label));
        int maxLabelW = width - 10;
        g.drawString(Minecraft.getInstance().font, truncate(labelText, maxLabelW), x + 5, y + (height - 8) / 2, textColor);
    }

    /** 带 开/关 状态的标签文本（供 render 统一调用，内部读取 isEnabled，避免各子类变量名差异） */
    protected String withState(String base) {
        return base + ": " + (isEnabled() ? "开" : "关");
    }

    /** 文字超出宽度时截断并加省略号，防止与药丸重叠 */
    protected String truncate(String s, int maxWidth) {
        var font = Minecraft.getInstance().font;
        if (font.width(s) <= maxWidth) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() > 1 && font.width(sb.toString() + "…") > maxWidth) sb.setLength(sb.length() - 1);
        return sb.toString() + "…";
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!isHovered(mx, my)) return false;
        if (button == 0) {
            if (HotkeySystem.isWaiting()) return false;
            toggle();
            saveConfig();
            NotificationCenter.pushToggle(label, isEnabled());
            return true;
        } else if (button == 2) {
            return handleMiddleClick(mx, my, button);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}
