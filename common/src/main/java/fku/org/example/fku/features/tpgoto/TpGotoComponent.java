package fku.org.example.fku.features.tpgoto; /* water */

import fku.org.example.fku.client.gui.components.ConfigButtonComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * 传送前往 组件 — 左键显示使用说明，右键打开配置
 * 合并传送玩家和传送坐标为一个模块，默认开启无开关
 */
public class TpGotoComponent extends ConfigButtonComponent {

    public TpGotoComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "传送前往", () -> {
            // 左键：显示使用说明
            var p = Minecraft.getInstance().player;
            if (p != null) {
                p.displayClientMessage(Component.literal(""), false);
                p.displayClientMessage(Component.literal("§6===== §e传送前往 §6====="), false);
                p.displayClientMessage(Component.literal("§7使用 /fku tpgoto <玩家名> 传送到玩家身边"), false);
                p.displayClientMessage(Component.literal("§7使用 /fku tpgotoPos <x> <y> <z> 传送到坐标"), false);
                p.displayClientMessage(Component.literal("§7使用 /fku tpgoto stop 或 /fku tpgotoPos stop 停止传送"), false);
                p.displayClientMessage(Component.literal("§7例如: /fku tpgoto Steve"), false);
                p.displayClientMessage(Component.literal("§7      /fku tpgotoPos 100 64 200"), false);
                p.displayClientMessage(Component.literal("§7通过快捷指令绑定热键，快速执行"), false);
                p.displayClientMessage(Component.literal(""), false);
            }
        });
    }

    @Override
    protected String getFeatureName() { return "传送前往"; }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!isHovered(mx, my)) return false;
        if (button == 0) {
            // 左键：显示使用说明（由父类 onClick 处理）
            return super.mouseClicked(mx, my, button);
        } else if (button == 1) {
            // 右键：打开配置界面
            Minecraft.getInstance().setScreen(new TpGotoScreen());
            return true;
        } else if (button == 2) {
            // 中键：绑定热键
            return super.mouseClicked(mx, my, button);
        }
        return false;
    }
}