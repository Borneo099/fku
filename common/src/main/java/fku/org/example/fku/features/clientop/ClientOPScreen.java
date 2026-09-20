package fku.org.example.fku.features.clientop; /* water */

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ColorWheelPicker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 客户端OP 配置界面
 * 分组：通用设置 / 补全增强 / 指令预览 / 聊天增强
 * 控件风格（开关=Button、颜色=ColorWheelPicker、下拉=循环Button、数字=EditBox）与项目其它配置界面一致。
 * 顶部明确标注能力边界：“仅本地UI便利，不实际获得服务端权限”。
 */
public class ClientOPScreen extends Screen {

    private static final int WIDTH = 340;
    private static final int HEIGHT = 460;

    private final ClientOPConfig cfg = ClientOPConfig.getInstance();
    private final ColorWheelPicker colorPicker;

    private EditBox maxInput;

    /** 当前正在编辑的颜色：0=OP标记色, 1=高亮色 */
    private int editingColor = -1;

    /** 分区标题：{名称, y} */
    private final List<String[]> sections = new ArrayList<>();

    public ClientOPScreen() {
        super(Component.literal("客户端OP 设置"));
        this.colorPicker = new ColorWheelPicker(cfg.opIndicatorColor, this::onColorPicked);
    }

    private int cx() { return (width - WIDTH) / 2; }
    private int cy0() { return (height - HEIGHT) / 2; }
    private int yOf(int row) { return cy0() + row; }

    @Override
    protected void init() {
        super.init();
        int x = cx();
        sections.clear();

        toggleBtn(x, yOf(60), "启用功能", cfg.enabled, v -> cfg.setEnabled(v));
        toggleBtn(x, yOf(84), "标记OP指令 [OP]", cfg.showOpIndicator, v -> cfg.showOpIndicator = v);
        // 颜色按钮（opIndicatorColor）在 yOf(108)

        sections.add(new String[]{"补全增强", String.valueOf(yOf(138))});
        toggleBtn(x, yOf(158), "本地指令树补全", cfg.enableLocalSuggestions, v -> cfg.enableLocalSuggestions = v);
        toggleBtn(x, yOf(182), "与服务端补全合并", cfg.mergeWithServer, v -> cfg.mergeWithServer = v);

        maxInput = new EditBox(font, x + 170, yOf(206) + 2, 60, 16, Component.literal(""));
        maxInput.setValue(String.valueOf(cfg.maxSuggestions));
        maxInput.setMaxLength(2);
        maxInput.setFilter(s -> s.matches("\\d*"));
        addRenderableWidget(maxInput);

        sections.add(new String[]{"指令预览", String.valueOf(yOf(238))});
        toggleBtn(x, yOf(258), "启用指令预览", cfg.enablePreview, v -> cfg.enablePreview = v);
        addRenderableWidget(Button.builder(Component.literal("预览位置: " + posName(cfg.previewPosition)), b -> {
            String[] arr = {"CHAT_ABOVE", "TOOLTIP", "ACTION_BAR"};
            int i = 0;
            for (int k = 0; k < arr.length; k++) if (arr[k].equals(cfg.previewPosition)) i = k;
            cfg.previewPosition = arr[(i + 1) % arr.length];
            cfg.saveConfig();
            b.setMessage(Component.literal("预览位置: " + posName(cfg.previewPosition)));
        }).bounds(x, yOf(282), WIDTH - 20, 20).build());
        toggleBtn(x, yOf(306), "仅对OP指令预览", cfg.showPreviewForOpOnly, v -> cfg.showPreviewForOpOnly = v);

        sections.add(new String[]{"聊天增强", String.valueOf(yOf(338))});
        toggleBtn(x, yOf(358), "高亮OP指令", cfg.highlightOpCommands, v -> cfg.highlightOpCommands = v);
        // 颜色按钮（highlightColor）在 yOf(382)
        toggleBtn(x, yOf(406), "显示指令说明", cfg.showCommandTooltip, v -> cfg.showCommandTooltip = v);

        addRenderableWidget(Button.builder(Component.literal("§a完成"), b -> { saveAll(); Minecraft.getInstance().setScreen(new ClickGuiScreen()); })
                .bounds(x + WIDTH / 2 - 50, yOf(HEIGHT - 28), 100, 20).build());
    }

    private void toggleBtn(int x, int y, String label, boolean val, Consumer<Boolean> setter) {
        addRenderableWidget(Button.builder(Component.literal(label + ": " + (val ? "开" : "关")), btn -> {
            boolean nv = !cfgBool(label);
            setter.accept(nv);
            cfg.saveConfig();
            btn.setMessage(Component.literal(label + ": " + (nv ? "开" : "关")));
        }).bounds(x, y, WIDTH - 20, 20).build());
    }

    /** 读取当前开关真实值（避免按钮文案与配置不同步） */
    private boolean cfgBool(String label) {
        return switch (label) {
            case "启用功能" -> cfg.enabled;
            case "标记OP指令 [OP]" -> cfg.showOpIndicator;
            case "本地指令树补全" -> cfg.enableLocalSuggestions;
            case "与服务端补全合并" -> cfg.mergeWithServer;
            case "启用指令预览" -> cfg.enablePreview;
            case "仅对OP指令预览" -> cfg.showPreviewForOpOnly;
            case "高亮OP指令" -> cfg.highlightOpCommands;
            case "显示指令说明" -> cfg.showCommandTooltip;
            default -> false;
        };
    }

    private static String posName(String p) {
        return switch (p) {
            case "TOOLTIP" -> "鼠标悬停";
            case "ACTION_BAR" -> "动作栏";
            default -> "聊天上方";
        };
    }

    private void onColorPicked(String hex) {
        if (editingColor == 0) cfg.opIndicatorColor = hex;
        else if (editingColor == 1) cfg.highlightColor = hex;
        cfg.saveConfig();
    }

    private void openColorPicker(int idx) {
        editingColor = idx;
        colorPicker.setColor(idx == 0 ? cfg.opIndicatorColor : cfg.highlightColor);
        colorPicker.open(width / 2, height / 2);
    }

    private boolean colorButtonHit(double mx, double my, int idx) {
        int y = idx == 0 ? yOf(108) : yOf(382);
        int x = cx() + 10;
        int w = WIDTH - 20, h = 20;
        return mx >= x && mx <= x + w && my >= y + 12 && my <= y + 12 + h;
    }

    private void saveAll() {
        try {
            int v = Integer.parseInt(maxInput.getValue());
            cfg.maxSuggestions = Math.max(5, Math.min(50, v));
        } catch (Exception ignored) {}
        cfg.saveConfig();
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int x = cx();
        int y0 = cy0();
        GuiRenderHelper.drawPanelBackground(g, x, y0, WIDTH, HEIGHT, false);

        g.drawString(font, "客户端OP 设置", x + 10, y0 + 8, 0xFFFFFF);
        g.drawString(font, "§c⚠ 本功能仅提供本地UI便利，不实际获得服务端权限", x + 10, y0 + 28, 0xFF6666);
        g.drawString(font, "§7任何指令执行仍需服务端OP；本地补全/预览仅供参考", x + 10, y0 + 42, 0x888888);

        for (String[] s : sections) {
            g.drawString(font, "§l§9" + s[0], x + 10, Integer.parseInt(s[1]), 0x66CCFF);
        }

        drawColorButton(g, mx, my, 0, yOf(108), "OP标记颜色", cfg.opIndicatorColor);
        drawColorButton(g, mx, my, 1, yOf(382), "高亮颜色", cfg.highlightColor);

        g.drawString(font, "最大补全数量:", x + 10, yOf(206) + 4, 0xAAAAAA);

        if (colorPicker.isOpen()) colorPicker.render(g, mx, my);

        super.render(g, mx, my, pt);
    }

    private void drawColorButton(GuiGraphics g, int mx, int my, int idx, int y, String label, String hex) {
        int x = cx() + 10;
        int w = WIDTH - 20, h = 20;
        g.drawString(font, label, x, y - 2, 0xAAAAAA);
        int swX = x, swY = y + 12;
        GuiRenderHelper.drawRoundedRect(g, swX, swY, w, h, 0xFF333333, 4);
        int c = 0xFF000000 | Integer.parseInt(hex.replace("#", ""), 16);
        GuiRenderHelper.drawRoundedRect(g, swX + 2, swY + 2, w - 4, h - 4, c, 2);
        if (mx >= swX && mx <= swX + w && my >= swY && my <= swY + h) {
            GuiRenderHelper.drawRoundedOutline(g, swX, swY, w, h, 0xFFFFFFFF, 4, 1);
        }
        g.drawString(font, "#" + hex, swX + w - 62, swY + 5, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (colorPicker.isOpen()) {
            colorPicker.mouseClicked(mx, my, button);
            editingColor = -1;
            return true;
        }
        if (colorButtonHit(mx, my, 0)) { openColorPicker(0); return true; }
        if (colorButtonHit(mx, my, 1)) { openColorPicker(1); return true; }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (maxInput.isFocused() && maxInput.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (keyCode == 256) { saveAll(); onClose(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (maxInput.isFocused() && maxInput.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        saveAll();
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}
