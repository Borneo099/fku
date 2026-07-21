package fku.org.example.fku.features.baritone;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.baritone.BaritoneConfig;
import fku.org.example.fku.features.baritone.BaritoneParkourFeature;
import fku.org.example.fku.features.baritone.BaritoneSpeedFeature;
import fku.org.example.fku.features.baritone.ElytraAnywhereFeature;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class BaritoneScreen
extends Screen {
    private static final int W = 300;
    private static final int H = 320;
    private int cx;
    private int cy;
    private int yParkourLabelY;
    private int ySpeedLabelY;
    private int yElytraLabelY;

    public BaritoneScreen() {
        super(Component.literal((String)"Baritone \u8bbe\u7f6e"));
    }

    protected void init() {
        super.init();
        this.cx = (this.width - 300) / 2;
        this.cy = (this.height - 320) / 2;
        BaritoneConfig cfg = BaritoneConfig.getInstance();
        int y = this.cy + 28;
        int sp = 16;
        this.toggle("  \u7834\u574f\u65b9\u5757", y, c -> c.allowBreak, (c, v) -> {
            c.allowBreak = v;
        });
        this.toggle("  \u653e\u7f6e\u65b9\u5757", y += sp, c -> c.allowPlace, (c, v) -> {
            c.allowPlace = v;
        });
        this.toggle("  \u75be\u8dd1", y += sp, c -> c.allowSprint, (c, v) -> {
            c.allowSprint = v;
        });
        this.toggle("  \u8dd1\u9177\u8df3\u8dc3", y += sp, c -> c.allowParkour, (c, v) -> {
            c.allowParkour = v;
        });
        this.toggle("  \u8dd1\u9177\u653e\u7f6e", y += sp, c -> c.allowParkourPlace, (c, v) -> {
            c.allowParkourPlace = v;
        });
        this.toggle("  \u80cc\u5305\u64cd\u4f5c", y += sp, c -> c.allowInventory, (c, v) -> {
            c.allowInventory = v;
        });
        this.toggleParkour(cfg, y += sp);
        y += sp + 4;
        int speedToggleY = y += 4;
        this.toggle("\u542f\u7528\u52a0\u901f", y, c -> c.speedEnabled, (c, v) -> {
            c.speedEnabled = v;
            BaritoneSpeedFeature.setEnabled(v);
        });
        int btnY = y += sp;
        this.speedBtn("-0.5", this.cx + 68, btnY, c -> {
            c.speedMultiplier = Math.max(1.0, c.speedMultiplier - 0.5);
        });
        this.speedBtn("-0.1", this.cx + 108, btnY, c -> {
            c.speedMultiplier = Math.max(1.0, c.speedMultiplier - 0.1);
        });
        this.speedBtn("+0.1", this.cx + 148, btnY, c -> {
            c.speedMultiplier = Math.min(32.0, c.speedMultiplier + 0.1);
        });
        this.speedBtn("+0.5", this.cx + 188, btnY, c -> {
            c.speedMultiplier = Math.min(32.0, c.speedMultiplier + 0.5);
        });
        this.toggle("  \u4ec5\u5730\u9762\u52a0\u901f", y += 18, c -> c.groundOnly, (c, v) -> {
            c.groundOnly = v;
        });
        y += sp + 6;
        int elytraY = y += 4;
        this.toggle("\u542f\u7528\u9798\u7fc5\u4efb\u610f\u7ef4\u5ea6", y, c -> c.elytraEnabled, (c, v) -> {
            c.elytraEnabled = v;
            ElytraAnywhereFeature.setEnabled(v);
        });
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a7a\u4fdd\u5b58\u5e76\u8fd4\u56de"), b -> this.onClose()).bounds(this.cx + 150 - 45, y += sp + 6, 90, 18).build());
        this.yParkourLabelY = this.cy + 26;
        this.ySpeedLabelY = speedToggleY - 2;
        this.yElytraLabelY = elytraY - 2;
    }

    private void toggle(String label, int y, Function<BaritoneConfig, Boolean> getter, BaritoneSetter setter) {
        BaritoneConfig cfg = BaritoneConfig.getInstance();
        boolean cur = getter.apply(cfg);
        this.addRenderableWidget(Button.builder(Component.literal((String)(label + (cur ? " \u00a7a[ON]" : " \u00a77[OFF]"))), btn -> {
            BaritoneConfig c = BaritoneConfig.getInstance();
            boolean now = (Boolean)getter.apply(c) == false;
            setter.accept(c, now);
            c.save();
            btn.setMessage(Component.literal((String)(label + (now ? " \u00a7a[ON]" : " \u00a77[OFF]"))));
        }).bounds(this.cx + 20, y, 250, 14).build());
    }

    private void toggleParkour(BaritoneConfig cfg, int y) {
        boolean cur = cfg.parkourEnabled;
        this.addRenderableWidget(Button.builder(Component.literal((String)("\u00a76\u00a7l\u2605 \u8dd1\u9177\u6a21\u5f0f \u00a77" + (cur ? "\u00a7a[ON]" : "\u00a7c[OFF]"))), btn -> {
            boolean now;
            BaritoneConfig c = BaritoneConfig.getInstance();
            c.parkourEnabled = now = !c.parkourEnabled;
            BaritoneParkourFeature.setEnabled(now);
            c.save();
            btn.setMessage(Component.literal((String)("\u00a76\u00a7l\u2605 \u8dd1\u9177\u6a21\u5f0f \u00a77" + (now ? "\u00a7a[ON]" : "\u00a7c[OFF]"))));
        }).bounds(this.cx + 10, y, 280, 15).build());
    }

    private void speedBtn(String text, int x, int y, Consumer<BaritoneConfig> modifier) {
        this.addRenderableWidget(Button.builder(Component.literal((String)text), b -> {
            modifier.accept(BaritoneConfig.getInstance());
            BaritoneConfig.save();
        }).bounds(x, y, 34, 15).build());
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        this.fillGradient(g);
        GuiRenderHelper.drawPanelBackground(g, this.cx, this.cy, 300, 320, false);
        g.drawString(this.font, "\u00a7l\u00a7dBaritone \u8bbe\u7f6e", this.cx + 10, this.cy + 8, 0xFFFFFF);
        g.drawString(this.font, "\u00a7b\u2261 \u8dd1\u9177\u6a21\u5f0f", this.cx + 12, this.yParkourLabelY, 0x55FFFF);
        g.drawString(this.font, "\u00a7e\u2261 \u52a0\u901f", this.cx + 12, this.ySpeedLabelY, 0xFFFF55);
        g.drawString(this.font, "\u00a7a\u2261 \u9798\u7fc5\u4efb\u610f\u7ef4\u5ea6", this.cx + 12, this.yElytraLabelY, 0x55FF55);
        double spd = BaritoneConfig.getInstance().speedMultiplier;
        g.drawString(this.font, "\u00a77\u901f\u5ea6: \u00a7b" + String.format("%.1f", spd) + "x", this.cx + 16, this.ySpeedLabelY + 32, 0xCCCCCC);
        g.drawString(this.font, "\u00a77\u00a7o\u9700\u5b89\u88c5 Baritone", this.cx + 10, this.cy + 320 - 14, 0x666666);
        super.render(g, mx, my, pt);
    }

    public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.onClose();
            return true;
        }
        return super.m_7933_(keyCode, scanCode, modifiers);
    }

    public boolean isPauseScreen() {
        return false;
    }

    @FunctionalInterface
    private static interface BaritoneSetter {
        public void accept(BaritoneConfig var1, boolean var2);
    }
}

