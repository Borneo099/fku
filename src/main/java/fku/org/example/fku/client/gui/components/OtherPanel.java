package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.FkuConfig;
import net.minecraft.client.Minecraft;

/**
 * 其它功能面板
 * 包含GUI按键绑定和外观设置
 */
public class OtherPanel extends GuiPanel {

    public OtherPanel() {
        super("其它", FkuConfig.guiXPos.get(), FkuConfig.guiYPos.get(), 120, 100);
    }

    @Override
    protected void init() {
        // 添加按键绑定组件
        addComponent(new GuiComponent(0, 0, 110, 20, "绑定GUI按键"));
        
        // 添加GUI外观设置按钮
        addComponent(new ConfigButtonComponent(0, 0, 110, 20, "外观设置", () -> {
            Minecraft.getInstance().setScreen(new GuiStyleScreen());
        }));
    }

    @Override
    protected void savePosition() {
        FkuConfig.guiXPos.set(this.x);
        FkuConfig.guiYPos.set(this.y);
    }
}