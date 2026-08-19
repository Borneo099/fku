package fku.org.example.fku.features.antiknockback; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 防击退（AntiKnockback）配置 — JSON 持久化
 *
 * ★ 设计思想：
 *   - enabled：总开关
 *   - mode：免疫模式
 *       FULL  → 完全免疫（取消 LivingKnockBackEvent，击退=0）
 *       REDUCE → 按比例减弱（strength 越大，剩余击退越少）
 *   - strength：减弱强度（REDUCE 模式生效，0.0~1.0，1.0 等同 FULL）
 */
public class AntiKnockbackConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static AntiKnockbackConfig instance;

    /** 功能总开关 */
    public boolean enabled = false;

    /** 免疫模式：FULL（完全免疫）/ REDUCE（按比例减弱） */
    public String mode = "FULL";

    /** 减弱强度（REDUCE 模式，0.0~1.0，剩余击退 = (1-strength)） */
    public float strength = 1.0f;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "antiknockback.json");
    }
    private static File getGameDirectory() {
        try { Minecraft mc = Minecraft.getInstance(); if (mc != null) return mc.gameDirectory; }
        catch (Exception ignored) {}
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }
    public static AntiKnockbackConfig getInstance() { if (instance == null) load(); return instance; }
    public static void load() {
        File f = getConfigFile();
        if (f.exists()) { try (FileReader r = new FileReader(f)) { instance = GSON.fromJson(r, AntiKnockbackConfig.class); } catch (IOException e) { instance = new AntiKnockbackConfig(); } }
        else { instance = new AntiKnockbackConfig(); save(); }
    }
    public static void save() {
        if (instance == null) return;
        try (FileWriter w = new FileWriter(getConfigFile())) { GSON.toJson(instance, w); } catch (IOException e) { e.printStackTrace(); }
    }

    public void setEnabled(boolean v) { this.enabled = v; save(); }
    public void setMode(String v) { this.mode = (v != null) ? v : "FULL"; save(); }
    public void setStrength(float v) { this.strength = Math.max(0.0f, Math.min(1.0f, v)); save(); }

    public enum Mode {
        FULL("完全免疫"),
        REDUCE("按比例减弱");
        private final String label;
        Mode(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }
    public Mode getMode() {
        try { return Mode.valueOf(mode); } catch (IllegalArgumentException e) { return Mode.FULL; }
    }
}
