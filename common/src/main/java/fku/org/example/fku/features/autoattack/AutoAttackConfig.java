package fku.org.example.fku.features.autoattack; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AutoAttack（自动攻击）配置类（JSON 持久化）
 *
 * 设计思想：
 * - 与 TpAuraConfig / KillFXConfig 保持一致的 JSON 静默读写风格
 * - 启动时强制关闭（避免误开启，符合项目其它功能约定）
 * - 分组：通用设置 / 目标过滤 / 距离与范围 / 反作弊与安全
 */
public class AutoAttackConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static AutoAttackConfig instance;

    // ════════ 分组1：通用设置 ════════
    /** 功能总开关（默认关闭；启动时静默加载配置文件，不再强制覆盖） */
    public boolean enabled = false;
    /** 攻击模式：SMART（等待冷却满，伤害高）/ FAST（冷却好就攻击，频率高伤害低） */
    public String attackMode = "SMART";
    /** 冷却阈值 (0.1~1.0)，SMART 模式有效 */
    public double cooldownThreshold = 1.0;
    /** 每次攻击后的间隔 Tick (0~10，0 = 无间隔)，配合 randomInterval 增加随机偏移 */
    public int attackInterval = 2;
    /** 是否在 attackInterval 基础上增加 ±1 Tick 随机偏移（抗反作弊规律检测） */
    public boolean randomInterval = true;
    /** 攻击时是否挥手 */
    public boolean swingHand = true;
    /** 仅手持武器（剑/斧/三叉戟）时生效 */
    public boolean onlyWhenHoldingWeapon = false;

    // ════════ 分组2：目标过滤（复用 TpAura 结构） ════════
    /** 目标实体类型（逗号分隔，如 PLAYER,ZOMBIE）。为空（默认）表示攻击所有类型 */
    public String entities = "";
    public boolean ignoreFriends = true;
    public boolean ignoreNamed = false;
    public boolean ignoreTamed = false;
    public boolean ignoreInvisible = false;
    public boolean ignoreCreative = false;
    /** 名单模式：Whitelist / Blacklist / Off（针对 playerList 中的玩家名） */
    public String listMode = "Off";
    /** 玩家名单（逗号分隔） */
    public String playerList = "";

    // ════════ 分组3：距离与范围 ════════
    /** 最大攻击距离 (1.0~8.0) */
    public double maxRange = 4.5;
    /** 是否仅当准星瞄准实体时攻击（关闭则攻击范围内最近实体） */
    public boolean onlyWhenLookingAt = true;
    /** 是否锁定目标：长按左键锁住准星小范围内敌人，持续攻击直到其死亡或松开左键（参考矛之冲锋锁敌） */
    public boolean lockTarget = true;

    // ════════ 分组4：反作弊与安全 ════════
    /** 每秒最大攻击次数 (1~20) */
    public int maxAttacksPerSecond = 8;
    /** 潜行时禁用 */
    public boolean disableOnSneak = false;
    /** 水中禁用 */
    public boolean disableInLiquid = false;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "autoattack.json");
    }

    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get(".").toAbsolutePath().normalize().toFile();
    }

    public static AutoAttackConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, AutoAttackConfig.class);
            } catch (IOException e) {
                instance = new AutoAttackConfig();
            }
        } else {
            instance = new AutoAttackConfig();
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

    // ════════ 便捷方法 ════════

    /** 解析目标实体类型集合（小写 path） */
    public Set<String> getEntityTypeSet() {
        if (entities == null || entities.isEmpty()) return new HashSet<>();
        Set<String> set = new HashSet<>();
        for (String s : entities.split(",")) {
            set.add(s.trim().toLowerCase());
        }
        return set;
    }

    /** 解析玩家名单（去空） */
    public java.util.List<String> getPlayerList() {
        if (playerList == null || playerList.isEmpty()) return java.util.Collections.emptyList();
        return Arrays.stream(playerList.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }
}
