package fku.org.example.fku.features.flight;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.flight.FlightConfig;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class FlightConfigScreen
extends Screen {
    private static final int W = 280;
    private static final int H = 230;
    private EditBox speedInput;
    private EditBox vertInput;
    private EditBox tapInput;
    private Button collisionBtn;
    private Button onlyCreativeBtn;
    private Button hungerBtn;
    private Button sprintBtn;
    private Button particleBtn;
    private Button soundBtn;
    private Button antiKickBtn;

    public FlightConfigScreen() {
        super(Component.literal("\u98de\u884c\u914d\u7f6e"));
    }

    protected void init() {
        FlightConfig cfg = FlightConfig.getInstance();
        int cx = (this.width - 280) / 2;
        int cy = (this.height - 230) / 2;
        this.addRenderableWidget(Button.builder(Component.literal("\u6c34\u5e73\u901f\u5ea6:"), b -> {}).bounds(cx + 5, cy + 8, 70, 18).build());
        this.speedInput = new EditBox(this.font, cx + 80, cy + 8, 50, 16, Component.literal(""));
        this.speedInput.setValue(String.format("%.2f", cfg.flySpeed));
        this.speedInput.setFilter(s -> s.matches("\\d*\\.?\\d*"));
        this.addWidget(this.speedInput);
        this.addRenderableWidget(Button.builder(Component.literal("\u5782\u76f4\u901f\u5ea6:"), b -> {}).bounds(cx + 145, cy + 8, 70, 18).build());
        this.vertInput = new EditBox(this.font, cx + 220, cy + 8, 50, 16, Component.literal(""));
        this.vertInput.setValue(String.format("%.2f", cfg.verticalSpeed));
        this.vertInput.setFilter(s -> s.matches("\\d*\\.?\\d*"));
        this.addWidget(this.vertInput);
        this.addRenderableWidget(Button.builder(Component.literal("\u53cc\u51fb\u7a97\u53e3:"), b -> {}).bounds(cx + 5, cy + 33, 70, 18).build());
        this.tapInput = new EditBox(this.font, cx + 80, cy + 33, 50, 16, Component.literal(""));
        this.tapInput.setValue(String.valueOf(cfg.doubleTapWindow));
        this.tapInput.setFilter(s -> s.matches("\\d*"));
        this.addWidget(this.tapInput);
        int ly = cy + 60;
        int sp = 20;
        int lx = cx + 10;
        int rx = cx + 145;
        this.collisionBtn = this.toggle(lx, ly, "\u7981\u7528\u78b0\u649e", cfg.disableCollision, v -> cfg.setDisableCollision((boolean)v));
        this.onlyCreativeBtn = this.toggle(rx, ly, "\u4ec5\u521b\u9020", cfg.onlyInCreative, v -> cfg.setOnlyInCreative((boolean)v));
        this.hungerBtn = this.toggle(lx, ly + sp, "\u9965\u997f\u6d88\u8017", cfg.consumeHunger, v -> cfg.setConsumeHunger((boolean)v));
        this.sprintBtn = this.toggle(rx, ly + sp, "\u5141\u8bb8\u75be\u8dd1", cfg.allowSprint, v -> cfg.setAllowSprint((boolean)v));
        this.particleBtn = this.toggle(lx, ly + sp * 2, "\u7c92\u5b50\u6548\u679c", cfg.particleEffect, v -> cfg.setParticleEffect((boolean)v));
        this.soundBtn = this.toggle(rx, ly + sp * 2, "\u97f3\u6548\u53cd\u9988", cfg.soundFeedback, v -> cfg.setSoundFeedback((boolean)v));
        this.antiKickBtn = this.toggle(lx, ly + sp * 3, "\u9632\u8e22", cfg.antiKick, v -> cfg.setAntiKick((boolean)v));
        this.addRenderableWidget(Button.builder(Component.literal("\u00a7a\u8fd4\u56de"), b -> this.saveAndClose()).bounds(cx + 140 - 30, cy + 230 - 28, 60, 18).build());
    }

    private Button toggle(int x, int y, String label, boolean cur, Consumer<Boolean> setter) {
        return (Button)this.addRenderableWidget(Button.builder(Component.literal((String)(label + ": " + (cur ? "\u00a7a\u5f00" : "\u00a7c\u5173"))), b -> {
            setter.accept(!cur);
            b.setMessage(Component.literal((String)(label + ": " + (!cur ? "\u00a7a\u5f00" : "\u00a7c\u5173"))));
        }).bounds(x, y, 125, 16).build());
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        int cx = (this.width - 280) / 2;
        int cy = (this.height - 230) / 2;
        GuiRenderHelper.drawPanelBackground(g, cx, cy, 280, 230, false);
        super.render(g, mx, my, pt);
        if (this.speedInput != null) {
            this.speedInput.render(g, mx, my, pt);
        }
        if (this.vertInput != null) {
            this.vertInput.render(g, mx, my, pt);
        }
        if (this.tapInput != null) {
            this.tapInput.render(g, mx, my, pt);
            g.drawString(this.font, "ms", cx + 134, cy + 34, 0x666666);
        }
    }

    public boolean mouseClicked(double mx, double my, int btn) {
        if (this.speedInput != null) {
            this.speedInput.mouseClicked(mx, my, btn);
        }
        if (this.vertInput != null) {
            this.vertInput.mouseClicked(mx, my, btn);
        }
        if (this.tapInput != null) {
            this.tapInput.mouseClicked(mx, my, btn);
        }
        return super.mouseClicked(mx, my, btn);
    }

    public boolean keyPressed(int k, int s, int m) {
        if (this.speedInput != null && this.speedInput.isFocused()) {
            return this.speedInput.keyPressed(k, s, m);
        }
        if (this.vertInput != null && this.vertInput.isFocused()) {
            return this.vertInput.keyPressed(k, s, m);
        }
        if (this.tapInput != null && this.tapInput.isFocused()) {
            return this.tapInput.keyPressed(k, s, m);
        }
        if (k == 256) {
            this.saveAndClose();
            return true;
        }
        return super.keyPressed(k, s, m);
    }

    public boolean charTyped(char c, int m) {
        if (this.speedInput != null && this.speedInput.isFocused()) {
            return this.speedInput.charTyped(c, m);
        }
        if (this.vertInput != null && this.vertInput.isFocused()) {
            return this.vertInput.charTyped(c, m);
        }
        if (this.tapInput != null && this.tapInput.isFocused()) {
            return this.tapInput.charTyped(c, m);
        }
        return super.charTyped(c, m);
    }

    public void onClose() {
        this.saveAndClose();
    }

    public boolean isPauseScreen() {
        return false;
    }

    private void saveAndClose() {
        FlightConfig cfg = FlightConfig.getInstance();
        try {
            cfg.setFlySpeed(Double.parseDouble(this.speedInput.getValue().trim()));
        }
        catch (NumberFormatException numberFormatException) {
            // ignored
        }
        try {
            cfg.setVerticalSpeed(Double.parseDouble(this.vertInput.getValue().trim()));
        }
        catch (NumberFormatException numberFormatException) {
            // ignored
        }
        try {
            cfg.setDoubleTapWindow(Integer.parseInt(this.tapInput.getValue().trim()));
        }
        catch (NumberFormatException numberFormatException) {
            // ignored
        }
        this.minecraft.setScreen(null);
    }
}

