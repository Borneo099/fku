package fku.org.example.fku.features.quickswitch;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.quickswitch.QuickSwitchConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class QuickSwitchConfigScreen
extends Screen {
    private static final int W = 300;
    private static final int H = 220;
    private int cx;
    private int cy;
    private EditBox customItemsInput;
    private EditBox rttDelayBox;
    private Button modeBtn;
    private Button visualBtn;
    private Button saveBtn;

    public QuickSwitchConfigScreen() {
        super(Component.literal((String)"\u9b3c\u624b\u79d2\u5207\u914d\u7f6e"));
    }

    protected void init() {
        super.init();
        this.cx = (this.width - 300) / 2;
        this.cy = (this.height - 220) / 2;
        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();
        this.modeBtn = Button.builder(Component.literal((String)("\u6a21\u5f0f: " + QuickSwitchConfigScreen.modeLabel(cfg.mode))), b -> {
            cfg.mode = QuickSwitchConfigScreen.cycleMode(cfg.mode);
            cfg.save();
            b.setMessage(Component.literal((String)("\u6a21\u5f0f: " + QuickSwitchConfigScreen.modeLabel(cfg.mode))));
        }).bounds(this.cx + 10, this.cy + 30, 130, 18).build();
        this.addRenderableWidget(this.modeBtn);
        this.visualBtn = Button.builder(Component.literal((String)("\u89c6\u89c9\u53cd\u9988: " + (cfg.visualFeedback ? "\u5f00" : "\u5173"))), b -> {
            cfg.visualFeedback = !cfg.visualFeedback;
            cfg.save();
            b.setMessage(Component.literal((String)("\u89c6\u89c9\u53cd\u9988: " + (cfg.visualFeedback ? "\u5f00" : "\u5173"))));
        }).bounds(this.cx + 150, this.cy + 30, 130, 18).build();
        this.addRenderableWidget(this.visualBtn);
        this.customItemsInput = new EditBox(this.font, this.cx + 10, this.cy + 75, 280, 16, Component.literal((String)"\u7269\u54c1\u5217\u8868"));
        this.customItemsInput.m_94199_(100000);
        this.customItemsInput.m_94144_(cfg.customItems);
        this.addRenderableWidget(this.customItemsInput);
        this.rttDelayBox = new EditBox(this.font, this.cx + 10, this.cy + 110, 100, 16, Component.literal((String)"\u5ef6\u8fdf(ms)"));
        this.rttDelayBox.m_94199_(5);
        this.rttDelayBox.m_94144_(String.valueOf(cfg.rttDelay));
        this.rttDelayBox.m_94153_(s -> s.matches("\\d*"));
        this.addRenderableWidget(this.rttDelayBox);
        this.addRenderableWidget(Button.builder(Component.literal((String)("\u4f18\u5148\u7ea7\u69fd\u4f4d: " + QuickSwitchConfigScreen.intArrStr(cfg.prioritySlots))), b -> {}).bounds(this.cx + 120, this.cy + 110, 160, 16).build());
        this.saveBtn = Button.builder(Component.literal((String)"\u00a7a\u4fdd\u5b58\u5e76\u8fd4\u56de"), b -> this.saveAndClose()).bounds(this.cx + 150 - 40, this.cy + 220 - 24, 80, 16).build();
        this.addRenderableWidget(this.saveBtn);
    }

    private void saveAndClose() {
        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();
        cfg.customItems = this.customItemsInput.m_94155_();
        try {
            int v = Integer.parseInt(this.rttDelayBox.m_94155_().trim());
            cfg.rttDelay = v < 0 ? 0 : (v > 2000 ? 2000 : v);
        }
        catch (NumberFormatException numberFormatException) {
        }
        cfg.save();
        this.minecraft.setScreen(null);
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        this.fillGradient(g);
        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();
        GuiRenderHelper.drawPanelBackground(g, this.cx, this.cy, 300, 220, false);
        g.drawString(this.font, "\u00a7l\u00a7bQuickSwitch \u9b3c\u624b\u79d2\u5207", this.cx + 10, this.cy + 10, 0xFFFFFF);
        String modeDesc = switch (cfg.mode) {
            case "SMART" -> "\u667a\u80fd: \u9644\u9b54\u8bc4\u5206\u6700\u9ad8\u6b66\u5668";
            case "CUSTOM" -> "\u81ea\u5b9a\u4e49: \u6309\u5217\u8868\u987a\u5e8f\u5207\u6362";
            default -> "\u5173\u95ed: \u529f\u80fd\u672a\u542f\u7528";
        };
        g.drawString(this.font, "\u00a77" + modeDesc, this.cx + 10, this.cy + 54, 0x888888);
        g.drawString(this.font, "\u00a77\u7269\u54c1\u5217\u8868(\u9017\u53f7\u5206\u9694):", this.cx + 10, this.cy + 98, 0xAAAAAA);
        g.drawString(this.font, "\u00a77\u5ef6\u8fdf(ms, 0-2000):", this.cx + 10, this.cy + 128, 0xAAAAAA);
        g.drawString(this.font, "\u00a77\u00a7o\u70b9\u51fb\u300c\u4fdd\u5b58\u5e76\u8fd4\u56de\u300d\u6216\u6309 ESC \u9000\u51fa\u5e76\u4fdd\u5b58", this.cx + 10, this.cy + 220 - 14, 0x666666);
        super.render(g, mx, my, pt);
    }

    public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.saveAndClose();
            return true;
        }
        return super.m_7933_(keyCode, scanCode, modifiers);
    }

    public void onClose() {
        this.saveAndClose();
        super.onClose();
    }

    public boolean isPauseScreen() {
        return false;
    }

    private static String modeLabel(String m) {
        return switch (m) {
            case "SMART" -> "\u667a\u80fd";
            case "CUSTOM" -> "\u81ea\u5b9a\u4e49";
            default -> "\u5173\u95ed";
        };
    }

    private static String cycleMode(String m) {
        return switch (m) {
            case "OFF" -> "SMART";
            case "SMART" -> "CUSTOM";
            default -> "OFF";
        };
    }

    private static String intArrStr(int[] arr) {
        if (arr == null || arr.length == 0) {
            return "\u65e0";
        }
        StringBuilder sb = new StringBuilder();
        for (int v : arr) {
            sb.append(v).append(",");
        }
        return sb.substring(0, sb.length() - 1);
    }
}

