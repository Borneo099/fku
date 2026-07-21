package fku.org.example.fku.features.healthtag;

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.healthtag.HealthTagConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class HealthTagConfigScreen
extends Screen {
    private static final int WIDTH = 250;
    private static final int HEIGHT = 150;
    private EditBox xPosInput;
    private EditBox yPosInput;
    private final HealthTagConfig config = HealthTagConfig.getInstance();

    public HealthTagConfigScreen() {
        super(Component.literal((String)"HealthTag\u914d\u7f6e"));
    }

    protected void init() {
        super.init();
        int x = (this.width - 250) / 2;
        int y = (this.height - 150) / 2;
        this.xPosInput = new EditBox(this.font, x + 150, y + 30, 60, 20, Component.literal((String)""));
        this.xPosInput.m_94144_(String.valueOf(this.config.x));
        this.xPosInput.m_94199_(5);
        this.addRenderableWidget(this.xPosInput);
        this.yPosInput = new EditBox(this.font, x + 150, y + 60, 60, 20, Component.literal((String)""));
        this.yPosInput.m_94144_(String.valueOf(this.config.y));
        this.yPosInput.m_94199_(5);
        this.addRenderableWidget(this.yPosInput);
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u5b8c\u6210"), btn -> {
            this.saveConfig();
            Minecraft.getInstance().setScreen(new ClickGuiScreen());
        }).bounds(x + 75, y + 100, 100, 20).build());
    }

    private void saveConfig() {
        try {
            int xPos = Integer.parseInt(this.xPosInput.m_94155_());
            int yPos = Integer.parseInt(this.yPosInput.m_94155_());
            this.config.x = xPos;
            this.config.y = yPos;
            HealthTagConfig.save();
            Minecraft.getInstance().player.m_5661_(Component.literal((String)"\u00a7aHealthTag\u914d\u7f6e\u5df2\u4fdd\u5b58"), true);
        }
        catch (NumberFormatException e) {
            Minecraft.getInstance().player.m_5661_(Component.literal((String)"\u00a7c\u8f93\u5165\u65e0\u6548"), true);
        }
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.fillGradient(guiGraphics);
        int x = (this.width - 250) / 2;
        int y = (this.height - 150) / 2;
        GuiRenderHelper.drawPanelBackground(guiGraphics, x, y, 250, 150, false);
        guiGraphics.drawString(this.font, "HealthTag\u914d\u7f6e", x + 10, y + 10, 0xFFFFFF);
        guiGraphics.drawString(this.font, "X\u5750\u6807:", x + 20, y + 35, 0xAAAAAA);
        guiGraphics.drawString(this.font, "Y\u5750\u6807:", x + 20, y + 65, 0xAAAAAA);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}

