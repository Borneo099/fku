package fku.org.example.fku.features.duplicator; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 三叉戟/箭矢复制工具配置类
 * 使用JSON持久化，支持运行时实时修改
 *
 * 设计思想（实践论）：
 * - 独立JSON配置，与 DisplayModelConfig 一致风格
 * - 所有字段修改即保存
 */
public class DuplicatorConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static DuplicatorConfig instance;

    /** 复制循环间隔（tick）- 默认5 */
    public int dupeDelay = 5;

    /** 蓄力持续时间（tick）- 默认10 */
    public int holdDuration = 10;

    /** 是否启用三叉戟复制 */
    public boolean enableTrident = true;

    /** 是否启用箭矢复制 */
    public boolean enableArrow = true;

    /** 连续失败次数（达到上限自动暂停） */
    public transient int consecutiveFails = 0;

    /** 是否自动丢出复制品（放到热栏[8]后自动丢出） */
    public boolean dropTridents = true;

    /** 是否启用耐久管理（自动选择最高耐久三叉戟） */
    public boolean durabilityManagement = true;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "duplicator.json");
    }

    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) {
                return mc.gameDirectory;
            }
        } catch (Exception e) {
            // 初始化失败时回退到当前目录
        }
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }

    public static DuplicatorConfig getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, DuplicatorConfig.class);
            } catch (IOException e) {
                instance = new DuplicatorConfig();
            }
        } else {
            instance = new DuplicatorConfig();
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

    // ============ Setter方法（自动保存） ============

    public void setDupeDelay(int value) {
        this.dupeDelay = Math.max(1, Math.min(200, value));
        save();
    }

    public void setHoldDuration(int value) {
        this.holdDuration = Math.max(1, Math.min(200, value));
        save();
    }

    public void setDropTridents(boolean value) {
        this.dropTridents = value;
        save();
    }

    public void setDurabilityManagement(boolean value) {
        this.durabilityManagement = value;
        save();
    }
}