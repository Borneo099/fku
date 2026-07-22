package fku.org.example.fku.features.criticals;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.criticals.CriticalsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CriticalsConfigScreen
extends Screen {
    private Button modeButton;
    private Button silentButton;

    public CriticalsConfigScreen() {
        super(Component.literal((String)"\u5200\u5200\u66b4\u51fb\u8bbe\u7f6e"));
    }

    protected void init() {
        int cx = this.width / 2;
        int top = 40;
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u8fd4\u56de"), b -> this.onClose()).bounds(cx - 100, top + 120, 200, 20).build());
        CriticalsConfig cfg = CriticalsConfig.getInstance();
        this.modeButton = Button.builder(Component.literal((String)("\u6a21\u5f0f: " + cfg.mode)), b -> {
            CriticalsConfig c = CriticalsConfig.getInstance();
            c.mode = CriticalsConfigScreen.nextMode(c.mode);
            b.setMessage(Component.literal((String)("\u6a21\u5f0f: " + c.mode)));
            c.saveConfig();
        }).bounds(cx - 100, top + 22, 200, 20).build();
        this.addRenderableWidget(this.modeButton);
        this.silentButton = Button.builder(Component.literal((String)("\u9759\u9ed8\u4fdd\u5b58: " + (cfg.silentSave ? "\u5f00" : "\u5173"))), b -> {
            CriticalsConfig c = CriticalsConfig.getInstance();
            c.silentSave = !c.silentSave;
            b.setMessage(Component.literal((String)("\u9759\u9ed8\u4fdd\u5b58: " + (c.silentSave ? "\u5f00" : "\u5173"))));
            c.saveConfig();
        }).bounds(cx - 100, top + 50, 200, 20).build();
        this.addRenderableWidget(this.silentButton);
    }

    private static String nextMode(String m) {
        if ("PACKET".equals(m)) {
            return "MINI_JUMP";
        }
        if ("MINI_JUMP".equals(m)) {
            return "JITTER";
        }
        return "PACKET";
    }

    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.fillGradient(g);
        int cx = this.width / 2;
        int top = 40;
        CriticalsConfig cfg = CriticalsConfig.getInstance();
        GuiRenderHelper.drawPanelBackground(g, cx - 110, top - 10, 220, 160, false);
        g.drawCenteredString(this.font, Component.literal((String)"\u5200\u5200\u66b4\u51fb\u8bbe\u7f6e"), cx, top - 2, -1);
        g.drawCenteredString(this.font, Component.literal((String)"\u00a77\u6a21\u5f0f: PACKET=\u53d1\u5305 / MINI_JUMP=\u5c0f\u8df3 / JITTER=\u6296\u52a8"), cx, top + 82, 0x888888);
        g.drawCenteredString(this.font, Component.literal((String)(cfg.silentSave ? "\u00a77\u00a7o\u9759\u9ed8\u4fdd\u5b58\u5f00\uff1a\u5f00\u5173/\u914d\u7f6e\u9759\u9ed8\u6301\u4e45\u5316" : "\u00a77\u00a7o\u9759\u9ed8\u4fdd\u5b58\u5173\uff1a\u5207\u6362\u65f6\u5f39\u63d0\u793a\u5e76\u6301\u4e45\u5316")), cx, top + 100, 0x666666);
        super.render(g, mx, my, pt);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

