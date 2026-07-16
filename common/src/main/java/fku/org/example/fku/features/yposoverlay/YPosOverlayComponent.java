package fku.org.example.fku.features.yposoverlay; /* water */

import fku.org.example.fku.config.MovementConfig;
import fku.org.example.fku.client.gui.components.ToggleComponent;

public class YPosOverlayComponent extends ToggleComponent {

    @Override
    protected String getFeatureName() { return "Y坐标显示"; }

    public YPosOverlayComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "Y坐标显示");
    }

    @Override
    protected boolean isEnabled() {
        return MovementConfig.getInstance().yPosOverlayEnabled;
    }

    @Override
    protected void toggle() {
        MovementConfig.getInstance().setYPosOverlayEnabled(!MovementConfig.getInstance().yPosOverlayEnabled);
    }

    @Override
    protected void saveConfig() {
        MovementConfig.save();
    }
}