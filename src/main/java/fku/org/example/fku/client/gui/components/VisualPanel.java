package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.features.healthtag.HealthTagComponent;
import fku.org.example.fku.features.killfx.KillFXComponent;

public class VisualPanel extends GuiPanel {

    public VisualPanel() {
        super("视觉", FkuConfig.visualXPos.get(), FkuConfig.visualYPos.get(), 120, 175);
    }

    @Override
    protected void init() {
        addComponent(new HealthTagComponent(0, 0, 110, 20));
        addComponent(new KillFXComponent(0, 0, 110, 20));
    }

    @Override
    protected void savePosition() {
        FkuConfig.visualXPos.set(this.x);
        FkuConfig.visualYPos.set(this.y);
    }
}