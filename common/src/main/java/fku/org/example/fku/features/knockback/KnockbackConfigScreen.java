package fku.org.example.fku.features.knockback;

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.knockback.KnockbackConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class KnockbackConfigScreen
extends Screen {
    private static final int WIDTH = 290;
    private static final int HEIGHT = 300;
    private static final int ROW_MODE_LABEL = 30;
    private static final int ROW_MODE_BTN = 44;
    private static final int ROW_CUSTOM = 72;
    private static final int ROW_CLIFF = 106;
    private static final int ROW_SMOOTH_TOGGLE = 140;
    private static final int ROW_SMOOTH_STEPS = 168;
    private static final int ROW_DELAY = 202;
    private static final int ROW_AGGRESSIVE = 236;
    private static final int ROW_BUTTON = 270;
    private final KnockbackConfig cfg = KnockbackConfig.getInstance();
    private Button modeButton;
    private final String[] modes = new String[]{"PUSHBACK", "PULLBACK", "CLIFF", "CUSTOM"};
    private final String[] modeLabels = new String[]{"\u63a8\u79bb", "\u62c9\u56de", "\u60ac\u5d16", "\u81ea\u5b9a\u4e49"};
    private EditBox customYawInput;
    private EditBox cliffRadiusInput;
    private EditBox smoothStepsInput;
    private EditBox delayInput;
    private Button smoothToggleButton;
    private Button aggressiveButton;
    private int scrollOffset = 0;

    public KnockbackConfigScreen() {
        super(Component.literal((String)"\u81ea\u7531\u51fb\u9000\u914d\u7f6e"));
    }

    protected void init() {
        super.init();
        int cx = (this.width - 290) / 2;
        String currentModeLabel = this.getModeLabel(this.cfg.mode);
        this.modeButton = Button.builder(Component.literal((String)("\u6a21\u5f0f: " + currentModeLabel)), btn -> {
            int idx = 0;
            for (int i = 0; i < this.modes.length; ++i) {
                if (!this.modes[i].equals(this.cfg.mode)) continue;
                idx = (i + 1) % this.modes.length;
                break;
            }
            this.cfg.setMode(this.modes[idx]);
            btn.setMessage(Component.literal((String)("\u6a21\u5f0f: " + this.getModeLabel(this.modes[idx]))));
            this.rebuildWidgets();
        }).bounds(cx + 10, this.cy(44), 120, 18).build();
        this.addRenderableWidget(this.modeButton);
        this.customYawInput = new EditBox(this.font, cx + 80, this.cy(86), 60, 14, Component.literal((String)""));
        this.customYawInput.m_94144_(String.format("%.0f", this.cfg.customYaw)));
        this.customYawInput.m_94199_(6);
        this.customYawInput.m_94153_(s -> s.matches("-?\\d*\\.?\\d*"));
        this.customYawInput.m_94194_("CUSTOM".equals(this.cfg.mode));
        this.addRenderableWidget(this.customYawInput);
        this.cliffRadiusInput = new EditBox(this.font, cx + 80, this.cy(120), 40, 14, Component.literal((String)""));
        this.cliffRadiusInput.m_94144_(String.valueOf(this.cfg.cliffSearchRadius));
        this.cliffRadiusInput.m_94199_(2);
        this.cliffRadiusInput.m_94153_(s -> s.matches("\\d*"));
        this.cliffRadiusInput.m_94194_("CLIFF".equals(this.cfg.mode));
        this.addRenderableWidget(this.cliffRadiusInput);
        this.smoothToggleButton = Button.builder(Component.literal((String)("\u5e73\u6ed1\u65cb\u8f6c: " + (this.cfg.smoothRotation ? "\u5f00" : "\u5173"))), btn -> {
            this.cfg.setSmoothRotation(!this.cfg.smoothRotation);
            btn.setMessage(Component.literal((String)("\u5e73\u6ed1\u65cb\u8f6c: " + (this.cfg.smoothRotation ? "\u5f00" : "\u5173"))));
            this.rebuildWidgets();
        }).bounds(cx + 10, this.cy(140), 110, 18).build();
        this.addRenderableWidget(this.smoothToggleButton);
        this.smoothStepsInput = new EditBox(this.font, cx + 80, this.cy(182), 40, 14, Component.literal((String)""));
        this.smoothStepsInput.m_94144_(String.valueOf(this.cfg.smoothSteps));
        this.smoothStepsInput.m_94199_(2);
        this.smoothStepsInput.m_94153_(s -> s.matches("\\d*"));
        this.smoothStepsInput.m_94194_(this.cfg.smoothRotation);
        this.addRenderableWidget(this.smoothStepsInput);
        this.delayInput = new EditBox(this.font, cx + 80, this.cy(216), 40, 14, Component.literal((String)""));
        this.delayInput.m_94144_(String.valueOf(this.cfg.rotationDelay));
        this.delayInput.m_94199_(1);
        this.delayInput.m_94153_(s -> s.isEmpty() || s.matches("[0-5]"));
        this.addRenderableWidget(this.delayInput);
        this.aggressiveButton = Button.builder(Component.literal((String)("\u6fc0\u8fdb\u6a21\u5f0f: " + (this.cfg.aggressiveMode ? "\u5f00" : "\u5173"))), btn -> {
            this.cfg.setAggressiveMode(!this.cfg.aggressiveMode);
            btn.setMessage(Component.literal((String)("\u6fc0\u8fdb\u6a21\u5f0f: " + (this.cfg.aggressiveMode ? "\u5f00" : "\u5173"))));
        }).bounds(cx + 10, this.cy(236), 110, 18).build();
        this.addRenderableWidget(this.aggressiveButton);
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u4fdd\u5b58"), btn -> this.saveConfig()).bounds(cx + 70, this.cy(270), 60, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u5b8c\u6210"), btn -> {
            this.saveConfig();
            Minecraft.getInstance().setScreen(new ClickGuiScreen());
        }).bounds(cx + 150, this.cy(270), 60, 18).build());
    }

    private String getModeLabel(String mode) {
        for (int i = 0; i < this.modes.length; ++i) {
            if (!this.modes[i].equals(mode)) continue;
            return this.modeLabels[i];
        }
        return mode;
    }

    private void saveConfig() {
        try {
            float yaw = Float.parseFloat(this.customYawInput.m_94155_());
            yaw = Math.max(-180.0f, Math.min(180.0f, yaw));
            this.cfg.setCustomYaw(yaw);
        }
        catch (Exception yaw) {
            // ignored
        }
        try {
            int radius = Integer.parseInt(this.cliffRadiusInput.m_94155_());
            radius = Math.max(1, Math.min(20, radius));
            this.cfg.setCliffSearchRadius(radius);
        }
        catch (Exception radius) {
            // ignored
        }
        try {
            int steps = Integer.parseInt(this.smoothStepsInput.m_94155_());
            steps = Math.max(2, Math.min(10, steps));
            this.cfg.setSmoothSteps(steps);
        }
        catch (Exception steps) {
            // ignored
        }
        try {
            int delay = Integer.parseInt(this.delayInput.m_94155_());
            delay = Math.max(0, Math.min(5, delay));
            this.cfg.setRotationDelay(delay);
        }
        catch (Exception exception) {
            // ignored
        }
        KnockbackConfig.save();
    }

    private int cy(int rowOffset) {
        return (this.height - 300) / 2 + rowOffset - this.scrollOffset;
    }

    public boolean m_6050_(double mouseX, double mouseY, double delta) {
        int cx = (this.width - 290) / 2;
        int cy2 = (this.height - 300) / 2;
        if (mouseX >= cx && mouseX <= (cx + 290) && mouseY >= cy2 && mouseY <= (cy2 + 300)) {
            this.scrollOffset = Math.max(0, this.scrollOffset - (delta * 20.0));
            this.init();
            return true;
        }
        return super.m_6050_(mouseX, mouseY, delta);
    }

    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.fillGradient(g);
        int cx = (this.width - 290) / 2;
        int cy = this.cy(0);
        GuiRenderHelper.drawPanelBackground(g, cx, this.cy(0), 290, 300, false);
        g.m_280588_(cx + 2, cy + 20, cx + 290 - 2, cy + 300 - 30);
        g.drawString(this.font, "\u81ea\u7531\u51fb\u9000\u914d\u7f6e", cx + 10, this.cy(8), 0xFFFFFF);
        boolean isCustom = "CUSTOM".equals(this.cfg.mode);
        boolean isCliff = "CLIFF".equals(this.cfg.mode);
        g.drawString(this.font, "\u00a77| \u51fb\u9000\u65b9\u5411\u6a21\u5f0f", cx + 135, this.cy(46), 0x666666);
        if (isCustom) {
            g.drawString(this.font, "\u81ea\u5b9a\u4e49\u89d2\u5ea6:", cx + 10, this.cy(72), 0xAAAAAA);
            g.drawString(this.font, "\u00a77(-180~180\u00b0)", cx + 142, this.cy(86), 0x666666);
        }
        if (isCliff) {
            g.drawString(this.font, "\u641c\u7d22\u534a\u5f84:", cx + 10, this.cy(106), 0xAAAAAA);
            g.drawString(this.font, "\u00a77(1~20 \u65b9\u5757)", cx + 122, this.cy(120), 0x666666);
        }
        if (this.cfg.smoothRotation) {
            g.drawString(this.font, "\u5e73\u6ed1\u6b65\u6570:", cx + 10, this.cy(168), 0xAAAAAA);
            g.drawString(this.font, "\u00a77(2~10)", cx + 122, this.cy(182), 0x666666);
        }
        g.drawString(this.font, "\u65cb\u8f6c\u5ef6\u8fdf:", cx + 10, this.cy(202), 0xAAAAAA);
        g.drawString(this.font, "\u00a77(Tick, 0~5)", cx + 122, this.cy(216), 0x666666);
        g.drawString(this.font, "\u00a77\u653b\u51fb\u540e\u5ef6\u8fdf\u591a\u5c11 Tick \u6062\u590d\u539f\u59cb\u65cb\u8f6c", cx + 10, this.cy(230), 0x666666);
        g.drawString(this.font, "\u00a77| \u5f3a\u5236\u53d1\u9001\u65cb\u8f6c\u5305\uff08\u65e0\u89c6\u65cb\u8f6c\u68c0\u6d4b\uff09", cx + 125, this.cy(238), 0x666666);
        String modeHint = switch (this.cfg.mode) {
            case "PUSHBACK" -> "\u00a77\u5c06\u76ee\u6807\u63a8\u79bb\u73a9\u5bb6";
            case "PULLBACK" -> "\u00a77\u5c06\u76ee\u6807\u62c9\u5411\u73a9\u5bb6";
            case "CLIFF" -> "\u00a77\u5c06\u76ee\u6807\u51fb\u9000\u5411\u6700\u8fd1\u7684\u60ac\u5d16\u65b9\u5411";
            case "CUSTOM" -> "\u00a77\u57fa\u4e8e\u73a9\u5bb6\u89c6\u89d2\u504f\u79fb\u6307\u5b9a\u89d2\u5ea6\u51fb\u9000\uff08\u598290\u00b0=\u53f3\u4fa7\u51fb\u9000\uff09";
            default -> "";
        };
        g.drawString(this.font, modeHint, cx + 10, this.cy(30), 0x888888);
        g.m_280618_();
        super.render(g, mouseX, mouseY, partialTick);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.customYawInput.mouseClicked(mouseX, mouseY, button)) {
            this.setEditBoxFocus(this.customYawInput);
            return true;
        }
        if (this.cliffRadiusInput.mouseClicked(mouseX, mouseY, button)) {
            this.setEditBoxFocus(this.cliffRadiusInput);
            return true;
        }
        if (this.smoothStepsInput.mouseClicked(mouseX, mouseY, button)) {
            this.setEditBoxFocus(this.smoothStepsInput);
            return true;
        }
        if (this.delayInput.mouseClicked(mouseX, mouseY, button)) {
            this.setEditBoxFocus(this.delayInput);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void setEditBoxFocus(EditBox focused) {
        this.customYawInput.m_93692_(false);
        this.cliffRadiusInput.m_93692_(false);
        this.smoothStepsInput.m_93692_(false);
        this.delayInput.m_93692_(false);
        focused.m_93692_(true);
    }

    public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
        if (this.customYawInput.m_93696_() && this.customYawInput.m_7933_(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.cliffRadiusInput.m_93696_() && this.cliffRadiusInput.m_7933_(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.smoothStepsInput.m_93696_() && this.smoothStepsInput.m_7933_(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.delayInput.m_93696_() && this.delayInput.m_7933_(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == 256) {
            this.onClose();
            return true;
        }
        return super.m_7933_(keyCode, scanCode, modifiers);
    }

    public boolean m_5534_(char codePoint, int modifiers) {
        if (this.customYawInput.m_93696_() && this.customYawInput.m_5534_(codePoint, modifiers)) {
            return true;
        }
        if (this.cliffRadiusInput.m_93696_() && this.cliffRadiusInput.m_5534_(codePoint, modifiers)) {
            return true;
        }
        if (this.smoothStepsInput.m_93696_() && this.smoothStepsInput.m_5534_(codePoint, modifiers)) {
            return true;
        }
        if (this.delayInput.m_93696_() && this.delayInput.m_5534_(codePoint, modifiers)) {
            return true;
        }
        return super.m_5534_(codePoint, modifiers);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        this.saveConfig();
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}

