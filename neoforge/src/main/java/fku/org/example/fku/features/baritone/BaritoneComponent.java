package fku.org.example.fku.features.baritone;

import fku.org.example.fku.client.gui.components.ConfigButtonComponent;
import net.minecraft.client.Minecraft;

/**
 * Baritone 功能 GUI 组件 — 左键打开配置，中键绑定热键
 */
public class BaritoneComponent extends ConfigButtonComponent {

    @Override
    protected String getFeatureName() { return "Baritone"; }

    public BaritoneComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "Baritone", () ->
                Minecraft.getInstance().setScreen(new BaritoneScreen()));
    }
}
