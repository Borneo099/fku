package fku.org.example.fku.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import java.io.File;
import net.minecraftforge.common.ForgeConfigSpec;

public class FkuConfig {
    private static final String FILE_NAME = "fku-config.toml";
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec SPEC;
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
    public static ForgeConfigSpec.IntValue combatPanelX;
    public static ForgeConfigSpec.IntValue combatPanelY;
    public static ForgeConfigSpec.BooleanValue disableConnectionTimeout;

    private static File getConfigDir() {
        File configDir = new File("fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return configDir;
    }

    public static void init() {
        File configDir = FkuConfig.getConfigDir();
        File configFile = new File(configDir, FILE_NAME);
        CommentedFileConfig configData = (CommentedFileConfig)CommentedFileConfig.builder((File)configFile).sync().autosave().writingMode(WritingMode.REPLACE).build();
        configData.load();
        SPEC.setConfig((CommentedConfig)configData);
    }

    static {
        BUILDER.push("GUI Settings");
        guiKey = BUILDER.comment("\u6253\u5f00GUI\u7684\u6309\u952e\u540d\u79f0\uff0c\u9ed8\u8ba4\u4e3a\u53f3Shift").define("gui_open_key", "key.keyboard.right.shift");
        guiXPos = BUILDER.comment("GUI\u7a97\u53e3X\u5750\u6807").defineInRange("gui_x_pos", 100, 0, Integer.MAX_VALUE);
        guiYPos = BUILDER.comment("GUI\u7a97\u53e3Y\u5750\u6807").defineInRange("gui_y_pos", 100, 0, Integer.MAX_VALUE);
        visualXPos = BUILDER.comment("\u89c6\u89c9\u9762\u677fX\u5750\u6807").defineInRange("visual_x_pos", 230, 0, Integer.MAX_VALUE);
        visualYPos = BUILDER.comment("\u89c6\u89c9\u9762\u677fY\u5750\u6807").defineInRange("visual_y_pos", 100, 0, Integer.MAX_VALUE);
        toolXPos = BUILDER.comment("\u5de5\u5177\u9762\u677fX\u5750\u6807").defineInRange("tool_x_pos", 360, 0, Integer.MAX_VALUE);
        toolYPos = BUILDER.comment("\u5de5\u5177\u9762\u677fY\u5750\u6807").defineInRange("tool_y_pos", 100, 0, Integer.MAX_VALUE);
        autoDropPanelXPos = BUILDER.comment("\u81ea\u52a8\u4e22\u9762\u677fX\u5750\u6807").defineInRange("auto_drop_panel_x_pos", 0, 0, Integer.MAX_VALUE);
        autoDropPanelYPos = BUILDER.comment("\u81ea\u52a8\u4e22\u9762\u677fY\u5750\u6807").defineInRange("auto_drop_panel_y_pos", 0, 0, Integer.MAX_VALUE);
        entertainmentPanelX = BUILDER.comment("\u5a31\u4e50\u9762\u677fX\u5750\u6807").defineInRange("entertainment_panel_x_pos", 490, 0, Integer.MAX_VALUE);
        entertainmentPanelY = BUILDER.comment("\u5a31\u4e50\u9762\u677fY\u5750\u6807").defineInRange("entertainment_panel_y_pos", 490, 0, Integer.MAX_VALUE);
        combatPanelX = BUILDER.comment("\u6218\u6597\u9762\u677fX\u5750\u6807").defineInRange("combat_panel_x_pos", 230, 0, Integer.MAX_VALUE);
        combatPanelY = BUILDER.comment("\u6218\u6597\u9762\u677fY\u5750\u6807").defineInRange("combat_panel_y_pos", 260, 0, Integer.MAX_VALUE);
        BUILDER.pop();
        BUILDER.push("Feature Toggles");
        disableConnectionTimeout = BUILDER.comment("\u7981\u7528\u8fde\u63a5\u8d85\u65f6\u68c0\u6d4b\uff0c\u5f00\u542f\u540e\u65ad\u5f00\u8fde\u63a5\u65f6\u4e0d\u4f1a\u5f39\u51fa\u8d85\u65f6\u63d0\u793a").define("disable_connection_timeout", false);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

