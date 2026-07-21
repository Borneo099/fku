package fku.org.example.fku.features.nofall;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.nofall.NoFallConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class NoFallConfigScreen
extends Screen {
    private static final int WIDTH = 260;
    private static final int HEIGHT = 140;
    private EditBox minDistInput;
    private Button immuneBtn;
    private Button onlyFlyBtn;

    public NoFallConfigScreen() {
        super(Component.literal((String)"\u9632\u6454\u914d\u7f6e"));
    }

    protected void init() {
        NoFallConfig cfg = NoFallConfig.getInstance();
        int cx = (this.width - 260) / 2;
        int cy = (this.height - 140) / 2;
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u89e6\u53d1\u9ad8\u5ea6:"), b -> {}).bounds(cx + 5, cy + 10, 70, 18).build());
        this.minDistInput = new EditBox(this.font, cx + 80, cy + 10, 50, 16, Component.literal((String)""));
        this.minDistInput.m_94144_(String.valueOf(cfg.minFallDistance));
        this.minDistInput.m_94153_(s -> s.matches("\\d*\\.?\\d*"));
        this.m_7787_(this.minDistInput);
        this.immuneBtn = (Button)this.addRenderableWidget(Button.builder(Component.literal((String)("\u5b8c\u5168\u514d\u75ab: " + (cfg.immune ? "\u00a7a\u5f00" : "\u00a7c\u5173"))), btn -> {
            cfg.setImmune(!cfg.immune);
            btn.setMessage(Component.literal((String)("\u5b8c\u5168\u514d\u75ab: " + (cfg.immune ? "\u00a7a\u5f00" : "\u00a7c\u5173"))));
        }).bounds(cx + 10, cy + 40, 140, 18).build());
        this.onlyFlyBtn = (Button)this.addRenderableWidget(Button.builder(Component.literal((String)("\u4ec5\u98de\u884c: " + (cfg.onlyWhenFlying ? "\u00a7a\u5f00" : "\u00a7c\u5173"))), btn -> {
            cfg.setOnlyWhenFlying(!cfg.onlyWhenFlying);
            btn.setMessage(Component.literal((String)("\u4ec5\u98de\u884c: " + (cfg.onlyWhenFlying ? "\u00a7a\u5f00" : "\u00a7c\u5173"))));
        }).bounds(cx + 10, cy + 65, 140, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a7a\u8fd4\u56de"), btn -> this.saveAndClose()).bounds(cx + 130 - 30, cy + 140 - 30, 60, 18).build());
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        this.fillGradient(g);
        int cx = (this.width - 260) / 2;
        int cy = (this.height - 140) / 2;
        GuiRenderHelper.drawPanelBackground(g, cx, cy, 260, 140, false);
        super.render(g, mx, my, pt);
        if (this.minDistInput != null) {
            this.minDistInput.render(g, mx, my, pt);
        }
        g.drawString(this.font, "\u683c\uff08\u4f4e\u4e8e\u6b64\u4e0d\u4fdd\u62a4\uff09", cx + 135, cy + 12, 0x666666);
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (this.minDistInput != null) {
            this.minDistInput.mouseClicked(mx, my, button);
        }
        return super.mouseClicked(mx, my, button);
    }

    public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
        if (this.minDistInput != null && this.minDistInput.m_93696_()) {
            return this.minDistInput.m_7933_(keyCode, scanCode, modifiers);
        }
        if (keyCode == 256) {
            this.saveAndClose();
            return true;
        }
        return super.m_7933_(keyCode, scanCode, modifiers);
    }

    public boolean m_5534_(char codePoint, int modifiers) {
        if (this.minDistInput != null && this.minDistInput.m_93696_()) {
            return this.minDistInput.m_5534_(codePoint, modifiers);
        }
        return super.m_5534_(codePoint, modifiers);
    }

    public void onClose() {
        this.saveAndClose();
    }

    private void saveAndClose() {
        NoFallConfig cfg = NoFallConfig.getInstance();
        try {
            double d = Double.parseDouble(this.minDistInput.m_94155_().trim());
            cfg.setMinFallDistance(d);
        }
        catch (NumberFormatException numberFormatException) {
            // ignored
        }
        this.minecraft.setScreen(null);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

