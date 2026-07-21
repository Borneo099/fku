package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.components.GuiPanel;
import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.features.attackindicator.AttackIndicatorComponent;
import fku.org.example.fku.features.healthtag.HealthTagComponent;
import fku.org.example.fku.features.killfx.KillFXComponent;
import fku.org.example.fku.features.killicon.KillIconComponent;

public class VisualPanel
extends GuiPanel {
    public VisualPanel() {
        super("\u89c6\u89c9", (Integer)FkuConfig.visualXPos.get(), (Integer)FkuConfig.visualYPos.get(), 120, 220);
    }

    @Override
    protected void init() {
        this.addComponent(new HealthTagComponent(0, 0, 110, 20));
        this.addComponent(new KillFXComponent(0, 0, 110, 20));
        this.addComponent(new KillIconComponent(0, 0, 110, 20));
        this.addComponent(new AttackIndicatorComponent(0, 0, 110, 20));
    }

    @Override
    protected void savePosition() {
        FkuConfig.visualXPos.set(this.x);
        FkuConfig.visualYPos.set(this.y);
    }
}

