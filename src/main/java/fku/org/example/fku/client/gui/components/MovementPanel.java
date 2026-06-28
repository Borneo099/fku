package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.MovementConfig;
import fku.org.example.fku.features.nojumpdelay.NoJumpDelayComponent;
import fku.org.example.fku.features.arrowdmfly.ArrowDmgFlyComponent;
import fku.org.example.fku.features.sprint.SprintComponent;

public class MovementPanel extends GuiPanel {

    public MovementPanel() {
        super("移动", MovementConfig.getInstance().guiX, MovementConfig.getInstance().guiY, 120, 200);
    }

    @Override
    protected void init() {
        addComponent(new NoJumpDelayComponent(0, 0, 110, 20));
        addComponent(new ArrowDmgFlyComponent(0, 0, 110, 20));
        addComponent(new SprintComponent(0, 0, 110, 20));
    }

    @Override
    protected void savePosition() {
        // 使用 setter 方法，自动保存配置
        MovementConfig.getInstance().setGuiX(this.x);
        MovementConfig.getInstance().setGuiY(this.y);
    }
}