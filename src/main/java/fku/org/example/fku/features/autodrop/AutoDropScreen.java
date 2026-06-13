package fku.org.example.fku.features.autodrop;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

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

        Color bgColor = new Color(30, 30, 30, 200);
        Color borderColor = new Color(60, 60, 60, 200);
        
        guiGraphics.fill(x, y, x + WIDTH, y + HEIGHT, bgColor.getRGB());
        guiGraphics.renderOutline(x, y, WIDTH, HEIGHT, borderColor.getRGB());

        guiGraphics.drawString(font, "自动丢配置", x + 10, y + 10, 0xFFFFFF);
        guiGraphics.drawString(font, "重置黑名单", x + 20, y + 35, 0xAAAAAA);
        guiGraphics.drawString(font, "丢弃时产生掉落物实体", x + 20, y + 75, 0xAAAAAA);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}