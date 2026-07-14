package fku.org.example.fku.features.loot; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 一键取物（Loot Nearby Containers）配置类
 *
 * ★ 职责：
 *   使用 JSON 持久化存储扫描/取物参数。
 *   与 DuplicatorConfig、LootConfig 风格一致。
 *
 * ★ 配置项：
 *   - enabled：功能总开关
 *   - radius：扫描半径（1~16），默认 8
 *   - clickDelay：每个物品点击间隔（ms），默认 20
 *   - containerDelay：每个容器处理后的间隔（ms），默认 200
 *   - dropOverflow：背包满时是否丢弃多余物品（默认 false）
 */
public class LootConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static LootConfig instance;

    /** 功能总开关 */
    public boolean enabled = false;

    /** 扫描半径（1~16） */
    public int radius = 8;

    /** 每个物品点击间隔（ms），防止触发反作弊 */
    public int clickDelay = 20;

    /** 每个容器处理后的等待间隔（ms） */
    public int containerDelay = 200;

    /** 背包满时是否丢弃多余物品（谨慎使用） */
    public boolean dropOverflow = false;

    /** 取完物品后是否自动关闭容器 GUI */
    public boolean autoCloseGUI = true;

    /** 扫描刷新间隔（tick），每 N tick 重新扫描容器，默认 20 tick = 1 秒 */
    public int scanRefreshInterval = 20;

    /** 热键键码（-1 表示未设置） */
    public int hotkeyKey = -1;

    /** 热键显示名称 */
    public String hotkeyName = "";

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "loot.json");
    }

    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }

    public static LootConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, LootConfig.class);
            } catch (IOException e) {
                instance = new LootConfig();
            }
        } else {
            instance = new LootConfig();
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

    // ════════ Setter（自动保存） ════════
    public void setEnabled(boolean v) { this.enabled = v; save(); }
    public void setRadius(int v) { this.radius = Math.max(1, Math.min(16, v)); save(); }
    public void setClickDelay(int v) { this.clickDelay = Math.max(0, Math.min(200, v)); save(); }
    public void setContainerDelay(int v) { this.containerDelay = Math.max(0, Math.min(2000, v)); save(); }
    public void setDropOverflow(boolean v) { this.dropOverflow = v; save(); }
    public void setAutoCloseGUI(boolean v) { this.autoCloseGUI = v; save(); }
    public void setScanRefreshInterval(int v) { this.scanRefreshInterval = Math.max(5, Math.min(200, v)); save(); }
    public void setHotkeyKey(int v) { this.hotkeyKey = v; save(); }
    public void setHotkeyName(String v) { this.hotkeyName = v != null ? v : ""; save(); }
}