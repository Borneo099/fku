package fku.org.example.fku.features.loot; /* water */

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 一键取物（Loot Nearby Containers）开关组件
 *
 * ★ 职责：
 *   左键切换启用/禁用，右键打开配置界面。
 *   启用后通过 ClientTickEvent 状态机执行容器扫描与取物。
 */
public class LootComponent extends fku.org.example.fku.client.gui.components.GuiComponent {

    public LootComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "一键取物");
    }

    private boolean isEnabled() {
        return LootConfig.getInstance().enabled;
    }

    private void toggle() {
        LootConfig cfg = LootConfig.getInstance();
        cfg.setEnabled(!cfg.enabled);
        if (!cfg.enabled) {
            LootFeature.stop();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;

        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();

        GuiRenderHelper.drawComponentBackground(guiGraphics, x, y, width, height, enabled);

        String status = enabled ? "ON" : "OFF";
        String displayStr = "一键取物: " + status;
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        guiGraphics.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2 - 4, textColor);

        // 运行时状态提示（仅运行时显示）
        if (LootFeature.isRunning()) {
            String runStatus = LootFeature.getStatus();
            if (!runStatus.isEmpty()) {
                guiGraphics.drawString(Minecraft.getInstance().font,
                        "§a" + runStatus,
                        x + 5, y + (height - 8) / 2 + 8, 0x55FF55);
            }
        }

        // ★ 右键打开配置提示
        guiGraphics.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                toggle();
                // 开启时立即触发一次扫描
                if (isEnabled()) {
                    LootFeature.start();
                }
                return true;
            } else if (button == 1) {
                Minecraft.getInstance().setScreen(new LootScreen());
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