package fku.org.example.fku.features.attackindicator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class AttackIndicatorConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public boolean enabled = false;
    public String triggerMode = "BOTH";
    public boolean smoothTransition = true;
    public boolean enableBeam = true;
    public String beamColor = "FF4444";
    public float beamWidth = 2.0f;
    public float beamFlowSpeed = 0.5f;
    public boolean enableLightning = false;
    public String lightningColor = "FFFFFF";
    public int lightningSegments = 12;
    public boolean enablePulseWave = false;
    public String waveColor = "44FF44";
    public float waveSpeed = 1.0f;
    public boolean enableTether = false;
    public String tetherColor = "8888FF";
    public float tetherSway = 0.3f;
    public boolean enableSwordWave = false;
    public String swordWaveColor = "FFAA44";
    public float swordWaveIntensity = 1.0f;
    public float swordWaveSpeed = 1.0f;
    public boolean enableLockBox = true;
    public String boxColor = "FF4444";
    public float boxRotateSpeed = 2.0f;
    public float boxSize = 1.0f;
    public boolean enableGlow = false;
    public String glowColor = "FF4444";
    public float glowIntensity = 0.5f;
    public boolean enableBeamMarker = false;
    public String beamMarkerColor = "FFAA00";
    public float beamMarkerHeight = 8.0f;
    public boolean enableHalo = false;
    public String haloColor = "44AAFF";
    public float haloRadius = 1.2f;
    public float haloRotateSpeed = 1.0f;
    public boolean enableEdgeFlash = false;
    public String flashColor = "FF4444";
    public float flashIntensity = 0.8f;
    public boolean enableDirectionArrow = true;
    public String arrowColor = "FF4444";
    public float arrowSize = 1.0f;
    public int maxParticles = 100;
    public float particleLODDistance = 32.0f;
    public boolean enablePerformanceMode = false;
    public int despawnDelay = 2;
    private static AttackIndicatorConfig instance;

    private static File getConfigFile() {
        File configDir = new File(AttackIndicatorConfig.getGameDirectory(), "fku");
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
        }
        catch (Exception exception) {
            // ignored
        }
        return Paths.get(".", new String[0]).toAbsolutePath().normalize().toFile();
    }

    public static AttackIndicatorConfig getInstance() {
        if (instance == null) {
            AttackIndicatorConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = AttackIndicatorConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (AttackIndicatorConfig)GSON.fromJson(reader, AttackIndicatorConfig.class);
            }
            catch (IOException e) {
                instance = new AttackIndicatorConfig();
            }
        } else {
            instance = new AttackIndicatorConfig();
            AttackIndicatorConfig.save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(AttackIndicatorConfig.getConfigFile());){
            GSON.toJson(instance, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

