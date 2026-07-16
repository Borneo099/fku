package fku.org.example.fku.features.pearlphase; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 珍珠卡墙配置类（JSON 持久化）
 *
 * ★ 职责：
 *   珍珠卡墙（PearlPhase）的所有配置项，修改即保存。
 *   通过末影珍珠穿入方块后自动开启 NoClip，提供受限移动。
 *
 * ★ 设计思想（实践论）：
 *   配置与逻辑分离，所有调节参数集中管理，方便迭代调优。
 */
public class PearlPhaseConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static PearlPhaseConfig instance;

    // ════════ 核心开关 ════════

    /** 功能总开关 */
    public boolean enabled = false;

    /** 自动投掷模式：启用后当看向墙壁且手持珍珠时自动投掷 */
    public boolean autoThrow = true;

    /** NoClip 模式：穿入方块后自动启用 NoClip */
    public boolean noClipEnabled = true;

    // ════════ 移动参数 ════════

    /** 方块内移动速度倍率（参考 PearlPhase.java speed 滑块） */
    public double speed = 5.0;

    /** 基础移动速度基数（越小越慢） */
    public double baseSpeed = 0.0001;

    // ════════ 投掷参数 ════════

    /** 投掷瞄准时间（毫秒），0=瞬间 */
    public int aimTime = 100;

    /** 投掷后最大等待 tick 数 */
    public int maxWaitTicks = 100;

    /** 瞄准方块边缘偏移量 */
    public double edgeOffset = 0.001;

    // ════════ 视觉 ════════

    /** 移除方块内窒息贴图 */
    public boolean removeOverlay = true;

    /** 禁用前方第三人称视角 */
    public boolean noFront = false;

    private PearlPhaseConfig() {}

    public static PearlPhaseConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, PearlPhaseConfig.class);
            } catch (IOException e) {
                instance = new PearlPhaseConfig();
            }
        } else {
            instance = new PearlPhaseConfig();
            save();
        }
        if (instance == null) instance = new PearlPhaseConfig();
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

    private static File getConfigFile() {
        File dir = getConfigDir();
        return new File(dir, "pearl_phase.json");
    }

    private static File getConfigDir() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.gameDirectory != null) {
                File fkuDir = new File(mc.gameDirectory, "fku");
                if (!fkuDir.exists()) fkuDir.mkdirs();
                return fkuDir;
            }
        } catch (Exception ignored) {}
        File fallback = new File(Paths.get("config").toAbsolutePath().normalize().toFile(), "fku");
        if (!fallback.exists()) fallback.mkdirs();
        return fallback;
    }

    // ════════ Setter（即时保存） ════════
    public void setEnabled(boolean v) { this.enabled = v; save(); }
    public void setAutoThrow(boolean v) { this.autoThrow = v; save(); }
    public void setNoClipEnabled(boolean v) { this.noClipEnabled = v; save(); }
    public void setSpeed(double v) { this.speed = Math.max(0, Math.min(20, v)); save(); }
    public void setBaseSpeed(double v) { this.baseSpeed = Math.max(0.00001, Math.min(0.1, v)); save(); }
    public void setAimTime(int v) { this.aimTime = Math.max(0, Math.min(1000, v)); save(); }
    public void setMaxWaitTicks(int v) { this.maxWaitTicks = Math.max(20, Math.min(600, v)); save(); }
    public void setEdgeOffset(double v) { this.edgeOffset = Math.max(0.0001, Math.min(0.1, v)); save(); }
    public void setRemoveOverlay(boolean v) { this.removeOverlay = v; save(); }
    public void setNoFront(boolean v) { this.noFront = v; save(); }
}