package fku.org.example.fku.features.liquidglass; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 液体玻璃面板配置类
 * 支持面板位置/大小、玻璃效果参数、着色器模式等所有可调选项
 * 配置持久化为 JSON 文件，保存在 ./fku/liquid_glass.json
 *
 * ★ 参考：LiquidGlassShader (https://github.com/Jacquesqwq/LiquidGlassShader)
 *   移植其 V3 单通道片源着色器方案，适配 Forge 1.20.1
 */
public class LiquidGlassConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static LiquidGlassConfig instance;

    // ============ 面板基础设置 ============

    /** 功能开关 */
    public boolean enabled = false;

    /** 面板X坐标（屏幕左上角为原点） */
    public int panelX = 50;
    /** 面板Y坐标 */
    public int panelY = 50;
    /** 面板宽度 */
    public float panelWidth = 200.0f;
    /** 面板高度 */
    public float panelHeight = 150.0f;

    // ============ 玻璃效果参数 ============

    /** 圆角半径 */
    public float cornerRadius = 8.0f;

    /** 模糊半径（影响背景模糊程度） */
    public float blurRadius = 4.0f;

    /** 折射强度 (0.0 ~ 10.0) */
    public float refractionPower = 0.75f;

    /** 折射边缘强度 (0.0 ~ 1.0) */
    public float refractionEdge = 0.3f;

    /** 色散强度 (0.0 ~ 0.05) */
    public float dispersion = 0.002f;

    /** 全局透明度 (0.0 ~ 1.0) */
    public float globalAlpha = 0.85f;

    // ============ 着色参数 ============

    /** 着色模式: 0=Clear(清澈), 1=Tinted(染色) */
    public int tintMode = 0;

    /** 染色R (0~1) */
    public float tintR = 0.82f;
    /** 染色G (0~1) */
    public float tintG = 0.88f;
    /** 染色B (0~1) */
    public float tintB = 1.0f;
    /** 染色强度 (0~1) */
    public float tintStrength = 0.12f;

    // ============ Clear 模式专用参数 ============

    /** 噪声/磨砂强度 (0.0 ~ 0.3) */
    public float noise = 0.03f;

    /** 边缘发光权重 (-1.0 ~ 1.0) */
    public float glowWeight = 0.3f;

    /** 边缘发光偏移 (-1.0 ~ 1.0) */
    public float glowBias = 0.0f;

    /** 边缘发光起始 (-1.0 ~ 1.0) */
    public float glowEdge0 = 0.06f;

    /** 边缘发光结束 (-1.0 ~ 1.0) */
    public float glowEdge1 = 0.0f;

    // ============ Tinted 模式专用参数 ============

    /** 色散强度 (0.0 ~ 0.01) */
    public float chromaStrength = 0.001f;

    /** 暗度 (0.0 ~ 1.0) */
    public float darkness = 0.0f;

    // ============ V3 高级参数 ============

    /** 亮度曲线控制点0 */
    public float lumaPoint0 = 0.0f;
    /** 亮度曲线控制点1 */
    public float lumaPoint1 = 0.5f;
    /** 亮度曲线控制点2 */
    public float lumaPoint2 = 0.8f;
    /** 亮度曲线控制点3 */
    public float lumaPoint3 = 1.0f;
    /** 亮度映射强度 */
    public float lumaMapForce = 0.5f;
    /** 额外亮度 */
    public float extraBrightness = 0.05f;

    // ============ 文件操作 ============

    private static File getConfigFile() {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null && mc.gameDirectory != null)
                return new File(new File(mc.gameDirectory, "fku"), "liquid_glass.json");
        } catch (Exception ignored) {}
        return new File(Paths.get("config").toAbsolutePath().normalize().getParent().toFile(), "fku/liquid_glass.json");
    }

    public static LiquidGlassConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File f = getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f)) {
                instance = GSON.fromJson(r, LiquidGlassConfig.class);
            } catch (Exception e) {
                instance = new LiquidGlassConfig();
            }
        } else {
            instance = new LiquidGlassConfig();
            save();
        }
    }

    public static void save() {
        if (instance == null) return;
        getConfigFile().getParentFile().mkdirs();
        try (FileWriter w = new FileWriter(getConfigFile())) {
            GSON.toJson(instance, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ============ Setter 方法（自动保存） ============

    public void setEnabled(boolean v) { this.enabled = v; save(); }
    public void setPanelX(int v) { this.panelX = v; save(); }
    public void setPanelY(int v) { this.panelY = v; save(); }
    public void setPanelWidth(float v) { this.panelWidth = Math.max(50, Math.min(500, v)); save(); }
    public void setPanelHeight(float v) { this.panelHeight = Math.max(50, Math.min(500, v)); save(); }
    public void setCornerRadius(float v) { this.cornerRadius = Math.max(0, Math.min(50, v)); save(); }
    public void setBlurRadius(float v) { this.blurRadius = Math.max(0, Math.min(20, v)); save(); }
    public void setRefractionPower(float v) { this.refractionPower = Math.max(-1, Math.min(10, v)); save(); }
    public void setRefractionEdge(float v) { this.refractionEdge = Math.max(0, Math.min(1, v)); save(); }
    public void setDispersion(float v) { this.dispersion = Math.max(0, Math.min(0.05f, v)); save(); }
    public void setGlobalAlpha(float v) { this.globalAlpha = Math.max(0, Math.min(1, v)); save(); }
    public void setTintMode(int v) { this.tintMode = Math.max(0, Math.min(1, v)); save(); }
    public void setTintR(float v) { this.tintR = Math.max(0, Math.min(1, v)); save(); }
    public void setTintG(float v) { this.tintG = Math.max(0, Math.min(1, v)); save(); }
    public void setTintB(float v) { this.tintB = Math.max(0, Math.min(1, v)); save(); }
    public void setTintStrength(float v) { this.tintStrength = Math.max(0, Math.min(1, v)); save(); }
    public void setNoise(float v) { this.noise = Math.max(0, Math.min(0.3f, v)); save(); }
    public void setGlowWeight(float v) { this.glowWeight = Math.max(-1, Math.min(1, v)); save(); }
    public void setGlowBias(float v) { this.glowBias = Math.max(-1, Math.min(1, v)); save(); }
    public void setGlowEdge0(float v) { this.glowEdge0 = Math.max(-1, Math.min(1, v)); save(); }
    public void setGlowEdge1(float v) { this.glowEdge1 = Math.max(-1, Math.min(1, v)); save(); }
    public void setChromaStrength(float v) { this.chromaStrength = Math.max(0, Math.min(0.01f, v)); save(); }
    public void setDarkness(float v) { this.darkness = Math.max(0, Math.min(1, v)); save(); }
}