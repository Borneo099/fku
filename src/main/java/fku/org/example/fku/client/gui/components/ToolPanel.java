package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.features.loot.LootComponent;
import fku.org.example.fku.features.pearlphase.PearlPhaseComponent;
import fku.org.example.fku.features.fakeplayer.FakePlayerComponent;

public class ToolPanel extends GuiPanel {

    public ToolPanel() {
        super("工具", FkuConfig.toolXPos.get(), FkuConfig.toolYPos.get(), 120, 200);
    }

    @Override
    protected void init() {
        addComponent(new BedrockBreakerComponent(0, 0, 110, 25));
        addComponent(new LootComponent(0, 0, 110, 25));
        addComponent(new AutoDropComponent(0, 0, 110, 25));
        addComponent(new DuplicatorComponent(0, 0, 110, 25));
        addComponent(new DisableTimeoutComponent(0, 0, 110, 25));
        addComponent(new AntiLagComponent(0, 0, 110, 25));
        addComponent(new PearlPhaseComponent(0, 0, 110, 25));
        addComponent(new FakePlayerComponent(0, 0, 110, 25));
    }

    @Override
    protected void savePosition() {
        FkuConfig.toolXPos.set(this.x);
        FkuConfig.toolYPos.set(this.y);
    }
}