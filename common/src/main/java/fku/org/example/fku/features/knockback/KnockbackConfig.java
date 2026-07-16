package fku.org.example.fku.features.knockback; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 自由击退方向配置类（JSON 持久化）
 *
 * 设计思想：
 * - 所有字段修改即保存
 * - 模式：PULLBACK（拉回）/ PUSHBACK（推离）/ CLIFF（悬崖）/ CUSTOM（自定义）
 */
public class KnockbackConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static KnockbackConfig instance;

    /** 功能总开关 */
    public boolean enabled = false;

    /** 击退模式 */
    public String mode = "PUSHBACK";

    /** CUSTOM 模式固定角度 */
    public float customYaw = 0.0f;

    /** CLIFF 模式搜索半径 */
    public int cliffSearchRadius = 5;

    /** 假旋转启用后等待的 Tick 数（0~5） */
    public int rotationDelay = 0;

    /** 强制发送旋转包（无视旋转检测） */
    public boolean aggressiveMode = false;

    /** 是否启用平滑旋转 */
    public boolean smoothRotation = true;

    /** 平滑旋转步数（2~10） */
    public int smoothSteps = 5;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "knockback.json");
    }

    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }

    public static KnockbackConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, KnockbackConfig.class);
            } catch (IOException e) {
                instance = new KnockbackConfig();
            }
        } else {
            instance = new KnockbackConfig();
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
    public void setMode(String v) { this.mode = (v != null) ? v : "PUSHBACK"; save(); }
    public void setCustomYaw(float v) { this.customYaw = v; save(); }
    public void setCliffSearchRadius(int v) { this.cliffSearchRadius = Math.max(1, Math.min(20, v)); save(); }
    public void setRotationDelay(int v) { this.rotationDelay = Math.max(0, Math.min(5, v)); save(); }
    public void setAggressiveMode(boolean v) { this.aggressiveMode = v; save(); }
    public void setSmoothRotation(boolean v) { this.smoothRotation = v; save(); }
    public void setSmoothSteps(int v) { this.smoothSteps = Math.max(2, Math.min(10, v)); save(); }
}