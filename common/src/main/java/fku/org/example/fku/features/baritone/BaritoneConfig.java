package fku.org.example.fku.features.baritone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class BaritoneConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static BaritoneConfig instance;
    public boolean parkourEnabled = false;
    public boolean allowBreak = false;
    public boolean allowPlace = false;
    public boolean allowSprint = true;
    public boolean allowParkour = true;
    public boolean allowParkourPlace = false;
    public boolean allowInventory = false;
    public boolean speedEnabled = false;
    public double speedMultiplier = 1.5;
    public boolean groundOnly = true;
    public boolean elytraEnabled = false;

    private BaritoneConfig() {
    }

    private static File getConfigFile() {
        File configDir = new File(BaritoneConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "baritone.json");
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

    public static BaritoneConfig getInstance() {
        if (instance == null) {
            BaritoneConfig.load();
        }
        return instance;
    }

    public static void load() {
        File f = BaritoneConfig.getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f);){
                instance = (BaritoneConfig)GSON.fromJson(r, BaritoneConfig.class);
            }
            catch (IOException e) {
                instance = new BaritoneConfig();
            }
        } else {
            instance = new BaritoneConfig();
            BaritoneConfig.save();
        }
        if (instance == null) {
            instance = new BaritoneConfig();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        try (FileWriter w = new FileWriter(BaritoneConfig.getConfigFile());){
            GSON.toJson(instance, w);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

