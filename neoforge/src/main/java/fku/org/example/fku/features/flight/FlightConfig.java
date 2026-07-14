package fku.org.example.fku.features.flight; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fku.org.example.fku.Fku;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 飞行功能配置类（JSON 持久化）
 *
 * ★ 参考 Meteor Client Flight 模块
 */
public class FlightConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static FlightConfig instance;

    public boolean enabled = false;
    /** 飞行速度 (0.01~2.0) */
    public double flySpeed = 0.1;
    /** 垂直飞行速度 (0.01~2.0) */
    public double verticalSpeed = 0.1;
    /** 双击窗口时间（毫秒） */
    public int doubleTapWindow = 500;
    /** 禁用碰撞箱 */
    public boolean disableCollision = false;
    /** 仅创造模式可用 */
    public boolean onlyInCreative = false;
    /** 消耗饥饿值 */
    public boolean consumeHunger = false;
    /** 每次 Tick 消耗 */
    public int hungerCost = 1;
    /** 允许疾跑 */
    public boolean allowSprint = true;
    /** 平滑加速 */
    public boolean smoothAcceleration = true;
    /** 粒子效果 */
    public boolean particleEffect = true;
    /** 音效反馈 */
    public boolean soundFeedback = true;
    /** 防踢 */
    public boolean antiKick = true;
    /** 防踢间隔（Tick） */
    public int antiKickInterval = 70;
    /** 防踢微降距离 */
    public double antiKickDistance = 0.07;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "flight.json");
    }

    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }

    public static FlightConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, FlightConfig.class);
            } catch (IOException e) {
                instance = new FlightConfig();
            }
        } else {
            instance = new FlightConfig();
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

    // Setter 即时保存
    public void setEnabled(boolean v) { this.enabled = v; save(); }
    public void setFlySpeed(double v) { this.flySpeed = Math.max(0.01, Math.min(2.0, v)); save(); }
    public void setVerticalSpeed(double v) { this.verticalSpeed = Math.max(0.01, Math.min(2.0, v)); save(); }
    public void setDoubleTapWindow(int v) { this.doubleTapWindow = Math.max(100, Math.min(2000, v)); save(); }
    public void setDisableCollision(boolean v) { this.disableCollision = v; save(); }
    public void setOnlyInCreative(boolean v) { this.onlyInCreative = v; save(); }
    public void setConsumeHunger(boolean v) { this.consumeHunger = v; save(); }
    public void setHungerCost(int v) { this.hungerCost = Math.max(1, Math.min(20, v)); save(); }
    public void setAllowSprint(boolean v) { this.allowSprint = v; save(); }
    public void setSmoothAcceleration(boolean v) { this.smoothAcceleration = v; save(); }
    public void setParticleEffect(boolean v) { this.particleEffect = v; save(); }
    public void setSoundFeedback(boolean v) { this.soundFeedback = v; save(); }
    public void setAntiKick(boolean v) { this.antiKick = v; save(); }
    public void setAntiKickInterval(int v) { this.antiKickInterval = Math.max(10, Math.min(200, v)); save(); }
    public void setAntiKickDistance(double v) { this.antiKickDistance = Math.max(0.01, Math.min(0.5, v)); save(); }
}
