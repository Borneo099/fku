package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.MovementConfig;

public class NoJumpDelayComponent extends ToggleComponent {

    public NoJumpDelayComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "取消跳跃间隔");
    }

    @Override
    protected boolean isEnabled() {
        return MovementConfig.getInstance().noJumpDelayEnabled;
    }

    @Override
    protected void toggle() {
        MovementConfig.getInstance().noJumpDelayEnabled = !MovementConfig.getInstance().noJumpDelayEnabled;
    }

    @Override
    protected void saveConfig() {
        MovementConfig.save();
    }
}