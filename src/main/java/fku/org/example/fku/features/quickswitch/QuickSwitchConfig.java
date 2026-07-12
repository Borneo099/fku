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
 * ★ 设计思想（参考 BedrockBreakerConfig）：
 *   - 使用 mc.gameDirectory 确定配置文件路径（不受 CWD 影响）
 *   - load() 不销毁已有 instance，仅在首次通过 getInstance() 惰性加载
 *   - 所有修改即时写入 config/fku/quickswitch.json
 */
public class QuickSwitchConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static QuickSwitchConfig instance;

    // ════════ 基础开关 ════════
    /** 功能总开关 */
    public boolean enabled = false;

    // ════════ 模式 ════════
    /** 秒切模式：SMART / CUSTOM / OFF */
    public String mode = "OFF";

    // ════════ 自定义物品（CUSTOM 模式使用） ════════
    public String customItems = "minecraft:diamond_sword,minecraft:diamond_axe,minecraft:mace";

    // ════════ 高级 ════════
    /** 视觉反馈：显示秒切切换消息 */
    public boolean visualFeedback = true;

    /** 优先级槽位列表 */
    public int[] prioritySlots = new int[]{1, 2, 3};

    /** 秒切恢复延迟（毫秒）默认 80ms */
    public int rttDelay = 80;

    private QuickSwitchConfig() {}

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "quickswitch.json");
    }

    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get(".").toAbsolutePath().normalize().toFile();
    }

    public static QuickSwitchConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, QuickSwitchConfig.class);
            } catch (IOException e) {
                instance = new QuickSwitchConfig();
            }
        } else {
            instance = new QuickSwitchConfig();
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

    public void setEnabled(boolean v) { this.enabled = v; save(); }

    /** 是否为活跃的秒切模式（非 OFF） */
    public boolean isActiveMode() {
        return !"OFF".equals(mode);
    }

    public static String[] getAvailableModes() {
        return new String[]{"OFF", "SMART", "CUSTOM"};
    }
}
