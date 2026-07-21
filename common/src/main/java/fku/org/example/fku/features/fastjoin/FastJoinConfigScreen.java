package fku.org.example.fku.features.fastjoin;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.fastjoin.FastJoinConfig;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class FastJoinConfigScreen
extends Screen {
    private static final int W = 280;
    private static final int H = 240;
    private static final String[] MODES = new String[]{"EXTREME", "SMOOTH", "COMPAT"};
    private static final String[] MODE_LABELS = new String[]{"\u6781\u901f\u6a21\u5f0f", "\u5e73\u6ed1\u6a21\u5f0f", "\u517c\u5bb9\u6a21\u5f0f"};
    private int modeIndex = 1;
    private EditBox targetInput;
    private EditBox speedInput;
    private Button modeBtn;
    private Button progressBtn;
    private Button timeoutBtn;

    public FastJoinConfigScreen() {
        super(Component.literal((String)"\u5feb\u901f\u52a0\u8f7d\u914d\u7f6e"));
    }

    protected void init() {
        FastJoinConfig cfg = FastJoinConfig.getInstance();
        int cx = (this.width - 280) / 2;
        int cy = (this.height - 240) / 2;
        for (int i = 0; i < MODES.length; ++i) {
            if (!MODES[i].equals(cfg.mode)) continue;
            this.modeIndex = i;
            break;
        }
        this.modeBtn = (Button)this.addRenderableWidget(Button.builder(Component.literal((String)MODE_LABELS[this.modeIndex]), b -> {
            this.modeIndex = (this.modeIndex + 1) % MODES.length;
            cfg.setMode(MODES[this.modeIndex]);
            b.setMessage(Component.literal((String)MODE_LABELS[this.modeIndex]));
        }).bounds(cx + 12, cy + 20, 90, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a7e?"), b -> {}).bounds(cx + 106, cy + 20, 16, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u76ee\u6807\u89c6\u8ddd:"), b -> {}).bounds(cx + 12, cy + 65, 70, 18).build());
        this.targetInput = new EditBox(this.font, cx + 86, cy + 65, 40, 16, Component.literal((String)""));
        this.targetInput.m_94144_(String.valueOf(cfg.targetRenderDistance));
        this.targetInput.m_94153_(s -> s.matches("\\d*"));
        this.targetInput.m_94199_(2);
        this.m_7787_(this.targetInput);
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u533a\u5757 (2~32)"), b -> {}).bounds(cx + 130, cy + 65, 80, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u6062\u590d\u901f\u5ea6:"), b -> {}).bounds(cx + 12, cy + 88, 70, 18).build());
        this.speedInput = new EditBox(this.font, cx + 86, cy + 88, 40, 16, Component.literal((String)""));
        this.speedInput.m_94144_(String.valueOf(cfg.recoverSpeed));
        this.speedInput.m_94153_(s -> s.matches("[1-4]"));
        this.speedInput.m_94199_(1);
        this.m_7787_(this.speedInput);
        this.addRenderableWidget(Button.builder(Component.literal((String)"(1~4)"), b -> {}).bounds(cx + 130, cy + 88, 50, 18).build());
        this.progressBtn = this.addToggle(cx + 12, cy + 130, "\u663e\u793a\u8fdb\u5ea6", cfg.showLoadingProgress, v -> cfg.setShowLoadingProgress((boolean)v));
        this.timeoutBtn = this.addToggle(cx + 12, cy + 153, "\u8d85\u65f6\u56de\u9000", cfg.onTimeoutFallback, v -> cfg.setOnTimeoutFallback((boolean)v));
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a7a\u8fd4\u56de"), b -> this.saveAndClose()).bounds(cx + 140 - 30, cy + 240 - 28, 60, 18).build());
    }

    private Button addToggle(int x, int y, String label, boolean cur, Consumer<Boolean> setter) {
        return (Button)this.addRenderableWidget(Button.builder(Component.literal((String)(label + ": " + (cur ? "\u00a7a\u5f00" : "\u00a7c\u5173"))), b -> {
            setter.accept(!cur);
            b.setMessage(Component.literal((String)(label + ": " + (!cur ? "\u00a7a\u5f00" : "\u00a7c\u5173"))));
        }).bounds(x, y, 130, 16).build());
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        String tip;
        this.fillGradient(g);
        int cx = (this.width - 280) / 2;
        int cy = (this.height - 240) / 2;
        GuiRenderHelper.drawPanelBackground(g, cx, cy, 280, 240, false);
        super.render(g, mx, my, pt);
        if (this.targetInput != null) {
            this.targetInput.render(g, mx, my, pt);
        }
        if (this.speedInput != null) {
            this.speedInput.render(g, mx, my, pt);
        }
        g.drawString(this.font, "\u00a77- - - \u6a21\u5f0f - - -", cx + 10, cy + 5, 0x666666);
        g.drawString(this.font, "\u00a77- - - \u53c2\u6570 - - -", cx + 10, cy + 50, 0x666666);
        g.drawString(this.font, "\u00a77- - - \u9009\u9879 - - -", cx + 10, cy + 115, 0x666666);
        g.drawString(this.font, "\u00a77\u8fde\u63a5\u65f6\u81ea\u52a8\u8054\u52a8\u7981\u8fde\u8d85\u65f6", cx + 10, cy + 180, 0x666666);
        if (mx >= cx + 106 && mx <= cx + 122 && my >= cy + 20 && my <= cy + 38 && !(tip = FastJoinConfig.getModeTooltip(MODES[this.modeIndex])).isEmpty()) {
            int tw = Math.max(180, this.font.m_92895_(tip));
            g.m_280509_(mx + 10, my + 10, mx + 10 + tw, my + 30, -533515469);
            g.drawString(this.font, tip, mx + 12, my + 14, 0xFFFFFF);
        }
    }

    public boolean mouseClicked(double mx, double my, int btn) {
        if (this.targetInput != null) {
            this.targetInput.mouseClicked(mx, my, btn);
        }
        if (this.speedInput != null) {
            this.speedInput.mouseClicked(mx, my, btn);
        }
        return super.mouseClicked(mx, my, btn);
    }

    public boolean m_7933_(int k, int s, int m) {
        if (this.targetInput != null && this.targetInput.m_93696_()) {
            return this.targetInput.m_7933_(k, s, m);
        }
        if (this.speedInput != null && this.speedInput.m_93696_()) {
            return this.speedInput.m_7933_(k, s, m);
        }
        if (k == 256) {
            this.saveAndClose();
            return true;
        }
        return super.m_7933_(k, s, m);
    }

    public boolean m_5534_(char c, int m) {
        if (this.targetInput != null && this.targetInput.m_93696_()) {
            return this.targetInput.m_5534_(c, m);
        }
        if (this.speedInput != null && this.speedInput.m_93696_()) {
            return this.speedInput.m_5534_(c, m);
        }
        return super.m_5534_(c, m);
    }

    public void onClose() {
        this.saveAndClose();
    }

    public boolean isPauseScreen() {
        return false;
    }

    private void saveAndClose() {
        FastJoinConfig cfg = FastJoinConfig.getInstance();
        try {
            cfg.setTargetRenderDistance(Integer.parseInt(this.targetInput.m_94155_().trim()));
        }
        catch (Exception exception) {
            // ignored
        }
        try {
            cfg.setRecoverSpeed(Integer.parseInt(this.speedInput.m_94155_().trim()));
        }
        catch (Exception exception) {
            // ignored
        }
        this.minecraft.setScreen(null);
    }
}

