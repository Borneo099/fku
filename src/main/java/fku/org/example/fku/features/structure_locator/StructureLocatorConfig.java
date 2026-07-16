package fku.org.example.fku.features.structure_locator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class StructureLocatorConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static StructureLocatorConfig instance;

    public boolean enabled = false;
    public String manualSeed = "";
    public long capturedSeed = 0;
    public boolean hasSeed = false;
    public int targetIndex = 0;
    public int searchRadius = 16;

    private StructureLocatorConfig() {}

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "structure_locator.json");
    }

    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get(".").toAbsolutePath().normalize().toFile();
    }

    public static StructureLocatorConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File f = getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f)) {
                instance = GSON.fromJson(r, StructureLocatorConfig.class);
            } catch (IOException e) {
                instance = new StructureLocatorConfig();
            }
        } else {
            instance = new StructureLocatorConfig();
            save();
        }
        if (instance == null) instance = new StructureLocatorConfig();
    }

    public static void save() {
        if (instance == null) return;
        try (FileWriter w = new FileWriter(getConfigFile())) {
            GSON.toJson(instance, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean v) { this.enabled = v; save(); }
}
