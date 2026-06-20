package fku.org.example.fku.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import java.io.File;

public class FkuConfig {

    private static File getConfigDir() {
        File configDir = new File("fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return configDir;
    }
    private static final String FILE_NAME = "fku-config.toml";

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec SPEC;

    // 只保留 GUI 相关配置
    public static ForgeConfigSpec.ConfigValue<String> guiKey;
    public static ForgeConfigSpec.IntValue guiXPos;
    public static ForgeConfigSpec.IntValue guiYPos;
    public static ForgeConfigSpec.IntValue visualXPos;
    public static ForgeConfigSpec.IntValue visualYPos;
    public static ForgeConfigSpec.IntValue toolXPos;
    public static ForgeConfigSpec.IntValue toolYPos;
    public static ForgeConfigSpec.IntValue autoDropPanelXPos;
    public static ForgeConfigSpec.IntValue autoDropPanelYPos;
    public static ForgeConfigSpec.IntValue entertainmentPanelX;
    public static ForgeConfigSpec.IntValue entertainmentPanelY;
    public static ForgeConfigSpec.BooleanValue disableConnectionTimeout;

    static {
        BUILDER.push("GUI Settings");
        guiKey = BUILDER
                .comment("打开GUI的按键名称，默认为右Shift")
                .define("gui_open_key", "key.keyboard.right.shift");
        guiXPos = BUILDER
                .comment("GUI窗口X坐标")
                .defineInRange("gui_x_pos", 100, 0, Integer.MAX_VALUE);
        guiYPos = BUILDER
                .comment("GUI窗口Y坐标")
                .defineInRange("gui_y_pos", 100, 0, Integer.MAX_VALUE);
        visualXPos = BUILDER
                .comment("视觉面板X坐标")
                .defineInRange("visual_x_pos", 230, 0, Integer.MAX_VALUE);
        visualYPos = BUILDER
                .comment("视觉面板Y坐标")
                .defineInRange("visual_y_pos", 100, 0, Integer.MAX_VALUE);
        toolXPos = BUILDER
                .comment("工具面板X坐标")
                .defineInRange("tool_x_pos", 360, 0, Integer.MAX_VALUE);
        toolYPos = BUILDER
                .comment("工具面板Y坐标")
                .defineInRange("tool_y_pos", 100, 0, Integer.MAX_VALUE);
        autoDropPanelXPos = BUILDER
                .comment("自动丢面板X坐标")
                .defineInRange("auto_drop_panel_x_pos", 0, 0, Integer.MAX_VALUE);
        autoDropPanelYPos = BUILDER
                .comment("自动丢面板Y坐标")
                .defineInRange("auto_drop_panel_y_pos", 0, 0, Integer.MAX_VALUE);
        entertainmentPanelX = BUILDER
                .comment("娱乐面板X坐标")
                .defineInRange("entertainment_panel_x_pos", 490, 0, Integer.MAX_VALUE);
        entertainmentPanelY = BUILDER
                .comment("娱乐面板Y坐标")
                .defineInRange("entertainment_panel_y_pos", 490, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Feature Toggles");
        disableConnectionTimeout = BUILDER
                .comment("禁用连接超时检测，开启后断开连接时不会弹出超时提示")
                .define("disable_connection_timeout", false);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void init() {
        File configDir = getConfigDir();
        File configFile = new File(configDir, FILE_NAME);
        final CommentedFileConfig configData = CommentedFileConfig.builder(configFile)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();
        configData.load();
        SPEC.setConfig(configData);
    }
}