package fku.org.example.fku.features.playeresp; /* water */

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 玩家ESP配置界面
 * 移植自 Lexis PlayerEspHack
 * 该功能由赛博教员实现
 */
public class PlayerEspScreen extends Screen {

    private static final int W = 280, H = 200;
    private int bx, by;
    private EditBox distanceInput;

    public PlayerEspScreen() {
        super(Component.literal("玩家ESP配置"));
    }

    @Override
    protected void init() {
        super.init();
        bx = (width - W) / 2;
        by = (height - H) / 2;
        PlayerEspConfig cfg = PlayerEspConfig.getInstance();

        int cx = bx + 10, cy = by + 30, sp = 22;

        // 显示模式
        addRenderableWidget(Button.builder(Component.literal("显示模式: §b" + modeName(cfg.mode)),
            btn -> {
                cfg.setMode(nextMode(cfg.mode));
                btn.setMessage(Component.literal("显示模式: §b" + modeName(cfg.mode)));
            }).bounds(cx, cy, 260, 18).build());
        cy += sp;

        // 颜色按钮（点击打开颜色选择器——这里简化处理，用文字提示）
        addRenderableWidget(Button.builder(Component.literal("§7方框颜色: §c#" + colorHex(cfg.boxColor)),
            btn -> {}).bounds(cx, cy, 126, 18).build());
        addRenderableWidget(Button.builder(Component.literal("§7连线颜色: §a#" + colorHex(cfg.linesColor)),
            btn -> {}).bounds(cx + 134, cy, 126, 18).build());
        cy += sp;

        addRenderableWidget(Button.builder(Component.literal("§7六面颜色: §9#" + colorHex(cfg.sidesColor)),
            btn -> {}).bounds(cx, cy, 126, 18).build());
        // 队伍颜色开关
        addRenderableWidget(Button.builder(Component.literal(cfg.forceTeamColor ? "§a队伍颜色: 开" : "§7队伍颜色: 关"),
            btn -> {
                cfg.setForceTeamColor(!cfg.forceTeamColor);
                btn.setMessage(Component.literal(cfg.forceTeamColor ? "§a队伍颜色: 开" : "§7队伍颜色: 关"));
            }).bounds(cx + 134, cy, 126, 18).build());
        cy += sp;

        // 最大距离
        addRenderableWidget(Button.builder(Component.literal("最大距离: §b" + cfg.maxDistance),
            btn -> {
                int[] levels = {16, 32, 64, 128, 256, 512, 1024};
                int idx = 0;
                for (int i = 0; i < levels.length; i++) {
                    if (cfg.maxDistance == levels[i]) { idx = i; break; }
                }
                idx = (idx + 1) % levels.length;
                cfg.setMaxDistance(levels[idx]);
                btn.setMessage(Component.literal("最大距离: §b" + cfg.maxDistance));
            }).bounds(cx, cy, 260, 18).build());
        cy += sp;

        // 模式说明
        addRenderableWidget(Button.builder(Component.literal("§7模式说明: 方框+连线+六面 | 可单独组合"), b -> {}).bounds(cx, cy, 260, 14).build());

        // 返回按钮
        addRenderableWidget(Button.builder(Component.literal("§a← 返回"),
            btn -> minecraft.setScreen(new ClickGuiScreen()))
            .bounds(bx + W / 2 - 30, by + H - 28, 60, 18).build());
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        GuiRenderHelper.drawRoundedRect(g, bx - 10, by - 8, W + 20, H + 16, 0xAA2D2D2D, 8);
        g.drawString(font, "§l玩家ESP 配置", bx + 10, by + 10, 0xFFFFFF);
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        minecraft.setScreen(new ClickGuiScreen());
    }

    private static String modeName(String mode) {
        return switch (mode) {
            case "BOX_ONLY" -> "仅方框";
            case "LINES_ONLY" -> "仅连线";
            case "SIDES_ONLY" -> "仅六面";
            case "BOX_LINES" -> "方框+连线";
            case "BOX_SIDES" -> "方框+六面";
            case "LINES_SIDES" -> "连线+六面";
            default -> "全部";
        };
    }

    private static String nextMode(String current) {
        return switch (current) {
            case "BOX_ONLY" -> "LINES_ONLY";
            case "LINES_ONLY" -> "SIDES_ONLY";
            case "SIDES_ONLY" -> "BOX_LINES";
            case "BOX_LINES" -> "BOX_SIDES";
            case "BOX_SIDES" -> "LINES_SIDES";
            case "LINES_SIDES" -> "ALL";
            default -> "BOX_ONLY";
        };
    }

    private static String colorHex(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return String.format("%02X%02X%02X", r, g, b);
    }
}