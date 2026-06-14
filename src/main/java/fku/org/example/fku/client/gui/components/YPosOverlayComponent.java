package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.MovementConfig;

public class YPosOverlayComponent extends ToggleComponent {

    public YPosOverlayComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "Y坐标显示");
    }

    @Override
    protected boolean isEnabled() {
        return MovementConfig.getInstance().yPosOverlayEnabled;
    }

    @Override
    protected void toggle() {
        // 使用 setter 方法，自动保存配置
        MovementConfig.getInstance().setYPosOverlayEnabled(!MovementConfig.getInstance().yPosOverlayEnabled);
    }

    @Override
    protected void saveConfig() {
        // 现在通过 setter 自动保存，这里可以留空或者仍然调用 save() 确保安全
        MovementConfig.save();
    }
}