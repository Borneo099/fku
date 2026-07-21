package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ColorWheelPicker;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GuiStyleScreen
extends Screen {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 350;
    private final GuiStyleConfig config = GuiStyleConfig.getInstance();
    private Button resetButton;
    private Button doneButton;
    private Button animationToggle;
    private Button shadowToggle;
    private int primaryColorR;
    private int primaryColorG;
    private int primaryColorB;
    private int backgroundColorR;
    private int backgroundColorG;
    private int backgroundColorB;
    private int borderColorR;
    private int borderColorG;
    private int borderColorB;
    private int textColorR;
    private int textColorG;
    private int textColorB;
    private int editingColorIndex = -1;
    private boolean colorPickerOpen = false;
    private ColorWheelPicker colorPicker = new ColorWheelPicker("88CCFF", hex -> {
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        this.onColorChanged(r, g, b);
    });

    public GuiStyleScreen() {
        super(Component.literal((String)"GUI\u5916\u89c2\u8bbe\u7f6e"));
        this.primaryColorR = this.config.primaryColorR;
        this.primaryColorG = this.config.primaryColorG;
        this.primaryColorB = this.config.primaryColorB;
        this.backgroundColorR = this.config.backgroundColorR;
        this.backgroundColorG = this.config.backgroundColorG;
        this.backgroundColorB = this.config.backgroundColorB;
        this.borderColorR = this.config.borderColorR;
        this.borderColorG = this.config.borderColorG;
        this.borderColorB = this.config.borderColorB;
        this.textColorR = this.config.textColorR;
        this.textColorG = this.config.textColorG;
        this.textColorB = this.config.textColorB;
    }

    protected void init() {
        super.init();
        int x = (this.width - 300) / 2;
        int y = (this.height - 350) / 2;
        this.resetButton = Button.builder(Component.literal((String)"\u91cd\u7f6e"), btn -> this.resetToDefaults()).bounds(x + 10, y + 350 - 30, 80, 20).build();
        this.addRenderableWidget(this.resetButton);
        this.doneButton = Button.builder(Component.literal((String)"\u5b8c\u6210"), btn -> {
            this.saveConfig();
            Minecraft.getInstance().setScreen(new ClickGuiScreen());
        }).bounds(x + 300 - 90, y + 350 - 30, 80, 20).build();
        this.addRenderableWidget(this.doneButton);
        this.animationToggle = Button.builder(Component.literal((String)(this.config.animationEnabled ? "\u5f39\u7c27\u52a8\u753b: \u5f00" : "\u5f39\u7c27\u52a8\u753b: \u5173")), btn -> {
            this.config.setAnimationEnabled(!this.config.animationEnabled);
            btn.setMessage(Component.literal((String)(this.config.animationEnabled ? "\u5f39\u7c27\u52a8\u753b: \u5f00" : "\u5f39\u7c27\u52a8\u753b: \u5173")));
        }).bounds(x + 10, y + 30, 130, 20).build();
        this.addRenderableWidget(this.animationToggle);
        this.shadowToggle = Button.builder(Component.literal((String)(this.config.shadowEnabled ? "\u9634\u5f71: \u5f00" : "\u9634\u5f71: \u5173")), btn -> {
            this.config.setShadowEnabled(!this.config.shadowEnabled);
            btn.setMessage(Component.literal((String)(this.config.shadowEnabled ? "\u9634\u5f71: \u5f00" : "\u9634\u5f71: \u5173")));
        }).bounds(x + 10, y + 55, 130, 20).build();
        this.addRenderableWidget(this.shadowToggle);
        Button glowToggle = Button.builder(Component.literal((String)(this.config.glowEnabled ? "\u9ad8\u5149: \u5f00" : "\u9ad8\u5149: \u5173")), btn -> {
            this.config.setGlowEnabled(!this.config.glowEnabled);
            btn.setMessage(Component.literal((String)(this.config.glowEnabled ? "\u9ad8\u5149: \u5f00" : "\u9ad8\u5149: \u5173")));
        }).bounds(x + 150, y + 30, 130, 20).build();
        this.addRenderableWidget(glowToggle);
    }

    private void openColorPicker(int colorIndex) {
        this.editingColorIndex = colorIndex;
        this.colorPickerOpen = true;
        switch (colorIndex) {
            case 0: {
                this.colorPicker.setColor(String.format("%02X%02X%02X", this.primaryColorR, this.primaryColorG, this.primaryColorB));
                break;
            }
            case 1: {
                this.colorPicker.setColor(String.format("%02X%02X%02X", this.backgroundColorR, this.backgroundColorG, this.backgroundColorB));
                break;
            }
            case 2: {
                this.colorPicker.setColor(String.format("%02X%02X%02X", this.borderColorR, this.borderColorG, this.borderColorB));
                break;
            }
            case 3: {
                this.colorPicker.setColor(String.format("%02X%02X%02X", this.textColorR, this.textColorG, this.textColorB));
            }
        }
        this.colorPicker.open(this.width / 2, this.height / 2);
    }

    private void onColorChanged(int r, int g, int b) {
        switch (this.editingColorIndex) {
            case 0: {
                this.primaryColorR = r;
                this.primaryColorG = g;
                this.primaryColorB = b;
                this.config.setPrimaryColor(r, g, b);
                break;
            }
            case 1: {
                this.backgroundColorR = r;
                this.backgroundColorG = g;
                this.backgroundColorB = b;
                this.config.setBackgroundColor(r, g, b);
                break;
            }
            case 2: {
                this.borderColorR = r;
                this.borderColorG = g;
                this.borderColorB = b;
                this.config.setBorderColor(r, g, b);
                break;
            }
            case 3: {
                this.textColorR = r;
                this.textColorG = g;
                this.textColorB = b;
                this.config.setTextColor(r, g, b);
            }
        }
    }

    private void resetToDefaults() {
        GuiStyleConfig defaultConfig = new GuiStyleConfig();
        this.primaryColorR = defaultConfig.primaryColorR;
        this.primaryColorG = defaultConfig.primaryColorG;
        this.primaryColorB = defaultConfig.primaryColorB;
        this.backgroundColorR = defaultConfig.backgroundColorR;
        this.backgroundColorG = defaultConfig.backgroundColorG;
        this.backgroundColorB = defaultConfig.backgroundColorB;
        this.borderColorR = defaultConfig.borderColorR;
        this.borderColorG = defaultConfig.borderColorG;
        this.borderColorB = defaultConfig.borderColorB;
        this.textColorR = defaultConfig.textColorR;
        this.textColorG = defaultConfig.textColorG;
        this.textColorB = defaultConfig.textColorB;
        this.config.cornerRadius = defaultConfig.cornerRadius;
        this.config.backgroundAlpha = defaultConfig.backgroundAlpha;
        this.config.blurStrength = defaultConfig.blurStrength;
        this.config.shadowStrength = defaultConfig.shadowStrength;
        this.config.setPrimaryColor(this.primaryColorR, this.primaryColorG, this.primaryColorB);
        this.config.setBackgroundColor(this.backgroundColorR, this.backgroundColorG, this.backgroundColorB);
        this.config.setBorderColor(this.borderColorR, this.borderColorG, this.borderColorB);
        this.config.setTextColor(this.textColorR, this.textColorG, this.textColorB);
        Minecraft.getInstance().player.m_5661_(Component.literal((String)"\u00a7aGUI\u5916\u89c2\u5df2\u91cd\u7f6e\u4e3a\u9ed8\u8ba4"), true);
    }

    private void saveConfig() {
        this.config.setPrimaryColor(this.primaryColorR, this.primaryColorG, this.primaryColorB);
        this.config.setBackgroundColor(this.backgroundColorR, this.backgroundColorG, this.backgroundColorB);
        this.config.setBorderColor(this.borderColorR, this.borderColorG, this.borderColorB);
        this.config.setTextColor(this.textColorR, this.textColorG, this.textColorB);
        GuiStyleConfig.save();
        Minecraft.getInstance().player.m_5661_(Component.literal((String)"\u00a7aGUI\u5916\u89c2\u914d\u7f6e\u5df2\u4fdd\u5b58"), true);
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.fillGradient(guiGraphics);
        int x = (this.width - 300) / 2;
        int y = (this.height - 350) / 2;
        GuiRenderHelper.drawPanelBackground(guiGraphics, x, y, 300, 350, false);
        guiGraphics.drawString(this.font, "GUI\u5916\u89c2\u8bbe\u7f6e", x + 10, y + 8, this.config.getTextColor());
        int btnY = y + 90;
        int btnWidth = 120;
        int btnHeight = 20;
        this.drawColorButton(guiGraphics, mouseX, mouseY, x + 20, btnY, btnWidth, btnHeight, "\u4e3b\u8272\u8c03", this.primaryColorR, this.primaryColorG, this.primaryColorB, 0);
        this.drawColorButton(guiGraphics, mouseX, mouseY, x + 20, btnY + 30, btnWidth, btnHeight, "\u80cc\u666f\u8272", this.backgroundColorR, this.backgroundColorG, this.backgroundColorB, 1);
        this.drawColorButton(guiGraphics, mouseX, mouseY, x + 20, btnY + 60, btnWidth, btnHeight, "\u8fb9\u6846\u8272", this.borderColorR, this.borderColorG, this.borderColorB, 2);
        this.drawColorButton(guiGraphics, mouseX, mouseY, x + 20, btnY + 90, btnWidth, btnHeight, "\u6587\u5b57\u8272", this.textColorR, this.textColorG, this.textColorB, 3);
        guiGraphics.drawString(this.font, "\u70b9\u51fb\u989c\u8272\u6309\u94ae\u9009\u62e9\u989c\u8272", x + 20, y + 350 - 55, 0x888888);
        if (this.colorPicker.isOpen()) {
            this.colorPicker.render(guiGraphics, mouseX, mouseY);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void drawColorButton(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y, int width, int height, String label, int r, int g, int b, int index) {
        guiGraphics.drawString(this.font, label, x, y, 0xAAAAAA);
        int colorPreviewX = x;
        int colorPreviewY = y + 15;
        GuiRenderHelper.drawRoundedRect(guiGraphics, colorPreviewX, colorPreviewY, width, height, -10066330, 4);
        GuiRenderHelper.drawRoundedRect(guiGraphics, colorPreviewX + 2, colorPreviewY + 2, width - 4, height - 4, 0xFF000000 | r << 16 | g << 8 | b, 2);
        if (mouseX >= colorPreviewX && mouseX <= colorPreviewX + width && mouseY >= colorPreviewY && mouseY <= colorPreviewY + height) {
            GuiRenderHelper.drawRoundedOutline(guiGraphics, colorPreviewX, colorPreviewY, width, height, -1, 4, 1);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.colorPicker.isOpen()) {
            if (this.colorPicker.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            this.colorPicker.close();
            this.colorPickerOpen = false;
            return true;
        }
        int x = (this.width - 300) / 2;
        int y = (this.height - 350) / 2;
        int btnY = y + 90;
        int btnWidth = 120;
        int btnHeight = 20;
        for (int i = 0; i < 4; ++i) {
            int colorY = btnY + i * 30 + 15;
            if (!(mouseX >= (x + 20)) || !(mouseX <= (x + 20 + btnWidth)) || !(mouseY >= colorY) || !(mouseY <= (colorY + btnHeight))) continue;
            this.openColorPicker(i);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}

