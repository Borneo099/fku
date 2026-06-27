package fku.org.example.fku.features.antilag; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * AntiLag（防拉回）配置类 — JSON 持久化
 *
 * 配置项映射源码 AntiLag.java 的全部设置：
 *   enabled / serverVersionMode / range / limitPerSecond / moveDistance
 *   searchVclipMode / searchFindStep / back / allowIntoVoid / printWhenTooManyPacket
 *
 * 设计思想（实事求是）：
 *   - 继承 BedrockBreakerConfig 的 JSON 持久化模式
 *   - 字段修改即保存，确保跨会话一致
 *   - 枚举用字符串存储避免反序列化问题
 */
public class AntiLagConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static AntiLagConfig instance;

    // ════════════ 配置字段 ════════════
    /** 功能总开关 */
    public boolean enabled = false;

    /** 服务端版本模式：MC1_16 = 路径拆分多步发，MC1_9 = 直接发 */
    public String serverVersionMode = "MC1_16";

    /** 触发拦截的最大距离（方块） */
    public double range = 100.0;

    /** 每秒允许的最大位置包数量（防止被踢） */
    public int limitPerSecond = 100;

    /** 路径拆分模式下每个小步的长度 */
    public double moveDistance = 0.5;

    /** 自动脱困竖直穿透方向：OnlyUp / Down / Both */
    public String searchVclipMode = "OnlyUp";

    /** 自动脱困竖直移动距离 */
    public double searchFindStep = 1.8;

    /** true=反拉回模式（保留原始拉回），false=防拉回模式（执行路径拆分） */
    public boolean back = false;

    /** 是否允许移动到虚空 */
    public boolean allowIntoVoid = false;

    /** 包超限时是否聊天栏警告 */
    public boolean printWhenTooManyPacket = true;

    // ════════════ 运行时内部状态（不持久化） ════════════
    /** 每秒包计数器 */
    public transient int packetCounter = 0;
    /** 上次重置计数的时间戳 */
    public transient long lastResetTime = System.currentTimeMillis();
    /** 是否处于速率限制状态 */
    public transient boolean rateLimited = false;

    // ════════════ 文件路径 ════════════
    private static File getConfigFile() {
        File configDir;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) configDir = new File(mc.gameDirectory, "fku");
            else configDir = Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
        } catch (Exception e) {
            configDir = Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
        }
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "antilag.json");
    }

    public static AntiLagConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, AntiLagConfig.class);
            } catch (IOException e) {
                instance = new AntiLagConfig();
            }
        } else {
            instance = new AntiLagConfig();
            save();
        }
        // 确保内部状态初始化
        instance.packetCounter = 0;
        instance.lastResetTime = System.currentTimeMillis();
        instance.rateLimited = false;
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

    // ════════════ Setter ════════════
    public void setEnabled(boolean v) { this.enabled = v; if (!v) resetState(); save(); }

    public void setServerVersionMode(String v) {
        this.serverVersionMode = ("MC1_16".equals(v) || "MC1_9".equals(v)) ? v : "MC1_16";
        save();
    }

    public void setRange(double v) { this.range = Math.max(0.1, Math.min(2000.0, v)); save(); }
    public void setLimitPerSecond(int v) { this.limitPerSecond = Math.max(1, Math.min(10000, v)); save(); }
    public void setMoveDistance(double v) { this.moveDistance = Math.max(0.01, Math.min(1.0, v)); save(); }

    public void setSearchVclipMode(String v) {
        this.searchVclipMode = ("OnlyUp".equals(v) || "Down".equals(v) || "Both".equals(v)) ? v : "OnlyUp";
        save();
    }

    public void setSearchFindStep(double v) { this.searchFindStep = Math.max(0.1, Math.min(5.0, v)); save(); }
    public void setBack(boolean v) { this.back = v; save(); }
    public void setAllowIntoVoid(boolean v) { this.allowIntoVoid = v; save(); }
    public void setPrintWhenTooManyPacket(boolean v) { this.printWhenTooManyPacket = v; save(); }

    /** 重置运行时计数器 */
    public void resetState() {
        packetCounter = 0;
        lastResetTime = System.currentTimeMillis();
        rateLimited = false;
    }
}