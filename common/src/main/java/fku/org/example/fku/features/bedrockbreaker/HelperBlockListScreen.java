package fku.org.example.fku.features.bedrockbreaker;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class HelperBlockListScreen
extends Screen {
    private static final int WIDTH = 320;
    private static final int HEIGHT = 260;
    private static final int INPUT_WIDTH = 280;
    private static final int INPUT_HEIGHT = 100;
    private EditBox listInput;
    private final Screen parentScreen;
    private String savedMessage = "";
    private int savedMessageTicks = 0;

    public HelperBlockListScreen(Screen parent) {
        super(Component.literal("\u8f85\u52a9\u65b9\u5757\u5217\u8868\u7f16\u8f91"));
        this.parentScreen = parent;
    }

    protected void init() {
        super.init();
        int cx = (this.width - 320) / 2;
        int inputX = cx + 20;
        int inputY = (this.height - 260) / 2 + 40;
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        this.listInput = new EditBox(this.font, inputX, inputY, 280, 16, Component.literal("\u8f85\u52a9\u65b9\u5757\u5217\u8868"));
        this.listInput.setMaxLength(5000);
        this.listInput.setValue(cfg.helperBlockList);
        this.addRenderableWidget(this.listInput);
        int btnY = inputY + 100 + 8;
        int btnWidth = 70;
        int btnHeight = 20;
        int spacing = 15;
        int totalWidth = btnWidth * 3 + spacing * 2;
        int startX = cx + (320 - totalWidth) / 2;
        this.addRenderableWidget(Button.builder(Component.literal("\u4fdd\u5b58"), btn -> this.doSave()).bounds(startX, btnY, btnWidth, btnHeight).build());
        this.addRenderableWidget(Button.builder(Component.literal("\u91cd\u7f6e"), btn -> this.listInput.setValue("minecraft:cobbled_deepslate,minecraft:andesite,minecraft:granite,minecraft:diorite,minecraft:netherrack,minecraft:tuff,minecraft:sandstone,minecraft:cobblestone,minecraft:dirt")).bounds(startX + btnWidth + spacing, btnY, btnWidth, btnHeight).build());
        this.addRenderableWidget(Button.builder(Component.literal("\u8fd4\u56de"), btn -> this.goBack()).bounds(startX + (btnWidth + spacing) * 2, btnY, btnWidth, btnHeight).build());
    }

    private void doSave() {
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        cfg.setHelperBlockList(this.listInput.getValue());
        this.savedMessage = "\u00a7a\u5df2\u4fdd\u5b58!";
        this.savedMessageTicks = 60;
    }

    private void goBack() {
        Minecraft.getInstance().setScreen(this.parentScreen);
    }

    public void tick() {
        super.tick();
        if (this.savedMessageTicks > 0) {
            --this.savedMessageTicks;
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.listInput.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (this.listInput.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean inputClicked = this.listInput.mouseClicked(mouseX, mouseY, button);
        return inputClicked || super.mouseClicked(mouseX, mouseY, button);
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        int cx = (this.width - 320) / 2;
        int cy = (this.height - 260) / 2;
        GuiRenderHelper.drawPanelBackground(guiGraphics, cx, cy, 320, 260, false);
        guiGraphics.drawString(this.font, "\u8f85\u52a9\u65b9\u5757\u5217\u8868", cx + 15, cy + 12, 0xFFFFFF);
        guiGraphics.drawString(this.font, "\u00a77\u9017\u53f7\u5206\u9694\uff0c\u4f18\u5148\u7ea7\u4ece\u524d\u5230\u540e", cx + 15, cy + 26, 0x888888);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (this.savedMessageTicks > 0) {
            guiGraphics.drawString(this.font, this.savedMessage, cx + 15, cy + 260 - 18, 0xFFFFFF);
        }
    }

    public boolean isPauseScreen() {
        return false;
    }
}

