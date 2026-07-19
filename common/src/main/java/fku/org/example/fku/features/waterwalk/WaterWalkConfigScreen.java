package fku.org.example.fku.features.waterwalk; /* water */

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * WaterWalk（水上行走）配置界面
 *
 * 右键移动菜单组件进入。当前为「实体方块式」水上行走，
 * 纯客户端物理修正，无需发包配置项。
 */
public class WaterWalkConfigScreen extends Screen {

    private static final int WIDTH = 280;
    private static final int HEIGHT = 150;
    private final WaterWalkConfig cfg;

    public WaterWalkConfigScreen() {
        super(Component.literal("水上行走配置"));
        this.cfg = WaterWalkConfig.getInstance();
    }

    private int cx() {
        return (width - WIDTH) / 2;
    }

    private int cy(int row) {
        return (height - HEIGHT) / 2 + row;
    }

    @Override
    protected void init() {
        super.init();
        int cx = cx();

        // 返回主菜单
        addRenderableWidget(Button.builder(
                Component.literal("返回主菜单"),
                btn -> Minecraft.getInstance().setScreen(new ClickGuiScreen())
        ).bounds(cx + 40, cy(112), 100, 20).build());
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cx = cx();
        int cy = cy(0);

        GuiRenderHelper.drawPanelBackground(g, cx, cy, WIDTH, HEIGHT, false);
        g.drawString(font, "水上行走配置", cx + 10, cy + 6, 0xFFFFFF);
        g.drawString(font, "§7模式 : 把水/岩浆当实体方块", cx + 10, cy(30), 0xAAAAAA);
        g.drawString(font, "§7效果 : 不沉、不弹跳、不烧伤", cx + 10, cy(46), 0xAAAAAA);
        g.drawString(font, "§7潜行 : 按住 Shift 可正常下潜", cx + 10, cy(62), 0xAAAAAA);
        g.drawString(font, "§7说明 : 纯客户端物理修正，无需发包", cx + 10, cy(78), 0x666666);

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}
