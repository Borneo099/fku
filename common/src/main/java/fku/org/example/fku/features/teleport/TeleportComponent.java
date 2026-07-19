package fku.org.example.fku.features.teleport; /* water */

import fku.org.example.fku.client.gui.components.ToggleComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class TeleportComponent extends ToggleComponent {
    public TeleportComponent(int x, int y, int w, int h) { super(x, y, w, h, "瞬移"); }
    @Override protected boolean isEnabled() { return TeleportConfig.getInstance().enabled; }
    @Override protected void toggle() { var c = TeleportConfig.getInstance(); c.setEnabled(!c.enabled); }
    @Override protected void saveConfig() {}

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible || currentAlpha <= 0.01f) return;
        super.render(g, mx, my, pt);
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 14, y + (height - 8) / 2, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (!isHovered(mx, my)) return false;
        if (btn == 0) { if (listeningForKey) return false; toggle(); return true; }
        if (btn == 1) {
            // 在聊天栏显示使用方法（类似 WorldEdit 风格）
            var p = Minecraft.getInstance().player;
            if (p != null) {
                p.displayClientMessage(Component.literal(""), false);
                p.displayClientMessage(Component.literal("§6===== §e瞬移 §6====="), false);
                p.displayClientMessage(Component.literal("§7使用 /fku tp <x> <y> <z> [snap] 瞬移"), false);
                p.displayClientMessage(Component.literal("§7snap 为 true/false，开启落点吸附"), false);
                p.displayClientMessage(Component.literal("§7例如: /fku tp ~ ~5 ~ true 向上瞬移5格"), false);
                p.displayClientMessage(Component.literal("§7   /fku tp ~ ~ ~ false 准星瞬移"), false);
                p.displayClientMessage(Component.literal("§7   /fku tp 100 64 100 false 传送到坐标"), false);
                p.displayClientMessage(Component.literal("§7通过快捷指令绑定热键，快速执行"), false);
                p.displayClientMessage(Component.literal("§7[中键] 绑定/更改开关热键"), false);
                p.displayClientMessage(Component.literal(""), false);
            }
            return true;
        }
        if (btn == 2) return handleMiddleClick(mx, my, btn);
        return false;
    }
    @Override public String getFeatureName() { return "瞬移"; }
}
