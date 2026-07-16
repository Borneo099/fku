package fku.org.example.fku.features.baritone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Baritone 子功能统一配置（JSON 持久化）
 */
public class BaritoneConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static BaritoneConfig instance;

    // ════════ Parkour 设置 ════════
    public boolean parkourEnabled = false;
    public boolean allowBreak = false;
    public boolean allowPlace = false;
    public boolean allowSprint = true;
    public boolean allowParkour = true;
    public boolean allowParkourPlace = false;
    public boolean allowInventory = false;

    // ════════ Speed 设置 ════════
    public boolean speedEnabled = false;
    public double speedMultiplier = 1.5;
    public boolean groundOnly = true;

    // ════════ ElytraAnywhere 设置 ════════
    public boolean elytraEnabled = false;

    private BaritoneConfig() {}

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "baritone.json");
    }

    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get(".").toAbsolutePath().normalize().toFile();
    }

    public static BaritoneConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File f = getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f)) {
                instance = GSON.fromJson(r, BaritoneConfig.class);
            } catch (IOException e) {
                instance = new BaritoneConfig();
            }
        } else {
            instance = new BaritoneConfig();
            save();
        }
        if (instance == null) instance = new BaritoneConfig();
    }

    public static void save() {
        if (instance == null) return;
        try (FileWriter w = new FileWriter(getConfigFile())) {
            GSON.toJson(instance, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
