package fku.org.example.fku.features.entitycontrol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 实体控制功能配置类（JSON 持久化）
 *
 * 移植自 Lexis EntityControlHack，覆盖：开关、坐骑水平速度、飞行模式、
 * 飞行上升速度、锁定坐骑朝向玩家视角、反踢（规避服务器踢出）及反踢参数。
 */
public class EntityControlConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static EntityControlConfig instance;

    public boolean enabled = false;
    /** 坐骑水平移动速度 */
    public double horizontalSpeed = 10.0;
    /** 飞行模式：开启后空格上升，可自由飞行 */
    public boolean flightMode = false;
    /** 飞行时上升速度 */
    public double verticalSpeed = 6.0;
    /** 强制坐骑面向玩家视角方向 */
    public boolean lockYaw = true;
    /** 反踢出：周期性微调 y 防止服务器踢出（飞行模式有效） */
    public boolean antiKick = true;
    /** 反踢上下移动距离 */
    public double antiKickDistance = 0.05;
    /** 反踢检测间隔（tick） */
    public int antiKickInterval = 30;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "entitycontrol.json");
    }

    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }

    public static EntityControlConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, EntityControlConfig.class);
            } catch (IOException e) {
                instance = new EntityControlConfig();
            }
        } else {
            instance = new EntityControlConfig();
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
    public void setHorizontalSpeed(double v) { this.horizontalSpeed = Math.max(0.0, Math.min(50.0, v)); save(); }
    public void setFlightMode(boolean v) { this.flightMode = v; save(); }
    public void setVerticalSpeed(double v) { this.verticalSpeed = Math.max(0.0, Math.min(20.0, v)); save(); }
    public void setLockYaw(boolean v) { this.lockYaw = v; save(); }
    public void setAntiKick(boolean v) { this.antiKick = v; save(); }
    public void setAntiKickDistance(double v) { this.antiKickDistance = Math.max(0.01, Math.min(3.5, v)); save(); }
    public void setAntiKickInterval(int v) { this.antiKickInterval = Math.max(1, Math.min(100, v)); save(); }
}
