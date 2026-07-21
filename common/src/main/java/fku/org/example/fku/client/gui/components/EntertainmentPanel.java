package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.components.ConfigButtonComponent;
import fku.org.example.fku.client.gui.components.GuiPanel;
import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.features.displaymodel.DisplayModelScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class EntertainmentPanel
extends GuiPanel {
    public EntertainmentPanel() {
        super("\u5a31\u4e50", (Integer)FkuConfig.entertainmentPanelX.get(), (Integer)FkuConfig.entertainmentPanelY.get(), 120, 80);
    }

    @Override
    protected void init() {
        this.addComponent(new ConfigButtonComponent(0, 0, 110, 20, "\u5b9e\u4f53\u6a21\u578b", () -> Minecraft.getInstance().setScreen((Screen)new DisplayModelScreen())));
    }

    @Override
    protected void savePosition() {
        FkuConfig.entertainmentPanelX.set(this.x);
        FkuConfig.entertainmentPanelY.set(this.y);
    }
}

