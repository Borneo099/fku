package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.FkuConfig;

public class OtherPanel extends GuiPanel {

    public OtherPanel() {
        super("其它", FkuConfig.guiXPos.get(), FkuConfig.guiYPos.get(), 120, 200);
    }

    @Override
    protected void init() {
        addComponent(new GuiComponent(0, 0, 110, 20, "绑定GUI按键"));
    }

    @Override
    protected void savePosition() {
        FkuConfig.guiXPos.set(this.x);
        FkuConfig.guiYPos.set(this.y);
    }
}
