package fku.org.example.fku.features.autodrop;

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 自动丢弃配置界面
 */
public class AutoDropScreen extends Screen {
    private static final int WIDTH = 250;
    private static final int HEIGHT = 150;
    private Button resetButton;
    private Button dropModeButton;

    public AutoDropScreen() {
        super(Component.literal("自动丢配置"));
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;

        resetButton = Button.builder(Component.literal("重置"), btn -> {
            AutoDropConfig config = AutoDropConfig.getInstance();
            config.clearBlacklist();
            AutoDropPanel.resetScroll();
            Minecraft.getInstance().player.displayClientMessage(Component.literal("§a自动丢弃黑名单已重置"), true);
        }).bounds(x + 150, y + 30, 80, 20).build();
        addRenderableWidget(resetButton);

        updateDropModeButton(x, y);
    }

    private void updateDropModeButton(int x, int y) {
        AutoDropConfig config = AutoDropConfig.getInstance();
        String text = config.dropAsEntity ? "§a开启" : "§c关闭";
        
        if (dropModeButton != null) {
            removeWidget(dropModeButton);
        }
        
        dropModeButton = Button.builder(Component.literal(text), btn -> {
            AutoDropConfig cfg = AutoDropConfig.getInstance();
            cfg.dropAsEntity = !cfg.dropAsEntity;
            AutoDropConfig.save();
            updateDropModeButton(x, y);
        }).bounds(x + 150, y + 70, 80, 20).build();
        addRenderableWidget(dropModeButton);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;
        
        GuiStyleConfig config = GuiStyleConfig.getInstance();

        // 绘制圆角背景面板
        GuiRenderHelper.drawPanelBackground(guiGraphics, x, y, WIDTH, HEIGHT, false);

        // 绘制标题
        guiGraphics.drawString(font, "自动丢配置", x + 10, y + 10, config.getTextColor());
        
        // 绘制标签
        guiGraphics.drawString(font, "重置黑名单", x + 20, y + 35, 0xAAAAAA);
        guiGraphics.drawString(font, "丢弃时产生掉落物实体", x + 20, y + 75, 0xAAAAAA);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
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