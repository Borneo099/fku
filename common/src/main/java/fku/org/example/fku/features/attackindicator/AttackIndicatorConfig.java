package fku.org.example.fku.features.attackindicator; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

/**
 * 攻击指示器配置类 — 持久化所有特效参数
 * 配置文件路径：./config/fku/attack_indicator.json
 * 该配置类由赛博教员实现
 */
public class AttackIndicatorConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ═══════ 通用设置 ═══════
    public boolean enabled = false;
    public String triggerMode = "BOTH"; // ON_ATTACK / ON_TPAURA_LOCK / BOTH
    public boolean smoothTransition = true;

    // ═══════ 连接特效 ═══════
    public boolean enableBeam = true;
    public String beamColor = "FF4444";
    public float beamWidth = 2.0f;
    public float beamFlowSpeed = 0.5f;

    public boolean enablePulseWave = true;
    public String waveColor = "44FF44";
    public float waveSpeed = 1.0f;

    public boolean enableSwordWave = true;
    public String swordWaveColor = "FFAA44";
    public float swordWaveIntensity = 1.0f;
    public float swordWaveSpeed = 1.0f;

    // ═══════ 目标标记 ═══════
    public boolean enableBeamMarker = true;
    public String beamMarkerColor = "FFAA00";
    public float beamMarkerHeight = 8.0f;

    public boolean enableHalo = true;
    public String haloColor = "44AAFF";
    public float haloRadius = 1.2f;
    public float haloRotateSpeed = 1.0f;

    // ═══════ 屏幕覆盖（已移除 — 锁定框改为2D狙击准星直接渲染在GUI层） ═══════

    // ═══════ 性能设置 ═══════
    public int maxParticles = 100;
    public float particleLODDistance = 32.0f;
    public boolean enablePerformanceMode = false;
    public int despawnDelay = 2;

    private static AttackIndicatorConfig instance;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "attack_indicator.json");
    }

    private static File getGameDirectory() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                return mc.gameDirectory;
            }
        } catch (Exception e) {
            // 静默降级
        }
        return Paths.get(".").toAbsolutePath().normalize().toFile();
    }

    public static AttackIndicatorConfig getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, AttackIndicatorConfig.class);
            } catch (IOException e) {
                instance = new AttackIndicatorConfig();
            }
        } else {
            instance = new AttackIndicatorConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(getConfigFile())) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}