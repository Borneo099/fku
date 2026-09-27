package fku.org.example.fku.features.skija;

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
import net.minecraft.client.Minecraft;

/**
 * Skija 渲染配置 — 全部静默读写（gameDirectory/fku/skija.json）
 * 仅 useSkija / enableColorWheel 在阶段1使用；其余为阶段2-4预留。
 */
public class SkijaConfig {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static SkijaConfig instance;

    // 分组1：Skija 渲染总开关（已回退：当前强制禁用，详见 SkijaRenderer.init）
    public boolean useSkija = false;
    public boolean enableColorWheel = true;
    public boolean enableRoundedCorners = true;
    public boolean enableTextRendering = true;
    public boolean enableIslandRenderer = true;

    // 分组2：颜色轮盘
    public int wheelSize = 150;
    public int wheelSmoothness = 64;
    public boolean showAlphaChannel = true;

    // 分组3：字体
    public String fontFamily = "DEFAULT";   // DEFAULT / YAHEI / NOTO / CUSTOM
    public int fontSize = 14;
    public boolean textAntiAlias = true;
    public boolean textShadow = true;

    // 分组4：性能与兼容
    public int skijaCacheSize = 64;
    public boolean disableOnOptiFine = true;
    public boolean debugMode = false;

    private static File getConfigFile() {
        File dir = new File(getGameDirectory(), "fku");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "skija.json");
    }

    private static File getGameDirectory() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get(".").toAbsolutePath().normalize().toFile();
    }

    public static SkijaConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File f = getConfigFile();
        if (f.exists()) {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
                instance = GSON.fromJson(r, SkijaConfig.class);
            } catch (Exception e) {
                instance = new SkijaConfig();
            }
        } else {
            instance = new SkijaConfig();
            save();
        }
        if (instance == null) instance = new SkijaConfig();
    }

    public static void save() {
        if (instance == null) return;
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getConfigFile()), StandardCharsets.UTF_8))) {
            GSON.toJson(instance, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
