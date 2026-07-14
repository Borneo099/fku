package fku.org.example.fku.features.bedrockbreaker; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class HelperBlockListScreen extends Screen {

    private static final int WIDTH = 320;
    private static final int HEIGHT = 260;

    private static final int INPUT_WIDTH = 280;
    private static final int INPUT_HEIGHT = 100;

    private EditBox listInput; // 使用 EditBox 代替 MultiLineEditBox（1.20.1 不存在）
    private final Screen parentScreen;
    private String savedMessage = "";
    private int savedMessageTicks = 0;

    public HelperBlockListScreen(Screen parent) {
        super(Component.literal("辅助方块列表编辑"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();
        int cx = (width - WIDTH) / 2;
        int inputX = cx + (WIDTH - INPUT_WIDTH) / 2;
        int inputY = (height - HEIGHT) / 2 + 40;

        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();

        listInput = new EditBox(font, inputX, inputY, INPUT_WIDTH, 16, Component.literal("辅助方块列表"));
        listInput.setMaxLength(5000);
        listInput.setValue(cfg.helperBlockList);
        addRenderableWidget(listInput);

        int btnY = inputY + INPUT_HEIGHT + 8;
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
                Component.literal("重置"),
                btn -> listInput.setValue(BedrockBreakerConfig.DEFAULT_HELPER_BLOCK_LIST)
        ).bounds(startX + btnWidth + spacing, btnY, btnWidth, btnHeight).build());

        addRenderableWidget(Button.builder(
                Component.literal("返回"),
                btn -> goBack()
        ).bounds(startX + (btnWidth + spacing) * 2, btnY, btnWidth, btnHeight).build());
    }

    private void doSave() {
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        cfg.setHelperBlockList(listInput.getValue());
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
        if (listInput.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (listInput.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean inputClicked = listInput.mouseClicked(mouseX, mouseY, button);
        return inputClicked || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        int cx = (width - WIDTH) / 2;
        int cy = (height - HEIGHT) / 2;

        GuiRenderHelper.drawPanelBackground(guiGraphics, cx, cy, WIDTH, HEIGHT, false);
        guiGraphics.drawString(font, "辅助方块列表", cx + 15, cy + 12, 0xFFFFFF);
        guiGraphics.drawString(font, "§7逗号分隔，优先级从前到后", cx + 15, cy + 26, 0x888888);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (savedMessageTicks > 0) {
            guiGraphics.drawString(font, savedMessage, cx + 15, cy + HEIGHT - 18, 0xFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
