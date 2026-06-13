package fku.org.example.fku.features.autodrop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AutoDropConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static AutoDropConfig instance;

    public boolean enabled = false;
    public boolean dropAsEntity = true;
    public List<String> blacklist = new ArrayList<>();

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "autodrop_config.json");
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

    public static AutoDropConfig getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, AutoDropConfig.class);
            } catch (IOException e) {
                instance = new AutoDropConfig();
            }
        } else {
            instance = new AutoDropConfig();
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

    public void addToBlacklist(String itemId) {
        if (!blacklist.contains(itemId)) {
            blacklist.add(itemId);
            save();
        }
    }

    public void removeFromBlacklist(String itemId) {
        blacklist.remove(itemId);
        save();
    }

    public boolean isBlacklisted(String itemId) {
        return blacklist.contains(itemId);
    }

    public void clearBlacklist() {
        blacklist.clear();
        save();
    }
}