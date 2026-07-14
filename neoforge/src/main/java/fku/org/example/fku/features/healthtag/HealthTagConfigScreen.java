package fku.org.example.fku.features.healthtag; /* water */

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * HealthTag配置界面
 * 支持调整位置和颜色
 */
public class HealthTagConfigScreen extends Screen {
    private static final int WIDTH = 250;
    private static final int HEIGHT = 150;
    
    private EditBox xPosInput;
    private EditBox yPosInput;
    
    private final HealthTagConfig config;

    public HealthTagConfigScreen() {
        super(Component.literal("HealthTag配置"));
        this.config = HealthTagConfig.getInstance();
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;

        xPosInput = new EditBox(font, x + 150, y + 30, 60, 20, Component.literal(""));
        xPosInput.setValue(String.valueOf(config.x));
        xPosInput.setMaxLength(5);
        addRenderableWidget(xPosInput);

        yPosInput = new EditBox(font, x + 150, y + 60, 60, 20, Component.literal(""));
        yPosInput.setValue(String.valueOf(config.y));
        yPosInput.setMaxLength(5);
        addRenderableWidget(yPosInput);

        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("完成"), btn -> {
            saveConfig();
            Minecraft.getInstance().setScreen(new ClickGuiScreen());
        }).bounds(x + 75, y + 100, 100, 20).build());
    }

    private void saveConfig() {
        try {
            int xPos = Integer.parseInt(xPosInput.getValue());
            int yPos = Integer.parseInt(yPosInput.getValue());
            config.x = xPos;
            config.y = yPos;
            HealthTagConfig.save();
            Minecraft.getInstance().player.displayClientMessage(Component.literal("§aHealthTag配置已保存"), true);
        } catch (NumberFormatException e) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal("§c输入无效"), true);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;
        GuiRenderHelper.drawPanelBackground(guiGraphics, x, y, WIDTH, HEIGHT, false);
        guiGraphics.drawString(font, "HealthTag配置", x + 10, y + 10, 0xFFFFFF);
        guiGraphics.drawString(font, "X坐标:", x + 20, y + 35, 0xAAAAAA);
        guiGraphics.drawString(font, "Y坐标:", x + 20, y + 65, 0xAAAAAA);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() { this.minecraft.setScreen(new ClickGuiScreen()); }
}