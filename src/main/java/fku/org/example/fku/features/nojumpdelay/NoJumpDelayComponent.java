package fku.org.example.fku.features.nojumpdelay; /* water */

import fku.org.example.fku.config.MovementConfig;
import fku.org.example.fku.client.gui.components.ToggleComponent;

public class NoJumpDelayComponent extends ToggleComponent {

    public NoJumpDelayComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "无跳跃延迟");
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