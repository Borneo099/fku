package fku.org.example.fku.features.sprint; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Sprint（强制疾跑）配置类
 *
 * ★ 职责：
 *   存储 BMWSprint 强制疾跑功能的所有配置项。
 *   持久化到 config/fku/sprint.json。
 *
 * ★ 配置项说明：
 *   - mode: 疾跑模式（LEGIT / OMNIDIRECTIONAL / OMNIROTATIONAL）
 *   - ignoreBlindness: 失明时仍疾跑
 *   - ignoreHunger: 饥饿时仍疾跑
 *   - ignoreCollision: 撞墙时仍疾跑
 *   - stopOnGround: 地面无正向移动时停止疾跑（仅 Legit）
 *   - stopOnAir: 空中无正向移动时停止疾跑（仅 Legit）
 *   - elytraRotation: 鞘翅飞行时启用旋转修正（Omnirotational 模式）
 *
 * ★ 参考来源：
 *   BMWSprint.java (Meteor Client Addon) - 配置项结构
 *   prompt.txt 源文件
 */
public class SprintConfig {

    private static File getConfigFile() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            File configDir = new File(mc.gameDirectory, "fku");
            if (!configDir.exists()) configDir.mkdirs();
            return new File(configDir, "sprint.json");
        } catch (Exception ignored) {}
        return Paths.get(".", "config", "fku", "sprint.json").toAbsolutePath().normalize().toFile();
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ===== 开关 =====
    /** 功能是否启用（重开后自动恢复） */
    public boolean enabled = false;

    // ===== 核心模式 =====
    /** 疾跑模式：LEGIT / OMNIDIRECTIONAL / OMNIROTATIONAL */
    public String mode = "OMNIROTATIONAL";

    // ===== 忽略条件 =====
    /** 失明时仍疾跑 */
    public boolean ignoreBlindness = false;
    /** 饥饿时仍疾跑 */
    public boolean ignoreHunger = false;
    /** 撞墙时仍疾跑 */
    public boolean ignoreCollision = false;

    // ===== Legit 模式专用 =====
    /** 地面无正向移动时停止疾跑 */
    public boolean stopOnGround = false;
    /** 空中无正向移动时停止疾跑 */
    public boolean stopOnAir = false;

    // ===== Omnirotational 模式专用 =====
    /** 鞘翅飞行时启用旋转修正（烟花沿 WASD 方向加速） */
    public boolean elytraRotation = true;
    /** 平滑旋转（渐进式 Tick 间插值，默认关闭） */
    public boolean smoothRotation = false;
    /**
     * 平滑旋转速度（1~360）
     *
     * 映射为指数因子 factor = speed/180，取值范围 [0.05, 1.0]
     * 默认90 → factor=0.5，90°转向约3~4帧完成
     */
    public int rotationSpeed = 90;

    // ===== 单例 =====
    private static SprintConfig instance;

    public static SprintConfig getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, SprintConfig.class);
                System.out.println("[Sprint] 配置已加载: " + configFile.getAbsolutePath());
            } catch (IOException e) {
                System.out.println("[Sprint] 配置加载失败，使用默认值");
                instance = new SprintConfig();
            }
        } else {
            instance = new SprintConfig();
            save();
        }
    }

    public static void save() {
        if (instance == null) return;
        File configFile = getConfigFile();
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(instance, writer);
            System.out.println("[Sprint] 配置已保存: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 获取当前模式枚举 */
    public Mode getMode() {
        try {
            return Mode.valueOf(mode);
        } catch (IllegalArgumentException e) {
            return Mode.OMNIROTATIONAL;
        }
    }

    public enum Mode {
        LEGIT("Legit"),
        OMNIDIRECTIONAL("Omnidirectional"),
        OMNIROTATIONAL("Omnirotational");

        private final String label;
        Mode(String label) { this.label = label; }
        @Override public String toString() { return label; }
        public String getLabel() { return label; }

        /** 中文显示名 */
        public String getChineseLabel() {
            return switch (this) {
                case LEGIT -> "原版模式";
                case OMNIDIRECTIONAL -> "全向疾跑";
                case OMNIROTATIONAL -> "全向旋转";
            };
        }
    }
}