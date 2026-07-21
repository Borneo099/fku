package fku.org.example.fku.features.killaura;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.killaura.KillAuraConfig;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class KillAuraScreen
extends Screen {
    private final KillAuraConfig cfg = KillAuraConfig.getInstance();
    private EditBox rangeBox;
    private EditBox delayBox;
    private EditBox whitelistBox;
    private static final int W = 140;
    private int bx;
    private int by0;

    public KillAuraScreen() {
        super(Component.literal((String)"\u6740\u622e\u5149\u73af"));
    }

    protected void init() {
        this.bx = this.width / 2 - 70;
        int y = this.by0 = this.height / 2 - 100;
        int sp = 20;
        this.addRenderableWidget(Button.builder(Component.literal((String)(this.cfg.enabled ? "\u00a7a\u25a0 \u5f00\u542f" : "\u00a7c\u25a1 \u5173\u95ed")), b -> {
            this.cfg.setEnabled(!this.cfg.enabled);
            b.setMessage(Component.literal((String)(this.cfg.enabled ? "\u00a7a\u25a0 \u5f00\u542f" : "\u00a7c\u25a1 \u5173\u95ed")));
        }).bounds(this.bx, y, 140, 18).build());
        this.rangeBox = new EditBox(this.font, this.bx + 46, y += sp + 4, 40, 16, Component.literal((String)""));
        this.rangeBox.m_94144_(String.valueOf(this.cfg.range));
        this.m_7787_(this.rangeBox);
        this.delayBox = new EditBox(this.font, this.bx + 71, y += sp, 30, 16, Component.literal((String)""));
        this.delayBox.m_94144_(String.valueOf(this.cfg.delay));
        this.m_7787_(this.delayBox);
        this.addRenderableWidget(Button.builder(Component.literal((String)(this.cfg.targetMode == 0 ? "\u00a7b[\u6700\u8fd1]" : "\u00a7b[\u6700\u4f4e\u8840]")), b -> {
            this.cfg.setTargetMode(this.cfg.targetMode == 0 ? 1 : 0);
            b.setMessage(Component.literal((String)(this.cfg.targetMode == 0 ? "\u00a7b[\u6700\u8fd1]" : "\u00a7b[\u6700\u4f4e\u8840]")));
        }).bounds(this.bx + 46, y += sp, 80, 16).build());
        this.mkToggle(y += sp + 2, "\u81ea\u52a8\u5207\u5251", this.cfg.autoSwitch, v -> this.cfg.setAutoSwitch((boolean)v));
        this.mkToggle(y += sp - 2, "\u81ea\u52a8\u65cb\u8f6c", this.cfg.autoRotate, v -> this.cfg.setAutoRotate((boolean)v));
        this.mkToggle(y += sp - 2, "\u4ec5\u73a9\u5bb6", this.cfg.playersOnly, v -> this.cfg.setPlayersOnly((boolean)v));
        this.mkToggle(y += sp - 2, "\u6ee1\u51b7\u5374\u653b\u51fb", this.cfg.attackCooldown, v -> this.cfg.setAttackCooldown((boolean)v));
        this.mkToggle(y += sp - 2, "\u591a\u76ee\u6807\u653b\u51fb", this.cfg.multiTarget, v -> this.cfg.setMultiTarget((boolean)v));
        this.whitelistBox = new EditBox(this.font, this.bx, y += sp, 140, 16, Component.literal((String)""));
        this.whitelistBox.m_94199_(10000);
        this.whitelistBox.m_94144_(String.join((CharSequence)",", this.cfg.whitelist));
        this.m_7787_(this.whitelistBox);
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a7a\u5b8c\u6210"), b -> {
            this.save();
            this.onClose();
        }).bounds(this.bx + 30, y += sp + 4, 80, 18).build());
    }

    private void mkToggle(int y, String label, boolean cur, Consumer<Boolean> cb) {
        boolean[] state = new boolean[]{cur};
        this.addRenderableWidget(Button.builder(Component.literal((String)((state[0] ? "\u00a7a" : "\u00a77") + label)), b -> {
            state[0] = !state[0];
            cb.accept(state[0]);
            b.setMessage(Component.literal((String)((state[0] ? "\u00a7a" : "\u00a77") + label)));
        }).bounds(this.bx, y, 140, 16).build());
    }

    private void save() {
        try {
            this.cfg.setRange(Double.parseDouble(this.rangeBox.m_94155_()));
        }
        catch (Exception exception) {
            // ignored
        }
        try {
            this.cfg.setDelay(Integer.parseInt(this.delayBox.m_94155_()));
        }
        catch (Exception exception) {
            // ignored
        }
        this.cfg.whitelist.clear();
        for (String s : this.whitelistBox.m_94155_().split(",")) {
            String t = s.trim();
            if (t.isEmpty()) continue;
            this.cfg.whitelist.add(t);
        }
        KillAuraScreen killAuraScreen = this;
        killAuraScreen.cfg.save();
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        this.fillGradient(g);
        GuiRenderHelper.drawRoundedRect(g, this.bx - 10, this.by0 - 8, 160, 280, -1440603614, 8);
        g.drawString(this.font, "\u00a7l\u6740\u622e\u5149\u73af", this.bx, this.by0, 0xFFFFFF);
        g.drawString(this.font, "\u8303\u56f4:", this.bx, this.by0 + 24, 0xFFFFFF);
        g.drawString(this.font, "\u5ef6\u8fdf(\u523b):", this.bx, this.by0 + 44, 0xFFFFFF);
        g.drawString(this.font, "\u76ee\u6807:", this.bx, this.by0 + 64, 0xFFFFFF);
        g.drawString(this.font, "\u00a77\u767d\u540d\u5355(\u9017\u53f7\u5206\u9694):", this.bx, this.by0 + 194, 0xFFFFFF);
        this.rangeBox.render(g, mx, my, pt);
        this.delayBox.render(g, mx, my, pt);
        this.whitelistBox.render(g, mx, my, pt);
        super.render(g, mx, my, pt);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

