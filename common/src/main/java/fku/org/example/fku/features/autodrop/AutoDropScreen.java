package fku.org.example.fku.features.autodrop;

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.autodrop.AutoDropConfig;
import fku.org.example.fku.features.autodrop.AutoDropPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class AutoDropScreen
extends Screen {
    private static final int WIDTH = 270;
    private static final int HEIGHT = 160;
    private Button resetButton;
    private Button dropModeButton;
    private EditBox scanIntervalField;

    public AutoDropScreen() {
        super(Component.literal((String)"\u81ea\u52a8\u4e22\u914d\u7f6e"));
    }

    protected void init() {
        super.init();
        int x = (this.width - 270) / 2;
        int y = (this.height - 160) / 2;
        AutoDropConfig config = AutoDropConfig.getInstance();
        this.dropModeButton = Button.builder(Component.literal((String)(config.dropAsEntity ? "\u00a7a\u6389\u843d\u7269" : "\u00a7c\u76f4\u63a5\u6d88\u5931")), btn -> {
            AutoDropConfig cfg = AutoDropConfig.getInstance();
            cfg.dropAsEntity = !cfg.dropAsEntity;
            AutoDropConfig.save();
            btn.setMessage(Component.literal((String)(cfg.dropAsEntity ? "\u00a7a\u6389\u843d\u7269" : "\u00a7c\u76f4\u63a5\u6d88\u5931")));
        }).bounds(x + 100, y + 30, 90, 20).build();
        this.addRenderableWidget(this.dropModeButton);
        this.scanIntervalField = new EditBox(this.font, x + 120, y + 60, 40, 18, Component.literal((String)"\u626b\u63cf\u95f4\u9694"));
        this.scanIntervalField.m_94144_(String.valueOf(config.scanInterval));
        this.scanIntervalField.m_94199_(2);
        this.addRenderableWidget(this.scanIntervalField);
        this.resetButton = Button.builder(Component.literal((String)"\u91cd\u7f6e\u9ed1\u540d\u5355"), btn -> {
            AutoDropConfig cfg = AutoDropConfig.getInstance();
            cfg.clearBlacklist();
            AutoDropPanel.resetScroll();
            Minecraft.getInstance().player.m_5661_(Component.literal((String)"\u00a7a\u81ea\u52a8\u4e22\u5f03\u9ed1\u540d\u5355\u5df2\u91cd\u7f6e"), true);
        }).bounds(x + 10, y + 95, 100, 18).build();
        this.addRenderableWidget(this.resetButton);
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u4fdd\u5b58"), btn -> this.saveConfig()).bounds(x + 95, y + 130, 80, 20).build());
    }

    private void saveConfig() {
        AutoDropConfig config = AutoDropConfig.getInstance();
        try {
            config.setScanInterval(Integer.parseInt(this.scanIntervalField.m_94155_()));
        }
        catch (NumberFormatException numberFormatException) {
            // ignored
        }
        this.onClose();
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        this.fillGradient(g);
        int x = (this.width - 270) / 2;
        int y = (this.height - 160) / 2;
        GuiRenderHelper.drawPanelBackground(g, x, y, 270, 160, false);
        g.drawString(this.font, "\u81ea\u52a8\u4e22\u914d\u7f6e", x + 10, y + 8, 0xFFFFFF);
        g.drawString(this.font, "\u4e22\u5f03\u6a21\u5f0f:", x + 10, y + 34, 0xAAAAAA);
        g.drawString(this.font, "\u626b\u63cf\u95f4\u9694(tick):", x + 10, y + 64, 0xAAAAAA);
        g.drawString(this.font, "1~20\uff0c\u8d8a\u5c0f\u8d8a\u5feb", x + 168, y + 64, 0x888888);
        super.render(g, mx, my, pt);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}

