package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.config.FkuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 禁用连接超时配置界面
 *
 * 配置项：
 *   - 禁用连接超时：开关，开启后屏蔽 Connection#exceptionCaught 事件
 */
public class DisableTimeoutConfigScreen extends Screen {

    private static final int WIDTH = 240;
    private static final int HEIGHT = 120;

    private Button timeoutToggle;

    public DisableTimeoutConfigScreen() {
        super(Component.literal("禁用超时配置"));
    }

    @Override
    protected void init() {
        super.init();
        int cx = (width - WIDTH) / 2;
        int cy = (height - HEIGHT) / 2;

        timeoutToggle = Button.builder(
                Component.literal(currentToggleLabel()),
                btn -> {
                    boolean current = FkuConfig.disableConnectionTimeout.get();
                    FkuConfig.disableConnectionTimeout.set(!current);
                    FkuConfig.disableConnectionTimeout.save();
                    btn.setMessage(Component.literal(currentToggleLabel()));
                })
                .bounds(cx + 130, cy + 30, 50, 18).build();
        addRenderableWidget(timeoutToggle);

        // 完成按钮
        addRenderableWidget(Button.builder(Component.literal("完成"), btn -> {
            Minecraft.getInstance().setScreen(new ClickGuiScreen());
        }).bounds(cx + 70, cy + 75, 100, 20).build());
    }

    private String currentToggleLabel() {
        return FkuConfig.disableConnectionTimeout.get() ? "§a是" : "§c否";
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        int cx = (width - WIDTH) / 2;
        int cy = (height - HEIGHT) / 2;

        GuiRenderHelper.drawPanelBackground(guiGraphics, cx, cy, WIDTH, HEIGHT, false);
        guiGraphics.drawString(font, "禁用超时配置", cx + 10, cy + 8, 0xFFFFFF);

        guiGraphics.drawString(font, "禁用连接超时:", cx + 12, cy + 34, 0xAAAAAA);
        guiGraphics.drawString(font, "§7屏蔽断线时的超时异常提示", cx + 12, cy + 56, 0x666666);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}