package fku.org.example.fku.features.structure_locator;

import fku.org.example.fku.client.gui.components.ConfigButtonComponent;
import net.minecraft.client.Minecraft;

/**
 * 结构定位 — 左键打开配置面板（参考娱乐·实体模型，无开关）
 */
public class StructureLocatorComponent extends ConfigButtonComponent {

    @Override
    protected String getFeatureName() { return "结构定位"; }

    public StructureLocatorComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "结构定位", () ->
                Minecraft.getInstance().setScreen(new StructureLocatorScreen()));
    }
}
