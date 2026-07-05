package fku.org.example.fku.features.worldedit; /* water */

import fku.org.example.fku.client.KeyBindings;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * WorldEdit Lite GUI 开关组件
 *
 * 左键：切换启用/禁用
 * 右键：帮助信息
 * 中键：绑定热键
 *
 * 显示热键名称便于查看
 */
public class WorldEditComponent extends GuiComponent {

    public WorldEditComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "创世神");
    }

    private WorldEditConfig cfg() { return WorldEditConfig.getInstance(); }
    private boolean isEnabled() { return cfg().enabled; }
    private void toggle() { cfg().setEnabled(!cfg().enabled); }
    private void saveConfig() { WorldEditConfig.save(); }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;

        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();

        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled);

        String status = enabled ? "ON" : "OFF";
        String displayStr = "创世神: " + status;
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        g.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2 - 4, textColor);

        // 右键配置提示
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!isHovered(mx, my)) return false;
        if (button == 0) {
            toggle();
            saveConfig();
            return true;
        } else if (button == 1) {
            // 右键显示帮助
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§6=== WorldEdit Lite 快速帮助 ==="), false);
                Minecraft.getInstance().player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§e//wand §7- 获取选区工具"), false);
                Minecraft.getInstance().player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§e//set §7/ //replace §7- 填充/替换"), false);
                Minecraft.getInstance().player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§e//sphere §7/ //cyl §7/ //pyramid §7- 形状"), false);
                Minecraft.getInstance().player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§e//copy §7/ //paste §7- 复制粘贴"), false);
                Minecraft.getInstance().player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§e//undo §7/ //redo §7- 撤销重做"), false);
                Minecraft.getInstance().player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§e输入 §7//help §e查看全部命令"), false);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}
