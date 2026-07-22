package fku.org.example.fku.features.killicon;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.killicon.KillIconConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class KillIconScreen
extends Screen {
    private static final int W = 280;
    private static final int H = 240;
    private int cx;
    private int cy;
    private EditBox xIn;
    private EditBox yIn;
    private EditBox durIn;
    private EditBox maxIn;
    private EditBox opIn;
    private Button saveBtn;
    private Button closeBtn;
    private final boolean[] toggles = new boolean[5];
    private static final String[] TOGGLE_LABELS = new String[]{"\u7206\u5934\u56fe\u6807", "\u80cc\u666f", "\u8fde\u6740", "\u8ddd\u79bb", "\u52a8\u753b"};

    public KillIconScreen() {
        super(Component.literal("\u51fb\u6740\u56fe\u6807\u914d\u7f6e"));
    }

    protected void init() {
        super.init();
        this.cx = (this.width - 280) / 2;
        this.cy = (this.height - 240) / 2;
        KillIconConfig c = KillIconConfig.getInstance();
        this.toggles[0] = c.headshotEnabled;
        this.toggles[1] = c.showBackground;
        this.toggles[2] = c.showCombo;
        this.toggles[3] = c.showDistance;
        this.toggles[4] = c.enableAnimation;
        this.xIn = this.mkEdit(this.cx + 40, this.cy + 25, 50, String.valueOf(c.x), "-?\\d*");
        this.yIn = this.mkEdit(this.cx + 130, this.cy + 25, 50, String.valueOf(c.y), "-?\\d*");
        this.durIn = this.mkEdit(this.cx + 85, this.cy + 50, 55, String.valueOf(c.displayDuration), "\\d*");
        this.maxIn = this.mkEdit(this.cx + 205, this.cy + 50, 40, String.valueOf(c.maxEntries), "\\d*");
        this.opIn = this.mkEdit(this.cx + 100, this.cy + 110, 45, String.valueOf(c.bgOpacity), "\\d*");
        for (int i = 0; i < 5; ++i) {
            int idx = i;
            int bx = this.cx + 10 + i % 3 * 90;
            int by = this.cy + 75 + i / 3 * 20;
            int bw = i % 3 == 2 && i < 3 ? 80 : 80;
            this.addRenderableWidget(Button.builder(Component.literal((String)this.toggleText(idx)), b -> {
                this.toggles[idx] = !this.toggles[idx];
                b.setMessage(Component.literal((String)this.toggleText(idx)));
            }).bounds(bx, by, bw, 16).build());
        }
        this.saveBtn = (Button)this.addRenderableWidget(Button.builder(Component.literal("\u00a7a\u4fdd\u5b58"), b -> this.save()).bounds(this.cx + 30, this.cy + 200, 100, 20).build());
        this.closeBtn = (Button)this.addRenderableWidget(Button.builder(Component.literal("\u00a7c\u5173\u95ed"), b -> this.onClose()).bounds(this.cx + 150, this.cy + 200, 100, 20).build());
    }

    private String toggleText(int idx) {
        return (this.toggles[idx] ? "\u00a7a\u2714 " : "\u00a77\u2718 ") + TOGGLE_LABELS[idx];
    }

    private EditBox mkEdit(int x, int y, int w, String val, String filter) {
        EditBox b = new EditBox(this.font, x, y, w, 14, Component.literal(""));
        b.setValue(val);
        b.setMaxLength(6);
        b.setFilter(s -> s.matches(filter));
        this.addWidget(b);
        return b;
    }

    private void save() {
        KillIconConfig c = KillIconConfig.getInstance();
        try {
            c.x = Integer.parseInt(this.xIn.getValue());
        }
        catch (NumberFormatException numberFormatException) {
            // ignored
        }
        try {
            c.y = Integer.parseInt(this.yIn.getValue());
        }
        catch (NumberFormatException numberFormatException) {
            // ignored
        }
        try {
            c.displayDuration = Math.max(10, Math.min(600, Integer.parseInt(this.durIn.getValue())));
        }
        catch (NumberFormatException numberFormatException) {
            // ignored
        }
        try {
            c.maxEntries = Math.max(1, Math.min(20, Integer.parseInt(this.maxIn.getValue())));
        }
        catch (NumberFormatException numberFormatException) {
            // ignored
        }
        try {
            c.bgOpacity = Math.max(0, Math.min(255, Integer.parseInt(this.opIn.getValue())));
        }
        catch (NumberFormatException numberFormatException) {
            // ignored
        }
        c.headshotEnabled = this.toggles[0];
        c.showBackground = this.toggles[1] && c.bgOpacity > 0;
        c.showCombo = this.toggles[2];
        c.showDistance = this.toggles[3];
        c.enableAnimation = this.toggles[4];
        KillIconConfig.save();
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        GuiRenderHelper.drawPanelBackground(g, this.cx, this.cy, 280, 240, false);
        g.drawString(this.font, "\u00a7l\u51fb\u6740\u56fe\u6807\u914d\u7f6e", this.cx + 10, this.cy + 8, 0xFFFFFF);
        g.fill(this.cx + 10, this.cy + 20, this.cx + 280 - 10, this.cy + 21, -12303292);
        g.drawString(this.font, "X:", this.cx + 10, this.cy + 26, 0xAAAAAA);
        this.xIn.render(g, mx, my, pt);
        g.drawString(this.font, "Y:", this.cx + 105, this.cy + 26, 0xAAAAAA);
        this.yIn.render(g, mx, my, pt);
        g.drawString(this.font, "\u663e\u793a\u65f6\u957f(Tick):", this.cx + 10, this.cy + 51, 0xAAAAAA);
        this.durIn.render(g, mx, my, pt);
        g.drawString(this.font, "Max:", this.cx + 175, this.cy + 51, 0xAAAAAA);
        this.maxIn.render(g, mx, my, pt);
        g.drawString(this.font, "\u80cc\u666f\u900f\u660e\u5ea6(0-255):", this.cx + 10, this.cy + 113, 0xAAAAAA);
        this.opIn.render(g, mx, my, pt);
        g.drawString(this.font, "\u00a77\u4f4d\u7f6e=\u62d6\u52a8UI\u533a\u57df\u8bbe\u7f6e", this.cx + 10, this.cy + 138, 0x666666);
        g.drawString(this.font, "\u00a77\u9884\u89c8:", this.cx + 10, this.cy + 155, 0x888888);
        Object preview = "";
        if (this.toggles[2]) {
            preview = (String)preview + "\u00a76[2\u8fde\u6740] ";
        }
        if (this.toggles[0]) {
            preview = (String)preview + "\u00a7c\u2620 ";
        }
        preview = (String)preview + "\u00a7c\u2726 \u00a7fSteve";
        if (this.toggles[3]) {
            preview = (String)preview + " \u00a77(15m)";
        }
        g.drawString(this.font, (String)preview, this.cx + 50, this.cy + 155, 0xFFFFFF);
        super.render(g, mx, my, pt);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

