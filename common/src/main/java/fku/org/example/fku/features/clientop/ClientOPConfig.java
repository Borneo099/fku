package fku.org.example.fku.features.clientop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * 客户端OP（Client OP）配置类
 * 配置持久化使用 JSON，遵循 FKU 现有配置模式（参考 KillFXConfig）。
 * 颜色以 6 位十六进制字符串存储（与 ColorWheelPicker 一致），如 "FF5555"。
 */
public class ClientOPConfig {

    private static File getConfigFile() {
        File dir = new File(getGameDirectory(), "fku");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "clientop.json");
    }

    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get(".").toAbsolutePath().normalize().toFile();
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    // ===== 通用设置 =====
    /** 功能总开关 */
    public boolean enabled = false;
    /** 是否在补全列表中标记需要 OP 的指令 */
    public boolean showOpIndicator = true;
    /** OP 指令标记颜色（6位十六进制，无 #） */
    public String opIndicatorColor = "FF5555";

    // ===== 补全增强 =====
    /** 是否启用本地指令树补全 */
    public boolean enableLocalSuggestions = true;
    /** 是否与服务端补全合并（false = 仅本地） */
    public boolean mergeWithServer = true;
    /** 最大补全数量（5~50） */
    public int maxSuggestions = 20;

    // ===== 指令预览 =====
    /** 是否启用指令执行预览 */
    public boolean enablePreview = true;
    /** 预览位置：CHAT_ABOVE / TOOLTIP / ACTION_BAR */
    public String previewPosition = "CHAT_ABOVE";
    /** 仅对需要 OP 的指令显示预览 */
    public boolean showPreviewForOpOnly = true;

    // ===== 聊天增强 =====
    /** 在聊天框中高亮显示 OP 指令 */
    public boolean highlightOpCommands = true;
    /** 高亮颜色（6位十六进制，无 #） */
    public String highlightColor = "FFAA00";
    /** 鼠标悬停/预览显示指令说明 */
    public boolean showCommandTooltip = true;

    private static ClientOPConfig instance;

    public static ClientOPConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File f = getConfigFile();
        if (f.exists()) {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
                instance = GSON.fromJson(r, ClientOPConfig.class);
            } catch (Exception e) {
                instance = new ClientOPConfig();
            }
        } else {
            instance = new ClientOPConfig();
            save();
        }
    }

    public static void save() {
        if (instance == null) return;
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getConfigFile()), StandardCharsets.UTF_8))) {
            GSON.toJson(instance, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean v) { enabled = v; save(); }
    public void saveConfig() { save(); }

    public int getOpIndicatorColor() { return parseHex(opIndicatorColor); }
    public int getHighlightColor() { return parseHex(highlightColor); }

    private static int parseHex(String h) {
        try {
            return Integer.parseInt(h.replace("#", ""), 16);
        } catch (Exception e) {
            return 0xFF5555;
        }
    }
}
