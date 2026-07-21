package fku.org.example.fku.features.structure_locator;

import fku.org.example.fku.client.gui.components.ConfigButtonComponent;
import fku.org.example.fku.features.structure_locator.StructureLocatorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class StructureLocatorComponent
extends ConfigButtonComponent {
    @Override
    protected String getFeatureName() {
        return "\u7ed3\u6784\u5b9a\u4f4d";
    }

    public StructureLocatorComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u7ed3\u6784\u5b9a\u4f4d", () -> Minecraft.getInstance().setScreen((Screen)new StructureLocatorScreen()));
    }
}

