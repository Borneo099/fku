package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.HealthTagConfig;

public class HealthTagComponent extends ToggleComponent {

    public HealthTagComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "HealthTag");
    }

    @Override
    protected boolean isEnabled() {
        return HealthTagConfig.getInstance().enabled;
    }

    @Override
    protected void toggle() {
        HealthTagConfig.getInstance().enabled = !HealthTagConfig.getInstance().enabled;
    }

    @Override
    protected void saveConfig() {
        HealthTagConfig.save();
    }
}