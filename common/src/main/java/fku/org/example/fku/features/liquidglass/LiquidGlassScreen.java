package fku.org.example.fku.features.liquidglass;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.liquidglass.LiquidGlassConfig;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class LiquidGlassScreen
extends Screen {
    private final LiquidGlassConfig cfg = LiquidGlassConfig.getInstance();
    private static final int W = 180;
    private int bx;
    private int by0;
    private int editMode = 0;

    public LiquidGlassScreen() {
        super(Component.literal("\u6db2\u4f53\u73bb\u7483\u914d\u7f6e"));
    }

    protected void init() {
        this.bx = this.width / 2 - 90;
        this.by0 = this.height / 2 - 120;
        this.rebuildWidgets();
    }

    protected void rebuildWidgets() {
        this.clearWidgets();
        int y = this.by0;
        int sp = 20;
        this.addRenderableWidget(Button.builder(Component.literal("\u00a7l\u00a7b\u6db2\u4f53\u73bb\u7483\u914d\u7f6e"), b -> {}).bounds(this.bx, y, 180, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)(this.cfg.enabled ? "\u00a7a\u25a0 \u5f00\u542f" : "\u00a7c\u25a1 \u5173\u95ed")), b -> {
            this.cfg.setEnabled(!this.cfg.enabled);
            b.setMessage(Component.literal((String)(this.cfg.enabled ? "\u00a7a\u25a0 \u5f00\u542f" : "\u00a7c\u25a1 \u5173\u95ed")));
        }).bounds(this.bx, y += sp + 4, 180, 18).build());
        y += sp;
        if (this.editMode == 0) {
            this.addRenderableWidget(Button.builder(Component.literal("\u00a77[\u57fa\u7840\u8bbe\u7f6e]"), b -> {
                this.editMode = 1;
            }).bounds(this.bx, y, 180, 16).build());
            this.addRenderableWidget(Button.builder(Component.literal("\u00a77[Clear\u6a21\u5f0f\u53c2\u6570]"), b -> {
                this.editMode = 2;
            }).bounds(this.bx, y += sp, 180, 16).build());
            this.addRenderableWidget(Button.builder(Component.literal("\u00a77[Tinted\u6a21\u5f0f\u53c2\u6570]"), b -> {
                this.editMode = 3;
            }).bounds(this.bx, y += sp, 180, 16).build());
            this.addRenderableWidget(Button.builder(Component.literal((String)("\u00a77[\u5207\u6362\u6a21\u5f0f] " + (this.cfg.tintMode == 0 ? "Clear" : "Tinted"))), b -> {
                this.cfg.setTintMode(this.cfg.tintMode == 0 ? 1 : 0);
                b.setMessage(Component.literal((String)("\u00a77[\u5207\u6362\u6a21\u5f0f] " + (this.cfg.tintMode == 0 ? "Clear" : "Tinted"))));
            }).bounds(this.bx, y += sp, 180, 16).build());
            y += sp;
            this.addSliderButton(y += 4, "\u9762\u677f\u5bbd\u5ea6", this.cfg.panelWidth, 50.0f, 500.0f, v -> this.cfg.setPanelWidth(v.floatValue()));
            this.addSliderButton(y += sp, "\u9762\u677f\u9ad8\u5ea6", this.cfg.panelHeight, 50.0f, 500.0f, v -> this.cfg.setPanelHeight(v.floatValue()));
            this.addSliderButton(y += sp, "\u5706\u89d2\u534a\u5f84", this.cfg.cornerRadius, 0.0f, 50.0f, v -> this.cfg.setCornerRadius(v.floatValue()));
            this.addSliderButton(y += sp, "\u6a21\u7cca\u5f3a\u5ea6", this.cfg.blurRadius, 0.0f, 20.0f, v -> this.cfg.setBlurRadius(v.floatValue()));
            this.addSliderButton(y += sp, "\u6298\u5c04\u5f3a\u5ea6", this.cfg.refractionPower, -1.0f, 10.0f, v -> this.cfg.setRefractionPower(v.floatValue()));
            this.addSliderButton(y += sp, "\u5168\u5c40\u900f\u660e\u5ea6", this.cfg.globalAlpha, 0.0f, 1.0f, v -> this.cfg.setGlobalAlpha(v.floatValue()));
            y += sp;
        } else if (this.editMode == 1) {
            this.addRenderableWidget(Button.builder(Component.literal("\u00a77\u2190 \u8fd4\u56de\u4e3b\u83dc\u5355"), b -> {
                this.editMode = 0;
            }).bounds(this.bx, y, 180, 16).build());
            this.addSliderButton(y += sp + 4, "\u9762\u677f\u5bbd\u5ea6", this.cfg.panelWidth, 50.0f, 500.0f, v -> this.cfg.setPanelWidth(v.floatValue()));
            this.addSliderButton(y += sp, "\u9762\u677f\u9ad8\u5ea6", this.cfg.panelHeight, 50.0f, 500.0f, v -> this.cfg.setPanelHeight(v.floatValue()));
            this.addSliderButton(y += sp, "\u5706\u89d2\u534a\u5f84", this.cfg.cornerRadius, 0.0f, 50.0f, v -> this.cfg.setCornerRadius(v.floatValue()));
            this.addSliderButton(y += sp, "\u6a21\u7cca\u5f3a\u5ea6", this.cfg.blurRadius, 0.0f, 20.0f, v -> this.cfg.setBlurRadius(v.floatValue()));
            this.addSliderButton(y += sp, "\u6298\u5c04\u5f3a\u5ea6", this.cfg.refractionPower, -1.0f, 10.0f, v -> this.cfg.setRefractionPower(v.floatValue()));
            this.addSliderButton(y += sp, "\u6298\u5c04\u8fb9\u7f18", this.cfg.refractionEdge, 0.0f, 1.0f, v -> this.cfg.setRefractionEdge(v.floatValue()));
            this.addSliderButton(y += sp, "\u5168\u5c40\u900f\u660e\u5ea6", this.cfg.globalAlpha, 0.0f, 1.0f, v -> this.cfg.setGlobalAlpha(v.floatValue()));
            y += sp;
        } else if (this.editMode == 2) {
            this.addRenderableWidget(Button.builder(Component.literal("\u00a77\u2190 \u8fd4\u56de\u4e3b\u83dc\u5355"), b -> {
                this.editMode = 0;
            }).bounds(this.bx, y, 180, 16).build());
            this.addSliderButton(y += sp + 4, "\u566a\u58f0/\u78e8\u7802", this.cfg.noise, 0.0f, 0.3f, v -> this.cfg.setNoise(v.floatValue()));
            this.addSliderButton(y += sp, "\u53d1\u5149\u6743\u91cd", this.cfg.glowWeight, -1.0f, 1.0f, v -> this.cfg.setGlowWeight(v.floatValue()));
            this.addSliderButton(y += sp, "\u53d1\u5149\u504f\u79fb", this.cfg.glowBias, -1.0f, 1.0f, v -> this.cfg.setGlowBias(v.floatValue()));
            this.addSliderButton(y += sp, "\u53d1\u5149\u8d77\u59cb", this.cfg.glowEdge0, -1.0f, 1.0f, v -> this.cfg.setGlowEdge0(v.floatValue()));
            this.addSliderButton(y += sp, "\u53d1\u5149\u7ed3\u675f", this.cfg.glowEdge1, -1.0f, 1.0f, v -> this.cfg.setGlowEdge1(v.floatValue()));
            y += sp;
        } else if (this.editMode == 3) {
            this.addRenderableWidget(Button.builder(Component.literal("\u00a77\u2190 \u8fd4\u56de\u4e3b\u83dc\u5355"), b -> {
                this.editMode = 0;
            }).bounds(this.bx, y, 180, 16).build());
            this.addSliderButton(y += sp + 4, "\u67d3\u8272R", this.cfg.tintR, 0.0f, 1.0f, v -> this.cfg.setTintR(v.floatValue()));
            this.addSliderButton(y += sp, "\u67d3\u8272G", this.cfg.tintG, 0.0f, 1.0f, v -> this.cfg.setTintG(v.floatValue()));
            this.addSliderButton(y += sp, "\u67d3\u8272B", this.cfg.tintB, 0.0f, 1.0f, v -> this.cfg.setTintB(v.floatValue()));
            this.addSliderButton(y += sp, "\u67d3\u8272\u5f3a\u5ea6", this.cfg.tintStrength, 0.0f, 1.0f, v -> this.cfg.setTintStrength(v.floatValue()));
            this.addSliderButton(y += sp, "\u8272\u6563\u5f3a\u5ea6", this.cfg.chromaStrength, 0.0f, 0.01f, v -> this.cfg.setChromaStrength(v.floatValue()));
            this.addSliderButton(y += sp, "\u6697\u5ea6", this.cfg.darkness, 0.0f, 1.0f, v -> this.cfg.setDarkness(v.floatValue()));
            y += sp;
        }
        y = Math.max(y + 10, this.by0 + 280);
        this.addRenderableWidget(Button.builder(Component.literal("\u00a7a\u5b8c\u6210"), b -> this.onClose()).bounds(this.bx + 90 - 40, y, 80, 18).build());
    }

    private void addSliderButton(int y, String label, float current, float min, float max, Consumer<Float> setter) {
        String display = String.format("%s: %.2f", label, current);
        this.addRenderableWidget(Button.builder(Component.literal((String)("\u00a77" + display)), b -> {}).bounds(this.bx, y, 180, 14).build());
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        GuiRenderHelper.drawRoundedRect(g, this.bx - 10, this.by0 - 8, 200, 320, -1440603614, 8);
        super.render(g, mx, my, pt);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

