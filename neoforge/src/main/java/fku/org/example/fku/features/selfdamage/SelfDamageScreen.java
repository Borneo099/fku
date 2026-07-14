package fku.org.example.fku.features.selfdamage;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 自伤配置界面 — 伤害值滑块
 */
public class SelfDamageScreen extends Screen {

    private static final int W = 260, H = 180;
    private int cx, cy;

    public SelfDamageScreen() {
        super(Component.literal("自伤配置"));
    }

    @Override
    protected void init() {
        super.init();
        cx = (width - W) / 2;
        cy = (height - H) / 2;
        var cfg = SelfDamageConfig.getInstance();

        // 伤害值增减
        addRenderableWidget(Button.builder(Component.literal("-1"), b -> { cfg.damageAmount = Math.max(1, cfg.damageAmount - 1); SelfDamageConfig.save(); })
                .bounds(cx + 50, cy + 50, 40, 18).build());
        addRenderableWidget(Button.builder(Component.literal("-5"), b -> { cfg.damageAmount = Math.max(1, cfg.damageAmount - 5); SelfDamageConfig.save(); })
                .bounds(cx + 10, cy + 50, 36, 18).build());
        addRenderableWidget(Button.builder(Component.literal("+5"), b -> { cfg.damageAmount = Math.min(20, cfg.damageAmount + 5); SelfDamageConfig.save(); })
                .bounds(cx + 155, cy + 50, 36, 18).build());
        addRenderableWidget(Button.builder(Component.literal("+1"), b -> { cfg.damageAmount = Math.min(20, cfg.damageAmount + 1); SelfDamageConfig.save(); })
                .bounds(cx + 195, cy + 50, 40, 18).build());

        // 执行 / 返回按钮
        addRenderableWidget(Button.builder(Component.literal("§c执行自伤"), b -> { SelfDamageFeature.applyDamage(); Minecraft.getInstance().setScreen(null); })
                .bounds(cx + 30, cy + 90, 90, 18).build());
        addRenderableWidget(Button.builder(Component.literal("§7返回"), b -> Minecraft.getInstance().setScreen(null))
                .bounds(cx + 140, cy + 90, 60, 18).build());
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);
        GuiRenderHelper.drawPanelBackground(g, cx, cy, W, H, false);
        g.drawString(font, "§l§4自伤配置", cx + 10, cy + 8, 0xFFFFFF);
        g.drawString(font, "§7伤害值: §c" + SelfDamageConfig.getInstance().damageAmount + " §7(1~20)", cx + 10, cy + 34, 0xCCCCCC);
        g.drawString(font, "§7§o点击「执行自伤」或左键组件立刻生效", cx + 10, cy + H - 14, 0x666666);
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { onClose(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean isPauseScreen() { return false; }
}
