package fku.org.example.fku.client.gui;

import fku.org.example.fku.client.KeyBindings;
import fku.org.example.fku.client.gui.components.CombatPanel;
import fku.org.example.fku.client.gui.components.EntertainmentPanel;
import fku.org.example.fku.client.gui.components.GuiPanel;
import fku.org.example.fku.client.gui.components.MovementPanel;
import fku.org.example.fku.client.gui.components.OtherPanel;
import fku.org.example.fku.client.gui.components.ToolPanel;
import fku.org.example.fku.client.gui.components.VisualPanel;
import fku.org.example.fku.util.HotkeySystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ClickGuiScreen
extends Screen {
    private final List<GuiPanel> panels = new ArrayList<GuiPanel>();

    public ClickGuiScreen() {
        super(Component.literal((String)"Fku ClickGUI"));
        this.panels.add(new OtherPanel());
        this.panels.add(new MovementPanel());
        this.panels.add(new VisualPanel());
        this.panels.add(new ToolPanel());
        this.panels.add(new EntertainmentPanel());
        this.panels.add(new CombatPanel());
        for (int i = 0; i < this.panels.size(); ++i) {
            this.panels.get(i).setPanelIndex(i);
        }
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        for (GuiPanel panel : this.panels) {
            panel.render(g, mx, my, pt);
        }
        if (!HotkeySystem.isWaiting()) {
            String hint = "\u00a77\u00a7o\u4e2d\u952e\u70b9\u51fb\u7ec4\u4ef6\u53ef\u7ed1\u5b9a\u70ed\u952e";
            int hw = this.font.width(hint.replace("\u00a77\u00a7o", "").replace("\u00a7r", ""));
            g.drawString(this.font, hint, (this.width - hw) / 2, 8, 0x888888);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = this.panels.size() - 1; i >= 0; --i) {
            if (!this.panels.get(i).mouseClicked(mouseX, mouseY, button)) continue;
            GuiPanel panel = this.panels.remove(i);
            this.panels.add(panel);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (GuiPanel panel : this.panels) {
            panel.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (GuiPanel panel : this.panels) {
            panel.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            if (HotkeySystem.isWaiting()) {
                HotkeySystem.cancelBinding();
                return true;
            }
            this.onClose();
            return true;
        }
        for (GuiPanel panel : this.panels) {
            if (!panel.keyPressed(keyCode, scanCode, modifiers)) continue;
            return true;
        }
        if (KeyBindings.OPEN_GUI_KEY.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void onClose() {
        this.minecraft.setScreen(null);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

