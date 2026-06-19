package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.FkuConfig;

public class ToolPanel extends GuiPanel {

    public ToolPanel() {
        super("工具", FkuConfig.toolXPos.get(), FkuConfig.toolYPos.get(), 120, 200);
    }

    @Override
    protected void init() {
        addComponent(new AutoDropComponent(0, 0, 110, 25));
        addComponent(new DuplicatorComponent(0, 0, 110, 25));
    }

    @Override
    protected void savePosition() {
        FkuConfig.toolXPos.set(this.x);
        FkuConfig.toolYPos.set(this.y);
    }
}