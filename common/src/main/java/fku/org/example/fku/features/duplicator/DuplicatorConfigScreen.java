package fku.org.example.fku.features.duplicator;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.duplicator.DuplicatorConfig;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class DuplicatorConfigScreen
extends Screen {
    private static final int W = 280;
    private static final int H = 240;
    private int cx;
    private int cy;
    private EditBox dupeDelayInput;
    private EditBox holdDurationInput;

    public DuplicatorConfigScreen() {
        super(Component.literal((String)"\u4e09\u53c9\u621f\u590d\u5236\u914d\u7f6e"));
    }

    protected void init() {
        super.init();
        this.cx = (this.width - 280) / 2;
        this.cy = (this.height - 240) / 2;
        DuplicatorConfig cfg = DuplicatorConfig.getInstance();
        this.dupeDelayInput = this.mkEdit(this.cx + 130, this.cy + 30, 50, String.valueOf(cfg.dupeDelay));
        this.holdDurationInput = this.mkEdit(this.cx + 130, this.cy + 52, 50, String.valueOf(cfg.holdDuration));
        this.toggle(cfg, "\u81ea\u52a8\u4e22\u5f03", this.cy + 76, c -> c.dropTridents, (c, v) -> c.setDropTridents(v));
        this.toggle(cfg, "\u7ed5\u8fc7GrimV3", this.cy + 96, c -> c.bypassGrim, (c, v) -> c.setBypassGrim(v));
        this.toggle(cfg, "\u53d7\u4f24\u81ea\u52a8\u5173\u95ed", this.cy + 116, c -> c.autoCloseOnDamage, (c, v) -> c.setAutoCloseOnDamage(v));
        this.toggle(cfg, "\u81ea\u52a8\u6e05\u7406\u80cc\u5305", this.cy + 136, c -> c.autoCleanInventory, (c, v) -> c.setAutoCleanInventory(v));
        this.toggle(cfg, "\u8010\u4e45\u7ba1\u7406", this.cy + 156, c -> c.durabilityManagement, (c, v) -> c.setDurabilityManagement(v));
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a7a\u4fdd\u5b58\u5e76\u8fd4\u56de"), b -> {
            this.saveInputs();
            Minecraft.getInstance().setScreen(null);
        }).bounds(this.cx + 140 - 40, this.cy + 240 - 22, 80, 16).build());
    }

    private void toggle(DuplicatorConfig cfg, String label, int y, Function<DuplicatorConfig, Boolean> getter, DupeSetter setter) {
        boolean cur = getter.apply(cfg);
        this.addRenderableWidget(Button.builder(Component.literal((String)(label + (cur ? " \u00a7a\u5f00" : " \u00a77\u5173"))), btn -> {
            DuplicatorConfig c = DuplicatorConfig.getInstance();
            boolean now = (Boolean)getter.apply(c) == false;
            setter.accept(c, now);
            btn.setMessage(Component.literal((String)(label + (now ? " \u00a7a\u5f00" : " \u00a77\u5173"))));
        }).bounds(this.cx + 10, y, 150, 14).build());
    }

    private void saveInputs() {
        DuplicatorConfig cfg = DuplicatorConfig.getInstance();
        try {
            cfg.setDupeDelay(Integer.parseInt(this.dupeDelayInput.getValue()));
        }
        catch (Exception exception) {
            // ignored
        }
        try {
            cfg.setHoldDuration(Integer.parseInt(this.holdDurationInput.getValue()));
        }
        catch (Exception exception) {
            // ignored
        }
    }

    private EditBox mkEdit(int x, int y, int w, String val) {
        EditBox b = new EditBox(this.font, x, y, w, 14, Component.literal((String)""));
        b.setValue(val);
        b.setMaxLength(8);
        b.setFilter(s -> s.matches("\\d*"));
        this.addWidget(b);
        return b;
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        this.fillGradient(g);
        GuiRenderHelper.drawPanelBackground(g, this.cx, this.cy, 280, 240, false);
        g.drawString(this.font, "\u00a7l\u00a7b\u4e09\u53c9\u621f\u590d\u5236\u914d\u7f6e", this.cx + 10, this.cy + 8, 0xFFFFFF);
        g.drawString(this.font, "\u00a77\u51b7\u5374\u5ef6\u8fdf(tick):", this.cx + 12, this.cy + 33, 0xAAAAAA);
        g.drawString(this.font, "\u00a77\u84c4\u529b\u65f6\u957f(tick):", this.cx + 12, this.cy + 55, 0xAAAAAA);
        this.dupeDelayInput.render(g, mx, my, pt);
        this.holdDurationInput.render(g, mx, my, pt);
        super.render(g, mx, my, pt);
    }

    public boolean mouseClicked(double mx, double my, int button) {
        this.dupeDelayInput.mouseClicked(mx, my, button);
        this.holdDurationInput.mouseClicked(mx, my, button);
        return super.mouseClicked(mx, my, button);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.saveInputs();
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void onClose() {
        this.saveInputs();
        super.onClose();
    }

    public boolean isPauseScreen() {
        return false;
    }

    @FunctionalInterface
    private static interface DupeSetter {
        public void accept(DuplicatorConfig var1, boolean var2);
    }
}

