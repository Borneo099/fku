package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.MovementConfig;

public class ArrowDmgFlyComponent extends ToggleComponent {

    public ArrowDmgFlyComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "弓箭飞行");
    }

    @Override
    protected boolean isEnabled() {
        return MovementConfig.getInstance().arrowDmgFlyEnabled;
    }

    @Override
    protected void toggle() {
        MovementConfig.getInstance().setArrowDmgFlyEnabled(!MovementConfig.getInstance().arrowDmgFlyEnabled);
    }

    @Override
    protected void saveConfig() {
        MovementConfig.save();
    }
}