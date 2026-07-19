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
        String savedValue = ""; // 用于重建时恢复内容
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
    private Button savePresetButton;
    private Button loadPresetButton;

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
    /** 当前面板总高度，用于GUI位置保存 */
    private int totalHeight = BASE_HEIGHT;

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

        // ★ 从配置文件恢复指令行
        commandRows.clear();
        if (config.commandLines != null && !config.commandLines.isEmpty()) {
            for (String line : config.commandLines) {
                CommandRow row = new CommandRow();
                row.savedValue = line; // rebuildLayout 会用 savedCmds 恢复
                commandRows.add(row);
            }
        }
        // 确保至少一行
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
            // ★ 优先取 input.getValue()，如果 input 还没创建则取 savedValue（预设载入时）
            String val = (row.input != null) ? row.input.getValue() : row.savedValue;
            savedCmds.add(val != null ? val : "");
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
        if (config.guiX >= 0 && config.guiY >= 0) {
            x = config.guiX;
        }
        this.totalHeight = BASE_HEIGHT + (commandRows.size() - 1) * ROW_HEIGHT;
        int y = (height - this.totalHeight) / 2;
        if (config.guiY >= 0) {
            y = config.guiY;
        }
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

        // ── 底部按钮（预设按钮 + 原4按钮，6按钮分2行） ──
        int btnY1 = y + totalHeight - 54;
        int btnY2 = y + totalHeight - 30;
        int btnW = 72;
        int gap6 = (WIDTH - 6 * btnW) / 7;
        int bX = x + gap6;

        // 第一行：打开模型网站 / 保存预设 / 载入预设
        openWebsiteButton = Button.builder(Component.literal("打开模型网站"),
                btn -> Util.getPlatform().openUri(URI.create("https://block-display.com/"))
        ).bounds(bX, btnY1, btnW, 20).build();
        myAddRenderableWidget(openWebsiteButton);

        savePresetButton = Button.builder(Component.literal("§a保存预设"), btn -> savePreset())
                .bounds(bX + (btnW + gap6), btnY1, btnW, 20).build();
        myAddRenderableWidget(savePresetButton);

        loadPresetButton = Button.builder(Component.literal("§b载入预设"), btn -> loadPreset())
                .bounds(bX + 2 * (btnW + gap6), btnY1, btnW, 20).build();
        myAddRenderableWidget(loadPresetButton);

        // 第二行：保存配置 / 召唤模型 / 中止
        saveButton = Button.builder(Component.literal("保存配置"), btn -> saveInputsToConfig())
                .bounds(bX, btnY2, btnW, 20).build();
        myAddRenderableWidget(saveButton);

        summonButton = Button.builder(Component.literal("召唤模型"), btn -> startSummon())
                .bounds(bX + (btnW + gap6), btnY2, btnW, 20).build();
        myAddRenderableWidget(summonButton);

        cancelButton = Button.builder(Component.literal("中止"), btn -> {
                    manager.stop();
                    updateFromManager();
                })
                .bounds(bX + 2 * (btnW + gap6), btnY2, btnW, 20).build();
        cancelButton.active = false;
        myAddRenderableWidget(cancelButton);
    }

    /** 保存为预设 */
    private void savePreset() {
        List<String> cmds = collectCommands();
        if (cmds.isEmpty()) { setStatusMessage("§c至少输入一行指令", 0xFF5555); return; }
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new PresetSaveScreen(cmds, name -> {
            DisplayModelConfig.savePreset(name, cmds);
            setStatusMessage("§a预设已保存: " + name, 0x55FF55);
            mc.setScreen(this);
        }));
    }

    /** 载入预设 */
    private void loadPreset() {
        String[] presets = DisplayModelConfig.listPresets();
        if (presets.length == 0) { setStatusMessage("§e没有已保存的预设", 0xFFFF55); return; }
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new PresetLoadScreen(presets, name -> {
            List<String> cmds = DisplayModelConfig.loadPreset(name);
            if (!cmds.isEmpty()) {
                // ★ 写入 config.commandLines，这样回到 DisplayModelScreen 时 init() 会从中恢复
                config.commandLines = new ArrayList<>(cmds);
                config.save();
                setStatusMessage("§a已载入预设: " + name + "（" + cmds.size() + " 条指令）", 0x55FF55);
            }
            mc.setScreen(this);
        }));
    }

    /** 收集当前所有指令 */
    private List<String> collectCommands() {
        List<String> cmds = new ArrayList<>();
        for (CommandRow row : commandRows) {
            String cmd = row.input != null ? row.input.getValue().trim() : "";
            if (!cmd.isEmpty()) cmds.add(cmd);
        }
        return cmds;
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
    /** 保存输入到配置（含指令行、GUI位置、所有参数） */
    private void saveInputsToConfig() {
        tryParseInt(placeDelayInput, v -> config.setPlaceDelay(v));
        tryParseInt(generationDelayInput, v -> config.setGenerationDelay(v));
        tryParseDouble(entitySpacingInput, v -> config.setEntitySpacing(Math.max(0, Math.min(10, v))));
        tryParseDouble(placeXInput, config::setPlaceX);
        tryParseDouble(placeYInput, config::setPlaceY);
        tryParseDouble(placeZInput, config::setPlaceZ);
        tryParseDouble(viewRangeInput, v -> config.setViewRange(Math.max(0, v)));

        // ★ 保存指令行
        config.commandLines = collectCommands();
        config.save();

        // ★ 保存GUI位置
        config.guiX = (width - WIDTH) / 2;
        config.guiY = (height - totalHeight) / 2;
        config.save();

        setStatusMessage("§a配置已保存（含指令行）", 0x55FF55);
        Fku.LOGGER.info("[DisplayModel] 配置已保存");
    }

    @Override
    public void onClose() {
        // 关闭时自动保存GUI位置
        int totalHeight = BASE_HEIGHT + (commandRows.size() - 1) * ROW_HEIGHT;
        config.guiX = (width - WIDTH) / 2;
        config.guiY = (height - totalHeight) / 2;
        config.commandLines = collectCommands();
        config.save();
        super.onClose();
    }

    // ══════════════════════════════════════
    //  预设选择界面（简易 Screen）
    // ══════════════════════════════════════

    /** 预设保存界面 */
    private static class PresetSaveScreen extends Screen {
        private final List<String> commands;
        private final java.util.function.Consumer<String> callback;
        private EditBox nameInput;

        PresetSaveScreen(List<String> commands, java.util.function.Consumer<String> callback) {
            super(Component.literal("保存预设"));
            this.commands = commands;
            this.callback = callback;
        }

        @Override
        protected void init() {
            int cx = width / 2, cy = height / 2;
            addRenderableWidget(Button.builder(Component.literal("§c取消"), b -> onClose())
                    .bounds(cx - 75, cy + 30, 70, 20).build());
            addRenderableWidget(Button.builder(Component.literal("§a保存"), b -> {
                        String name = nameInput.getValue().trim();
                        if (!name.isEmpty()) callback.accept(name);
                    }).bounds(cx + 5, cy + 30, 70, 20).build());
            nameInput = new EditBox(font, cx - 70, cy - 10, 140, 18, Component.literal("预设名"));
            nameInput.setMaxLength(64);
            addWidget(nameInput);
        }

        @Override
        public void render(GuiGraphics g, int mx, int my, float pt) {
            renderBackground(g);
            g.drawString(font, "§l输入预设名称:", width / 2 - 50, height / 2 - 30, 0xFFFFFF);
            g.drawString(font, "§7共 " + commands.size() + " 条指令", width / 2 - 40, height / 2 + 12, 0x888888);
            nameInput.render(g, mx, my, pt);
            super.render(g, mx, my, pt);
        }
        @Override public boolean keyPressed(int k, int sc, int mod) {
            if (k == 256) { onClose(); return true; }
            if ((k == 257 || k == 335) && nameInput.isFocused()) {
                String name = nameInput.getValue().trim();
                if (!name.isEmpty()) callback.accept(name);
                return true;
            }
            if (nameInput.isFocused()) return nameInput.keyPressed(k, sc, mod);
            return super.keyPressed(k, sc, mod);
        }
        @Override public boolean isPauseScreen() { return false; }
    }

    /** 预设加载界面 */
    private static class PresetLoadScreen extends Screen {
        private final String[] presets;
        private final java.util.function.Consumer<String> callback;
        private int scrollOffset = 0;

        PresetLoadScreen(String[] presets, java.util.function.Consumer<String> callback) {
            super(Component.literal("载入预设"));
            this.presets = presets;
            this.callback = callback;
        }

        @Override
        protected void init() {
            int cx = width / 2, cy = height / 2;
            int btnW = 120;
            int maxVis = Math.min(presets.length, 8);
            int startY = cy - maxVis * 12;
            for (int i = 0; i < maxVis; i++) {
                int idx = i + scrollOffset;
                if (idx >= presets.length) break;
                final String name = presets[idx];
                addRenderableWidget(Button.builder(Component.literal(name),
                        b -> callback.accept(name)
                ).bounds(cx - btnW / 2, startY + i * 22, btnW, 20).build());
            }
            addRenderableWidget(Button.builder(Component.literal("§c关闭"), b -> onClose())
                    .bounds(cx - 30, startY + maxVis * 22 + 8, 60, 20).build());
        }

        @Override
        public void render(GuiGraphics g, int mx, int my, float pt) {
            renderBackground(g);
            g.drawString(font, "§l选择预设:", width / 2 - 40, height / 2 - (Math.min(presets.length, 8) * 22 / 2) - 20, 0xFFFFFF);
            super.render(g, mx, my, pt);
        }
        @Override public boolean isPauseScreen() { return false; }
        @Override public boolean mouseScrolled(double mx, double my, double delta) {
            scrollOffset = (int) Math.max(0, Math.min(presets.length - 1, scrollOffset - delta));
            rebuildWidgets();
            return true;
        }
    }

    // ════════ 渲染底部按钮 ════════

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

        int x = (width - WIDTH) / 2;
        if (config.guiX >= 0) x = config.guiX;
        int y = (height - this.totalHeight) / 2;
        if (config.guiY >= 0) y = config.guiY;

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
            guiGraphics.drawString(font, statusMessage, x + 15, y + totalHeight - 62, statusColor);
        }

        // ── 底部按钮（6按钮2行） ──
        openWebsiteButton.render(guiGraphics, mouseX, mouseY, partialTick);
        savePresetButton.render(guiGraphics, mouseX, mouseY, partialTick);
        loadPresetButton.render(guiGraphics, mouseX, mouseY, partialTick);
        saveButton.render(guiGraphics, mouseX, mouseY, partialTick);
        summonButton.render(guiGraphics, mouseX, mouseY, partialTick);
        cancelButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}