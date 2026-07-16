package fku.org.example.fku.features.fastjoin; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 快速加载（FastJoin）配置 — JSON 持久化
 */
public class FastJoinConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static FastJoinConfig instance;

    public boolean enabled = false;
    /** 模式: EXTREME / SMOOTH / COMPAT / OFF */
    public String mode = "SMOOTH";
    /** 目标视距 (2~32) */
    public int targetRenderDistance = 12;
    /** 恢复速度 (1~4) */
    public int recoverSpeed = 1;
    /** 显示加载进度 */
    public boolean showLoadingProgress = true;
    /** 超时自动回退极速模式 */
    public boolean onTimeoutFallback = true;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "fastjoin.json");
    }

    private static File getGameDirectory() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }

    public static FastJoinConfig getInstance() { if (instance == null) load(); return instance; }

    public static void load() {
        File f = getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f)) { instance = GSON.fromJson(r, FastJoinConfig.class); }
            catch (IOException e) { instance = new FastJoinConfig(); }
        } else { instance = new FastJoinConfig(); save(); }
    }
    public static void save() {
        if (instance == null) return;
        try (FileWriter w = new FileWriter(getConfigFile())) { GSON.toJson(instance, w); }
        catch (IOException e) { e.printStackTrace(); }
    }

    public void setEnabled(boolean v) { this.enabled = v; save(); }
    public void setMode(String v) {
        if ("EXTREME".equals(v) || "SMOOTH".equals(v) || "COMPAT".equals(v)) this.mode = v;
        save();
    }
    public void setTargetRenderDistance(int v) { this.targetRenderDistance = Math.max(2, Math.min(32, v)); save(); }
    public void setRecoverSpeed(int v) { this.recoverSpeed = Math.max(1, Math.min(4, v)); save(); }
    public void setShowLoadingProgress(boolean v) { this.showLoadingProgress = v; save(); }
    public void setOnTimeoutFallback(boolean v) { this.onTimeoutFallback = v; save(); }

    public static String getModeTooltip(String mode) {
        return switch (mode) {
            case "EXTREME" -> "§7初始仅加载1个区块，进入后逐步恢复视距。加载最快，但进入后可能短暂看到地形加载。";
            case "SMOOTH" -> "§7初始加载一半视距，进入后平缓恢复。速度与稳定性均衡。";
            case "COMPAT" -> "§7不改动加载逻辑，仅启用超时保护。遇到兼容性问题时使用。";
            default -> "";
        };
    }
}
