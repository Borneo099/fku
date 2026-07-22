package fku.org.example.fku.features.waterwalk;

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.waterwalk.WaterWalkConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class WaterWalkConfigScreen
extends Screen {
    private static final int WIDTH = 280;
    private static final int HEIGHT = 150;
    private final WaterWalkConfig cfg = WaterWalkConfig.getInstance();

    public WaterWalkConfigScreen() {
        super(Component.literal("\u6c34\u4e0a\u884c\u8d70\u914d\u7f6e"));
    }

    private int cx() {
        return (this.width - 280) / 2;
    }

    private int cy(int row) {
        return (this.height - 150) / 2 + row;
    }

    protected void init() {
        super.init();
        int cx = this.cx();
        this.addRenderableWidget(Button.builder(Component.literal("\u8fd4\u56de\u4e3b\u83dc\u5355"), btn -> Minecraft.getInstance().setScreen(new ClickGuiScreen())).bounds(cx + 40, this.cy(112), 100, 20).build());
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        int cx = this.cx();
        int cy = this.cy(0);
        GuiRenderHelper.drawPanelBackground(g, cx, cy, 280, 150, false);
        g.drawString(this.font, "\u6c34\u4e0a\u884c\u8d70\u914d\u7f6e", cx + 10, cy + 6, 0xFFFFFF);
        g.drawString(this.font, "\u00a77\u6a21\u5f0f : \u628a\u6c34/\u5ca9\u6d46\u5f53\u5b9e\u4f53\u65b9\u5757", cx + 10, this.cy(30), 0xAAAAAA);
        g.drawString(this.font, "\u00a77\u6548\u679c : \u4e0d\u6c89\u3001\u4e0d\u5f39\u8df3\u3001\u4e0d\u70e7\u4f24", cx + 10, this.cy(46), 0xAAAAAA);
        g.drawString(this.font, "\u00a77\u6f5c\u884c : \u6309\u4f4f Shift \u53ef\u6b63\u5e38\u4e0b\u6f5c", cx + 10, this.cy(62), 0xAAAAAA);
        g.drawString(this.font, "\u00a77\u8bf4\u660e : \u7eaf\u5ba2\u6237\u7aef\u7269\u7406\u4fee\u6b63\uff0c\u65e0\u9700\u53d1\u5305", cx + 10, this.cy(78), 0x666666);
        super.render(g, mx, my, pt);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}

