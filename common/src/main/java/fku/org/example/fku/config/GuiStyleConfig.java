package fku.org.example.fku.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class GuiStyleConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static GuiStyleConfig instance;
    public int cornerRadius = 8;
    public int backgroundAlpha = 180;
    public int panelOpacity = 220;
    public int blurStrength = 50;
    public boolean animationEnabled = true;
    public float springStiffness = 8.0f;
    public boolean glowEnabled = true;
    public int animationSpeed = 200;
    public boolean shadowEnabled = true;
    public int shadowStrength = 30;
    public int primaryColorR = 0;
    public int primaryColorG = 102;
    public int primaryColorB = 204;
    public int backgroundColorR = 30;
    public int backgroundColorG = 30;
    public int backgroundColorB = 30;
    public int borderColorR = 60;
    public int borderColorG = 60;
    public int borderColorB = 60;
    public int textColorR = 255;
    public int textColorG = 255;
    public int textColorB = 255;
    public int enabledColorR = 0;
    public int enabledColorG = 200;
    public int enabledColorB = 0;
    public int disabledColorR = 200;
    public int disabledColorG = 0;
    public int disabledColorB = 0;
    public int panelWidth = 120;
    public int panelSpacing = 10;
    public int componentHeight = 20;
    public int componentSpacing = 5;

    private static File getConfigFile() {
        File configDir = new File(GuiStyleConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "gui_style.json");
    }

    private static File getGameDirectory() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                return mc.gameDirectory;
            }
        }
        catch (Exception exception) {
            // ignored
        }
        return Paths.get("config", new String[0]).toAbsolutePath().normalize().getParent().toFile();
    }

    public static GuiStyleConfig getInstance() {
        if (instance == null) {
            GuiStyleConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = GuiStyleConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (GuiStyleConfig)GSON.fromJson(reader, GuiStyleConfig.class);
            }
            catch (IOException e) {
                instance = new GuiStyleConfig();
            }
        } else {
            instance = new GuiStyleConfig();
            GuiStyleConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        File configFile = GuiStyleConfig.getConfigFile();
        try (FileWriter writer = new FileWriter(configFile);){
            GSON.toJson(instance, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPrimaryColor() {
        return this.primaryColorR << 16 | this.primaryColorG << 8 | this.primaryColorB;
    }

    public int getPrimaryColorWithAlpha(int alpha) {
        return alpha << 24 | this.primaryColorR << 16 | this.primaryColorG << 8 | this.primaryColorB;
    }

    public int getBackgroundColorWithAlpha(int alpha) {
        return alpha << 24 | this.backgroundColorR << 16 | this.backgroundColorG << 8 | this.backgroundColorB;
    }

    public int getBorderColorWithAlpha(int alpha) {
        return alpha << 24 | this.borderColorR << 16 | this.borderColorG << 8 | this.borderColorB;
    }

    public int getTextColor() {
        return this.textColorR << 16 | this.textColorG << 8 | this.textColorB;
    }

    public int getEnabledColor() {
        return this.enabledColorR << 16 | this.enabledColorG << 8 | this.enabledColorB;
    }

    public int getDisabledColor() {
        return this.disabledColorR << 16 | this.disabledColorG << 8 | this.disabledColorB;
    }

    public void setCornerRadius(int value) {
        this.cornerRadius = Math.max(0, Math.min(20, value));
        GuiStyleConfig.save();
    }

    public void setBackgroundAlpha(int value) {
        this.backgroundAlpha = Math.max(0, Math.min(255, value));
        GuiStyleConfig.save();
    }

    public void setBlurStrength(int value) {
        this.blurStrength = Math.max(0, Math.min(100, value));
        GuiStyleConfig.save();
    }

    public void setAnimationEnabled(boolean value) {
        this.animationEnabled = value;
        GuiStyleConfig.save();
    }

    public void setAnimationSpeed(int value) {
        this.animationSpeed = Math.max(50, Math.min(500, value));
        GuiStyleConfig.save();
    }

    public void setSpringStiffness(float value) {
        this.springStiffness = Math.max(2.0f, Math.min(20.0f, value));
        GuiStyleConfig.save();
    }

    public void setGlowEnabled(boolean value) {
        this.glowEnabled = value;
        GuiStyleConfig.save();
    }

    public void setPanelOpacity(int value) {
        this.panelOpacity = Math.max(100, Math.min(255, value));
        GuiStyleConfig.save();
    }

    public void setShadowEnabled(boolean value) {
        this.shadowEnabled = value;
        GuiStyleConfig.save();
    }

    public void setShadowStrength(int value) {
        this.shadowStrength = Math.max(0, Math.min(100, value));
        GuiStyleConfig.save();
    }

    public void setPrimaryColor(int r, int g, int b) {
        this.primaryColorR = Math.max(0, Math.min(255, r));
        this.primaryColorG = Math.max(0, Math.min(255, g));
        this.primaryColorB = Math.max(0, Math.min(255, b));
        GuiStyleConfig.save();
    }

    public void setBackgroundColor(int r, int g, int b) {
        this.backgroundColorR = Math.max(0, Math.min(255, r));
        this.backgroundColorG = Math.max(0, Math.min(255, g));
        this.backgroundColorB = Math.max(0, Math.min(255, b));
        GuiStyleConfig.save();
    }

    public void setBorderColor(int r, int g, int b) {
        this.borderColorR = Math.max(0, Math.min(255, r));
        this.borderColorG = Math.max(0, Math.min(255, g));
        this.borderColorB = Math.max(0, Math.min(255, b));
        GuiStyleConfig.save();
    }

    public void setTextColor(int r, int g, int b) {
        this.textColorR = Math.max(0, Math.min(255, r));
        this.textColorG = Math.max(0, Math.min(255, g));
        this.textColorB = Math.max(0, Math.min(255, b));
        GuiStyleConfig.save();
    }

    public void setEnabledColor(int r, int g, int b) {
        this.enabledColorR = Math.max(0, Math.min(255, r));
        this.enabledColorG = Math.max(0, Math.min(255, g));
        this.enabledColorB = Math.max(0, Math.min(255, b));
        GuiStyleConfig.save();
    }

    public void setDisabledColor(int r, int g, int b) {
        this.disabledColorR = Math.max(0, Math.min(255, r));
        this.disabledColorG = Math.max(0, Math.min(255, g));
        this.disabledColorB = Math.max(0, Math.min(255, b));
        GuiStyleConfig.save();
    }

    public void setPanelWidth(int value) {
        this.panelWidth = Math.max(80, Math.min(200, value));
        GuiStyleConfig.save();
    }

    public void setPanelSpacing(int value) {
        this.panelSpacing = Math.max(5, Math.min(30, value));
        GuiStyleConfig.save();
    }

    public void setComponentHeight(int value) {
        this.componentHeight = Math.max(15, Math.min(30, value));
        GuiStyleConfig.save();
    }

    public void setComponentSpacing(int value) {
        this.componentSpacing = Math.max(2, Math.min(15, value));
        GuiStyleConfig.save();
    }
}

