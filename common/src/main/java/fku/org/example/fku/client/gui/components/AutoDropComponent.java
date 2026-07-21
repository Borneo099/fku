package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.autodrop.AutoDropConfig;
import fku.org.example.fku.features.autodrop.AutoDropScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class AutoDropComponent
extends GuiComponent {
    protected String label = "\u81ea\u52a8\u4e22";

    public AutoDropComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u81ea\u52a8\u4e22");
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
        if (!this.visible) {
            return;
        }
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = this.isEnabled();
        GuiRenderHelper.drawComponentBackground(guiGraphics, this.x, this.y, this.width, this.height, enabled);
        String displayStr = this.label + ": " + (enabled ? "ON" : "OFF");
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        guiGraphics.drawString(Minecraft.getInstance().font, displayStr, this.x + 5, this.y + (this.height - 8) / 2 - 4, textColor);
        guiGraphics.drawString(Minecraft.getInstance().font, ">>", this.x + this.width - 18, this.y + (this.height - 8) / 2 - 4, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovered(mouseX, mouseY)) {
            if (button == 0) {
                this.toggle();
                this.saveConfig();
                return true;
            }
            if (button == 1) {
                Minecraft.getInstance().setScreen((Screen)new AutoDropScreen());
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

