package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.components.AntiLagComponent;
import fku.org.example.fku.client.gui.components.AutoDropComponent;
import fku.org.example.fku.client.gui.components.BedrockBreakerComponent;
import fku.org.example.fku.client.gui.components.DisableTimeoutComponent;
import fku.org.example.fku.client.gui.components.DuplicatorComponent;
import fku.org.example.fku.client.gui.components.GuiPanel;
import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.features.baritone.BaritoneComponent;
import fku.org.example.fku.features.fakeplayer.FakePlayerComponent;
import fku.org.example.fku.features.fastjoin.FastJoinComponent;
import fku.org.example.fku.features.loot.LootComponent;
import fku.org.example.fku.features.pearlphase.PearlPhaseComponent;
import fku.org.example.fku.features.quickcommand.QuickCommandComponent;
import fku.org.example.fku.features.selfdamage.SelfDamageComponent;
import fku.org.example.fku.features.structure_locator.StructureLocatorComponent;
import fku.org.example.fku.features.worldedit.WorldEditComponent;

public class ToolPanel
extends GuiPanel {
    public ToolPanel() {
        super("\u5de5\u5177", (Integer)FkuConfig.toolXPos.get(), (Integer)FkuConfig.toolYPos.get(), 120, 330);
    }

    @Override
    protected void init() {
        this.addComponent(new BedrockBreakerComponent(0, 0, 110, 25));
        this.addComponent(new LootComponent(0, 0, 110, 25));
        this.addComponent(new AutoDropComponent(0, 0, 110, 25));
        this.addComponent(new DuplicatorComponent(0, 0, 110, 25));
        this.addComponent(new DisableTimeoutComponent(0, 0, 110, 25));
        this.addComponent(new FastJoinComponent(0, 0, 110, 25));
        this.addComponent(new AntiLagComponent(0, 0, 110, 25));
        this.addComponent(new PearlPhaseComponent(0, 0, 110, 25));
        this.addComponent(new FakePlayerComponent(0, 0, 110, 25));
        this.addComponent(new WorldEditComponent(0, 0, 110, 25));
        this.addComponent(new StructureLocatorComponent(0, 0, 110, 25));
        this.addComponent(new BaritoneComponent(0, 0, 110, 25));
        this.addComponent(new SelfDamageComponent(0, 0, 110, 25));
        this.addComponent(new QuickCommandComponent(0, 0, 110, 22));
    }

    @Override
    protected void savePosition() {
        FkuConfig.toolXPos.set(this.x);
        FkuConfig.toolYPos.set(this.y);
    }
}

