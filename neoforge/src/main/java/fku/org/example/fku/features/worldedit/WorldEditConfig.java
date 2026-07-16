package fku.org.example.fku.features.worldedit; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * WorldEdit Lite 配置类 — JSON 持久化
 *
 * 设计思想：
 * - 继承 AntiLagConfig 的 JSON 配置模式
 * - 字段修改即保存，确保跨会话一致
 */
public class WorldEditConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static WorldEditConfig instance;

    // ════════════ 配置字段 ════════════
    /** 功能总开关 */
    public boolean enabled = false;

    /** 选区工具物品ID */
    public String toolItem = "minecraft:wooden_axe";

    /** 超远距离倍率 */
    public int rangeMultiplier = 114514;

    /** 每tick最大发包数 */
    public int maxPacketsPerTick = 50;

    /** 最大撤销步数 */
    public int maxUndoSteps = 50;

    /** 是否渲染选区边框 */
    public boolean renderSelection = true;

    /** 选区边框颜色 (十六进制) */
    public String selectionColor = "#00FF00";

    /** 操作后是否自动恢复原手持物品 */
    public boolean autoRestoreSlot = true;

    /** 安全模式（防止误操作） */
    public boolean safeMode = true;

    /** 是否启用剪贴板 */
    public boolean enableClipboard = true;

    /** 结构文件保存文件夹 */
    public String schematicsFolder = "fku/schematics";

    // ════════════ 运行时内部状态（不持久化） ════════════
    public transient boolean taskRunning = false;
    public transient String taskStatus = "";

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
        return new File(configDir, "worldedit.json");
    }

    public static WorldEditConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, WorldEditConfig.class);
            } catch (IOException e) {
                instance = new WorldEditConfig();
            }
        } else {
            instance = new WorldEditConfig();
            save();
        }
        instance.taskRunning = false;
        instance.taskStatus = "";
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
    public void setEnabled(boolean v) {
        this.enabled = v;
        if (!v) {
            taskRunning = false;
            taskStatus = "";
        }
        save();
    }

    public void setToolItem(String v) {
        this.toolItem = v != null ? v : "minecraft:wooden_axe";
        save();
    }

    public void setRangeMultiplier(int v) {
        this.rangeMultiplier = Math.max(1, Math.min(999999, v));
        save();
    }

    public void setMaxPacketsPerTick(int v) {
        this.maxPacketsPerTick = Math.max(1, Math.min(5000, v));
        save();
    }

    public void setMaxUndoSteps(int v) {
        this.maxUndoSteps = Math.max(1, Math.min(500, v));
        save();
    }

    public void setRenderSelection(boolean v) {
        this.renderSelection = v;
        save();
    }

    public void setSelectionColor(String v) {
        this.selectionColor = v != null ? v : "#00FF00";
        save();
    }

    public void setAutoRestoreSlot(boolean v) {
        this.autoRestoreSlot = v;
        save();
    }

    public void setSafeMode(boolean v) {
        this.safeMode = v;
        save();
    }

    public void setEnableClipboard(boolean v) {
        this.enableClipboard = v;
        save();
    }

    public void setSchematicsFolder(String v) {
        this.schematicsFolder = v != null ? v : "fku/schematics";
        save();
    }
}
