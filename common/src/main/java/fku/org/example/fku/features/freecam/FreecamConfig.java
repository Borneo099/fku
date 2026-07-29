package fku.org.example.fku.features.freecam; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 灵魂出窍（自由相机）配置类（JSON 持久化）
 *
 * 配置项：
 * - maxSpeed: 最大移动速度（格/秒，5~500）
 * - smoothness: 速度平滑度（1~100）
 * 该配置由赛博教员实现
 */
public class FreecamConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static FreecamConfig instance;

    /** 最大移动速度（格/秒） */
    public double maxSpeed = 50.0;
    /** 速度平滑度（越大越快） */
    public double smoothness = 20.0;
    /** 功能开关 */
    public boolean enabled = false;
    /** 是否显示状态提示 overlay */
    public boolean showOverlay = true;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "freecam.json");
    }

    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }

    public static FreecamConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, FreecamConfig.class);
            } catch (IOException e) {
                instance = new FreecamConfig();
            }
        } else {
            instance = new FreecamConfig();
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

    public void setEnabled(boolean v) { this.enabled = v; save(); }
    public void setMaxSpeed(double v) { this.maxSpeed = Math.max(5, Math.min(500, v)); save(); }
    public void setSmoothness(double v) { this.smoothness = Math.max(1, Math.min(100, v)); save(); }
    public void setShowOverlay(boolean v) { this.showOverlay = v; save(); }
}