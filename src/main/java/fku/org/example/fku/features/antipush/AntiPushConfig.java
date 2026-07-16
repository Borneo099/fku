package fku.org.example.fku.features.antipush; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 防推功能配置 — JSON 持久化
 */
public class AntiPushConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static AntiPushConfig instance;

    public boolean enabled = false;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "antipush.json");
    }
    private static File getGameDirectory() {
        try { Minecraft mc = Minecraft.getInstance(); if (mc != null) return mc.gameDirectory; }
        catch (Exception ignored) {}
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }
    public static AntiPushConfig getInstance() { if (instance == null) load(); return instance; }
    public static void load() {
        File f = getConfigFile();
        if (f.exists()) { try (FileReader r = new FileReader(f)) { instance = GSON.fromJson(r, AntiPushConfig.class); } catch (IOException e) { instance = new AntiPushConfig(); } }
        else { instance = new AntiPushConfig(); save(); }
    }
    public static void save() {
        if (instance == null) return;
        try (FileWriter w = new FileWriter(getConfigFile())) { GSON.toJson(instance, w); } catch (IOException e) { e.printStackTrace(); }
    }
    public void setEnabled(boolean v) { this.enabled = v; save(); }
}
