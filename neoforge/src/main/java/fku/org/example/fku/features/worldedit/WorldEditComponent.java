package fku.org.example.fku.features.worldedit;

import fku.org.example.fku.client.gui.components.ConfigButtonComponent;
import net.minecraft.client.Minecraft;

/**
 * WorldEdit Lite GUI — 左键打开帮助面板（参考娱乐·实体模型模式，无开关）
 */
public class WorldEditComponent extends ConfigButtonComponent {

    @Override
    protected String getFeatureName() { return "创世神"; }

    public WorldEditComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "创世神", () -> showHelp());
    }

    private static void showHelp() {
        if (Minecraft.getInstance().player == null) return;
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
}
