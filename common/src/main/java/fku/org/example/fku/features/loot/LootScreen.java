package fku.org.example.fku.features.loot;

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.loot.LootConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class LootScreen
extends Screen {
    private static final int WIDTH = 290;
    private static final int HEIGHT = 320;
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
    private boolean waitingHotkey = false;
    private final LootConfig cfg = LootConfig.getInstance();

    public LootScreen() {
        super(Component.literal((String)"\u4e00\u952e\u53d6\u7269\u914d\u7f6e"));
    }

    protected void init() {
        super.init();
        int cx = (this.width - 290) / 2;
        int cy = (this.height - 320) / 2;
        this.radiusField = this.createEditBox(cx + 100, cy + 35, String.valueOf(this.cfg.radius), 2);
        this.clickDelayField = this.createEditBox(cx + 100, cy + 58, String.valueOf(this.cfg.clickDelay), 3);
        this.containerDelayField = this.createEditBox(cx + 100, cy + 81, String.valueOf(this.cfg.containerDelay), 4);
        this.scanIntervalField = this.createEditBox(cx + 150, cy + 104, String.valueOf(this.cfg.scanRefreshInterval), 3);
        this.dropOverflowButton = Button.builder(Component.literal((String)(this.cfg.dropOverflow ? "\u00a7a\u5f00\u542f" : "\u00a7c\u5173\u95ed")), btn -> {
            this.cfg.setDropOverflow(!this.cfg.dropOverflow);
            btn.setMessage(Component.literal((String)(this.cfg.dropOverflow ? "\u00a7a\u5f00\u542f" : "\u00a7c\u5173\u95ed")));
        }).bounds(cx + 185, cy + 127, 60, 18).build();
        this.addRenderableWidget(this.dropOverflowButton);
        this.autoCloseButton = Button.builder(Component.literal((String)(this.cfg.autoCloseGUI ? "\u00a7a\u5f00\u542f" : "\u00a7c\u5173\u95ed")), btn -> {
            this.cfg.setAutoCloseGUI(!this.cfg.autoCloseGUI);
            btn.setMessage(Component.literal((String)(this.cfg.autoCloseGUI ? "\u00a7a\u5f00\u542f" : "\u00a7c\u5173\u95ed")));
        }).bounds(cx + 185, cy + 150, 60, 18).build();
        this.addRenderableWidget(this.autoCloseButton);
        String hotkeyText = this.cfg.hotkeyKey >= 0 ? "\u70ed\u952e: " + this.getKeyName(this.cfg.hotkeyKey) : "\u70ed\u952e: \u672a\u8bbe\u7f6e";
        this.hotkeyBindButton = Button.builder(Component.literal((String)hotkeyText), btn -> {
            this.waitingHotkey = !this.waitingHotkey;
            this.updateHotkeyButton();
        }).bounds(cx + 10, cy + 178, 185, 18).build();
        this.addRenderableWidget(this.hotkeyBindButton);
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u6e05\u9664"), btn -> {
            this.cfg.setHotkeyKey(-1);
            this.cfg.setHotkeyName("");
            this.waitingHotkey = false;
            this.updateHotkeyButton();
        }).bounds(cx + 205, cy + 178, 55, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u4fdd\u5b58"), btn -> this.saveConfig()).bounds(cx + 105, cy + 220, 80, 20).build());
    }

    private EditBox createEditBox(int x, int y, String value, int maxLen) {
        EditBox box = new EditBox(this.font, x, y, 50, 18, (Component)Component.m_237119_());
        box.m_94144_(value);
        box.m_94199_(maxLen);
        this.addRenderableWidget(box);
        return box;
    }

    private void updateHotkeyButton() {
        if (this.hotkeyBindButton == null) {
            return;
        }
        if (this.waitingHotkey) {
            this.hotkeyBindButton.setMessage(Component.literal((String)"\u6309\u4e0b\u6309\u952e. (Esc\u53d6\u6d88)"));
        } else {
            String text = this.cfg.hotkeyKey >= 0 ? "\u70ed\u952e: " + this.getKeyName(this.cfg.hotkeyKey) : "\u70ed\u952e: \u672a\u8bbe\u7f6e";
            this.hotkeyBindButton.setMessage(Component.literal((String)text));
        }
    }

    private String getKeyName(int key) {
        if (key <= 0) {
            return "\u672a\u8bbe\u7f6e";
        }
        String name = GLFW.glfwGetKeyName(key, GLFW.glfwGetKeyScancode(key));
        if (name != null && !name.isEmpty()) {
            return name.toUpperCase();
        }
        return switch (key) {
            case 340 -> "LSHIFT";
            case 344 -> "RSHIFT";
            case 341 -> "LCTRL";
            case 345 -> "RCTRL";
            case 342 -> "LALT";
            case 346 -> "RALT";
            case 32 -> "SPACE";
            case 258 -> "TAB";
            case 256 -> "ESC";
            case 257 -> "ENTER";
            case 280 -> "CAPS";
            default -> "KEY_" + key;
        };
    }

    public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
        if (this.waitingHotkey) {
            if (keyCode == 256) {
                this.waitingHotkey = false;
                this.updateHotkeyButton();
                return true;
            }
            this.cfg.setHotkeyKey(keyCode);
            String keyName = GLFW.glfwGetKeyName(keyCode, scanCode);
            keyName = keyName == null || keyName.isEmpty() ? this.getKeyName(keyCode) : keyName.toUpperCase();
            this.cfg.setHotkeyName(keyName);
            this.waitingHotkey = false;
            this.updateHotkeyButton();
            return true;
        }
        return super.m_7933_(keyCode, scanCode, modifiers);
    }

    private void saveConfig() {
        try {
            this.cfg.setRadius(Integer.parseInt(this.radiusField.m_94155_()));
        }
        catch (NumberFormatException numberFormatException) {
            // ignored
        }
        try {
            this.cfg.setClickDelay(Integer.parseInt(this.clickDelayField.m_94155_()));
        }
        catch (NumberFormatException numberFormatException) {
            // ignored
        }
        try {
            this.cfg.setContainerDelay(Integer.parseInt(this.containerDelayField.m_94155_()));
        }
        catch (NumberFormatException numberFormatException) {
            // ignored
        }
        try {
            this.cfg.setScanRefreshInterval(Integer.parseInt(this.scanIntervalField.m_94155_()));
        }
        catch (NumberFormatException numberFormatException) {
            // ignored
        }
        this.onClose();
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.fillGradient(guiGraphics);
        int cx = (this.width - 290) / 2;
        int cy = (this.height - 320) / 2;
        GuiRenderHelper.drawPanelBackground(guiGraphics, cx, cy, 290, 320, false);
        guiGraphics.drawString(this.font, "\u4e00\u952e\u53d6\u7269\u914d\u7f6e", cx + 10, cy + 10, 0xFFFFFF);
        guiGraphics.drawString(this.font, "\u626b\u63cf\u534a\u5f84:", cx + 12, cy + 35 + 2, 0xAAAAAA);
        guiGraphics.drawString(this.font, "\u7269\u54c1\u70b9\u51fb\u95f4\u9694(ms):", cx + 12, cy + 58 + 2, 0xAAAAAA);
        guiGraphics.drawString(this.font, "\u5bb9\u5668\u95f4\u9694(ms):", cx + 12, cy + 81 + 2, 0xAAAAAA);
        guiGraphics.drawString(this.font, "\u5237\u65b0\u95f4\u9694(tick):", cx + 66, cy + 104 + 2, 0xAAAAAA);
        guiGraphics.drawString(this.font, "\u80cc\u5305\u6ee1\u4e22\u5f03:", cx + 100, cy + 127 + 2, 0xAAAAAA);
        guiGraphics.drawString(this.font, "\u81ea\u52a8\u5173\u95edGUI:", cx + 100, cy + 150 + 2, 0xAAAAAA);
        if (this.waitingHotkey) {
            guiGraphics.drawString(this.font, "\u00a7e\u8bf7\u5728\u952e\u76d8\u4e0a\u6309\u4e0b\u8981\u7ed1\u5b9a\u7684\u6309\u952e.", cx + 10, cy + 178 + 22, 0xFFFFAA);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        Minecraft minecraft = this.minecraft;
        if (minecraft != null) {
            minecraft.setScreen(new ClickGuiScreen());
        }
    }
}

