package fku.org.example.fku.features.quickswitch; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * QuickSwitch（鬼手秒切）配置类（JSON 持久化）
 *
 * ★ 职责：
 *   管理秒切功能的开关、模式、自定义物品列表、恢复行为等配置项。
 *   所有修改即时写入 config/fku/quickswitch.json。
 *
 * ★ 模式说明：
 *   SILENT    — 静默秒切：攻击前切换到武器，攻击后立即恢复原槽位（1次切换+1次攻击）
 *   NINE_SLOT — 在单 Tick 内遍历 9 个热栏槽位，每槽发送一次切换+攻击
 *   CUSTOM    — 遍历用户自定义物品列表
 *   OFF       — 功能关闭
 *
 * ★ 参考：
 *   Meteor Client AutoSwitch (Silent/Normal) 模式设计
 *   该方法由赛博教员实现
 */
public class QuickSwitchConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = Paths.get("config", "fku", "quickswitch.json").toFile();
    private static QuickSwitchConfig instance;

    // ════════ 基础开关 ════════
    /** 功能总开关 */
    public boolean enabled = false;

    // ════════ 模式 ════════
    /** 秒切模式：SILENT / NINE_SLOT / CUSTOM / OFF */
    public String mode = "OFF";

    // ════════ 自定义物品（SILENT/CUSTOM 模式共用） ════════
    /**
     * 自定义物品列表（逗号分隔的注册名）。
     * SILENT 模式：扫描热栏找到第一个匹配的物品，切换后攻击
     * CUSTOM 模式：遍历所有匹配物品逐个攻击
     * 如 "minecraft:diamond_sword,minecraft:diamond_axe,minecraft:mace"
     */
    public String customItems = "minecraft:diamond_sword,minecraft:diamond_axe,minecraft:mace";

    // ════════ 行为 ════════
    /** 攻击后是否恢复原槽位（SILENT 模式默认开启） */
    public boolean restoreSlot = true;

    // ════════ 高级 ════════
    /** 九切模式下每组（切换+攻击）之间的延迟 Tick（0=无延迟，1/2=按Tick分散） */
    public int burstDelay = 0;

    /** 视觉反馈：显示秒切切换消息 */
    public boolean visualFeedback = true;

    /** 优先级槽位列表（CUSTOM 模式：按此顺序切换，如 [0, 2, 4]） */
    public int[] prioritySlots = new int[]{0, 1, 2};

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
}