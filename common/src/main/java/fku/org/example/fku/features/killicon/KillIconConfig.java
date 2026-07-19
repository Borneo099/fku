package fku.org.example.fku.features.killicon; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class KillIconConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static KillIconConfig instance;

    public boolean enabled = false;
    public int x = 10;
    public int y = 10;
    public int displayDuration = 60;
    public int maxEntries = 5;
    public int entryHeight = 12;
    public int entrySpacing = 2;
    public int bgOpacity = 0;
    public boolean showBackground = false;
    public float scale = 1.0f;
    public boolean showCombo = true;
    public boolean showDistance = true;
    public boolean headshotEnabled = true;
    public boolean enableAnimation = true;

    private static File getConfigFile() {
        File dir = new File(getGameDir(), "fku");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "killicon.json");
    }

    private static File getGameDir() {
        try { Minecraft mc = Minecraft.getInstance(); if (mc != null) return mc.gameDirectory; }
        catch (Exception ignored) {}
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }

    public static KillIconConfig getInstance() { if (instance == null) load(); return instance; }

    public static void load() {
        File f = getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f)) { instance = GSON.fromJson(r, KillIconConfig.class); }
            catch (IOException e) { instance = new KillIconConfig(); }
        } else { instance = new KillIconConfig(); save(); }
        if (instance == null) instance = new KillIconConfig();
    }

    public static void save() {
        if (instance == null) return;
        try (FileWriter w = new FileWriter(getConfigFile())) { GSON.toJson(instance, w); }
        catch (IOException e) { e.printStackTrace(); }
    }
}
