package fku.org.example.fku.features.quickcommand; /* water */

import com.mojang.blaze3d.platform.InputConstants;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * 快捷指令配置界面 — 类似 DisplayModel 的多行输入 + 每行可绑定组合键
 *
 * 布局：
 * 标题 + 提示
 * ┌─────────────────────────────────┐
 * │ [commmand1] [×] [绑定热键] [▲▼]  │
 * │ [commmand2] [×] [Ctrl+Shift+K]  │
 * │ [+] 添加一行                      │
 * └─────────────────────────────────┘
 * 底部：[完成]
 */
public class QuickCommandScreen extends Screen {
    private static final int W = 410;
    private static final int ROW_H = 22;
    private static final int BASE_H = 90;

    private final QuickCommandConfig cfg;
    private final List<Row> rows = new ArrayList<>();
    private int scrollY = 0;

    /** 正在等待绑定的行索引 */
    private int listeningRow = -1;

    private static class Row {
        EditBox input;
        Button toggle;
        Button bindBtn;
        Button up;
        Button down;
    }

    public QuickCommandScreen() { super(Component.literal("快捷指令配置")); this.cfg = QuickCommandConfig.getInstance(); }

    @Override
    protected void init() {
        listeningRow = -1;
        resize();
    }

    private void resize() {
        rows.clear();
        clearWidgets();
        int cx = (width - W) / 2;
        int y = (height - totalH()) / 2;
        int ly = y + 44;

        for (int i = 0; i < cfg.commands.size(); i++) {
            var cmd = cfg.commands.get(i);
            Row r = new Row();
            r.input = new EditBox(font, cx + 10, ly, 140, 16, Component.literal(""));
            r.input.setMaxLength(32767);
            r.input.setValue(cmd.enabled ? cmd.command : "§7(已禁用)");
            r.input.setEditable(cmd.enabled);
            addWidget(r.input);

            final int fi = i;
            r.toggle = Button.builder(Component.literal(cmd.enabled ? "§a✓" : "§c×"), b -> {
                cmd.enabled = !cmd.enabled;
                r.toggle.setMessage(Component.literal(cmd.enabled ? "§a✓" : "§c×"));
                r.input.setEditable(cmd.enabled);
                if (cmd.enabled) {
                    r.input.setValue(cmd.command);              // ★ 恢复原始指令
                } else {
                    cmd.command = r.input.getValue();            // ★ 保存当前内容再替换
                    r.input.setValue("§7(已禁用)");
                }
                cfg.save();
            }).bounds(cx + 156, ly, 22, 16).build();
            addRenderableWidget(r.toggle);

            String hkText = hotkeyDisplay(cmd.hotkeyKey, cmd.hotkeyModifiers);
            r.bindBtn = Button.builder(Component.literal("§e" + hkText), b -> {
                listeningRow = fi;
                r.bindBtn.setMessage(Component.literal("§a按下按键..."));
            }).bounds(cx + 182, ly, 100, 16).build();
            addRenderableWidget(r.bindBtn);

            r.up = Button.builder(Component.literal("▲"), b -> move(fi, -1))
                    .bounds(cx + 286, ly, 16, 16).build();
            addRenderableWidget(r.up);
            r.down = Button.builder(Component.literal("▼"), b -> move(fi, 1))
                    .bounds(cx + 303, ly, 16, 16).build();
            addRenderableWidget(r.down);

            // ★ 每行删除按钮
            addRenderableWidget(Button.builder(Component.literal("§c×"), b -> {
                cfg.commands.remove(fi); cfg.save(); resize();
            }).bounds(cx + 321, ly, 18, 16).build());

            rows.add(r);
            ly += ROW_H;
        }

        // 添加行按钮
        int ly2 = ly;
        addRenderableWidget(Button.builder(Component.literal("§a+ 添加"),
                b -> { cfg.commands.add(new QuickCommandConfig.CommandEntry()); cfg.save(); resize(); }
        ).bounds(cx + 10, ly2, 70, 18).build());
        addRenderableWidget(Button.builder(Component.literal("§c删除最后一行"),
                b -> { if (!cfg.commands.isEmpty()) { cfg.commands.remove(cfg.commands.size() - 1); cfg.save(); resize(); } }
        ).bounds(cx + 85, ly2, 90, 18).build());

        // 完成按钮
        int btnY = y + totalH() - 28;
        addRenderableWidget(Button.builder(Component.literal("§a保存并关闭"), b -> { saveAll(); onClose(); })
                .bounds(cx + W / 2 - 50, btnY, 100, 20).build());
    }

    private void saveAll() {
        for (int i = 0; i < cfg.commands.size() && i < rows.size(); i++) {
            var cmd = cfg.commands.get(i);
            String val = rows.get(i).input.getValue();
            if (cmd.enabled && !val.startsWith("§7")) cmd.command = val;
        }
        cfg.save();
    }

    private void move(int idx, int dir) {
        int ni = idx + dir;
        if (ni < 0 || ni >= cfg.commands.size()) return;
        var list = cfg.commands;
        var tmp = list.get(idx);
        list.set(idx, list.get(ni));
        list.set(ni, tmp);
        cfg.save();
        resize();
    }

    private int totalH() { return BASE_H + cfg.commands.size() * ROW_H; }

    /** 将键码+修饰键转为显示文字 */
    private static String hotkeyDisplay(int key, int mods) {
        if (key < 0) return "未绑定";
        StringBuilder sb = new StringBuilder();
        if ((mods & 2) != 0) sb.append("Ctrl+");
        if ((mods & 1) != 0) sb.append("Shift+");
        if ((mods & 4) != 0) sb.append("Alt+");
        sb.append(InputConstants.getKey(key, 0).getDisplayName().getString());
        return sb.toString();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningRow >= 0 && listeningRow < rows.size()) {
            var cmd = cfg.commands.get(listeningRow);
            // 捕获当前修饰键状态
            long win = Minecraft.getInstance().getWindow().getWindow();
            boolean shift = GLFW.glfwGetKey(win, 340) == 1 || GLFW.glfwGetKey(win, 344) == 1;
            boolean ctrl = GLFW.glfwGetKey(win, 341) == 1 || GLFW.glfwGetKey(win, 345) == 1;
            boolean alt = GLFW.glfwGetKey(win, 342) == 1 || GLFW.glfwGetKey(win, 346) == 1;
            int mods = (shift ? 1 : 0) | (ctrl ? 2 : 0) | (alt ? 4 : 0);
            cmd.hotkeyKey = keyCode;
            cmd.hotkeyModifiers = mods;
            // 排除纯修饰键（只按 Shift/Ctrl/Alt 不算）
            if (keyCode != 340 && keyCode != 344 && keyCode != 341 && keyCode != 345 && keyCode != 342 && keyCode != 346 && keyCode != 256) {
                cfg.save();
                listeningRow = -1;
                resize();
                return true;
            }
            // ESC 取消绑定
            if (keyCode == 256) { listeningRow = -1; resize(); return true; }
            return true;
        }

        // 正常按键处理
        for (var r : rows) {
            if (r.input.isFocused() && r.input.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        if (keyCode == 256) { saveAll(); onClose(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cx = (width - W) / 2;
        int y = (height - totalH()) / 2;
        int h = totalH();

        GuiRenderHelper.drawRoundedRect(g, cx - 2, y - 2, W + 4, h + 4, 0xAA222222, 8);
        GuiRenderHelper.drawRoundedOutline(g, cx - 2, y - 2, W + 4, h + 4, 0xFF555555, 8, 1);

        g.drawString(font, "§l快捷指令配置 — 每行可绑定组合键热键", cx + 10, y + 8, 0xFFFFFF);
        g.drawString(font, "§7指令以 / 开头  点击[绑定热键]后按组合键  ▲▼排序", cx + 10, y + 22, 0x888888);
        if (listeningRow >= 0)
            g.drawString(font, "§a§l按组合键绑定此行... (ESC取消)", cx + 10, y + 36, 0xFFFF00);

        for (var r : rows) {
            r.input.render(g, mx, my, pt);
        }

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
