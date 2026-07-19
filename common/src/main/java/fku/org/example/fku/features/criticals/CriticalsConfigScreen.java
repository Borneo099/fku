package fku.org.example.fku.features.criticals; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 刀刀暴击配置界面 — 切换攻击暴击模式
 */
public class CriticalsConfigScreen extends Screen {

    private Button modeButton;
    private Button silentButton;

    public CriticalsConfigScreen() {
        super(Component.literal("刀刀暴击设置"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int top = 40;

        this.addRenderableWidget(Button.builder(Component.literal("返回"), b -> this.onClose())
                .bounds(cx - 100, top + 120, 200, 20).build());

        CriticalsConfig cfg = CriticalsConfig.getInstance();

        this.modeButton = Button.builder(Component.literal("模式: " + cfg.mode), b -> {
            CriticalsConfig c = CriticalsConfig.getInstance();
            c.mode = nextMode(c.mode);
            b.setMessage(Component.literal("模式: " + c.mode));
            c.saveConfig();
        }).bounds(cx - 100, top + 22, 200, 20).build();
        this.addRenderableWidget(this.modeButton);

        this.silentButton = Button.builder(Component.literal("静默保存: " + (cfg.silentSave ? "开" : "关")), b -> {
            CriticalsConfig c = CriticalsConfig.getInstance();
            c.silentSave = !c.silentSave;
            b.setMessage(Component.literal("静默保存: " + (c.silentSave ? "开" : "关")));
            c.saveConfig();
        }).bounds(cx - 100, top + 50, 200, 20).build();
        this.addRenderableWidget(this.silentButton);
    }

    private static String nextMode(String m) {
        if ("PACKET".equals(m)) return "MINI_JUMP";
        if ("MINI_JUMP".equals(m)) return "JITTER";
        return "PACKET";
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        int cx = this.width / 2;
        int top = 40;
        CriticalsConfig cfg = CriticalsConfig.getInstance();
        GuiRenderHelper.drawPanelBackground(g, cx - 110, top - 10, 220, 160, false);
        g.drawCenteredString(this.font, Component.literal("刀刀暴击设置"), cx, top - 2, 0xFFFFFFFF);
        g.drawCenteredString(this.font,
                Component.literal("§7模式: PACKET=发包 / MINI_JUMP=小跳 / JITTER=抖动"),
                cx, top + 82, 0x888888);
        g.drawCenteredString(this.font,
                Component.literal(cfg.silentSave ? "§7§o静默保存开：开关/配置静默持久化" : "§7§o静默保存关：切换时弹提示并持久化"),
                cx, top + 100, 0x666666);
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
