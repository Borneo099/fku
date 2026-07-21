package fku.org.example.fku.features.waterwalk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class WaterWalkConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public boolean enabled = false;
    private static WaterWalkConfig instance;

    private static File getConfigFile() {
        try {
            Minecraft mc = Minecraft.getInstance();
            File configDir = new File(mc.gameDirectory, "fku");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            return new File(configDir, "waterwalk.json");
        }
        catch (Exception exception) {
            return Paths.get(".", "config", "fku", "waterwalk.json").toAbsolutePath().normalize().toFile();
        }
    }

    public static WaterWalkConfig getInstance() {
        if (instance == null) {
            WaterWalkConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = WaterWalkConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (WaterWalkConfig)GSON.fromJson(reader, WaterWalkConfig.class);
                System.out.println("[WaterWalk] \u914d\u7f6e\u5df2\u52a0\u8f7d: " + configFile.getAbsolutePath());
            }
            catch (IOException e) {
                System.out.println("[WaterWalk] \u914d\u7f6e\u52a0\u8f7d\u5931\u8d25\uff0c\u4f7f\u7528\u9ed8\u8ba4\u503c");
                instance = new WaterWalkConfig();
            }
        } else {
            instance = new WaterWalkConfig();
            WaterWalkConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        File configFile = WaterWalkConfig.getConfigFile();
        try (FileWriter writer = new FileWriter(configFile);){
            GSON.toJson(instance, writer);
            System.out.println("[WaterWalk] \u914d\u7f6e\u5df2\u4fdd\u5b58: " + configFile.getAbsolutePath());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

