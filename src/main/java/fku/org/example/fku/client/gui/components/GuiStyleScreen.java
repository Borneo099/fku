package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * GUI外观设置界面
 * 支持调整圆角、颜色、动画等参数
 */
public class GuiStyleScreen extends Screen {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 350;
    
    private final GuiStyleConfig config;
    
    private Button resetButton;
    private Button doneButton;
    private Button animationToggle;
    private Button shadowToggle;
    
    // 颜色调整滑块
    private int primaryColorR, primaryColorG, primaryColorB;
    private int backgroundColorR, backgroundColorG, backgroundColorB;
    private int borderColorR, borderColorG, borderColorB;
    private int textColorR, textColorG, textColorB;
    
    // 当前编辑的颜色索引
    private int editingColorIndex = -1;
    private boolean colorPickerOpen = false;
    
    // 颜色轮盘
    private ColorWheelPicker colorPicker;
    
    public GuiStyleScreen() {
        super(Component.literal("GUI外观设置"));
        this.config = GuiStyleConfig.getInstance();
        this.colorPicker = new ColorWheelPicker(0, 0, 0, this::onColorChanged);
        
        // 初始化当前颜色
        primaryColorR = config.primaryColorR;
        primaryColorG = config.primaryColorG;
        primaryColorB = config.primaryColorB;
        backgroundColorR = config.backgroundColorR;
        backgroundColorG = config.backgroundColorG;
        backgroundColorB = config.backgroundColorB;
        borderColorR = config.borderColorR;
        borderColorG = config.borderColorG;
        borderColorB = config.borderColorB;
        textColorR = config.textColorR;
        textColorG = config.textColorG;
        textColorB = config.textColorB;
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;

        // 重置按钮
        resetButton = Button.builder(Component.literal("重置"), btn -> {
            resetToDefaults();
        }).bounds(x + 10, y + HEIGHT - 30, 80, 20).build();
        addRenderableWidget(resetButton);

        // 完成按钮
        doneButton = Button.builder(Component.literal("完成"), btn -> {
            saveConfig();
            Minecraft.getInstance().setScreen(new ClickGuiScreen());
        }).bounds(x + WIDTH - 90, y + HEIGHT - 30, 80, 20).build();
        addRenderableWidget(doneButton);
        
        // 动画开关
        animationToggle = Button.builder(Component.literal(config.animationEnabled ? "动画: 开" : "动画: 关"), btn -> {
            config.setAnimationEnabled(!config.animationEnabled);
            btn.setMessage(Component.literal(config.animationEnabled ? "动画: 开" : "动画: 关"));
        }).bounds(x + 10, y + 30, 100, 20).build();
        addRenderableWidget(animationToggle);
        
        // 阴影开关
        shadowToggle = Button.builder(Component.literal(config.shadowEnabled ? "阴影: 开" : "阴影: 关"), btn -> {
            config.setShadowEnabled(!config.shadowEnabled);
            btn.setMessage(Component.literal(config.shadowEnabled ? "阴影: 开" : "阴影: 关"));
        }).bounds(x + 10, y + 55, 100, 20).build();
        addRenderableWidget(shadowToggle);
    }
    
    private void openColorPicker(int colorIndex) {
        editingColorIndex = colorIndex;
        colorPickerOpen = true;
        
        switch (colorIndex) {
            case 0 -> colorPicker.setColor(primaryColorR, primaryColorG, primaryColorB);
            case 1 -> colorPicker.setColor(backgroundColorR, backgroundColorG, backgroundColorB);
            case 2 -> colorPicker.setColor(borderColorR, borderColorG, borderColorB);
            case 3 -> colorPicker.setColor(textColorR, textColorG, textColorB);
        }
        
        colorPicker.open(width / 2, height / 2);
    }
    
    private void onColorChanged(int r, int g, int b) {
        switch (editingColorIndex) {
            case 0 -> {
                primaryColorR = r;
                primaryColorG = g;
                primaryColorB = b;
                config.setPrimaryColor(r, g, b);
            }
            case 1 -> {
                backgroundColorR = r;
                backgroundColorG = g;
                backgroundColorB = b;
                config.setBackgroundColor(r, g, b);
            }
            case 2 -> {
                borderColorR = r;
                borderColorG = g;
                borderColorB = b;
                config.setBorderColor(r, g, b);
            }
            case 3 -> {
                textColorR = r;
                textColorG = g;
                textColorB = b;
                config.setTextColor(r, g, b);
            }
        }
    }

    private void resetToDefaults() {
        GuiStyleConfig defaultConfig = new GuiStyleConfig();
        
        primaryColorR = defaultConfig.primaryColorR;
        primaryColorG = defaultConfig.primaryColorG;
        primaryColorB = defaultConfig.primaryColorB;
        backgroundColorR = defaultConfig.backgroundColorR;
        backgroundColorG = defaultConfig.backgroundColorG;
        backgroundColorB = defaultConfig.backgroundColorB;
        borderColorR = defaultConfig.borderColorR;
        borderColorG = defaultConfig.borderColorG;
        borderColorB = defaultConfig.borderColorB;
        textColorR = defaultConfig.textColorR;
        textColorG = defaultConfig.textColorG;
        textColorB = defaultConfig.textColorB;
        
        config.cornerRadius = defaultConfig.cornerRadius;
        config.backgroundAlpha = defaultConfig.backgroundAlpha;
        config.blurStrength = defaultConfig.blurStrength;
        config.shadowStrength = defaultConfig.shadowStrength;
        config.setPrimaryColor(primaryColorR, primaryColorG, primaryColorB);
        config.setBackgroundColor(backgroundColorR, backgroundColorG, backgroundColorB);
        config.setBorderColor(borderColorR, borderColorG, borderColorB);
        config.setTextColor(textColorR, textColorG, textColorB);
        
        Minecraft.getInstance().player.displayClientMessage(Component.literal("§aGUI外观已重置为默认"), true);
    }

    private void saveConfig() {
        config.setPrimaryColor(primaryColorR, primaryColorG, primaryColorB);
        config.setBackgroundColor(backgroundColorR, backgroundColorG, backgroundColorB);
        config.setBorderColor(borderColorR, borderColorG, borderColorB);
        config.setTextColor(textColorR, textColorG, textColorB);
        GuiStyleConfig.save();
        Minecraft.getInstance().player.displayClientMessage(Component.literal("§aGUI外观配置已保存"), true);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;

        // 绘制背景面板
        GuiRenderHelper.drawPanelBackground(guiGraphics, x, y, WIDTH, HEIGHT, false);

        // 绘制标题
        guiGraphics.drawString(font, "GUI外观设置", x + 10, y + 8, config.getTextColor());
        
        // 绘制颜色按钮
        int btnY = y + 90;
        int btnWidth = 120;
        int btnHeight = 20;
        
        // 主色调
        drawColorButton(guiGraphics, mouseX, mouseY, x + 20, btnY, btnWidth, btnHeight, "主色调", primaryColorR, primaryColorG, primaryColorB, 0);
        // 背景色
        drawColorButton(guiGraphics, mouseX, mouseY, x + 20, btnY + 30, btnWidth, btnHeight, "背景色", backgroundColorR, backgroundColorG, backgroundColorB, 1);
        // 边框色
        drawColorButton(guiGraphics, mouseX, mouseY, x + 20, btnY + 60, btnWidth, btnHeight, "边框色", borderColorR, borderColorG, borderColorB, 2);
        // 文字色
        drawColorButton(guiGraphics, mouseX, mouseY, x + 20, btnY + 90, btnWidth, btnHeight, "文字色", textColorR, textColorG, textColorB, 3);
        
        // 绘制提示
        guiGraphics.drawString(font, "点击颜色按钮选择颜色", x + 20, y + HEIGHT - 55, 0x888888);

        // 绘制颜色轮盘
        if (colorPicker.isOpen()) {
            colorPicker.render(guiGraphics, mouseX, mouseY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    private void drawColorButton(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y, int width, int height, String label, int r, int g, int b, int index) {
        // 绘制标签
        guiGraphics.drawString(font, label, x, y, 0xAAAAAA);
        
        // 绘制颜色预览框
        int colorPreviewX = x;
        int colorPreviewY = y + 15;
        GuiRenderHelper.drawRoundedRect(guiGraphics, colorPreviewX, colorPreviewY, width, height, 0xFF666666, 4);
        GuiRenderHelper.drawRoundedRect(guiGraphics, colorPreviewX + 2, colorPreviewY + 2, width - 4, height - 4, (255 << 24) | (r << 16) | (g << 8) | b, 2);
        
        // 检查鼠标悬停
        if (mouseX >= colorPreviewX && mouseX <= colorPreviewX + width && mouseY >= colorPreviewY && mouseY <= colorPreviewY + height) {
            GuiRenderHelper.drawRoundedOutline(guiGraphics, colorPreviewX, colorPreviewY, width, height, 0xFFFFFFFF, 4, 1);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 检查颜色轮盘点击
        if (colorPicker.isOpen()) {
            if (colorPicker.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            // 点击外部关闭轮盘
            colorPicker.close();
            colorPickerOpen = false;
            return true;
        }
        
        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;
        
        // 检查颜色按钮点击
        int btnY = y + 90;
        int btnWidth = 120;
        int btnHeight = 20;
        
        for (int i = 0; i < 4; i++) {
            int colorY = btnY + i * 30 + 15;
            if (mouseX >= x + 20 && mouseX <= x + 20 + btnWidth && mouseY >= colorY && mouseY <= colorY + btnHeight) {
                openColorPicker(i);
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
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