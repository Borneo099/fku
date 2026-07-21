package fku.org.example.fku.features.yposoverlay;

import fku.org.example.fku.client.gui.components.ToggleComponent;
import fku.org.example.fku.config.MovementConfig;

public class YPosOverlayComponent
extends ToggleComponent {
    @Override
    protected String getFeatureName() {
        return "Y\u5750\u6807\u663e\u793a";
    }

    public YPosOverlayComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "Y\u5750\u6807\u663e\u793a");
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

