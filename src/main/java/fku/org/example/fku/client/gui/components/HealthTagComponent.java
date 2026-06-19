package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.HealthTagConfig;
import net.minecraft.client.Minecraft;

/**
 * HealthTag开关组件
 * 左键切换开关，右键打开配置菜单
 */
public class HealthTagComponent extends ToggleComponent {

    public HealthTagComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "HealthTag");
    }

    @Override
    protected boolean isEnabled() {
        return HealthTagConfig.getInstance().enabled;
    }

    @Override
    protected void toggle() {
        HealthTagConfig.getInstance().enabled = !HealthTagConfig.getInstance().enabled;
    }

    @Override
    protected void saveConfig() {
        HealthTagConfig.save();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                // 左键切换开关
                toggle();
                saveConfig();
                return true;
            } else if (button == 1) {
                // 右键打开配置菜单
                Minecraft.getInstance().setScreen(new HealthTagConfigScreen());
                return true;
            }
        }
        return false;
    }
}