package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.features.fastjoin.FastJoinComponent;
import fku.org.example.fku.features.loot.LootComponent;
import fku.org.example.fku.features.pearlphase.PearlPhaseComponent;
import fku.org.example.fku.features.fakeplayer.FakePlayerComponent;
import fku.org.example.fku.features.quickcommand.QuickCommandComponent;
import fku.org.example.fku.features.worldedit.WorldEditComponent;
import fku.org.example.fku.features.structure_locator.StructureLocatorComponent;
import fku.org.example.fku.features.baritone.BaritoneComponent;
import fku.org.example.fku.features.selfdamage.SelfDamageComponent;


public class ToolPanel extends GuiPanel {

    public ToolPanel() {
        super("工具", FkuConfig.toolXPos.get(), FkuConfig.toolYPos.get(), 120, 330);
    }

    @Override
    protected void init() {
        addComponent(new BedrockBreakerComponent(0, 0, 110, 25));
        addComponent(new LootComponent(0, 0, 110, 25));
        addComponent(new AutoDropComponent(0, 0, 110, 25));
        addComponent(new DuplicatorComponent(0, 0, 110, 25));
        addComponent(new DisableTimeoutComponent(0, 0, 110, 25));
        addComponent(new FastJoinComponent(0, 0, 110, 25));
        addComponent(new AntiLagComponent(0, 0, 110, 25));
        addComponent(new DisableCheatutilsChunkComponent(0, 0, 110, 25));
        addComponent(new PearlPhaseComponent(0, 0, 110, 25));
        addComponent(new FakePlayerComponent(0, 0, 110, 25));
        addComponent(new WorldEditComponent(0, 0, 110, 25));
        addComponent(new StructureLocatorComponent(0, 0, 110, 25));
        addComponent(new BaritoneComponent(0, 0, 110, 25));
        addComponent(new SelfDamageComponent(0, 0, 110, 25));
        addComponent(new QuickCommandComponent(0, 0, 110, 22));
    }

    @Override
    protected void savePosition() {
        FkuConfig.toolXPos.set(this.x);
        FkuConfig.toolYPos.set(this.y);
    }
}