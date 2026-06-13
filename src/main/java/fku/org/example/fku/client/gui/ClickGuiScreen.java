package fku.org.example.fku.client.gui;

import fku.org.example.fku.client.gui.components.GuiPanel;
import fku.org.example.fku.client.gui.components.OtherPanel;
import fku.org.example.fku.client.gui.components.MovementPanel;
import fku.org.example.fku.client.gui.components.VisualPanel;
import fku.org.example.fku.client.gui.components.ToolPanel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private final List<GuiPanel> panels = new ArrayList<>();

    public ClickGuiScreen() {
        super(Component.literal("Fku ClickGUI"));
        panels.add(new OtherPanel());
        panels.add(new MovementPanel());
        panels.add(new VisualPanel());
        panels.add(new ToolPanel());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (GuiPanel panel : panels) {
            panel.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Reverse order for clicking (topmost panel first)
        for (int i = panels.size() - 1; i >= 0; i--) {
            if (panels.get(i).mouseClicked(mouseX, mouseY, button)) {
                // Move clicked panel to the end of the list (bring to front)
                GuiPanel panel = panels.remove(i);
                panels.add(panel);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (GuiPanel panel : panels) {
            panel.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (GuiPanel panel : panels) {
            panel.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (GuiPanel panel : panels) {
            if (panel.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        if (keyCode == 256 || fku.org.example.fku.client.KeyBindings.OPEN_GUI_KEY.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
