package fku.org.example.fku.features.quickswitch; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * QuickSwitch（鬼手秒切）配置类（JSON 持久化）
 *
 * ★ 职责：
 *   管理秒切功能的开关、模式、自定义物品列表等配置项。
 *   所有修改即时写入 config/fku/quickswitch.json。
 *
 * ★ 模式说明（v8 — 纯附魔武器模式）：
 *   SMART    — 智能秒切：查找附魔评分最高武器 → 切换 → 攻击包发出 → RTT延迟后切回原槽位。不切空手
 *   CUSTOM   — 自定义模式：按 prioritySlots 顺序切换 → 攻击 → 不切回（常驻自定义武器）
 *   OFF      — 功能关闭
 *
 * ★ v8 变更（2026-07）：
 *   - SMART 模式移除空手/无耐久物品切换，直接使用附魔武器攻击
 *   - 因为空手攻击对大多数实体无伤害且服务器端仍记录耐久消耗
 *
 * ★ 该方法由赛博教员实现
 */
public class QuickSwitchConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = Paths.get("config", "fku", "quickswitch.json").toFile();
    private static QuickSwitchConfig instance;

    // ════════ 基础开关 ════════
    /** 功能总开关 */
    public boolean enabled = false;

    // ════════ 模式 ════════
    /** 秒切模式：SMART / CUSTOM / OFF */
    public String mode = "OFF";

    // ════════ 自定义物品（CUSTOM 模式使用） ════════
    /**
     * 自定义物品列表（逗号分隔的注册名）。
     * CUSTOM 模式：遍历 prioritySlots 中每个槽位，切换后攻击
     * 如 "minecraft:diamond_sword,minecraft:diamond_axe,minecraft:mace"
     */
    public String customItems = "minecraft:diamond_sword,minecraft:diamond_axe,minecraft:mace";

    // ════════ 高级 ════════
    /** 视觉反馈：显示秒切切换消息 */
    public boolean visualFeedback = true;

    /** 优先级槽位列表（CUSTOM 模式：按此顺序切换，如 [1, 2, 3]） */
    public int[] prioritySlots = new int[]{1, 2, 3};

    /**
     * 秒切恢复延迟（毫秒）
     * 切换武器 → 攻击包到达服务端 → 切回原槽位 之间的等待时间。
     * 建议值：50~150ms，默认 80ms（约 1.5 个游戏 tick）。
     * 网络延迟高时可适当调高。
     */
    public int rttDelay = 80;

    private QuickSwitchConfig() {}

    public static QuickSwitchConfig getInstance() {
        if (instance == null) {
            instance = new QuickSwitchConfig();
        }
        return instance;
    }

    public static void load() {
        instance = null;
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                instance = GSON.fromJson(reader, QuickSwitchConfig.class);
            } catch (IOException e) {
                getInstance();
            }
        }
        // 兼容旧版配置迁移：若 mode 为旧的 SILENT/NINE_SLOT，自动升级
        if (instance != null) {
            if ("SILENT".equals(instance.mode)) {
                instance.mode = "SMART";
                save();
            } else if ("NINE_SLOT".equals(instance.mode)) {
                instance.mode = "SMART";
                save();
            }
        }
        getInstance();
    }

    public static void save() {
        try {
            File parent = CONFIG_FILE.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(getInstance(), writer);
            }
        } catch (IOException e) {
            // 静默失败
        }
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        save();
    }

    /** 是否为活跃的秒切模式（非 OFF） */
    public boolean isActiveMode() {
        return !"OFF".equals(mode);
    }

    /** 获取所有可用模式（循环用） */
    public static String[] getAvailableModes() {
        return new String[]{"OFF", "SMART", "CUSTOM"};
    }
}
