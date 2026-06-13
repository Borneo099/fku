package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.MovementConfig;

public class YPosOverlayComponent extends ToggleComponent {

    public YPosOverlayComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "Y坐标显示");
    }

    @Override
    protected boolean isEnabled() {
        return MovementConfig.getInstance().yPosOverlayEnabled;
    }

    @Override
    protected void toggle() {
        MovementConfig.getInstance().yPosOverlayEnabled = !MovementConfig.getInstance().yPosOverlayEnabled;
    }

    @Override
    protected void saveConfig() {
        MovementConfig.save();
    }
}