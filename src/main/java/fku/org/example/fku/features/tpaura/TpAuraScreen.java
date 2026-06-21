package fku.org.example.fku.features.tpaura; /* water */

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * TpAura（如来神掌/瞬移攻击）配置界面
 *
 * ★ 布局分组：
 *   攻击机制 → 武器 → 瞬移 → 目标 → 白名单 → 热键 → 图腾绕过
 *   所有修改即时保存到 TpAuraConfig 并持久化到 config/fku/tpaura.json
 */
public class TpAuraScreen extends Screen {

    private static final int WIDTH = 340;
    private static final int HEIGHT = 520;

    // ════════ 行偏移常量（重新排版，间距保持整齐） ════════
    private static final int ROW_TITLE = 10;
    private static final int ROW_ATTACK_MODE = 35;
    private static final int ROW_COOLDOWN = 62;
    private static final int ROW_AUTO_SWITCH = 92;
    private static final int ROW_SWING = 118;
    private static final int ROW_MODE = 144;
    private static final int ROW_RANGE = 170;
    private static final int ROW_TRANSPORT = 198;
    private static final int ROW_RENDER = 224;
    private static final int ROW_ALL_ENTITIES = 250;      // ★ 新增：全生物开关
    private static final int ROW_ENTITY_TYPES = 276;
    private static final int ROW_IGNORE = 306;
    private static final int ROW_WHITELIST = 330;
    private static final int ROW_WHITELIST_INPUT = 356;
    private static final int ROW_HOTKEY = 390;
    private static final int ROW_TOTEM = 414;
    private static final int ROW_TOTEM_INPUT = 440;
    private static final int ROW_BUTTONS = 470;

    // ════════ 控件 ════════
    private EditBox cooldownInput;
    private EditBox delayInput;
    private EditBox rangeInput;
    private EditBox packetsInput;
    private EditBox totemAtkInput;
    private EditBox totemHeightInput;

    private Button attackModeButton;
    private Button autoSwitchButton;
    private Button requireMaceButton;
    private Button swingHandButton;
    private Button silentSwapButton;
    private Button modeButton;
    private Button goUpButton;
    private Button returnPosButton;
    private Button offsetFixButton;
    private Button renderPathButton;
    private Button ignoreNamedButton;
    private Button ignoreTamedButton;
    private Button attackAllButton;
    private Button entityTypeButton;        // ★ 新增：实体类型选择按钮
    private Button whitelistToggleButton;
    private Button whitelistButton;         // ★ 新增：白名单选择按钮
    private Button hotkeyBindButton;
    private Button totemBypassButton;

    /** 热键绑定状态：true 时等待按键输入 */
    private boolean waitingHotkey = false;

    private final TpAuraConfig cfg = TpAuraConfig.getInstance();

    public TpAuraScreen() {
        super(Component.literal("如来神掌配置"));
    }

    @Override
    protected void init() {
        super.init();
        int cx = (width - WIDTH) / 2;

        // ── 攻击模式（3态循环：Smart→Fast→Universal→Smart） ──
        attackModeButton = Button.builder(
                Component.literal("攻击模式: " + getAttackModeLabel(cfg.attackMode)),
                btn -> {
                    String cur = cfg.attackMode;
                    String next;
                    if ("Smart".equals(cur)) next = "Fast";
                    else if ("Fast".equals(cur)) next = "Universal";
                    else next = "Smart";
                    cfg.setAttackMode(next);
                    btn.setMessage(Component.literal("攻击模式: " + getAttackModeLabel(next)));
                }
        ).bounds(cx + 10, cy(ROW_ATTACK_MODE), 150, 18).build();
        addRenderableWidget(attackModeButton);

        // ── 蓄力阈值 ──
        cooldownInput = new EditBox(font, cx + 80, cy(ROW_COOLDOWN + 14), 60, 14, Component.literal(""));
        cooldownInput.setValue(String.format("%.1f", cfg.cooldownThreshold));
        cooldownInput.setMaxLength(4);
        cooldownInput.setFilter(s -> s.matches("\\d*\\.?\\d*"));
        addRenderableWidget(cooldownInput);

        // ── 额外延迟 ──
        delayInput = new EditBox(font, cx + 210, cy(ROW_COOLDOWN + 14), 40, 14, Component.literal(""));
        delayInput.setValue(String.valueOf(cfg.attackDelay));
        delayInput.setMaxLength(2);
        delayInput.setFilter(s -> s.matches("\\d*"));
        addRenderableWidget(delayInput);

        // ── 自动切武 ──
        autoSwitchButton = toggleButton(cx + 10, cy(ROW_AUTO_SWITCH), 110, "自动切武",
                () -> cfg.autoSwitch, v -> cfg.setAutoSwitch(v));
        addRenderableWidget(autoSwitchButton);

        // ── 仅重锤 ──
        requireMaceButton = toggleButton(cx + 130, cy(ROW_AUTO_SWITCH), 110, "仅手持重锤",
                () -> cfg.requireMace, v -> cfg.setRequireMace(v));
        addRenderableWidget(requireMaceButton);

        // ── 挥手 + 静默切换 ──
        swingHandButton = toggleButton(cx + 10, cy(ROW_SWING), 90, "挥手",
                () -> cfg.swingHand, v -> cfg.setSwingHand(v));
        addRenderableWidget(swingHandButton);
        silentSwapButton = toggleButton(cx + 110, cy(ROW_SWING), 110, "静默切换",
                () -> cfg.silentSwap, v -> cfg.setSilentSwap(v));
        addRenderableWidget(silentSwapButton);

        // ── 兼容模式 ──
        modeButton = Button.builder(
                Component.literal("模式: " + getModeLabel(cfg.mode)),
                btn -> {
                    String next = "Paper".equals(cfg.mode) ? "Vanilla" : "Paper";
                    cfg.setMode(next);
                    btn.setMessage(Component.literal("模式: " + getModeLabel(next)));
                }
        ).bounds(cx + 10, cy(ROW_MODE), 150, 18).build();
        addRenderableWidget(modeButton);

        // ── 最大范围 ──
        rangeInput = new EditBox(font, cx + 80, cy(ROW_RANGE + 14), 60, 14, Component.literal(""));
        rangeInput.setValue(String.format("%.0f", cfg.maxRange));
        rangeInput.setMaxLength(4);
        rangeInput.setFilter(s -> s.matches("\\d*\\.?\\d*"));
        addRenderableWidget(rangeInput);

        // ── 垫包数量 ──
        packetsInput = new EditBox(font, cx + 210, cy(ROW_RANGE + 14), 40, 14, Component.literal(""));
        packetsInput.setValue(String.valueOf(cfg.paperPackets));
        packetsInput.setMaxLength(2);
        packetsInput.setFilter(s -> s.matches("\\d*"));
        addRenderableWidget(packetsInput);

        // ── V-Clip + 回传 ──
        goUpButton = toggleButton(cx + 10, cy(ROW_TRANSPORT), 90, "V-Clip",
                () -> cfg.goUp, v -> cfg.setGoUp(v));
        addRenderableWidget(goUpButton);
        returnPosButton = toggleButton(cx + 110, cy(ROW_TRANSPORT), 110, "攻击后回传",
                () -> cfg.returnPos, v -> cfg.setReturnPos(v));
        addRenderableWidget(returnPosButton);

        // ── 偏移同步 + 显示路径 ──
        offsetFixButton = toggleButton(cx + 10, cy(ROW_RENDER), 110, "偏移同步",
                () -> cfg.offsetFix, v -> cfg.setOffsetFix(v));
        addRenderableWidget(offsetFixButton);
        renderPathButton = toggleButton(cx + 130, cy(ROW_RENDER), 110, "显示路径",
                () -> cfg.renderPath, v -> cfg.setRenderPath(v));
        addRenderableWidget(renderPathButton);

        // ════════ ★ 全生物攻击（新增） ════════
        attackAllButton = toggleButton(cx + 10, cy(ROW_ALL_ENTITIES), 120, "全生物攻击",
                () -> cfg.attackAllEntities, v -> cfg.setAttackAllEntities(v));
        addRenderableWidget(attackAllButton);

        // ── 目标实体类型（按钮 → 打开选择列表） ──
        entityTypeButton = Button.builder(
                Component.literal("目标实体: " + getSelectedCountLabel(cfg.entityTypes)),
                btn -> openEntityTypeSelector()
        ).bounds(cx + 10, cy(ROW_ENTITY_TYPES), 280, 18).build();
        addRenderableWidget(entityTypeButton);

        // ── 忽略命名 + 忽略驯服 ──
        ignoreNamedButton = toggleButton(cx + 10, cy(ROW_IGNORE), 100, "忽略命名",
                () -> cfg.ignoreNamed, v -> cfg.setIgnoreNamed(v));
        addRenderableWidget(ignoreNamedButton);
        ignoreTamedButton = toggleButton(cx + 120, cy(ROW_IGNORE), 100, "忽略驯服",
                () -> cfg.ignoreTamed, v -> cfg.setIgnoreTamed(v));
        addRenderableWidget(ignoreTamedButton);

        // ════════ ★ 白名单 ════════
        whitelistToggleButton = toggleButton(cx + 10, cy(ROW_WHITELIST), 120, "白名单保护",
                () -> cfg.whitelistEnabled, v -> cfg.setWhitelistEnabled(v));
        addRenderableWidget(whitelistToggleButton);

        // 白名单选择按钮（打开选择列表）
        whitelistButton = Button.builder(
                Component.literal("白名单: " + getSelectedCountLabel(cfg.whitelist)),
                btn -> openWhitelistSelector()
        ).bounds(cx + 10, cy(ROW_WHITELIST_INPUT), 280, 18).build();
        addRenderableWidget(whitelistButton);

        // ════════ ★ 热键绑定 ════════
        { // block for variable scope
            String hotkeyText = cfg.hotkeyKey >= 0
                    ? "热键: " + getKeyName(cfg.hotkeyKey)
                    : "热键: 未设置";
            hotkeyBindButton = Button.builder(
                    Component.literal(hotkeyText),
                    btn -> {
                        waitingHotkey = !waitingHotkey;
                        updateHotkeyButton();
                    }
            ).bounds(cx + 10, cy(ROW_HOTKEY), 180, 18).build();
            addRenderableWidget(hotkeyBindButton);

            // ★ 清除热键按钮
            addRenderableWidget(Button.builder(
                    Component.literal("清除"),
                    btn -> {
                        cfg.setHotkeyKey(-1);
                        cfg.setHotkeyName("");
                        waitingHotkey = false;
                        updateHotkeyButton();
                    }
            ).bounds(cx + 200, cy(ROW_HOTKEY), 50, 18).build());
        }

        // ════════ 图腾绕过 ════════
        totemBypassButton = toggleButton(cx + 10, cy(ROW_TOTEM), 130, "图腾绕过",
                () -> cfg.totemBypass, v -> cfg.setTotemBypass(v));
        addRenderableWidget(totemBypassButton);

        // ── 攻击次数 + 递增高度 ──
        totemAtkInput = new EditBox(font, cx + 80, cy(ROW_TOTEM_INPUT + 14), 40, 14, Component.literal(""));
        totemAtkInput.setValue(String.valueOf(cfg.totemAttacks));
        totemAtkInput.setMaxLength(1);
        totemAtkInput.setFilter(s -> s.matches("[1-3]"));
        addRenderableWidget(totemAtkInput);
        totemHeightInput = new EditBox(font, cx + 200, cy(ROW_TOTEM_INPUT + 14), 50, 14, Component.literal(""));
        totemHeightInput.setValue(String.valueOf(cfg.totemHeightIncrease));
        totemHeightInput.setMaxLength(3);
        totemHeightInput.setFilter(s -> s.matches("\\d*"));
        addRenderableWidget(totemHeightInput);

        // ── 底部按钮 ──
        addRenderableWidget(Button.builder(
                Component.literal("保存"),
                btn -> saveConfig()
        ).bounds(cx + 80, cy(ROW_BUTTONS), 60, 18).build());

        addRenderableWidget(Button.builder(
                Component.literal("完成"),
                btn -> {
                    saveConfig();
                    Minecraft.getInstance().setScreen(new ClickGuiScreen());
                }
        ).bounds(cx + 170, cy(ROW_BUTTONS), 60, 18).build());
    }

    /**
     * 创建开关按钮（修复版）
     * 使用 BooleanSupplier 代替 boolean 参数，解决点击一次后无法再次点击的 Bug。
     * 每次点击重新读取 getter.getAsBoolean() 获取最新状态。
     */
    private Button toggleButton(int x, int y, int w, String label, BooleanSupplier getter, Consumer<Boolean> setter) {
        return Button.builder(
                Component.literal(label + ": " + (getter.getAsBoolean() ? "开" : "关")),
                btn -> {
                    boolean next = !getter.getAsBoolean();
                    setter.accept(next);
                    btn.setMessage(Component.literal(label + ": " + (next ? "开" : "关")));
                }
        ).bounds(x, y, w, 18).build();
    }

    // ══════════════════════════════════════════════
    //  实体选择列表打开
    // ══════════════════════════════════════════════

    /** 打开目标实体类型选择列表 */
    private void openEntityTypeSelector() {
        // 从 Forge 注册表获取所有实体类型 ID
        Set<String> all = new TreeSet<>();
        for (var key : ForgeRegistries.ENTITY_TYPES.getKeys()) {
            all.add(key.getPath());
        }
        // 当前已选
        Set<String> current = new HashSet<>();
        for (String s : cfg.entityTypes.split(",")) {
            String t = s.trim().toLowerCase();
            if (!t.isEmpty()) current.add(t);
        }
        Minecraft.getInstance().setScreen(new ListSelectScreen(
                "选择目标实体类型", all, current, newSelection -> {
            String joined = String.join(",", newSelection);
            cfg.setEntityTypes(joined.isEmpty() ? "PLAYER" : joined);
            if (entityTypeButton != null)
                entityTypeButton.setMessage(Component.literal("目标实体: " + getSelectedCountLabel(cfg.entityTypes)));
        }));
    }

    /** 打开白名单实体选择列表 */
    private void openWhitelistSelector() {
        Set<String> all = new TreeSet<>();
        for (var key : ForgeRegistries.ENTITY_TYPES.getKeys()) {
            all.add(key.getPath());
        }
        Set<String> current = new HashSet<>();
        for (String s : cfg.whitelist.split(",")) {
            String t = s.trim().toLowerCase();
            if (!t.isEmpty()) current.add(t);
        }
        Minecraft.getInstance().setScreen(new ListSelectScreen(
                "选择白名单实体", all, current, newSelection -> {
            String joined = String.join(",", newSelection);
            cfg.setWhitelist(joined);
            if (whitelistButton != null)
                whitelistButton.setMessage(Component.literal("白名单: " + getSelectedCountLabel(cfg.whitelist)));
        }));
    }

    /** 生成已选数量标签（如 "已选3项"） */
    private String getSelectedCountLabel(String csv) {
        if (csv == null || csv.isEmpty()) return "未选择";
        String[] parts = csv.split(",");
        return "已选" + parts.length + "项";
    }

    // ══════════════════════════════════════════════
    //  热键
    // ══════════════════════════════════════════════

    /** 更新热键按钮文字 */
    private void updateHotkeyButton() {
        if (hotkeyBindButton == null) return;
        if (waitingHotkey) {
            hotkeyBindButton.setMessage(Component.literal("按下按键... (Esc取消)"));
        } else {
            String text = cfg.hotkeyKey >= 0
                    ? "热键: " + getKeyName(cfg.hotkeyKey)
                    : "热键: 未设置";
            hotkeyBindButton.setMessage(Component.literal(text));
        }
    }

    /** 将 GLFW 键码转换为可读名称（常用键） */
    private String getKeyName(int key) {
        if (key <= 0) return "未设置";
        String name = GLFW.glfwGetKeyName(key, GLFW.glfwGetKeyScancode(key));
        if (name != null && !name.isEmpty()) return name.toUpperCase();
        return switch (key) {
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_ESCAPE -> "ESC";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS";
            default -> "KEY_" + key;
        };
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cx = (width - WIDTH) / 2;

        // ── 面板背景（圆角+边框） ──
        GuiRenderHelper.drawPanelBackground(g, cx, cy(0), WIDTH, HEIGHT, false);
        g.drawString(font, "TpAura 如来神掌配置", cx + 10, cy(ROW_TITLE), 0xFFFFFF);

        // ── 攻击机制说明 ──
        String modeHint;
        if ("Smart".equals(cfg.attackMode)) modeHint = "§7满蓄力攻击（推荐）";
        else if ("Universal".equals(cfg.attackMode)) modeHint = "§7通用蓄力（兼容所有武器）";
        else modeHint = "§7零蓄力连打（高速）";
        g.drawString(font, modeHint, cx + 165, cy(ROW_ATTACK_MODE + 2), 0x666666);

        // ── 标签 ──
        g.drawString(font, "蓄力阈值:", cx + 10, cy(ROW_COOLDOWN), 0xAAAAAA);
        g.drawString(font, "§7(0.1~1.0)", cx + 142, cy(ROW_COOLDOWN + 14), 0x666666);
        g.drawString(font, "额外延迟:", cx + 160, cy(ROW_COOLDOWN), 0xAAAAAA);
        g.drawString(font, "§7Tick", cx + 252, cy(ROW_COOLDOWN + 14), 0x666666);
        g.drawString(font, "最大范围:", cx + 10, cy(ROW_RANGE), 0xAAAAAA);
        g.drawString(font, "§7方块", cx + 142, cy(ROW_RANGE + 14), 0x666666);
        g.drawString(font, "垫包数量:", cx + 160, cy(ROW_RANGE), 0xAAAAAA);
        g.drawString(font, "§7(1~20)", cx + 252, cy(ROW_RANGE + 14), 0x666666);

        // ── 全生物提示 ──
        if (cfg.attackAllEntities) {
            g.drawString(font, "§7开启=攻击所有实体（白名单除外）", cx + 135, cy(ROW_ALL_ENTITIES + 2), 0x666666);
        } else {
            g.drawString(font, "§7关闭=仅攻击下方指定类型", cx + 135, cy(ROW_ALL_ENTITIES + 2), 0x666666);
        }

        // ── 目标实体 ──
        g.drawString(font, "§7点击按钮选择目标实体类型", cx + 135, cy(ROW_ENTITY_TYPES + 2), 0x666666);

        // ── 白名单 ──
        g.drawString(font, "§7白名单中的实体不会被攻击", cx + 135, cy(ROW_WHITELIST + 2), 0x666666);
        g.drawString(font, "§7点击按钮选择白名单实体", cx + 135, cy(ROW_WHITELIST_INPUT + 2), 0x666666);

        // ── 热键等待提示 ──
        if (waitingHotkey) {
            g.drawString(font, "§e>> 按下键盘按键绑定热键 <<", cx + 10, cy(ROW_HOTKEY + 20), 0xFFFF00);
        }

        // ── 图腾 ──
        g.drawString(font, "攻击次数:", cx + 10, cy(ROW_TOTEM_INPUT), 0xAAAAAA);
        g.drawString(font, "§7(1~3)", cx + 82, cy(ROW_TOTEM_INPUT + 14), 0x666666);
        g.drawString(font, "递增高度:", cx + 130, cy(ROW_TOTEM_INPUT), 0xAAAAAA);
        g.drawString(font, "§7(1~100)", cx + 252, cy(ROW_TOTEM_INPUT + 14), 0x666666);

        // ── 模式说明 ──
        g.drawString(font, "§7| Paper=垫包+上升 | Vanilla=仅瞬移", cx + 165, cy(ROW_MODE + 2), 0x666666);

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (cooldownInput.mouseClicked(mx, my, button)) { setEditBoxFocus(cooldownInput); return true; }
        if (delayInput.mouseClicked(mx, my, button)) { setEditBoxFocus(delayInput); return true; }
        if (rangeInput.mouseClicked(mx, my, button)) { setEditBoxFocus(rangeInput); return true; }
        if (packetsInput.mouseClicked(mx, my, button)) { setEditBoxFocus(packetsInput); return true; }
        if (totemAtkInput.mouseClicked(mx, my, button)) { setEditBoxFocus(totemAtkInput); return true; }
        if (totemHeightInput.mouseClicked(mx, my, button)) { setEditBoxFocus(totemHeightInput); return true; }
        return super.mouseClicked(mx, my, button);
    }

    private void setEditBoxFocus(EditBox focused) {
        cooldownInput.setFocused(false);
        delayInput.setFocused(false);
        rangeInput.setFocused(false);
        packetsInput.setFocused(false);
        totemAtkInput.setFocused(false);
        totemHeightInput.setFocused(false);
        focused.setFocused(true);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ★ 热键等待模式：按下的键绑定为热键
        if (waitingHotkey) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                waitingHotkey = false;
                updateHotkeyButton();
                return true;
            }
            cfg.setHotkeyKey(keyCode);
            cfg.setHotkeyName(getKeyName(keyCode));
            waitingHotkey = false;
            updateHotkeyButton();
            return true;
        }

        if (cooldownInput.isFocused() && cooldownInput.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (delayInput.isFocused() && delayInput.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (rangeInput.isFocused() && rangeInput.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (packetsInput.isFocused() && packetsInput.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (totemAtkInput.isFocused() && totemAtkInput.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (totemHeightInput.isFocused() && totemHeightInput.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (keyCode == 256) { onClose(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (cooldownInput.isFocused() && cooldownInput.charTyped(codePoint, modifiers)) return true;
        if (delayInput.isFocused() && delayInput.charTyped(codePoint, modifiers)) return true;
        if (rangeInput.isFocused() && rangeInput.charTyped(codePoint, modifiers)) return true;
        if (packetsInput.isFocused() && packetsInput.charTyped(codePoint, modifiers)) return true;
        if (totemAtkInput.isFocused() && totemAtkInput.charTyped(codePoint, modifiers)) return true;
        if (totemHeightInput.isFocused() && totemHeightInput.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    private void saveConfig() {
        try { cfg.setCooldownThreshold(Double.parseDouble(cooldownInput.getValue())); } catch (Exception ignored) {}
        try { cfg.setAttackDelay(Integer.parseInt(delayInput.getValue())); } catch (Exception ignored) {}
        try { cfg.setMaxRange(Double.parseDouble(rangeInput.getValue())); } catch (Exception ignored) {}
        try { cfg.setPaperPackets(Integer.parseInt(packetsInput.getValue())); } catch (Exception ignored) {}
        try { cfg.setTotemAttacks(Integer.parseInt(totemAtkInput.getValue())); } catch (Exception ignored) {}
        try { cfg.setTotemHeightIncrease(Integer.parseInt(totemHeightInput.getValue())); } catch (Exception ignored) {}
        TpAuraConfig.save();
    }

    private int cy(int rowOffset) {
        return (height - HEIGHT) / 2 + rowOffset;
    }

    /** 攻击模式显示名称（支持3种模式） */
    private String getAttackModeLabel(String mode) {
        return switch (mode) {
            case "Smart" -> "满蓄力攻击";
            case "Fast" -> "零蓄力连打";
            case "Universal" -> "通用蓄力（全武器）";
            default -> mode;
        };
    }

    private String getModeLabel(String mode) {
        return "Paper".equals(mode) ? "Paper (垫包)" : "Vanilla (原版)";
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        saveConfig();
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}