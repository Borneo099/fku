package fku.org.example.fku.features.autodrop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AutoDropConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static AutoDropConfig instance;
    public boolean enabled = false;
    public boolean dropAsEntity = true;
    public int scanInterval = 3;
    public List<String> blacklist = new ArrayList<String>();

    private static File getConfigFile() {
        File configDir = new File(AutoDropConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "autodrop_config.json");
    }

    private static File getGameDirectory() {
        try {
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            Object minecraft = minecraftClass.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
            return (File)minecraftClass.getField("gameDir").get(minecraft);
        }
        catch (Exception e) {
            return Paths.get(".", new String[0]).toAbsolutePath().normalize().toFile();
        }
    }

    public static AutoDropConfig getInstance() {
        if (instance == null) {
            AutoDropConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = AutoDropConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (AutoDropConfig)GSON.fromJson(reader, AutoDropConfig.class);
            }
            catch (IOException e) {
                instance = new AutoDropConfig();
            }
        } else {
            instance = new AutoDropConfig();
            AutoDropConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        File configFile = AutoDropConfig.getConfigFile();
        try (FileWriter writer = new FileWriter(configFile);){
            GSON.toJson(instance, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addToBlacklist(String itemId) {
        if (!this.blacklist.contains(itemId)) {
            this.blacklist.add(itemId);
            AutoDropConfig.save();
        }
    }

    public void setScanInterval(int v) {
        this.scanInterval = Math.max(1, Math.min(20, v));
        AutoDropConfig.save();
    }

    public void removeFromBlacklist(String itemId) {
        this.blacklist.remove(itemId);
        AutoDropConfig.save();
    }

    public boolean isBlacklisted(String itemId) {
        return this.blacklist.contains(itemId);
    }

    public void clearBlacklist() {
        this.blacklist.clear();
        AutoDropConfig.save();
    }
}

