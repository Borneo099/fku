package fku.org.example.fku.features.baritone;

import fku.org.example.fku.client.gui.components.ConfigButtonComponent;
import fku.org.example.fku.features.baritone.BaritoneScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class BaritoneComponent
extends ConfigButtonComponent {
    @Override
    protected String getFeatureName() {
        return "Baritone";
    }

    public BaritoneComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "Baritone", () -> Minecraft.getInstance().setScreen((Screen)new BaritoneScreen()));
    }
}

