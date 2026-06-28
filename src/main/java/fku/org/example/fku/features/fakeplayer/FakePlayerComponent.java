package fku.org.example.fku.features.fakeplayer; /* water */

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 假人（FakePlayer）开关组件 — 工具菜单
 *
 * ★ 职责：
 *   左键：切换启用/禁用（启用时自动生成假人，禁用时移除）
 *   右键：打开配置界面
 *
 * ★ 交互方式：
 *   启用后，在原地生成一个客户端假人实体。
 *   用武器攻击假人可测试伤害数值和 DPS。
 *
 * ★ 参考来源：
 *   AntiLagComponent 的交互模式
 *   AdvancedFakePlayer / IMGFakePlayer (InvincibleMachineGun)
 */
public class FakePlayerComponent extends GuiComponent {

    public FakePlayerComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "假人");
    }

    private boolean isEnabled() {
        return FakePlayerConfig.getInstance().enabled;
    }

    private void toggle() {
        FakePlayerFeature.toggle();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;

        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();

        GuiRenderHelper.drawComponentBackground(guiGraphics, x, y, width, height, enabled);

        String status = enabled ? "ON" : "OFF";
        String displayStr = "假人: " + status;
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        guiGraphics.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2 - 4, textColor);

        // ★ 假人存活提示
        if (enabled) {
            String alive = FakePlayerFeature.hasFakePlayer() ? "§a存活" : "§c已死亡";
            guiGraphics.drawString(Minecraft.getInstance().font, alive, x + width - 40, y + (height - 8) / 2 - 4, 0x888888);
        } else {
            guiGraphics.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0x888888);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                toggle();
                return true;
            } else if (button == 1) {
                Minecraft.getInstance().setScreen(new FakePlayerConfigScreen());
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