package fku.org.example.fku.features.healthtag; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class HealthTagConfig {
    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "healthtag.json");
    }
    
    private static File getGameDirectory() {
        try {
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            Object minecraft = minecraftClass.getMethod("getInstance").invoke(null);
            return (File) minecraftClass.getField("gameDir").get(minecraft);
        } catch (Exception e) {
            return Paths.get(".").toAbsolutePath().normalize().toFile();
        }
    }
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean enabled = true;
    public int x = 100;
    public int y = 100;

    private static HealthTagConfig instance;

    public static HealthTagConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, HealthTagConfig.class);
            } catch (IOException e) {
                instance = new HealthTagConfig();
            }
        } else {
            instance = new HealthTagConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(getConfigFile())) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}