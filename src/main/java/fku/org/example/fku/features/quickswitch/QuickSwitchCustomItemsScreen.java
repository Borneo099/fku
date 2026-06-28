package fku.org.example.fku.features.quickswitch; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * QuickSwitchCustomItemsScreen — 自定义物品列表编辑界面（该方法由赛博教员实现）
 *
 * ★ 职责：
 *   提供多行文本输入框（MultiLineEditBox），方便用户编辑以逗号分隔的物品注册名列表。
 *   参考 BedrockBreaker 的 HelperBlockListScreen 实现模式，
 *   点击"编辑自定义物品列表..."按钮后弹出此界面。
 *
 * ★ 设计思想：
 *   单行 EditBox 输入物品列表时容易超出可视区域，使用 MultiLineEditBox
 *   提供更大的编辑空间，支持多行显示和滚动。
 *
 * ★ 参考：
 *   HelperBlockListScreen (BedrockBreaker) 的多行编辑+保存/重置/返回模式
 */
public class QuickSwitchCustomItemsScreen extends Screen {

    private static final int WIDTH = 330;
    private static final int HEIGHT = 270;

    private static final int INPUT_WIDTH = 290;
    private static final int INPUT_HEIGHT = 150;
    private static final int MAX_LENGTH = 2000;

    private MultiLineEditBox listInput;
    private final Screen parentScreen;
    private String savedMessage = "";
    private int savedMessageTicks = 0;

    public QuickSwitchCustomItemsScreen(Screen parent) {
        super(Component.literal("自定义物品列表编辑"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();
        int cx = (width - WIDTH) / 2;
        int inputX = cx + (WIDTH - INPUT_WIDTH) / 2;
        int inputY = (height - HEIGHT) / 2 + 42;

        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();

        listInput = new MultiLineEditBox(font, inputX, inputY, INPUT_WIDTH, INPUT_HEIGHT,
                Component.literal("在此输入物品注册名，逗号分隔..."),
                Component.literal("自定义物品列表"));
        listInput.setCharacterLimit(MAX_LENGTH);
        listInput.setValue(cfg.customItems);
        addRenderableWidget(listInput);

        int btnY = inputY + INPUT_HEIGHT + 16;
        int btnWidth = 70;
        int btnHeight = 20;
        int spacing = 15;
        int totalWidth = btnWidth * 3 + spacing * 2;
        int startX = cx + (WIDTH - totalWidth) / 2;

        addRenderableWidget(Button.builder(
                Component.literal("保存"),
                btn -> doSave()
        ).bounds(startX, btnY, btnWidth, btnHeight).build());

        addRenderableWidget(Button.builder(
                Component.literal("重置默认"),
                btn -> listInput.setValue("minecraft:diamond_sword,minecraft:diamond_axe,minecraft:mace")
        ).bounds(startX + btnWidth + spacing, btnY, btnWidth + 10, btnHeight).build());

        addRenderableWidget(Button.builder(
                Component.literal("返回"),
                btn -> goBack()
        ).bounds(startX + (btnWidth + spacing) * 2 + 10, btnY, btnWidth, btnHeight).build());
    }

    private void doSave() {
        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();
        cfg.customItems = listInput.getValue();
        cfg.save();
        savedMessage = "§a已保存!";
        savedMessageTicks = 60;
    }

    private void goBack() {
        Minecraft.getInstance().setScreen(parentScreen);
    }

    @Override
    public void tick() {
        super.tick();
        if (savedMessageTicks > 0) savedMessageTicks--;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listInput.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        // ESC → 返回上一级
        if (keyCode == 256) {
            goBack();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (listInput.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean inputClicked = listInput.mouseClicked(mouseX, mouseY, button);

        // 鼠标在输入框区域内时强制设置焦点
        if (isMouseOverInput(mouseX, mouseY)) {
            listInput.setFocused(true);
            return true;
        }

        return inputClicked || super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isMouseOverInput(double mouseX, double mouseY) {
        int inputX = listInput.getX();
        int inputY = listInput.getY();
        int inputRight = inputX + listInput.getWidth();
        int inputBottom = inputY + listInput.getHeight();
        return mouseX >= inputX && mouseX <= inputRight && mouseY >= inputY && mouseY <= inputBottom;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (listInput.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (listInput.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        int cx = (width - WIDTH) / 2;
        int cy = (height - HEIGHT) / 2;

        GuiRenderHelper.drawPanelBackground(guiGraphics, cx, cy, WIDTH, HEIGHT, false);
        guiGraphics.drawString(font, "自定义物品列表", cx + 15, cy + 12, 0xFFFFFF);
        guiGraphics.drawString(font, "§7逗号分隔注册名，每行一个或用逗号分隔", cx + 15, cy + 26, 0x888888);
        guiGraphics.drawString(font, "§7格式: minecraft:diamond_sword, minecraft:diamond_axe", cx + 15, cy + 38, 0x666666);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (savedMessageTicks > 0) {
            guiGraphics.drawString(font, savedMessage, cx + 15, cy + HEIGHT - 18, 0xFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}