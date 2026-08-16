package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.features.entitycontrol.EntityControlComponent;

/**
 * 世界菜单面板
 * 聚合与“世界/实体”相关的功能（实体控制等）
 */
public class WorldPanel extends GuiPanel {

    public WorldPanel() {
        super("世界", FkuConfig.worldXPos.get(), FkuConfig.worldYPos.get(), 120, 60);
    }

    @Override
    protected void init() {
        addComponent(new EntityControlComponent(0, 0, 110, 25));
    }

    @Override
    protected void savePosition() {
        FkuConfig.worldXPos.set(this.x);
        FkuConfig.worldYPos.set(this.y);
    }
}
