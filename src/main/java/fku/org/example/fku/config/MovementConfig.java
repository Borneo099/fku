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
            // 尝试从 Minecraft 获取游戏目录（仅客户端）
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            Object minecraft = minecraftClass.getMethod("getInstance").invoke(null);
            return (File) minecraftClass.getField("gameDir").get(minecraft);
        } catch (Exception e) {
            // 服务端环境或其他情况，返回当前工作目录
            return Paths.get(".").toAbsolutePath().normalize().toFile();
        }
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
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, MovementConfig.class);
            } catch (IOException e) {
                instance = new MovementConfig();
            }
        } else {
            instance = new MovementConfig();
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
}
