package fku.org.example.fku.features.nojumpdelay;

import fku.org.example.fku.client.gui.components.ToggleComponent;
import fku.org.example.fku.config.MovementConfig;

public class NoJumpDelayComponent
extends ToggleComponent {
    @Override
    protected String getFeatureName() {
        return "\u65e0\u8df3\u8dc3\u5ef6\u8fdf";
    }

    public NoJumpDelayComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u65e0\u8df3\u8dc3\u5ef6\u8fdf");
    }

    @Override
    protected boolean isEnabled() {
        return MovementConfig.getInstance().noJumpDelayEnabled;
    }

    @Override
    protected void toggle() {
        MovementConfig.getInstance().setNoJumpDelayEnabled(!MovementConfig.getInstance().noJumpDelayEnabled);
    }

    @Override
    protected void saveConfig() {
        MovementConfig.save();
    }
}

