package fku.org.example.fku.features.pearlphase; /* water */

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 珍珠卡墙（PearlPhase）开关组件
 *
 * ★ 职责：
 *   左键切换启用/禁用，右键打开配置界面。
 *   启用后通过 ClientTickEvent 状态机执行自动投掷和卡墙移动。
 *
 * ★ 设计思想：
 *   - 状态显示：OFF / ON + 当前子状态（IDLE/AIMING/THROWING/WAITING/INSIDE）
 *   - 右键配置箭头提示
 */
public class PearlPhaseComponent extends fku.org.example.fku.client.gui.components.GuiComponent {

    public PearlPhaseComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "珍珠卡墙");
    }

    private boolean isEnabled() {
        return PearlPhaseConfig.getInstance().enabled;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;

        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();

        // 绘制圆角背景
        GuiRenderHelper.drawComponentBackground(guiGraphics, x, y, width, height, enabled);

        String status = enabled ? "ON" : "OFF";
        String displayStr = "珍珠卡墙: " + status;
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;

        // 主文字
        guiGraphics.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2 - 4, textColor);

        // ★ 运行时子状态提示（仅启用时显示）
        if (enabled) {
            PearlPhaseFeature.PhaseState phaseState = PearlPhaseFeature.getState();
            String phaseName = switch (phaseState) {
                case IDLE -> "";
                case AIMING -> "§e瞄准中";
                case THROWING -> "§6投掷中";
                case WAITING -> "§b等待珍珠";
                case INSIDE -> "§a卡墙中";
            };
            if (!phaseName.isEmpty()) {
                guiGraphics.drawString(Minecraft.getInstance().font, phaseName,
                        x + 5, y + (height - 8) / 2 + 8, 0x55FF55);
            }
        }

        // ★ 右键配置提示
        guiGraphics.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                PearlPhaseFeature.toggle();
                return true;
            } else if (button == 1) {
                Minecraft.getInstance().setScreen(new PearlPhaseConfigScreen());
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