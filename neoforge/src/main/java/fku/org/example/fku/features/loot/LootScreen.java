package fku.org.example.fku.features.loot; /* water */

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

/**
 * 一键取物（Loot Nearby Containers）配置屏幕
 *
 * ★ 职责：
 *   提供扫描半径、点击延迟、容器延迟、背包满丢弃、自动关闭GUI、热键等参数的可视化配置。
 */
public class LootScreen extends Screen {

    private static final int WIDTH = 290;
    private static final int HEIGHT = 320;

    // ════════ 行偏移常量 ════════
    private static final int ROW_RADIUS = 35;
    private static final int ROW_CLICK_DELAY = 58;
    private static final int ROW_CONTAINER_DELAY = 81;
    private static final int ROW_SCAN_INTERVAL = 104;
    private static final int ROW_DROP = 127;
    private static final int ROW_AUTO_CLOSE = 150;
    private static final int ROW_HOTKEY = 178;
    private static final int ROW_SAVE = 220;

    private EditBox radiusField;
    private EditBox clickDelayField;
    private EditBox containerDelayField;
    private EditBox scanIntervalField;
    private Button dropOverflowButton;
    private Button autoCloseButton;
    private Button hotkeyBindButton;

    /** 本地热键绑定等待状态 */
    private boolean waitingHotkey = false;

    private final LootConfig cfg = LootConfig.getInstance();

    public LootScreen() {
        super(Component.literal("一键取物配置"));
    }

    @Override
    protected void init() {
        super.init();
        int cx = (width - WIDTH) / 2;
        int cy = (height - HEIGHT) / 2;

        // ── 扫描半径 ──
        radiusField = createEditBox(cx + 100, cy + ROW_RADIUS, String.valueOf(cfg.radius), 2);
        // ── 点击延迟 ──
        clickDelayField = createEditBox(cx + 100, cy + ROW_CLICK_DELAY, String.valueOf(cfg.clickDelay), 3);
        // ── 容器间隔 ──
        containerDelayField = createEditBox(cx + 100, cy + ROW_CONTAINER_DELAY, String.valueOf(cfg.containerDelay), 4);
        // ── 扫描刷新间隔 ──
        scanIntervalField = createEditBox(cx + 150, cy + ROW_SCAN_INTERVAL, String.valueOf(cfg.scanRefreshInterval), 3);

        // ── 背包满丢弃 ──
        dropOverflowButton = Button.builder(
                Component.literal(cfg.dropOverflow ? "§a开启" : "§c关闭"),
                btn -> {
                    cfg.setDropOverflow(!cfg.dropOverflow);
                    btn.setMessage(Component.literal(cfg.dropOverflow ? "§a开启" : "§c关闭"));
                })
                .bounds(cx + 185, cy + ROW_DROP, 60, 18).build();
        addRenderableWidget(dropOverflowButton);

        // ── 自动关闭GUI ──
        autoCloseButton = Button.builder(
                Component.literal(cfg.autoCloseGUI ? "§a开启" : "§c关闭"),
                btn -> {
                    cfg.setAutoCloseGUI(!cfg.autoCloseGUI);
                    btn.setMessage(Component.literal(cfg.autoCloseGUI ? "§a开启" : "§c关闭"));
                })
                .bounds(cx + 185, cy + ROW_AUTO_CLOSE, 60, 18).build();
        addRenderableWidget(autoCloseButton);

        // ════════ ★ 热键绑定 ════════
        {
            String hotkeyText = cfg.hotkeyKey >= 0
                    ? "热键: " + getKeyName(cfg.hotkeyKey)
                    : "热键: 未设置";
            hotkeyBindButton = Button.builder(
                    Component.literal(hotkeyText),
                    btn -> {
                        waitingHotkey = !waitingHotkey;
                        updateHotkeyButton();
                    }
            ).bounds(cx + 10, cy + ROW_HOTKEY, 185, 18).build();
            addRenderableWidget(hotkeyBindButton);

            addRenderableWidget(Button.builder(
                    Component.literal("清除"),
                    btn -> {
                        cfg.setHotkeyKey(-1);
                        cfg.setHotkeyName("");
                        waitingHotkey = false;
                        updateHotkeyButton();
                    }
            ).bounds(cx + 205, cy + ROW_HOTKEY, 55, 18).build());
        }

        // ── 保存 ──
        addRenderableWidget(Button.builder(
                Component.literal("保存"),
                btn -> saveConfig()
        ).bounds(cx + 105, cy + ROW_SAVE, 80, 20).build());
    }

    /** 简化 EditBox 创建 */
    private EditBox createEditBox(int x, int y, String value, int maxLen) {
        EditBox box = new EditBox(font, x, y, 50, 18, Component.empty());
        box.setValue(value);
        box.setMaxLength(maxLen);
        addRenderableWidget(box);
        return box;
    }

    // ══════════════════════════════════════════════
    //  热键
    // ══════════════════════════════════════════════

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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingHotkey) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                waitingHotkey = false;
                updateHotkeyButton();
                return true;
            }
            cfg.setHotkeyKey(keyCode);
            String keyName = GLFW.glfwGetKeyName(keyCode, scanCode);
            if (keyName == null || keyName.isEmpty()) {
                keyName = getKeyName(keyCode);
            } else {
                keyName = keyName.toUpperCase();
            }
            cfg.setHotkeyName(keyName);
            waitingHotkey = false;
            updateHotkeyButton();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ══════════════════════════════════════════════
    //  保存 / 渲染
    // ══════════════════════════════════════════════

    private void saveConfig() {
        try { cfg.setRadius(Integer.parseInt(radiusField.getValue())); } catch (NumberFormatException ignored) {}
        try { cfg.setClickDelay(Integer.parseInt(clickDelayField.getValue())); } catch (NumberFormatException ignored) {}
        try { cfg.setContainerDelay(Integer.parseInt(containerDelayField.getValue())); } catch (NumberFormatException ignored) {}
        try { cfg.setScanRefreshInterval(Integer.parseInt(scanIntervalField.getValue())); } catch (NumberFormatException ignored) {}
        onClose();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        int cx = (width - WIDTH) / 2;
        int cy = (height - HEIGHT) / 2;

        GuiRenderHelper.drawPanelBackground(guiGraphics, cx, cy, WIDTH, HEIGHT, false);
        guiGraphics.drawString(font, "一键取物配置", cx + 10, cy + 10, 0xFFFFFF);

        // 字段说明
        guiGraphics.drawString(font, "扫描半径:", cx + 12, cy + ROW_RADIUS + 2, 0xAAAAAA);
        guiGraphics.drawString(font, "物品点击间隔(ms):", cx + 12, cy + ROW_CLICK_DELAY + 2, 0xAAAAAA);
        guiGraphics.drawString(font, "容器间隔(ms):", cx + 12, cy + ROW_CONTAINER_DELAY + 2, 0xAAAAAA);
        guiGraphics.drawString(font, "刷新间隔(tick):", cx + 66, cy + ROW_SCAN_INTERVAL + 2, 0xAAAAAA);
        guiGraphics.drawString(font, "背包满丢弃:", cx + 100, cy + ROW_DROP + 2, 0xAAAAAA);
        guiGraphics.drawString(font, "自动关闭GUI:", cx + 100, cy + ROW_AUTO_CLOSE + 2, 0xAAAAAA);

        // 热键提示
        if (waitingHotkey) {
            guiGraphics.drawString(font, "§e请在键盘上按下要绑定的按键...", cx + 10, cy + ROW_HOTKEY + 22, 0xFFFFAA);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        Minecraft minecraft = this.minecraft;
        if (minecraft != null) {
            minecraft.setScreen(new ClickGuiScreen());
        }
    }
}