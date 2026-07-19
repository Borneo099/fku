package fku.org.example.fku.features.waterwalk; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * WaterWalk（水上行走 / Jesus）配置类
 *
 * ★ 职责：
 *   持久化水上行走功能的所有配置项，存储到 config/fku/waterwalk.json。
 *
 * ★ 配置项说明：
 *   - enabled : 功能总开关（重开后自动恢复）
 *
 * ★ 参考来源：
 *   lexis1.20.1/lexis/Hack/Hacks/Movement/JesusHack.java
 */
public class WaterWalkConfig {

    private static File getConfigFile() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            File configDir = new File(mc.gameDirectory, "fku");
            if (!configDir.exists()) configDir.mkdirs();
            return new File(configDir, "waterwalk.json");
        } catch (Exception ignored) {
        }
        return Paths.get(".", "config", "fku", "waterwalk.json").toAbsolutePath().normalize().toFile();
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ===== 开关 =====
    /** 功能总开关（重开后自动恢复） */
    public boolean enabled = false;

    // ===== 单例 =====
    private static WaterWalkConfig instance;

    public static WaterWalkConfig getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, WaterWalkConfig.class);
                System.out.println("[WaterWalk] 配置已加载: " + configFile.getAbsolutePath());
            } catch (IOException e) {
                System.out.println("[WaterWalk] 配置加载失败，使用默认值");
                instance = new WaterWalkConfig();
            }
        } else {
            instance = new WaterWalkConfig();
            save();
        }
    }

    public static void save() {
        if (instance == null) return;
        File configFile = getConfigFile();
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(instance, writer);
            System.out.println("[WaterWalk] 配置已保存: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
