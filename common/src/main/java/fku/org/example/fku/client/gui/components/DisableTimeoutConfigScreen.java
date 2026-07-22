package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.config.FkuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class DisableTimeoutConfigScreen
extends Screen {
    private static final int WIDTH = 240;
    private static final int HEIGHT = 120;
    private Button timeoutToggle;

    public DisableTimeoutConfigScreen() {
        super(Component.literal("\u7981\u7528\u8d85\u65f6\u914d\u7f6e"));
    }

    protected void init() {
        super.init();
        int cx = (this.width - 240) / 2;
        int cy = (this.height - 120) / 2;
        this.timeoutToggle = Button.builder(Component.literal(this.currentToggleLabel()), btn -> {
            boolean current = (Boolean)FkuConfig.disableConnectionTimeout.get();
            FkuConfig.disableConnectionTimeout.set(!current);
            FkuConfig.disableConnectionTimeout.save();
            btn.setMessage(Component.literal(this.currentToggleLabel()));
        }).bounds(cx + 130, cy + 30, 50, 18).build();
        this.addRenderableWidget(this.timeoutToggle);
        this.addRenderableWidget(Button.builder(Component.literal("\u5b8c\u6210"), btn -> Minecraft.getInstance().setScreen(new ClickGuiScreen())).bounds(cx + 70, cy + 75, 100, 20).build());
    }

    private String currentToggleLabel() {
        return (Boolean)FkuConfig.disableConnectionTimeout.get() != false ? "\u00a7a\u662f" : "\u00a7c\u5426";
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        int cx = (this.width - 240) / 2;
        int cy = (this.height - 120) / 2;
        GuiRenderHelper.drawPanelBackground(guiGraphics, cx, cy, 240, 120, false);
        guiGraphics.drawString(this.font, "\u7981\u7528\u8d85\u65f6\u914d\u7f6e", cx + 10, cy + 8, 0xFFFFFF);
        guiGraphics.drawString(this.font, "\u7981\u7528\u8fde\u63a5\u8d85\u65f6:", cx + 12, cy + 34, 0xAAAAAA);
        guiGraphics.drawString(this.font, "\u00a77\u5c4f\u853d\u65ad\u7ebf\u65f6\u7684\u8d85\u65f6\u5f02\u5e38\u63d0\u793a", cx + 12, cy + 56, 0x666666);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}

