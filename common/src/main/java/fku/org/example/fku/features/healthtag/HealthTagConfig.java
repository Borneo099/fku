package fku.org.example.fku.features.healthtag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class HealthTagConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public boolean enabled = true;
    public int x = 100;
    public int y = 100;
    private static HealthTagConfig instance;

    private static File getConfigFile() {
        File configDir = new File(HealthTagConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "healthtag.json");
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

    public static HealthTagConfig getInstance() {
        if (instance == null) {
            HealthTagConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = HealthTagConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (HealthTagConfig)GSON.fromJson(reader, HealthTagConfig.class);
            }
            catch (IOException e) {
                instance = new HealthTagConfig();
            }
        } else {
            instance = new HealthTagConfig();
            HealthTagConfig.save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(HealthTagConfig.getConfigFile());){
            GSON.toJson(instance, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

