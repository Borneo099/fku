package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 禁用 CheatUtils 区块绕过开关组件
 * 左键切换开/关：开启后强制让 CheatUtils 的忽略服务器视距失效，
 * 远处地图区块会被正常卸载与刷新，修复“远处地图不加载”的问题。
 */
public class DisableCheatutilsChunkComponent extends GuiComponent {

    public DisableCheatutilsChunkComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "禁CU区块");
    }

    private boolean isEnabled() {
        return FkuConfig.disableCheatutilsChunkBypass.get();
    }

    private void toggle() {
        FkuConfig.disableCheatutilsChunkBypass.set(!FkuConfig.disableCheatutilsChunkBypass.get());
        FkuConfig.disableCheatutilsChunkBypass.save();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;

        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();

        GuiRenderHelper.drawComponentBackground(guiGraphics, x, y, width, height, enabled);

        String displayStr = "禁CU区块: " + (enabled ? "ON" : "OFF");
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        guiGraphics.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2 - 4, textColor);
        guiGraphics.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                toggle();
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
