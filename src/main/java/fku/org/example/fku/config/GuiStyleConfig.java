package fku.org.example.fku.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * GUI外观配置类
 * 支持自定义圆角、毛玻璃效果、动画、颜色等
 */
public class GuiStyleConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static GuiStyleConfig instance;

    // ============ 外观设置 ============
    
    /** 圆角半径 - 默认8 */
    public int cornerRadius = 8;
    
    /** 背景透明度 (0-255) - 默认180 */
    public int backgroundAlpha = 180;
    
    /** 毛玻璃效果强度 (0-100) - 默认50 */
    public int blurStrength = 50;
    
    /** 是否启用动画 - 默认true */
    public boolean animationEnabled = true;
    
    /** 动画速度 (毫秒) - 默认200 */
    public int animationSpeed = 200;
    
    /** 是否启用阴影效果 - 默认true */
    public boolean shadowEnabled = true;
    
    /** 阴影强度 (0-100) - 默认30 */
    public int shadowStrength = 30;
    
    // ============ 颜色设置 ============
    
    /** 主色调 RGB - 默认蓝色 */
    public int primaryColorR = 0;
    public int primaryColorG = 102;
    public int primaryColorB = 204;
    
    /** 背景色 RGB - 默认深灰 */
    public int backgroundColorR = 30;
    public int backgroundColorG = 30;
    public int backgroundColorB = 30;
    
    /** 边框色 RGB - 默认浅灰 */
    public int borderColorR = 60;
    public int borderColorG = 60;
    public int borderColorB = 60;
    
    /** 文字颜色 RGB - 默认白色 */
    public int textColorR = 255;
    public int textColorG = 255;
    public int textColorB = 255;
    
    /** 启用状态颜色 RGB - 默认绿色 */
    public int enabledColorR = 0;
    public int enabledColorG = 200;
    public int enabledColorB = 0;
    
    /** 禁用状态颜色 RGB - 默认红色 */
    public int disabledColorR = 200;
    public int disabledColorG = 0;
    public int disabledColorB = 0;

    // ============ 其他设置 ============
    
    /** 面板宽度 - 默认120 */
    public int panelWidth = 120;
    
    /** 面板间距 - 默认10 */
    public int panelSpacing = 10;
    
    /** 组件高度 - 默认20 */
    public int componentHeight = 20;
    
    /** 组件间距 - 默认5 */
    public int componentSpacing = 5;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "gui_style.json");
    }

    /**
     * 获取Minecraft游戏目录
     * 使用Minecraft.getInstance().gameDirectory直接访问（兼容Mojang官方映射，字段名为gameDirectory而非MCP的gameDir）
     */
    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) {
                return mc.gameDirectory;
            }
        } catch (Exception e) {
            // 初始化失败时回退到当前目录
        }
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }

    public static GuiStyleConfig getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, GuiStyleConfig.class);
            } catch (IOException e) {
                instance = new GuiStyleConfig();
            }
        } else {
            instance = new GuiStyleConfig();
            save();
        }
    }

    public static void save() {
        if (instance == null) return;
        
        File configFile = getConfigFile();
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ============ 辅助方法 ============
    
    public int getPrimaryColor() {
        return (primaryColorR << 16) | (primaryColorG << 8) | primaryColorB;
    }
    
    public int getPrimaryColorWithAlpha(int alpha) {
        return (alpha << 24) | (primaryColorR << 16) | (primaryColorG << 8) | primaryColorB;
    }
    
    public int getBackgroundColorWithAlpha(int alpha) {
        return (alpha << 24) | (backgroundColorR << 16) | (backgroundColorG << 8) | backgroundColorB;
    }
    
    public int getBorderColorWithAlpha(int alpha) {
        return (alpha << 24) | (borderColorR << 16) | (borderColorG << 8) | borderColorB;
    }
    
    public int getTextColor() {
        return (textColorR << 16) | (textColorG << 8) | textColorB;
    }
    
    public int getEnabledColor() {
        return (enabledColorR << 16) | (enabledColorG << 8) | enabledColorB;
    }
    
    public int getDisabledColor() {
        return (disabledColorR << 16) | (disabledColorG << 8) | disabledColorB;
    }

    // ============ Setter方法（自动保存） ============
    
    public void setCornerRadius(int value) {
        this.cornerRadius = Math.max(0, Math.min(20, value));
        save();
    }
    
    public void setBackgroundAlpha(int value) {
        this.backgroundAlpha = Math.max(0, Math.min(255, value));
        save();
    }
    
    public void setBlurStrength(int value) {
        this.blurStrength = Math.max(0, Math.min(100, value));
        save();
    }
    
    public void setAnimationEnabled(boolean value) {
        this.animationEnabled = value;
        save();
    }
    
    public void setAnimationSpeed(int value) {
        this.animationSpeed = Math.max(50, Math.min(500, value));
        save();
    }
    
    public void setShadowEnabled(boolean value) {
        this.shadowEnabled = value;
        save();
    }
    
    public void setShadowStrength(int value) {
        this.shadowStrength = Math.max(0, Math.min(100, value));
        save();
    }
    
    public void setPrimaryColor(int r, int g, int b) {
        this.primaryColorR = Math.max(0, Math.min(255, r));
        this.primaryColorG = Math.max(0, Math.min(255, g));
        this.primaryColorB = Math.max(0, Math.min(255, b));
        save();
    }
    
    public void setBackgroundColor(int r, int g, int b) {
        this.backgroundColorR = Math.max(0, Math.min(255, r));
        this.backgroundColorG = Math.max(0, Math.min(255, g));
        this.backgroundColorB = Math.max(0, Math.min(255, b));
        save();
    }
    
    public void setBorderColor(int r, int g, int b) {
        this.borderColorR = Math.max(0, Math.min(255, r));
        this.borderColorG = Math.max(0, Math.min(255, g));
        this.borderColorB = Math.max(0, Math.min(255, b));
        save();
    }
    
    public void setTextColor(int r, int g, int b) {
        this.textColorR = Math.max(0, Math.min(255, r));
        this.textColorG = Math.max(0, Math.min(255, g));
        this.textColorB = Math.max(0, Math.min(255, b));
        save();
    }
    
    public void setEnabledColor(int r, int g, int b) {
        this.enabledColorR = Math.max(0, Math.min(255, r));
        this.enabledColorG = Math.max(0, Math.min(255, g));
        this.enabledColorB = Math.max(0, Math.min(255, b));
        save();
    }
    
    public void setDisabledColor(int r, int g, int b) {
        this.disabledColorR = Math.max(0, Math.min(255, r));
        this.disabledColorG = Math.max(0, Math.min(255, g));
        this.disabledColorB = Math.max(0, Math.min(255, b));
        save();
    }
    
    public void setPanelWidth(int value) {
        this.panelWidth = Math.max(80, Math.min(200, value));
        save();
    }
    
    public void setPanelSpacing(int value) {
        this.panelSpacing = Math.max(5, Math.min(30, value));
        save();
    }
    
    public void setComponentHeight(int value) {
        this.componentHeight = Math.max(15, Math.min(30, value));
        save();
    }
    
    public void setComponentSpacing(int value) {
        this.componentSpacing = Math.max(2, Math.min(15, value));
        save();
    }
}