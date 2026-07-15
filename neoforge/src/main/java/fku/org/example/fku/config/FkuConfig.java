package fku.org.example.fku.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class FkuConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static ModConfigSpec SPEC;

    // 只保留 GUI 相关配置
    public static ModConfigSpec.ConfigValue<String> guiKey;
    public static ModConfigSpec.IntValue guiXPos;
    public static ModConfigSpec.IntValue guiYPos;
    public static ModConfigSpec.IntValue visualXPos;
    public static ModConfigSpec.IntValue visualYPos;
    public static ModConfigSpec.IntValue toolXPos;
    public static ModConfigSpec.IntValue toolYPos;
    public static ModConfigSpec.IntValue autoDropPanelXPos;
    public static ModConfigSpec.IntValue autoDropPanelYPos;
    public static ModConfigSpec.IntValue entertainmentPanelX;
    public static ModConfigSpec.IntValue entertainmentPanelY;
    public static ModConfigSpec.IntValue combatPanelX;
    public static ModConfigSpec.IntValue combatPanelY;
    public static ModConfigSpec.BooleanValue disableConnectionTimeout;

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
        combatPanelX = BUILDER
                .comment("战斗面板X坐标")
                .defineInRange("combat_panel_x_pos", 230, 0, Integer.MAX_VALUE);
        combatPanelY = BUILDER
                .comment("战斗面板Y坐标")
                .defineInRange("combat_panel_y_pos", 260, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Feature Toggles");
        disableConnectionTimeout = BUILDER
                .comment("禁用连接超时检测，开启后断开连接时不会弹出超时提示")
                .define("disable_connection_timeout", false);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}