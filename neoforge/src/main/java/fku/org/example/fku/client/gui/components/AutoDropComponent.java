package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.autodrop.AutoDropConfig;
import fku.org.example.fku.features.autodrop.AutoDropScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 自动丢弃开关组件
 * 左键切换开关，右键打开配置菜单
 */
public class AutoDropComponent extends GuiComponent {
    protected String label;

    public AutoDropComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "自动丢");
        this.label = "自动丢";
    }

    protected boolean isEnabled() {
        return AutoDropConfig.getInstance().enabled;
    }

    protected void toggle() {
        AutoDropConfig.getInstance().enabled = !AutoDropConfig.getInstance().enabled;
    }

    protected void saveConfig() {
        AutoDropConfig.save();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;
        
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();

        // 绘制圆角背景
        GuiRenderHelper.drawComponentBackground(guiGraphics, x, y, width, height, enabled);

        // 绘制文字
        String displayStr = label + ": " + (enabled ? "ON" : "OFF");
        int textColor = enabled ? config.getTextColor() : (0xAAAAAA);
        guiGraphics.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2 - 4, textColor);
        // ★ 右键打开配置提示
        guiGraphics.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                toggle();
                saveConfig();
                return true;
            } else if (button == 1) {
                Minecraft.getInstance().setScreen(new AutoDropScreen());
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