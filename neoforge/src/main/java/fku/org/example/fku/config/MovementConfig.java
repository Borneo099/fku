package fku.org.example.fku.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class MovementConfig {
    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "movement.json");
    }
    
    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get(".").toAbsolutePath().normalize().toFile();
    }
    
    // 添加调试日志，打印配置路径
    private static void debugConfigPath() {
        File configFile = getConfigFile();
        System.out.println("[FKU] MovementConfig path: " + configFile.getAbsolutePath());
    }
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean noJumpDelayEnabled = false;
    public boolean arrowDmgFlyEnabled = false;
    public boolean yPosOverlayEnabled = false;
    public int guiX = 250;
    public int guiY = 100;

    private static MovementConfig instance;

    public static MovementConfig getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        debugConfigPath();
        
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, MovementConfig.class);
                System.out.println("[FKU] MovementConfig loaded successfully");
            } catch (IOException e) {
                System.out.println("[FKU] Failed to load MovementConfig, creating new instance");
                instance = new MovementConfig();
            }
        } else {
            System.out.println("[FKU] MovementConfig file not found, creating new instance");
            instance = new MovementConfig();
            save();
        }
    }

    public static void save() {
        if (instance == null) {
            System.out.println("[FKU] MovementConfig instance is null, cannot save");
            return;
        }
        
        File configFile = getConfigFile();
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(instance, writer);
            System.out.println("[FKU] MovementConfig saved to: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 确保配置被修改后自动保存
    public void setNoJumpDelayEnabled(boolean value) {
        this.noJumpDelayEnabled = value;
        save();
    }
    
    public void setArrowDmgFlyEnabled(boolean value) {
        this.arrowDmgFlyEnabled = value;
        save();
    }
    
    public void setYPosOverlayEnabled(boolean value) {
        this.yPosOverlayEnabled = value;
        save();
    }
    
    public void setGuiX(int value) {
        this.guiX = value;
        save();
    }
    
    public void setGuiY(int value) {
        this.guiY = value;
        save();
    }
}