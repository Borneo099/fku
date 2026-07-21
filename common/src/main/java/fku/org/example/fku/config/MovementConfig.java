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

public class MovementConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public boolean noJumpDelayEnabled = false;
    public boolean arrowDmgFlyEnabled = false;
    public boolean yPosOverlayEnabled = false;
    public int guiX = 250;
    public int guiY = 100;
    private static MovementConfig instance;

    private static File getConfigFile() {
        File configDir = new File(MovementConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "movement.json");
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
        return Paths.get(".", new String[0]).toAbsolutePath().normalize().toFile();
    }

    private static void debugConfigPath() {
        File configFile = MovementConfig.getConfigFile();
        System.out.println("[FKU] MovementConfig path: " + configFile.getAbsolutePath());
    }

    public static MovementConfig getInstance() {
        if (instance == null) {
            MovementConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = MovementConfig.getConfigFile();
        MovementConfig.debugConfigPath();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (MovementConfig)GSON.fromJson(reader, MovementConfig.class);
                System.out.println("[FKU] MovementConfig loaded successfully");
            }
            catch (IOException e) {
                System.out.println("[FKU] Failed to load MovementConfig, creating new instance");
                instance = new MovementConfig();
            }
        } else {
            System.out.println("[FKU] MovementConfig file not found, creating new instance");
            instance = new MovementConfig();
            MovementConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            System.out.println("[FKU] MovementConfig instance is null, cannot save");
            return;
        }
        File configFile = MovementConfig.getConfigFile();
        try (FileWriter writer = new FileWriter(configFile);){
            GSON.toJson(instance, writer);
            System.out.println("[FKU] MovementConfig saved to: " + configFile.getAbsolutePath());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setNoJumpDelayEnabled(boolean value) {
        this.noJumpDelayEnabled = value;
        MovementConfig.save();
    }

    public void setArrowDmgFlyEnabled(boolean value) {
        this.arrowDmgFlyEnabled = value;
        MovementConfig.save();
    }

    public void setYPosOverlayEnabled(boolean value) {
        this.yPosOverlayEnabled = value;
        MovementConfig.save();
    }

    public void setGuiX(int value) {
        this.guiX = value;
        MovementConfig.save();
    }

    public void setGuiY(int value) {
        this.guiY = value;
        MovementConfig.save();
    }
}

