package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.components.ConfigButtonComponent;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.client.gui.components.GuiPanel;
import fku.org.example.fku.client.gui.components.GuiStyleScreen;
import fku.org.example.fku.config.FkuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class OtherPanel
extends GuiPanel {
    public OtherPanel() {
        super("\u5176\u5b83", (Integer)FkuConfig.guiXPos.get(), (Integer)FkuConfig.guiYPos.get(), 120, 100);
    }

    @Override
    protected void init() {
        this.addComponent(new GuiComponent(0, 0, 110, 20, "\u7ed1\u5b9aGUI\u6309\u952e"));
        this.addComponent(new ConfigButtonComponent(0, 0, 110, 20, "\u5916\u89c2\u8bbe\u7f6e", () -> Minecraft.getInstance().setScreen((Screen)new GuiStyleScreen())));
    }

    @Override
    protected void savePosition() {
        FkuConfig.guiXPos.set(this.x);
        FkuConfig.guiYPos.set(this.y);
    }
}

