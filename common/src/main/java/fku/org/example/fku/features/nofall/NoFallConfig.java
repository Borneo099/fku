package fku.org.example.fku.features.nofall; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 防摔（NoFall）配置类 — JSON 持久化
 */
public class NoFallConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static NoFallConfig instance;

    public boolean enabled = false;
    /** 最小触发高度（低于此高度不保护，防止短距离也无摔伤判定） */
    public double minFallDistance = 3.0;
    /** 完全免疫掉落伤害 */
    public boolean immune = true;
    /** 仅保护飞行状态（与 FlightFeature 联动） */
    public boolean onlyWhenFlying = false;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "nofall.json");
    }

    private static File getGameDirectory() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }

    public static NoFallConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, NoFallConfig.class);
            } catch (IOException e) {
                instance = new NoFallConfig();
            }
        } else {
            instance = new NoFallConfig();
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
    public void setMinFallDistance(double v) { this.minFallDistance = Math.max(0, v); save(); }
    public void setImmune(boolean v) { this.immune = v; save(); }
    public void setOnlyWhenFlying(boolean v) { this.onlyWhenFlying = v; save(); }
}
