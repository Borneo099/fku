package fku.org.example.fku.features.tpaura; /* water */

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

/**
 * TpAura（如来神掌/瞬移攻击）配置类（JSON 持久化）
 *
 * 设计思想：
 * - 所有字段修改即保存
 * - 参考 KnockbackConfig / AntiLagConfig 的 JSON 配置模式
 * - 全部配置项来自 Meteor TpAura 模块，已适配 Forge 1.20.1
 */
public class TpAuraConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static TpAuraConfig instance;

    // ════════ 攻击机制 ════════
    /** 攻击模式：Smart（满蓄力重击）/ Fast（0蓄力连打） */
    public String attackMode = "Smart";
    /** 蓄力阈值 (0.1~1.0)，Smart模式有效 */
    public double cooldownThreshold = 1.0;
    /** 额外延迟 (Tick，0~20) */
    public int attackDelay = 0;

    // ════════ 通用 ════════
    public boolean autoSwitch = true;
    public boolean requireMace = false;
    public boolean swingHand = true;
    public boolean silentSwap = true;

    // ════════ 瞬移 ════════
    /** 兼容模式：Vanilla / Paper */
    public String mode = "Paper";
    /** 最大范围 (1~99) */
    public double maxRange = 49.0;
    /** V-Clip (Paper模式有效) */
    public boolean goUp = true;
    /** 垫包数量 (1~20) */
    public int paperPackets = 8;
    /** 限制天花板高度：传送高度不超过天花板下方2格（防穿墙拉回） */
    public boolean limitCeiling = true;
    /** 天花板扫描步长 (1~2)：1=精确但略耗性能，2=快速但可能漏检薄天花板 */
    public int ceilingScanStep = 1;
    /** 攻击后回传 */
    public boolean returnPos = true;
    /** 偏移同步 */
    public boolean offsetFix = true;

    // ════════ 目标 ════════
    /** 全生物攻击：开启后攻击所有实体（白名单除外），关闭才按entityTypes过滤 */
    public boolean attackAllEntities = true;
    /** 目标实体类型（逗号分隔，如 PLAYER, ZOMBIE） */
    public String entityTypes = "PLAYER";
    /** 玩家攻击距离 (3~6)：仅对玩家目标生效，实际攻击时TP到该距离内 */
    public int attackDistance = 3;
    /** TP落点偏移 (0~6)：在目标周围tpOffset格范围内随机选择安全落点 */
    public int tpOffset = 0;
    public boolean ignoreFriends = false;
    public boolean ignoreNamed = true;
    public boolean ignoreTamed = false;

    // ════════ 名单（白名单模式） ════════
    /** 白名单开关：开启后仅白名单外玩家默认攻击 */
    public boolean whitelistEnabled = false;
    /** 白名单玩家列表（逗号分隔） */
    public String whitelist = "";

    // ════════ 渲染 ════════
    public boolean renderPath = true;
    public int pathColorR = 255, pathColorG = 0, pathColorB = 0, pathColorA = 100;
    public int targetColorR = 255, targetColorG = 0, targetColorB = 0, targetColorA = 200;

    // ════════ 热键 ════════
    /** GLFW按键码（-1表示未设置） */
    public int hotkeyKey = -1;
    /** 热键名称（展示用） */
    public String hotkeyName = "";

    // ════════ 图腾绕过 ════════
    public boolean totemBypass = false;
    public int totemAttacks = 2;
    public int totemHeightIncrease = 9;

    /** 功能总开关（由 TpAuraComponent 控制） */
    public boolean enabled = false;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "tpaura.json");
    }

    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }

    public static TpAuraConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, TpAuraConfig.class);
            } catch (IOException e) {
                instance = new TpAuraConfig();
            }
        } else {
            instance = new TpAuraConfig();
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

    // ════════ 便捷获取方法 ════════

    /** 获取解析后的目标实体类型集合 */
    public Set<String> getEntityTypeSet() {
        if (entityTypes == null || entityTypes.isEmpty()) return new HashSet<>(Arrays.asList("player"));
        Set<String> set = new HashSet<>();
        for (String s : entityTypes.split(",")) {
            set.add(s.trim().toLowerCase());
        }
        return set;
    }

    /** 获取 RGBA 路径颜色 (int) */
    public int getPathColor() {
        return (pathColorA << 24) | ((pathColorR & 0xFF) << 16) | ((pathColorG & 0xFF) << 8) | (pathColorB & 0xFF);
    }

    /** 获取 RGBA 目标颜色 (int) */
    public int getTargetColor() {
        return (targetColorA << 24) | ((targetColorR & 0xFF) << 16) | ((targetColorG & 0xFF) << 8) | (targetColorB & 0xFF);
    }

    // ════════ Setter（即时保存） ════════
    public void setEnabled(boolean v) { this.enabled = v; save(); }
    public void setAttackMode(String v) { this.attackMode = ("Smart".equals(v) || "Fast".equals(v) || "Universal".equals(v)) ? v : "Smart"; save(); }
    public void setCooldownThreshold(double v) { this.cooldownThreshold = Math.max(0.1, Math.min(1.0, v)); save(); }
    public void setAttackDelay(int v) { this.attackDelay = Math.max(0, Math.min(20, v)); save(); }
    public void setAutoSwitch(boolean v) { this.autoSwitch = v; save(); }
    public void setRequireMace(boolean v) { this.requireMace = v; save(); }
    public void setSwingHand(boolean v) { this.swingHand = v; save(); }
    public void setSilentSwap(boolean v) { this.silentSwap = v; save(); }
    public void setMode(String v) { this.mode = ("Vanilla".equals(v) || "Paper".equals(v)) ? v : "Paper"; save(); }
    public void setMaxRange(double v) { this.maxRange = Math.max(1, Math.min(99, v)); save(); }
    public void setGoUp(boolean v) { this.goUp = v; save(); }
    public void setPaperPackets(int v) { this.paperPackets = Math.max(1, Math.min(20, v)); save(); }
    public void setLimitCeiling(boolean v) { this.limitCeiling = v; save(); }
    public void setCeilingScanStep(int v) { this.ceilingScanStep = Math.max(1, Math.min(2, v)); save(); }
    public void setReturnPos(boolean v) { this.returnPos = v; save(); }
    public void setOffsetFix(boolean v) { this.offsetFix = v; save(); }
    public void setEntityTypes(String v) { this.entityTypes = (v != null && !v.isEmpty()) ? v : "player"; save(); }
    public void setAttackAllEntities(boolean v) { this.attackAllEntities = v; save(); }
    public void setAttackDistance(int v) { this.attackDistance = Math.max(3, Math.min(6, v)); save(); }
    public void setTpOffset(int v) { this.tpOffset = Math.max(0, Math.min(6, v)); save(); }
    public void setIgnoreFriends(boolean v) { this.ignoreFriends = v; save(); }
    public void setIgnoreNamed(boolean v) { this.ignoreNamed = v; save(); }
    public void setIgnoreTamed(boolean v) { this.ignoreTamed = v; save(); }
    public void setWhitelistEnabled(boolean v) { this.whitelistEnabled = v; save(); }
    public void setWhitelist(String v) { this.whitelist = v != null ? v : ""; save(); }
    public void setHotkeyKey(int key) { this.hotkeyKey = key; save(); }
    public void setHotkeyName(String name) { this.hotkeyName = name != null ? name : ""; save(); }
    public void setRenderPath(boolean v) { this.renderPath = v; save(); }
    public void setPathColor(int r, int g, int b, int a) { this.pathColorR = r; this.pathColorG = g; this.pathColorB = b; this.pathColorA = a; save(); }
    public void setTargetColor(int r, int g, int b, int a) { this.targetColorR = r; this.targetColorG = g; this.targetColorB = b; this.targetColorA = a; save(); }
    public void setTotemBypass(boolean v) { this.totemBypass = v; save(); }
    public void setTotemAttacks(int v) { this.totemAttacks = Math.max(1, Math.min(3, v)); save(); }
    public void setTotemHeightIncrease(int v) { this.totemHeightIncrease = Math.max(1, Math.min(100, v)); save(); }
}