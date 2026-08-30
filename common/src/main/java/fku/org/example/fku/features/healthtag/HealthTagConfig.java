package fku.org.example.fku.features.healthtag; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class HealthTagConfig {
    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "healthtag.json");
    }
    
    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get(".").toAbsolutePath().normalize().toFile();
    }
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean enabled = true;
    public int x = 100;
    public int y = 100;

    /** 准星瞄准：开启后只要实体在准星附近即显示 HealthTag，无需持弓或攻击到目标 */
    public boolean crosshairAim = false;
    /** 准星探测距离（方块） */
    public double aimRange = 128.0;
    /** 准星基础夹角（度，距离为0时的允许偏差，随距离衰减） */
    public double aimAngle = 15.0;

    private static HealthTagConfig instance;

    public static HealthTagConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, HealthTagConfig.class);
            } catch (IOException e) {
                instance = new HealthTagConfig();
            }
        } else {
            instance = new HealthTagConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(getConfigFile())) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCrosshairAim(boolean v) { this.crosshairAim = v; save(); }
    public void setAimRange(double v) { this.aimRange = Math.max(8.0, Math.min(256.0, v)); save(); }
    public void setAimAngle(double v) { this.aimAngle = Math.max(1.0, Math.min(90.0, v)); save(); }
}