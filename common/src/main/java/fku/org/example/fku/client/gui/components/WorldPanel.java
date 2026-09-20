package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.features.clientop.ClientOPComponent;
import fku.org.example.fku.features.fastjoin.FastJoinComponent;

/**
 * 世界菜单面板
 * 聚合与“世界/连接”相关的功能（客户端OP、连接与点加载等）
 */
public class WorldPanel extends GuiPanel {

    public WorldPanel() {
        super("世界", FkuConfig.worldXPos.get(), FkuConfig.worldYPos.get(), 120, 200);
    }

    @Override
    protected void init() {
        addComponent(new ClientOPComponent(0, 0, 110, 25));
        addComponent(new DisableTimeoutComponent(0, 0, 110, 25));
        addComponent(new FastJoinComponent(0, 0, 110, 25));
        addComponent(new DisableCheatutilsChunkComponent(0, 0, 110, 25));
        addComponent(new LinkInterruptComponent(0, 0, 110, 25));
    }

    @Override
    protected void savePosition() {
        FkuConfig.worldXPos.set(this.x);
        FkuConfig.worldYPos.set(this.y);
    }
}
