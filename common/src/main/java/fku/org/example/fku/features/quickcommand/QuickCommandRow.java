package fku.org.example.fku.features.quickcommand;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;

/**
 * 快捷指令配置界面中每一行的控件容器。
 * 独立顶层类（避免嵌套类在 mod 运行环境的 module classloader 下加载失败 -> NoClassDefFoundError）。
 */
public class QuickCommandRow {
    public EditBox input;
    public Button toggle;
    public Button bindBtn;
    public Button up;
    public Button down;
}
