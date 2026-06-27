package fku.org.example.fku.features.antilag; /* water */

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
 * AntiLag（防拉回）配置界面
 *
 * ★ 职责：
 *   提供完整的防拉回配置修改界面，遵循 ClickGUI 风格（带面板边框）。
 *   所有修改即时保存到 AntiLagConfig 并持久化到 config/fku/antilag.json。
 *
 * ★ 参考：
 *   KnockbackConfigScreen 的布局模式（标签+输入框+按钮分层 + 圆角面板背景）
 *
 * 配置项（10项）：
 *   行1: 版本模式（MC1_16 / MC1_9）
 *   行2: 触发距离 range
 *   行3: 限速 limitPerSecond
 *   行4: 路径步长 moveDistance
 *   行5: 自动脱困模式 searchVclipMode
 *   行6: 脱困步距 searchFindStep
 *   行7: back 反拉回模式开关
 *   行8: allowIntoVoid 虚空保护开关
 *   行9: printWhenTooManyPacket 超限警告开关
 *   行10: 实时包计数器（只读）+ 保存/完成按钮
 */
public class AntiLagScreen extends Screen {

    private static final int WIDTH = 310;
    private static final int HEIGHT = 340;

    // 行Y偏移（相对于面板顶部）
    private static final int ROW_TITLE = 10;
    private static final int ROW_SERVER_MODE = 30;
    private static final int ROW_RANGE = 56;
    private static final int ROW_LIMIT = 82;
    private static final int ROW_MOVE_DIST = 108;
    private static final int ROW_VCLIP_MODE = 134;
    private static final int ROW_VCLIP_STEP = 160;
    private static final int ROW_BACK = 186;
    private static final int ROW_VOID = 212;
    private static final int ROW_PRINT = 238;
    private static final int ROW_COUNTER = 264;
    private static final int ROW_BUTTONS = 290;

    private EditBox rangeInput;
    private EditBox limitInput;
    private EditBox moveDistInput;
    private EditBox vclipStepInput;
    private Button serverModeButton;
    private Button vclipModeButton;
    private Button backButton;
    private Button voidButton;
    private Button printButton;

    private final AntiLagConfig cfg = AntiLagConfig.getInstance();
    private int tickCounter = 0;

    public AntiLagScreen() {
        super(Component.literal("防拉回配置"));
    }

    @Override
    protected void init() {
        super.init();
        int cx = (width - WIDTH) / 2;

        // ── 行1：版本模式 ──
        serverModeButton = Button.builder(
                Component.literal("模式: " + getVersionLabel(cfg.serverVersionMode)),
                btn -> {
                    String next = "MC1_16".equals(cfg.serverVersionMode) ? "MC1_9" : "MC1_16";
                    cfg.setServerVersionMode(next);
                    btn.setMessage(Component.literal("模式: " + getVersionLabel(next)));
                }
        ).bounds(cx + 10, cy(ROW_SERVER_MODE), 160, 18).build();
        addRenderableWidget(serverModeButton);

        // ── 行2：触发距离 ──
        rangeInput = new EditBox(font, cx + 80, cy(ROW_RANGE + 14), 60, 14, Component.literal(""));
        rangeInput.setValue(String.format("%.1f", cfg.range));
        rangeInput.setMaxLength(6);
        rangeInput.setFilter(s -> s.matches("\\d*\\.?\\d*"));
        addRenderableWidget(rangeInput);

        // ── 行3：限速 ──
        limitInput = new EditBox(font, cx + 80, cy(ROW_LIMIT + 14), 50, 14, Component.literal(""));
        limitInput.setValue(String.valueOf(cfg.limitPerSecond));
        limitInput.setMaxLength(5);
        limitInput.setFilter(s -> s.matches("\\d*"));
        addRenderableWidget(limitInput);

        // ── 行4：路径步长 ──
        moveDistInput = new EditBox(font, cx + 80, cy(ROW_MOVE_DIST + 14), 50, 14, Component.literal(""));
        moveDistInput.setValue(String.format("%.2f", cfg.moveDistance));
        moveDistInput.setMaxLength(5);
        moveDistInput.setFilter(s -> s.matches("\\d*\\.?\\d*"));
        addRenderableWidget(moveDistInput);

        // ── 行5：脱困模式 ──
        vclipModeButton = Button.builder(
                Component.literal("脱困方向: " + getVclipLabel(cfg.searchVclipMode)),
                btn -> {
                    String next = cycleVclipMode(cfg.searchVclipMode);
                    cfg.setSearchVclipMode(next);
                    btn.setMessage(Component.literal("脱困方向: " + getVclipLabel(next)));
                }
        ).bounds(cx + 10, cy(ROW_VCLIP_MODE), 150, 18).build();
        addRenderableWidget(vclipModeButton);

        // ── 行6：脱困步距 ──
        vclipStepInput = new EditBox(font, cx + 80, cy(ROW_VCLIP_STEP + 14), 50, 14, Component.literal(""));
        vclipStepInput.setValue(String.format("%.1f", cfg.searchFindStep));
        vclipStepInput.setMaxLength(5);
        vclipStepInput.setFilter(s -> s.matches("\\d*\\.?\\d*"));
        addRenderableWidget(vclipStepInput);

        // ── 行7：back 模式 ──
        backButton = Button.builder(
                Component.literal("反拉回模式: " + (cfg.back ? "开" : "关")),
                btn -> {
                    cfg.setBack(!cfg.back);
                    btn.setMessage(Component.literal("反拉回模式: " + (cfg.back ? "开" : "关")));
                }
        ).bounds(cx + 10, cy(ROW_BACK), 160, 18).build();
        addRenderableWidget(backButton);

        // ── 行8：虚空保护 ──
        voidButton = Button.builder(
                Component.literal("允许虚空: " + (cfg.allowIntoVoid ? "开" : "关")),
                btn -> {
                    cfg.setAllowIntoVoid(!cfg.allowIntoVoid);
                    btn.setMessage(Component.literal("允许虚空: " + (cfg.allowIntoVoid ? "开" : "关")));
                }
        ).bounds(cx + 10, cy(ROW_VOID), 160, 18).build();
        addRenderableWidget(voidButton);

        // ── 行9：超限警告 ──
        printButton = Button.builder(
                Component.literal("超限警告: " + (cfg.printWhenTooManyPacket ? "开" : "关")),
                btn -> {
                    cfg.setPrintWhenTooManyPacket(!cfg.printWhenTooManyPacket);
                    btn.setMessage(Component.literal("超限警告: " + (cfg.printWhenTooManyPacket ? "开" : "关")));
                }
        ).bounds(cx + 10, cy(ROW_PRINT), 160, 18).build();
        addRenderableWidget(printButton);

        // ── 行10：底部按钮 ──
        addRenderableWidget(Button.builder(
                Component.literal("保存"),
                btn -> saveConfig()
        ).bounds(cx + 70, cy(ROW_BUTTONS), 60, 18).build());

        addRenderableWidget(Button.builder(
                Component.literal("完成"),
                btn -> {
                    saveConfig();
                    Minecraft.getInstance().setScreen(new ClickGuiScreen());
                }
        ).bounds(cx + 150, cy(ROW_BUTTONS), 60, 18).build());
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cx = (width - WIDTH) / 2;

        // ── 面板背景（圆角+边框） ──
        GuiRenderHelper.drawPanelBackground(g, cx, cy(0), WIDTH, HEIGHT, false);
        g.drawString(font, "AntiLag 防拉回配置", cx + 10, cy(ROW_TITLE), 0xFFFFFF);

        // ── 触发距离 ──
        g.drawString(font, "触发距离:", cx + 10, cy(ROW_RANGE), 0xAAAAAA);
        g.drawString(font, "§7(0.1~2000)", cx + 142, cy(ROW_RANGE + 14), 0x666666);

        // ── 每秒限包 ──
        g.drawString(font, "每秒限包:", cx + 10, cy(ROW_LIMIT), 0xAAAAAA);
        g.drawString(font, "§7(1~10000)", cx + 132, cy(ROW_LIMIT + 14), 0x666666);

        // ── 路径步长 ──
        g.drawString(font, "路径步长:", cx + 10, cy(ROW_MOVE_DIST), 0xAAAAAA);
        g.drawString(font, "§7(0.01~1.0)", cx + 132, cy(ROW_MOVE_DIST + 14), 0x666666);

        // ── 脱困步距 ──
        g.drawString(font, "脱困步距:", cx + 10, cy(ROW_VCLIP_STEP), 0xAAAAAA);
        g.drawString(font, "§7(0.1~5.0)", cx + 132, cy(ROW_VCLIP_STEP + 14), 0x666666);

        // ── 模式说明 ──
        g.drawString(font, "§7| 版本模式：1.16=路径拆分;1.9=直接发送", cx + 175, cy(ROW_SERVER_MODE + 2), 0x666666);
        g.drawString(font, "§7| 反拉回模式：开启后保留服务端拉回效果", cx + 175, cy(ROW_BACK + 2), 0x666666);

        // ── 实时包计数器（每秒刷新） ──
        if (++tickCounter % 20 == 0) {
            int cnt = AntiLagFeature.getCurrentPacketCount();
            String rateState = cfg.rateLimited ? " §c(限速中)" : "";
            g.drawString(font,
                    "本秒发包: " + cnt + "/" + cfg.limitPerSecond + rateState,
                    cx + 10, cy(ROW_COUNTER), cnt > cfg.limitPerSecond ? 0xFF5555 : 0xAAAAAA);
        } else {
            int cnt = AntiLagFeature.getCurrentPacketCount();
            String rateState = cfg.rateLimited ? " §c(限速中)" : "";
            g.drawString(font,
                    "本秒发包: " + cnt + "/" + cfg.limitPerSecond + rateState,
                    cx + 10, cy(ROW_COUNTER), cnt > cfg.limitPerSecond ? 0xFF5555 : 0xAAAAAA);
        }

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // 依赖 super.mouseClicked() 默认分发，它会自动将点击事件派发给对应的 EditBox/Button
        // 手动拦截反而会破坏 EditBox 的内部焦点状态
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 只拦截 Escape 键用于关闭，其余输入全部交给父类默认分发
        // Screen.keyPressed() 会按 children 顺序将按键事件派发给焦点控件
        if (keyCode == 256) { // Escape
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // 字符输入直接交给父类默认分发到焦点 EditBox
        return super.charTyped(codePoint, modifiers);
    }

    // ════════════ 辅助方法 ════════════

    private String getVersionLabel(String mode) {
        return "MC1_16".equals(mode) ? "1.16 (路径拆分)" : "1.9 (直接)";
    }

    private String getVclipLabel(String mode) {
        switch (mode) {
            case "OnlyUp": return "↑ 仅向上";
            case "Down": return "↓ 仅向下";
            case "Both": return "⇅ 双向";
            default: return mode;
        }
    }

    private String cycleVclipMode(String current) {
        switch (current) {
            case "OnlyUp": return "Down";
            case "Down": return "Both";
            case "Both": return "OnlyUp";
            default: return "OnlyUp";
        }
    }

    private void saveConfig() {
        try {
            double r = Double.parseDouble(rangeInput.getValue());
            cfg.setRange(Math.max(0.1, Math.min(2000, r)));
        } catch (NumberFormatException ignored) {}
        try {
            int l = Integer.parseInt(limitInput.getValue());
            cfg.setLimitPerSecond(Math.max(1, Math.min(10000, l)));
        } catch (NumberFormatException ignored) {}
        try {
            double d = Double.parseDouble(moveDistInput.getValue());
            cfg.setMoveDistance(Math.max(0.01, Math.min(1.0, d)));
        } catch (NumberFormatException ignored) {}
        try {
            double s = Double.parseDouble(vclipStepInput.getValue());
            cfg.setSearchFindStep(Math.max(0.1, Math.min(5.0, s)));
        } catch (NumberFormatException ignored) {}
        cfg.save();
    }

    /** 计算相对于面板顶部的 Y 坐标 */
    private int cy(int rowOffset) {
        return (height - HEIGHT) / 2 + rowOffset;
    }

    @Override
    public void onClose() {
        saveConfig();
        Minecraft.getInstance().setScreen(null);
    }
}