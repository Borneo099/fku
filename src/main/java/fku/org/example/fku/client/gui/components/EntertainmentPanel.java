package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.features.displaymodel.DisplayModelScreen;
import net.minecraft.client.Minecraft;

/**
 * 娱乐功能面板
 * 包含实体模型展示等娱乐功能
 */
public class EntertainmentPanel extends GuiPanel {

    public EntertainmentPanel() {
        super("娱乐", FkuConfig.entertainmentPanelX.get(), FkuConfig.entertainmentPanelY.get(), 120, 80);
    }

    @Override
    protected void init() {
        // 添加实体模型按钮
        addComponent(new ConfigButtonComponent(0, 0, 110, 20, "实体模型", () -> {
            Minecraft.getInstance().setScreen(new DisplayModelScreen());
        }));
    }

    @Override
    protected void savePosition() {
        FkuConfig.entertainmentPanelX.set(this.x);
        FkuConfig.entertainmentPanelY.set(this.y);
    }
}