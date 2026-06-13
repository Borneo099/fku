package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.features.autodrop.AutoDropConfig;
import fku.org.example.fku.features.autodrop.AutoDropScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

public class AutoDropComponent extends GuiComponent {
    protected String label;

    public AutoDropComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "自动丢");
        this.label = "自动丢";
    }

    protected boolean isEnabled() {
        return AutoDropConfig.getInstance().enabled;
    }

    protected void toggle() {
        AutoDropConfig.getInstance().enabled = !AutoDropConfig.getInstance().enabled;
    }

    protected void saveConfig() {
        AutoDropConfig.save();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;
        boolean enabled = isEnabled();

        int bgColor = enabled ? new Color(0, 150, 0, 200).getRGB() : new Color(150, 0, 0, 200).getRGB();
        guiGraphics.fill(x, y, x + width, y + height, bgColor);
        guiGraphics.renderOutline(x, y, width, height, new Color(200, 200, 200).getRGB());

        String displayStr = label + ": " + (enabled ? "ON" : "OFF");
        guiGraphics.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2, 0xFFFFFF);

        guiGraphics.drawString(Minecraft.getInstance().font, "右键配置", x + 5, y + height - 8, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                toggle();
                saveConfig();
                return true;
            } else if (button == 1) {
                Minecraft.getInstance().setScreen(new AutoDropScreen());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}