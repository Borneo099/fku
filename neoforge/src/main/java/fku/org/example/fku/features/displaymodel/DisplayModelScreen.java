package fku.org.example.fku.features.displaymodel; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * 实体模型展示 GUI（纯 UI）
 *
 * 职责：
 * - 多行指令输入（+/- 按钮增删行）
 * - 配置选项（放置延迟、生成间隔、实体间距、放置坐标 X/Y/Z）
 * - 多行队列执行：第一行→第二行→...，坐标统一使用固定玩家坐标
 * - 实时显示 Manager 进度
 *
 * UI 布局（动态高度）：
 *   [标题行]
 *   [提示文字]
 *   [+][指令输入框1]       ← 固定带加号
 *   [-][指令输入框2]       ← 点加号新增，带减号
 *   ...
 *   配置选项:
 *   放置延迟(ms): [___]   生成间隔(ms): [___]   实体间距(格): [___]
 *   可视距离: [___] (0=默认)
 *   放置坐标:  X [___]  Y [___]  Z [___]
 *   [保存配置]     [召唤模型]     [中止]
 *
 * 设计思想：
 * - UI 与 Manager 完全分离，Screen 只管布局和事件委托
 * - 配置保存仅通过"保存配置"按钮触发
 */
public class DisplayModelScreen extends Screen {

    private static final int WIDTH = 480;
    /** 基础高度（不含指令行） */
    private static final int BASE_HEIGHT = 222;
    /** 每行指令高度 */
    private static final int ROW_HEIGHT = 24;

    // ============ 多行指令输入 ============
    private final List<CommandRow> commandRows = new ArrayList<>();
    private static class CommandRow {
        EditBox input;
        Button toggleBtn;  // "+" 或 "-"
    }

    // ============ 配置输入框 ============
    private EditBox placeDelayInput;
    private EditBox generationDelayInput;
    private EditBox entitySpacingInput;
    private EditBox placeXInput;
    private EditBox placeYInput;
    private EditBox placeZInput;
    private EditBox viewRangeInput;

    // ============ 按钮 ============
    private Button summonButton;
    private Button saveButton;
    private Button cancelButton;
    private Button openWebsiteButton;
    private Button writePosButton;
    private Button clearPosButton;

    // ============ 状态 ============
    private String statusMessage = "";
    private int statusColor = 0xFFFFFF;

    private final DisplayModelConfig config;
    private final DisplayModelManager manager;

    /** 追踪所有由本 Screen 创建的控件，用于重建时清理 */
    private final List<GuiEventListener> myChildren = new ArrayList<>();
    private final List<Renderable> myRenderables = new ArrayList<>();

    /** 标记需要在下个 tick 重建布局 */
    private boolean rebuildScheduled = false;

    public DisplayModelScreen() {
        super(Component.literal("实体模型展示"));
        this.config = DisplayModelConfig.getInstance();
        this.manager = DisplayModelManager.getInstance();
        manager.setOnStatusUpdate(this::updateFromManager);
    }

    // ====================================================================
    //  init — 初始化第一行指令 + 完整 UI
    // ====================================================================
    @Override
    protected void init() {
        super.init();

        if (commandRows.isEmpty()) {
            commandRows.add(new CommandRow());
        }

        rebuildLayout();
        updateFromManager();
    }

    // ====================================================================
    //  myAddWidget / myAddRenderableWidget — 追踪式添加控件
    // ====================================================================
    private <T extends GuiEventListener & Renderable & NarratableEntry> T myAddRenderableWidget(T widget) {
        myChildren.add(widget);
        myRenderables.add(widget);
        return addRenderableWidget(widget);
    }

    private <T extends GuiEventListener & NarratableEntry> T myAddWidget(T widget) {
        myChildren.add(widget);
        return addWidget(widget);
    }

    // ====================================================================
    //  rebuildLayout — 清除旧控件 + 重建全部
    //
    //  由于 children 是 private，用追踪列表来移除旧控件。
    //  使用 Minecraft.getInstance().tell() 延迟到 tick 中调用，
    //  避免在 mouseClicked 循环中修改 children 导致 CME。
    // ====================================================================
    private void rebuildLayout() {
        if (commandRows == null || commandRows.isEmpty()) return;

        // 保存指令行内容
        List<String> savedCmds = new ArrayList<>();
        for (CommandRow row : commandRows) {
            savedCmds.add(row.input != null ? row.input.getValue() : "");
        }

        // 清除旧控件
        for (GuiEventListener w : myChildren) {
            removeWidget(w);
        }
        for (Renderable r : myRenderables) {
            renderables.remove(r);
        }
        myChildren.clear();
        myRenderables.clear();

        int x = (width - WIDTH) / 2;
        int totalHeight = BASE_HEIGHT + (commandRows.size() - 1) * ROW_HEIGHT;
        int y = (height - totalHeight) / 2;
        int currentY = y + 44; // 与 render 同步：第一个指令行位置

        // ── 指令输入行 ──
        for (int i = 0; i < commandRows.size(); i++) {
            CommandRow row = commandRows.get(i);
            boolean isFirst = (i == 0);
            String savedVal = i < savedCmds.size() ? savedCmds.get(i) : "";
            final int rowIndex = i;

            // 切换按钮（+ 或 -）
            String btnLabel = isFirst ? "§a+" : "§c-";
            row.toggleBtn = Button.builder(Component.literal(btnLabel), btn -> {
                if (isFirst) {
                    commandRows.add(new CommandRow());
                } else {
                    commandRows.remove(rowIndex);
                }
                Minecraft.getInstance().tell(this::rebuildLayout);
            }).bounds(x + 10, currentY, 18, 18).build();
            myAddRenderableWidget(row.toggleBtn);

            // 指令输入框
            row.input = new EditBox(font, x + 32, currentY, WIDTH - 44, 18, Component.literal(""));
            row.input.setMaxLength(32767);
            row.input.setValue(savedVal);
            myAddWidget(row.input);

            currentY += ROW_HEIGHT;
        }

        // ── 与 render 同步：指令区结束后加间距，分割线在 currentY-4 处 ──
        currentY += 14; // render: gap after cmd rows
        // "配置选项:" label 由 render 绘制，无需 widget
        currentY += 13; // render: 从 label 到第一行输入区的偏移

        // ── 配置输入区 - 第一行：放置延迟 / 生成间隔 / 实体间距 ──
        int inputY = currentY + 1; // render draws labels at currentY+1

        placeDelayInput = createConfigInput(x + 90, inputY, 60,
                String.valueOf((int) config.placeDelay), true, "\\d*");
        myAddWidget(placeDelayInput);

        generationDelayInput = createConfigInput(x + 240, inputY, 60,
                String.valueOf((int) config.generationDelay), true, "\\d*");
        myAddWidget(generationDelayInput);

        entitySpacingInput = createConfigInput(x + 380, inputY, 55,
                String.valueOf(config.entitySpacing), true, "\\d*\\.?\\d*");
        myAddWidget(entitySpacingInput);

        // ── 配置输入区 - 第二行：可视距离 ──
        int viewRangeY = inputY + 22;

        viewRangeInput = createConfigInput(x + 80, viewRangeY, 60,
                config.viewRange > 0 ? String.valueOf(config.viewRange) : "", false, "\\d*\\.?\\d*");
        myAddWidget(viewRangeInput);

        // ── 配置输入区 - 第三行：放置坐标 ──
        int coordY = inputY + 44; // 两行偏移

        placeXInput = createConfigInput(x + 80, coordY, 55,
                config.placeX != 0 ? String.valueOf(config.placeX) : "", false, "-?\\d*\\.?\\d*");
        myAddWidget(placeXInput);

        placeYInput = createConfigInput(x + 165, coordY, 55,
                config.placeY != 0 ? String.valueOf(config.placeY) : "", false, "-?\\d*\\.?\\d*");
        myAddWidget(placeYInput);

        placeZInput = createConfigInput(x + 250, coordY, 55,
                config.placeZ != 0 ? String.valueOf(config.placeZ) : "", false, "-?\\d*\\.?\\d*");
        myAddWidget(placeZInput);

        // ── 坐标辅助按钮 ──
        int btnCoordY = coordY - 1;

        writePosButton = Button.builder(Component.literal("写入玩家坐标"), btn -> {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer p = mc.player;
            if (p != null) {
                BlockPos bp = p.blockPosition();
                placeXInput.setValue(String.valueOf(bp.getX()));
                placeYInput.setValue(String.valueOf(bp.getY()));
                placeZInput.setValue(String.valueOf(bp.getZ()));
            }
        }).bounds(x + 313, btnCoordY, 80, 16).build();
        myAddRenderableWidget(writePosButton);

        clearPosButton = Button.builder(Component.literal("清空坐标"), btn -> {
            placeXInput.setValue("");
            placeYInput.setValue("");
            placeZInput.setValue("");
        }).bounds(x + 398, btnCoordY, 55, 16).build();
        myAddRenderableWidget(clearPosButton);

        // ── 底部按钮（4 按钮居中，对称排列） ──
        int btnY = y + totalHeight - 30;
        // 4 个按钮各 80px，间距 (480-4*80)/5 = 32px
        int btnGap = (WIDTH - 4 * 80) / 5;
        int btn0X = x + btnGap;

        openWebsiteButton = Button.builder(
                Component.literal("打开模型网站"),
                btn -> Util.getPlatform().openUri(URI.create("https://block-display.com/"))
        ).bounds(btn0X, btnY, 80, 20).build();
        myAddRenderableWidget(openWebsiteButton);

        saveButton = Button.builder(Component.literal("保存配置"), btn -> saveInputsToConfig())
                .bounds(btn0X + 80 + btnGap, btnY, 80, 20).build();
        myAddRenderableWidget(saveButton);

        summonButton = Button.builder(Component.literal("召唤模型"), btn -> startSummon())
                .bounds(btn0X + 2 * (80 + btnGap), btnY, 80, 20).build();
        myAddRenderableWidget(summonButton);

        cancelButton = Button.builder(Component.literal("中止"), btn -> {
                    manager.stop();
                    updateFromManager();
                })
                .bounds(btn0X + 3 * (80 + btnGap), btnY, 80, 20).build();
        cancelButton.active = false;
        myAddRenderableWidget(cancelButton);
    }

    /** 创建配置输入框的辅助方法 */
    private EditBox createConfigInput(int x, int y, int width, String value,
                                      boolean intOnly, String filter) {
        EditBox box = new EditBox(font, x, y, width, 14, Component.literal(""));
        box.setValue(value);
        box.setMaxLength(intOnly ? 5 : 10);
        box.setFilter(s -> s.matches(filter));
        return box;
    }

    // ====================================================================
    //  tick
    // ====================================================================
    @Override
    public void tick() {
        super.tick();
        for (CommandRow row : commandRows) {
            if (row.input != null) row.input.tick();
        }
        if (placeDelayInput != null) placeDelayInput.tick();
        if (generationDelayInput != null) generationDelayInput.tick();
        if (entitySpacingInput != null) entitySpacingInput.tick();
        if (placeXInput != null) placeXInput.tick();
        if (placeYInput != null) placeYInput.tick();
        if (placeZInput != null) placeZInput.tick();
        if (viewRangeInput != null) viewRangeInput.tick();

        updateFromManager();
    }

    // ====================================================================
    //  updateFromManager
    // ====================================================================
    private void updateFromManager() {
        if (manager.isRunning()) {
            String msg = manager.getStatusMessage();
            if (msg != null && !msg.isEmpty()) {
                this.statusMessage = msg;
                this.statusColor = msg.startsWith("§c") ? 0xFF5555 : 0x55FF55;
            }
            if (summonButton != null) {
                summonButton.setMessage(Component.literal(
                        "放置中 " + manager.getCurrentIndex() + "/" + manager.getTotalCount()));
                summonButton.active = false;
            }
            if (cancelButton != null) cancelButton.active = true;
        } else {
            if (summonButton != null) {
                summonButton.setMessage(Component.literal("召唤模型"));
                summonButton.active = true;
            }
            if (cancelButton != null) cancelButton.active = false;
        }
    }

    // ====================================================================
    //  startSummon
    // ====================================================================
    private void startSummon() {
        if (manager.isRunning()) {
            setStatusMessage("§e放置正在进行中...", 0xFFFF55);
            return;
        }

        List<String> cmds = new ArrayList<>();
        for (CommandRow row : commandRows) {
            String cmd = row.input.getValue().trim();
            if (!cmd.isEmpty()) cmds.add(cmd);
        }
        if (cmds.isEmpty()) {
            setStatusMessage("§c请至少输入一行 /summon 指令", 0xFF5555);
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!player.isCreative()) {
            setStatusMessage("§c需要创造模式", 0xFF5555);
            return;
        }

        int placeDelayMs = parseIntOrDefault(placeDelayInput, 50);
        int generationDelayMs = parseIntOrDefault(generationDelayInput, 50);
        double spacing = parseDoubleClamped(entitySpacingInput, 0.5, 0, 10);

        double px = parseDoubleOrDefault(placeXInput, 0);
        double py = parseDoubleOrDefault(placeYInput, 0);
        double pz = parseDoubleOrDefault(placeZInput, 0);
        double vr = parseDoubleOrDefault(viewRangeInput, 0);

        BlockPos fixedPos = (px == 0 && py == 0 && pz == 0)
                ? player.blockPosition()
                : BlockPos.containing(px, py, pz);

        List<DisplayModelManager.CommandEntry> queue = new ArrayList<>();
        for (String cmd : cmds) {
            queue.add(new DisplayModelManager.CommandEntry(cmd));
        }

        manager.start(queue, generationDelayMs, placeDelayMs, spacing, fixedPos, vr);
        if (manager.isRunning()) {
            setStatusMessage("§a开始放置，" + cmds.size() + " 行指令...", 0x55FF55);
            summonButton.setMessage(Component.literal("放置中..."));
            summonButton.active = false;
        }
    }

    // ====================================================================
    //  saveInputsToConfig
    // ====================================================================
    private void saveInputsToConfig() {
        tryParseInt(placeDelayInput, v -> config.setPlaceDelay(v));
        tryParseInt(generationDelayInput, v -> config.setGenerationDelay(v));
        tryParseDouble(entitySpacingInput, v -> config.setEntitySpacing(Math.max(0, Math.min(10, v))));
        tryParseDouble(placeXInput, config::setPlaceX);
        tryParseDouble(placeYInput, config::setPlaceY);
        tryParseDouble(placeZInput, config::setPlaceZ);
        tryParseDouble(viewRangeInput, v -> config.setViewRange(Math.max(0, v)));

        setStatusMessage("§a配置已保存", 0x55FF55);
        Fku.LOGGER.info("[DisplayModel] 配置已保存");
    }

    // ====================================================================
    //  辅助
    // ====================================================================
    @FunctionalInterface
    private interface IntConsumer { void accept(int v); }
    @FunctionalInterface
    private interface DoubleConsumer { void accept(double v); }

    private static void tryParseInt(EditBox input, IntConsumer consumer) {
        try {
            String val = input.getValue().trim();
            if (!val.isEmpty()) consumer.accept(Integer.parseInt(val));
        } catch (NumberFormatException ignored) {}
    }

    private static void tryParseDouble(EditBox input, DoubleConsumer consumer) {
        try {
            String val = input.getValue().trim();
            if (!val.isEmpty()) consumer.accept(Double.parseDouble(val));
        } catch (NumberFormatException ignored) {}
    }

    /** 解析整数输入框，解析失败返回默认值 */
    private static int parseIntOrDefault(EditBox input, int defaultValue) {
        try {
            String val = input.getValue().trim();
            return val.isEmpty() ? defaultValue : Integer.parseInt(val);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    /** 解析浮点数输入框，解析失败返回默认值 */
    private static double parseDoubleOrDefault(EditBox input, double defaultValue) {
        try {
            String val = input.getValue().trim();
            return val.isEmpty() ? defaultValue : Double.parseDouble(val);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    /** 解析浮点数并限制在 [min, max] 范围内，解析失败返回默认值 */
    private static double parseDoubleClamped(EditBox input, double defaultValue, double min, double max) {
        double val = parseDoubleOrDefault(input, defaultValue);
        return Math.max(min, Math.min(max, val));
    }

    private void setStatusMessage(String msg, int color) {
        this.statusMessage = msg;
        this.statusColor = color;
    }

    // ====================================================================
    //  render
    // ====================================================================
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        int totalHeight = BASE_HEIGHT + (commandRows.size() - 1) * ROW_HEIGHT;
        int x = (width - WIDTH) / 2;
        int y = (height - totalHeight) / 2;

        // ── 背景面板 ──
        guiGraphics.fill(x - 2, y - 2, x + WIDTH + 2, y + totalHeight + 2, 0xCC222222);
        guiGraphics.renderOutline(x - 2, y - 2, WIDTH + 4, totalHeight + 4, 0xFF555555);

        // ── 标题行 ──
        guiGraphics.drawString(font, "§l实体模型展示", x + 10, y + 8, 0xFFFFFF);
        guiGraphics.drawString(font, "粘贴 /summon 指令（含 Passengers）:", x + 10, y + 24, 0x888888);

        // ── 分割线（标题与指令区之间） ──
        guiGraphics.fill(x + 10, y + 38, x + WIDTH - 10, y + 39, 0xFF444444);

        // ── 指令行 ──
        int currentY = y + 44;
        for (CommandRow row : commandRows) {
            row.toggleBtn.render(guiGraphics, mouseX, mouseY, partialTick);
            row.input.render(guiGraphics, mouseX, mouseY, partialTick);
            if (row.input.getValue().isEmpty() && !row.input.isFocused()) {
                guiGraphics.drawString(font, "§7/summon minecraft:block_display ~-0.5 ~-0.5 ~-0.5 {...}",
                        x + 36, row.input.getY() + 2, 0x444444);
            }
            currentY += ROW_HEIGHT;
        }
        currentY += 14;

        // ── 分割线（指令区与配置区之间） ──
        guiGraphics.fill(x + 10, currentY - 4, x + WIDTH - 10, currentY - 3, 0xFF444444);

        // ── 配置区 ──
        guiGraphics.drawString(font, "§7配置选项:", x + 10, currentY, 0x888888);
        currentY += 13;

        // 第一行：放置延迟 / 生成间隔 / 实体间距
        guiGraphics.drawString(font, "放置延迟(ms):", x + 10, currentY + 1, 0xAAAAAA);
        placeDelayInput.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(font, "生成间隔(ms):", x + 165, currentY + 1, 0xAAAAAA);
        generationDelayInput.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(font, "实体间距(格):", x + 320, currentY + 1, 0xAAAAAA);
        entitySpacingInput.render(guiGraphics, mouseX, mouseY, partialTick);
        currentY += 22;

        // 第二行：可视距离
        guiGraphics.drawString(font, "可视距离:", x + 10, currentY + 1, 0xAAAAAA);
        viewRangeInput.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(font, "§7(0=默认)", x + 145, currentY + 1, 0x666666);
        currentY += 22;

        // 第三行：放置坐标 + 辅助按钮
        guiGraphics.drawString(font, "放置坐标:", x + 10, currentY + 1, 0xAAAAAA);
        guiGraphics.drawString(font, "X", x + 72, currentY + 1, 0x888888);
        placeXInput.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(font, "Y", x + 152, currentY + 1, 0x888888);
        placeYInput.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(font, "Z", x + 232, currentY + 1, 0x888888);
        placeZInput.render(guiGraphics, mouseX, mouseY, partialTick);
        writePosButton.render(guiGraphics, mouseX, mouseY, partialTick);
        clearPosButton.render(guiGraphics, mouseX, mouseY, partialTick);

        // ── 底部状态栏（按钮上方） ──
        if (!statusMessage.isEmpty()) {
            guiGraphics.drawString(font, statusMessage, x + 15, y + totalHeight - 45, statusColor);
        }

        // ── 底部按钮（必须显式渲染，未调用 super.render()） ──
        openWebsiteButton.render(guiGraphics, mouseX, mouseY, partialTick);
        saveButton.render(guiGraphics, mouseX, mouseY, partialTick);
        summonButton.render(guiGraphics, mouseX, mouseY, partialTick);
        cancelButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}