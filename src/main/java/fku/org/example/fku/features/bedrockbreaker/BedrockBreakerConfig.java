package fku.org.example.fku.features.bedrockbreaker; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 基岩破坏器配置类（JSON 持久化）
 *
 * 设计思想：
 * - 继承 DisplayModelConfig 的 JSON 配置模式，保持一致风格
 * - 所有字段修改即保存
 * - 参考 cheatutils BedrockBreakerConfig 的配置结构
 *   (https://github.com/Zergatul/cheatutils)
 */
public class BedrockBreakerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static BedrockBreakerConfig instance;

    public static final String DEFAULT_HELPER_BLOCK_LIST = "minecraft:cobbled_deepslate,minecraft:andesite,minecraft:granite,minecraft:diorite,minecraft:netherrack,minecraft:tuff,minecraft:sandstone,minecraft:cobblestone,minecraft:dirt";

    /** 是否对所有硬方块生效（而非仅目标方块） */
    public boolean allBlocks = false;

    /** 功能总开关 */
    public boolean enabled = false;

    /** 目标方块ID（默认为基岩） */
    public String targetBlockId = "minecraft:bedrock";

    /** 破坏后替换方块的 ID（空字符串=不替换） */
    public String replaceBlockId = "minecraft:diamond_block";

    /** 扫描模式：true=自动扫描周围方块，false=仅处理看向的方块 */
    public boolean scanMode = false;

    /** 自动搜索范围（0=手动模式，仅对看向的方块处理） */
    public int autoFindRange = 0;

    /** 破坏活塞超时（tick 数）*/
    public int breakTimeout = 50;

    /** 等待活塞伸出超时（tick 数）*/
    public int extendTimeout = 10;

    /** 破坏拉杆超时（tick 数）*/
    public int leverBreakTimeout = 30;

    /** 触发热键名称（如 key.keyboard.b），空=使用默认 B 键 */
    public String triggerKey = "";

    // ════════ 辅助方块系统（v2.2 新增） ════════

    /** 是否启用辅助方块：当检测不到拉杆放置位置时，自动放置辅助方块提供附着面 */
    public boolean enableHelperBlocks = true;

    /** 辅助方块ID列表（逗号分隔），优先级从前到后。
     *  默认使用深板岩圆石、安山岩、花岗岩、闪长岩、下界岩、凝灰岩、砂岩、圆石、泥土 */
    public String helperBlockList = DEFAULT_HELPER_BLOCK_LIST;

    /** 是否清理辅助方块：功能完成时自动移除辅助方块 */
    public boolean cleanupHelpers = true;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "bedrock_breaker.json");
    }

    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }

    public static BedrockBreakerConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, BedrockBreakerConfig.class);
            } catch (IOException e) {
                instance = new BedrockBreakerConfig();
            }
        } else {
            instance = new BedrockBreakerConfig();
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

    // ============ Setter ============
    public void setEnabled(boolean v) { this.enabled = v; save(); }
    public void setTargetBlockId(String v) { this.targetBlockId = v != null ? v : "minecraft:bedrock"; save(); }
    public void setReplaceBlockId(String v) { this.replaceBlockId = v; save(); }
    public void setScanMode(boolean v) { this.scanMode = v; save(); }
    public void setAllBlocks(boolean v) { this.allBlocks = v; save(); }
    public void setAutoFindRange(int v) { this.autoFindRange = Math.max(0, Math.min(10, v)); save(); }
    public void setBreakTimeout(int v) { this.breakTimeout = Math.max(10, Math.min(200, v)); save(); }
    public void setExtendTimeout(int v) { this.extendTimeout = Math.max(5, Math.min(50, v)); save(); }
    public void setLeverBreakTimeout(int v) { this.leverBreakTimeout = Math.max(5, Math.min(100, v)); save(); }
    public void setTriggerKey(String v) { this.triggerKey = v != null ? v : ""; save(); }

    // ════════ 辅助方块 Setter（v2.2） ════════
    public void setEnableHelperBlocks(boolean v) { this.enableHelperBlocks = v; save(); }
    public void setHelperBlockList(String v) { this.helperBlockList = v != null ? v : DEFAULT_HELPER_BLOCK_LIST; save(); }
    public void setCleanupHelpers(boolean v) { this.cleanupHelpers = v; save(); }
}