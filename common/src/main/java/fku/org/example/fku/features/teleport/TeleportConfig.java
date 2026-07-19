package fku.org.example.fku.features.teleport; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class TeleportConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static TeleportConfig instance;
    public boolean enabled = true;
    public double maxDistance = 100;
    /** 是否启用方块吸附（落点检测） */
    public boolean snapToBlock = true;

    private static File getConfigFile() {
        try { var mc = net.minecraft.client.Minecraft.getInstance(); if (mc != null && mc.gameDirectory != null) return new File(new File(mc.gameDirectory, "fku"), "teleport.json"); } catch (Exception ignored) {}
        return new File(Paths.get("config").toAbsolutePath().normalize().getParent().toFile(), "fku/teleport.json");
    }
    public static TeleportConfig getInstance() { if (instance == null) load(); return instance; }
    public static void load() { File f = getConfigFile(); if (f.exists()) { try (FileReader r = new FileReader(f)) { instance = GSON.fromJson(r, TeleportConfig.class); } catch (Exception e) { instance = new TeleportConfig(); } } else { instance = new TeleportConfig(); save(); } }
    public static void save() { if (instance == null) return; getConfigFile().getParentFile().mkdirs(); try (FileWriter w = new FileWriter(getConfigFile())) { GSON.toJson(instance, w); } catch (IOException e) { e.printStackTrace(); } }
    public void setEnabled(boolean v) { this.enabled = v; save(); }
    public void setMaxDistance(double v) { this.maxDistance = Math.max(10, Math.min(500, v)); save(); }
    public void setSnapToBlock(boolean v) { this.snapToBlock = v; save(); }
}
