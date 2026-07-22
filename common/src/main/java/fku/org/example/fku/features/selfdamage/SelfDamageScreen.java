package fku.org.example.fku.features.selfdamage;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.selfdamage.SelfDamageConfig;
import fku.org.example.fku.features.selfdamage.SelfDamageFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SelfDamageScreen
extends Screen {
    private static final int W = 260;
    private static final int H = 180;
    private int cx;
    private int cy;

    public SelfDamageScreen() {
        super(Component.literal("\u81ea\u4f24\u914d\u7f6e"));
    }

    protected void init() {
        super.init();
        this.cx = (this.width - 260) / 2;
        this.cy = (this.height - 180) / 2;
        SelfDamageConfig cfg = SelfDamageConfig.getInstance();
        this.addRenderableWidget(Button.builder(Component.literal("-1"), b -> {
            cfg.damageAmount = Math.max(1, cfg.damageAmount - 1);
            SelfDamageConfig.save();
        }).bounds(this.cx + 50, this.cy + 50, 40, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("-5"), b -> {
            cfg.damageAmount = Math.max(1, cfg.damageAmount - 5);
            SelfDamageConfig.save();
        }).bounds(this.cx + 10, this.cy + 50, 36, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("+5"), b -> {
            cfg.damageAmount = Math.min(20, cfg.damageAmount + 5);
            SelfDamageConfig.save();
        }).bounds(this.cx + 155, this.cy + 50, 36, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("+1"), b -> {
            cfg.damageAmount = Math.min(20, cfg.damageAmount + 1);
            SelfDamageConfig.save();
        }).bounds(this.cx + 195, this.cy + 50, 40, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("\u00a7c\u6267\u884c\u81ea\u4f24"), b -> {
            SelfDamageFeature.applyDamage();
            Minecraft.getInstance().setScreen(null);
        }).bounds(this.cx + 30, this.cy + 90, 90, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("\u00a77\u8fd4\u56de"), b -> Minecraft.getInstance().setScreen(null)).bounds(this.cx + 140, this.cy + 90, 60, 18).build());
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        GuiRenderHelper.drawPanelBackground(g, this.cx, this.cy, 260, 180, false);
        g.drawString(this.font, "\u00a7l\u00a74\u81ea\u4f24\u914d\u7f6e", this.cx + 10, this.cy + 8, 0xFFFFFF);
        g.drawString(this.font, "\u00a77\u4f24\u5bb3\u503c: \u00a7c" + SelfDamageConfig.getInstance().damageAmount + " \u00a77(1~20)", this.cx + 10, this.cy + 34, 0xCCCCCC);
        g.drawString(this.font, "\u00a77\u00a7o\u70b9\u51fb\u300c\u6267\u884c\u81ea\u4f24\u300d\u6216\u5de6\u952e\u7ec4\u4ef6\u7acb\u523b\u751f\u6548", this.cx + 10, this.cy + 180 - 14, 0x666666);
        super.render(g, mx, my, pt);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

