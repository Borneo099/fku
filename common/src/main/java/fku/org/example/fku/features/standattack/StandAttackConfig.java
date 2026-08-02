package fku.org.example.fku.features.standattack; /* water */

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
 * 替身攻击配置类（JSON 持久化）
 *
 * 移植自 TpAuraConfig 的完整配置项体系：
 * - 攻击机制（Smart/Fast/Universal、蓄力阈值）
 * - 瞬移参数（步长、间隔、模式、V-Clip、偏移）
 * - 目标筛选（全生物/实体类型/攻击距离/TP偏移、忽略条件）
 * - 白名单
 * - 自动飞行
 * - 图腾绕过
 * 该配置类由赛博教员实现
 */
public class StandAttackConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static StandAttackConfig instance;

    // ════════ 攻击机制 ════════
    /** 攻击模式：Smart（满蓄力重击）/ Fast（0蓄力连打）/ Universal */
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
    /** 寻路算法：0=直线传送, 1=Paper上升, 2=A星寻路 */
    public int pathfindingMode = 0;
    /** 最大步长（格），超10格分段 */
    public int maxStepLength = 10;
    /** 发包间隔（毫秒） */
    public int packetInterval = 50;
    /** V-Clip (Paper模式有效) */
    public boolean goUp = true;
    /** 垫包数量 (1~20) */
    public int paperPackets = 8;
    /** 限制天花板高度 */
    public boolean limitCeiling = true;
    /** 天花板扫描步长 (1~2) */
    public int ceilingScanStep = 1;
    /** 攻击后回传 */
    public boolean returnPos = true;
    /** 偏移同步 */
    public boolean offsetFix = true;
    /** 防摔 */
    public boolean antiFall = true;

    // ════════ 目标 ════════
    /** 最大范围（格） */
    public double maxRange = 49.0;
    /** 全生物攻击 */
    public boolean attackAllEntities = true;
    /** 目标实体类型（逗号分隔） */
    public String entityTypes = "PLAYER";
    /** 攻击距离（3~6）：TP到目标多少格内发动攻击 */
    public int attackDistance = 3;
    /** TP落点偏移 (0~6)：在目标周围随机选择安全落点 */
    public int tpOffset = 0;
    public boolean ignoreFriends = false;
    public boolean ignoreNamed = true;
    public boolean ignoreTamed = false;

    // ════════ 名单 ════════
    public boolean whitelistEnabled = false;
    public String whitelist = "";

    // ════════ 选中模式（32k弓式） ════════
    /** 选中模式：开启后不自动范围搜索，需手动选中目标，长按左键攻击 */
    public boolean selectMode = false;

    // ════════ 死亡回传 ════════
    /** 目标死亡后再回传（开启后持续攻击直到目标死亡，期间随机偏移传送） */
    public boolean deathReturn = false;
    /** 随机偏移传送间隔（毫秒） */
    public int teleportInterval = 2000;

    // ════════ 相机视角 ════════
    /** 相机视角锁定：开启后相机不跟随玩家TP，保持原位，回传时传至相机位置 */
    public boolean cameraLock = false;

    // ════════ 渲染 ════════
    public boolean renderPath = true;
    public int pathColorR = 0, pathColorG = 255, pathColorB = 0, pathColorA = 100;
    public int targetColorR = 255, targetColorG = 0, targetColorB = 0, targetColorA = 200;

    // ════════ 图腾绕过 ════════
    public boolean totemBypass = false;
    public int totemAttacks = 2;
    public int totemHeightIncrease = 9;

    // ════════ 自动飞行 ════════
    public boolean autoFlight = false;
    public double autoFlightSpeed = 0.3;
    public double autoFlightHorizontalSpeed = 1.0;

    // ════════ 热键 ════════
    public int hotkeyKey = -1;
    public String hotkeyName = "";

    // ════════ 消息 ════════
    /** 显示聊天消息（关闭则仅物品栏上方显示状态） */
    public boolean showMessages = true;

    // ════════ 开关 ════════
    public boolean enabled = false;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "standattack.json");
    }

    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }

    public static StandAttackConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, StandAttackConfig.class);
            } catch (IOException e) {
                instance = new StandAttackConfig();
            }
        } else {
            instance = new StandAttackConfig();
            save();
        }
        // ★ 强制关闭：每次启动都默认关闭，避免误开启
        if (instance != null) instance.enabled = false;
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

    public Set<String> getEntityTypeSet() {
        if (entityTypes == null || entityTypes.isEmpty()) return new HashSet<>(Arrays.asList("player"));
        Set<String> set = new HashSet<>();
        for (String s : entityTypes.split(",")) {
            set.add(s.trim().toLowerCase());
        }
        return set;
    }

    public int getPathColor() {
        return (pathColorA << 24) | ((pathColorR & 0xFF) << 16) | ((pathColorG & 0xFF) << 8) | (pathColorB & 0xFF);
    }

    public int getTargetColor() {
        return (targetColorA << 24) | ((targetColorR & 0xFF) << 16) | ((targetColorG & 0xFF) << 8) | (targetColorB & 0xFF);
    }

    public static String pathfindingModeName(int mode) {
        return switch (mode) {
            case 0 -> "直线传送";
            case 1 -> "Paper上升";
            case 2 -> "A星寻路";
            default -> "未知";
        };
    }

    // ════════ Setter ════════
    /** 开关：不静默保存，默认关闭，避免进游戏误开启 */
    public void setEnabled(boolean v) { this.enabled = v; }
    public void setAttackMode(String v) { this.attackMode = ("Smart".equals(v) || "Fast".equals(v) || "Universal".equals(v)) ? v : "Smart"; save(); }
    public void setCooldownThreshold(double v) { this.cooldownThreshold = Math.max(0.1, Math.min(1.0, v)); save(); }
    public void setAttackDelay(int v) { this.attackDelay = Math.max(0, Math.min(20, v)); save(); }
    public void setAutoSwitch(boolean v) { this.autoSwitch = v; save(); }
    public void setRequireMace(boolean v) { this.requireMace = v; save(); }
    public void setSwingHand(boolean v) { this.swingHand = v; save(); }
    public void setSilentSwap(boolean v) { this.silentSwap = v; save(); }
    public void setPathfindingMode(int v) { this.pathfindingMode = Math.max(0, Math.min(2, v)); save(); }
    public void setMaxStepLength(int v) { this.maxStepLength = Math.max(1, Math.min(64, v)); save(); }
    public void setPacketInterval(int v) { this.packetInterval = Math.max(0, Math.min(500, v)); save(); }
    public void setGoUp(boolean v) { this.goUp = v; save(); }
    public void setPaperPackets(int v) { this.paperPackets = Math.max(1, Math.min(20, v)); save(); }
    public void setLimitCeiling(boolean v) { this.limitCeiling = v; save(); }
    public void setCeilingScanStep(int v) { this.ceilingScanStep = Math.max(1, Math.min(2, v)); save(); }
    public void setReturnPos(boolean v) { this.returnPos = v; save(); }
    public void setOffsetFix(boolean v) { this.offsetFix = v; save(); }
    public void setAntiFall(boolean v) { this.antiFall = v; save(); }
    public void setMaxRange(double v) { this.maxRange = Math.max(1, Math.min(256, v)); save(); }
    public void setAttackAllEntities(boolean v) { this.attackAllEntities = v; save(); }
    public void setEntityTypes(String v) { this.entityTypes = (v != null && !v.isEmpty()) ? v : "player"; save(); }
    public void setAttackDistance(int v) { this.attackDistance = Math.max(1, Math.min(10, v)); save(); }
    public void setTpOffset(int v) { this.tpOffset = Math.max(0, Math.min(6, v)); save(); }
    public void setIgnoreFriends(boolean v) { this.ignoreFriends = v; save(); }
    public void setIgnoreNamed(boolean v) { this.ignoreNamed = v; save(); }
    public void setIgnoreTamed(boolean v) { this.ignoreTamed = v; save(); }
    public void setWhitelistEnabled(boolean v) { this.whitelistEnabled = v; save(); }
    public void setWhitelist(String v) { this.whitelist = v != null ? v : ""; save(); }
    public void setCameraLock(boolean v) { this.cameraLock = v; save(); }
    public void setSelectMode(boolean v) { this.selectMode = v; save(); }
    public void setDeathReturn(boolean v) { this.deathReturn = v; save(); }
    public void setTeleportInterval(int v) { this.teleportInterval = Math.max(30, Math.min(30000, v)); save(); }
    public void setRenderPath(boolean v) { this.renderPath = v; save(); }
    public void setPathColor(int r, int g, int b, int a) { this.pathColorR = r; this.pathColorG = g; this.pathColorB = b; this.pathColorA = a; save(); }
    public void setTargetColor(int r, int g, int b, int a) { this.targetColorR = r; this.targetColorG = g; this.targetColorB = b; this.targetColorA = a; save(); }
    public void setTotemBypass(boolean v) { this.totemBypass = v; save(); }
    public void setTotemAttacks(int v) { this.totemAttacks = Math.max(1, Math.min(3, v)); save(); }
    public void setTotemHeightIncrease(int v) { this.totemHeightIncrease = Math.max(1, Math.min(100, v)); save(); }
    public void setAutoFlight(boolean v) { this.autoFlight = v; save(); }
    public void setAutoFlightSpeed(double v) { this.autoFlightSpeed = Math.max(0, Math.min(2.0, v)); save(); }
    public void setAutoFlightHorizontalSpeed(double v) { this.autoFlightHorizontalSpeed = Math.max(0, Math.min(3.0, v)); save(); }
    public void setHotkeyKey(int key) { this.hotkeyKey = key; save(); }
    public void setHotkeyName(String name) { this.hotkeyName = name != null ? name : ""; save(); }
    public void setShowMessages(boolean v) { this.showMessages = v; save(); }
}