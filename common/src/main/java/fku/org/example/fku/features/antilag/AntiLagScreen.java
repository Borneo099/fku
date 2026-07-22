package fku.org.example.fku.features.antilag;

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.antilag.AntiLagConfig;
import fku.org.example.fku.features.antilag.AntiLagFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class AntiLagScreen
extends Screen {
    private static final int WIDTH = 310;
    private static final int HEIGHT = 340;
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
        super(Component.literal((String)"\u9632\u62c9\u56de\u914d\u7f6e"));
    }

    protected void init() {
        super.init();
        int cx = (this.width - 310) / 2;
        this.serverModeButton = Button.builder(Component.literal((String)("\u6a21\u5f0f: " + this.getVersionLabel(this.cfg.serverVersionMode))), btn -> {
            String next = "MC1_16".equals(this.cfg.serverVersionMode) ? "MC1_9" : "MC1_16";
            this.cfg.setServerVersionMode(next);
            btn.setMessage(Component.literal((String)("\u6a21\u5f0f: " + this.getVersionLabel(next))));
        }).bounds(cx + 10, this.cy(30), 160, 18).build();
        this.addRenderableWidget(this.serverModeButton);
        this.rangeInput = new EditBox(this.font, cx + 80, this.cy(70), 60, 14, Component.literal((String)""));
        this.rangeInput.setValue(String.format("%.1f", this.cfg.range));
        this.rangeInput.setMaxLength(6);
        this.rangeInput.setFilter(s -> s.matches("\\d*\\.?\\d*"));
        this.addRenderableWidget(this.rangeInput);
        this.limitInput = new EditBox(this.font, cx + 80, this.cy(96), 50, 14, Component.literal((String)""));
        this.limitInput.setValue(String.valueOf(this.cfg.limitPerSecond));
        this.limitInput.setMaxLength(5);
        this.limitInput.setFilter(s -> s.matches("\\d*"));
        this.addRenderableWidget(this.limitInput);
        this.moveDistInput = new EditBox(this.font, cx + 80, this.cy(122), 50, 14, Component.literal((String)""));
        this.moveDistInput.setValue(String.format("%.2f", this.cfg.moveDistance));
        this.moveDistInput.setMaxLength(5);
        this.moveDistInput.setFilter(s -> s.matches("\\d*\\.?\\d*"));
        this.addRenderableWidget(this.moveDistInput);
        this.vclipModeButton = Button.builder(Component.literal((String)("\u8131\u56f0\u65b9\u5411: " + this.getVclipLabel(this.cfg.searchVclipMode))), btn -> {
            String next = this.cycleVclipMode(this.cfg.searchVclipMode);
            this.cfg.setSearchVclipMode(next);
            btn.setMessage(Component.literal((String)("\u8131\u56f0\u65b9\u5411: " + this.getVclipLabel(next))));
        }).bounds(cx + 10, this.cy(134), 150, 18).build();
        this.addRenderableWidget(this.vclipModeButton);
        this.vclipStepInput = new EditBox(this.font, cx + 80, this.cy(174), 50, 14, Component.literal((String)""));
        this.vclipStepInput.setValue(String.format("%.1f", this.cfg.searchFindStep));
        this.vclipStepInput.setMaxLength(5);
        this.vclipStepInput.setFilter(s -> s.matches("\\d*\\.?\\d*"));
        this.addRenderableWidget(this.vclipStepInput);
        this.backButton = Button.builder(Component.literal((String)("\u53cd\u62c9\u56de\u6a21\u5f0f: " + (this.cfg.back ? "\u5f00" : "\u5173"))), btn -> {
            this.cfg.setBack(!this.cfg.back);
            btn.setMessage(Component.literal((String)("\u53cd\u62c9\u56de\u6a21\u5f0f: " + (this.cfg.back ? "\u5f00" : "\u5173"))));
        }).bounds(cx + 10, this.cy(186), 160, 18).build();
        this.addRenderableWidget(this.backButton);
        this.voidButton = Button.builder(Component.literal((String)("\u5141\u8bb8\u865a\u7a7a: " + (this.cfg.allowIntoVoid ? "\u5f00" : "\u5173"))), btn -> {
            this.cfg.setAllowIntoVoid(!this.cfg.allowIntoVoid);
            btn.setMessage(Component.literal((String)("\u5141\u8bb8\u865a\u7a7a: " + (this.cfg.allowIntoVoid ? "\u5f00" : "\u5173"))));
        }).bounds(cx + 10, this.cy(212), 160, 18).build();
        this.addRenderableWidget(this.voidButton);
        this.printButton = Button.builder(Component.literal((String)("\u8d85\u9650\u8b66\u544a: " + (this.cfg.printWhenTooManyPacket ? "\u5f00" : "\u5173"))), btn -> {
            this.cfg.setPrintWhenTooManyPacket(!this.cfg.printWhenTooManyPacket);
            btn.setMessage(Component.literal((String)("\u8d85\u9650\u8b66\u544a: " + (this.cfg.printWhenTooManyPacket ? "\u5f00" : "\u5173"))));
        }).bounds(cx + 10, this.cy(238), 160, 18).build();
        this.addRenderableWidget(this.printButton);
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u4fdd\u5b58"), btn -> this.saveConfig()).bounds(cx + 70, this.cy(290), 60, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u5b8c\u6210"), btn -> {
            this.saveConfig();
            Minecraft.getInstance().setScreen(new ClickGuiScreen());
        }).bounds(cx + 150, this.cy(290), 60, 18).build());
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        this.fillGradient(g);
        int cx = (this.width - 310) / 2;
        GuiRenderHelper.drawPanelBackground(g, cx, this.cy(0), 310, 340, false);
        g.drawString(this.font, "AntiLag \u9632\u62c9\u56de\u914d\u7f6e", cx + 10, this.cy(10), 0xFFFFFF);
        g.drawString(this.font, "\u89e6\u53d1\u8ddd\u79bb:", cx + 10, this.cy(56), 0xAAAAAA);
        g.drawString(this.font, "\u00a77(0.1~2000)", cx + 142, this.cy(70), 0x666666);
        g.drawString(this.font, "\u6bcf\u79d2\u9650\u5305:", cx + 10, this.cy(82), 0xAAAAAA);
        g.drawString(this.font, "\u00a77(1~10000)", cx + 132, this.cy(96), 0x666666);
        g.drawString(this.font, "\u8def\u5f84\u6b65\u957f:", cx + 10, this.cy(108), 0xAAAAAA);
        g.drawString(this.font, "\u00a77(0.01~1.0)", cx + 132, this.cy(122), 0x666666);
        g.drawString(this.font, "\u8131\u56f0\u6b65\u8ddd:", cx + 10, this.cy(160), 0xAAAAAA);
        g.drawString(this.font, "\u00a77(0.1~5.0)", cx + 132, this.cy(174), 0x666666);
        g.drawString(this.font, "\u00a77| \u7248\u672c\u6a21\u5f0f\uff1a1.16=\u8def\u5f84\u62c6\u5206;1.9=\u76f4\u63a5\u53d1\u9001", cx + 175, this.cy(32), 0x666666);
        g.drawString(this.font, "\u00a77| \u53cd\u62c9\u56de\u6a21\u5f0f\uff1a\u5f00\u542f\u540e\u4fdd\u7559\u670d\u52a1\u7aef\u62c9\u56de\u6548\u679c", cx + 175, this.cy(188), 0x666666);
        if (++this.tickCounter % 20 == 0) {
            int cnt = AntiLagFeature.getCurrentPacketCount();
            String rateState = this.cfg.rateLimited ? " \u00a7c(\u9650\u901f\u4e2d)" : "";
            g.drawString(this.font, "\u672c\u79d2\u53d1\u5305: " + cnt + "/" + this.cfg.limitPerSecond + rateState, cx + 10, this.cy(264), cnt > this.cfg.limitPerSecond ? 0xFF5555 : 0xAAAAAA);
        } else {
            int cnt = AntiLagFeature.getCurrentPacketCount();
            String rateState = this.cfg.rateLimited ? " \u00a7c(\u9650\u901f\u4e2d)" : "";
            g.drawString(this.font, "\u672c\u79d2\u53d1\u5305: " + cnt + "/" + this.cfg.limitPerSecond + rateState, cx + 10, this.cy(264), cnt > this.cfg.limitPerSecond ? 0xFF5555 : 0xAAAAAA);
        }
        super.render(g, mx, my, pt);
    }

    public boolean mouseClicked(double mx, double my, int button) {
        return super.mouseClicked(mx, my, button);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return super.charTyped(codePoint, modifiers);
    }

    private String getVersionLabel(String mode) {
        return "MC1_16".equals(mode) ? "1.16 (\u8def\u5f84\u62c6\u5206)" : "1.9 (\u76f4\u63a5)";
    }

    private String getVclipLabel(String mode) {
        switch (mode) {
            case "OnlyUp": {
                return "\u2191 \u4ec5\u5411\u4e0a";
            }
            case "Down": {
                return "\u2193 \u4ec5\u5411\u4e0b";
            }
            case "Both": {
                return "\u21c5 \u53cc\u5411";
            }
        }
        return mode;
    }

    private String cycleVclipMode(String current) {
        switch (current) {
            case "OnlyUp": {
                return "Down";
            }
            case "Down": {
                return "Both";
            }
            case "Both": {
                return "OnlyUp";
            }
        }
        return "OnlyUp";
    }

    private void saveConfig() {
        try {
            double r = Double.parseDouble(this.rangeInput.getValue());
            this.cfg.setRange(Math.max(0.1, Math.min(2000.0, r)));
        }
        catch (NumberFormatException r) {
            // ignored
        }
        try {
            int l = Integer.parseInt(this.limitInput.getValue());
            this.cfg.setLimitPerSecond(Math.max(1, Math.min(10000, l)));
        }
        catch (NumberFormatException l) {
            // ignored
        }
        try {
            double d = Double.parseDouble(this.moveDistInput.getValue());
            this.cfg.setMoveDistance(Math.max(0.01, Math.min(1.0, d)));
        }
        catch (NumberFormatException d) {
            // ignored
        }
        try {
            double s = Double.parseDouble(this.vclipStepInput.getValue());
            this.cfg.setSearchFindStep(Math.max(0.1, Math.min(5.0, s)));
        }
        catch (NumberFormatException numberFormatException) {
            // ignored
        }
        AntiLagScreen antiLagScreen = this;
        antiLagScreen.cfg.save();
    }

    private int cy(int rowOffset) {
        return (this.height - 340) / 2 + rowOffset;
    }

    public void onClose() {
        this.saveConfig();
        Minecraft.getInstance().setScreen(null);
    }
}

