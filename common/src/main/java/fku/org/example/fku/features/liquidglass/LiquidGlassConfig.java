package fku.org.example.fku.features.liquidglass;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class LiquidGlassConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static LiquidGlassConfig instance;
    public boolean enabled = false;
    public int panelX = 50;
    public int panelY = 50;
    public float panelWidth = 200.0f;
    public float panelHeight = 150.0f;
    public float cornerRadius = 8.0f;
    public float blurRadius = 4.0f;
    public float refractionPower = 0.75f;
    public float refractionEdge = 0.3f;
    public float dispersion = 0.002f;
    public float globalAlpha = 0.85f;
    public int tintMode = 0;
    public float tintR = 0.82f;
    public float tintG = 0.88f;
    public float tintB = 1.0f;
    public float tintStrength = 0.12f;
    public float noise = 0.03f;
    public float glowWeight = 0.3f;
    public float glowBias = 0.0f;
    public float glowEdge0 = 0.06f;
    public float glowEdge1 = 0.0f;
    public float chromaStrength = 0.001f;
    public float darkness = 0.0f;
    public float lumaPoint0 = 0.0f;
    public float lumaPoint1 = 0.5f;
    public float lumaPoint2 = 0.8f;
    public float lumaPoint3 = 1.0f;
    public float lumaMapForce = 0.5f;
    public float extraBrightness = 0.05f;

    private static File getConfigFile() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.gameDirectory != null) {
                return new File(new File(mc.gameDirectory, "fku"), "liquid_glass.json");
            }
        }
        catch (Exception exception) {
            // ignored
        }
        return new File(Paths.get("config", new String[0]).toAbsolutePath().normalize().getParent().toFile(), "fku/liquid_glass.json");
    }

    public static LiquidGlassConfig getInstance() {
        if (instance == null) {
            LiquidGlassConfig.load();
        }
        return instance;
    }

    public static void load() {
        File f = LiquidGlassConfig.getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f);){
                instance = (LiquidGlassConfig)GSON.fromJson(r, LiquidGlassConfig.class);
            }
            catch (Exception e) {
                instance = new LiquidGlassConfig();
            }
        } else {
            instance = new LiquidGlassConfig();
            LiquidGlassConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        LiquidGlassConfig.getConfigFile().getParentFile().mkdirs();
        try (FileWriter w = new FileWriter(LiquidGlassConfig.getConfigFile());){
            GSON.toJson(instance, w);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        LiquidGlassConfig.save();
    }

    public void setPanelX(int v) {
        this.panelX = v;
        LiquidGlassConfig.save();
    }

    public void setPanelY(int v) {
        this.panelY = v;
        LiquidGlassConfig.save();
    }

    public void setPanelWidth(float v) {
        this.panelWidth = Math.max(50.0f, Math.min(500.0f, v));
        LiquidGlassConfig.save();
    }

    public void setPanelHeight(float v) {
        this.panelHeight = Math.max(50.0f, Math.min(500.0f, v));
        LiquidGlassConfig.save();
    }

    public void setCornerRadius(float v) {
        this.cornerRadius = Math.max(0.0f, Math.min(50.0f, v));
        LiquidGlassConfig.save();
    }

    public void setBlurRadius(float v) {
        this.blurRadius = Math.max(0.0f, Math.min(20.0f, v));
        LiquidGlassConfig.save();
    }

    public void setRefractionPower(float v) {
        this.refractionPower = Math.max(-1.0f, Math.min(10.0f, v));
        LiquidGlassConfig.save();
    }

    public void setRefractionEdge(float v) {
        this.refractionEdge = Math.max(0.0f, Math.min(1.0f, v));
        LiquidGlassConfig.save();
    }

    public void setDispersion(float v) {
        this.dispersion = Math.max(0.0f, Math.min(0.05f, v));
        LiquidGlassConfig.save();
    }

    public void setGlobalAlpha(float v) {
        this.globalAlpha = Math.max(0.0f, Math.min(1.0f, v));
        LiquidGlassConfig.save();
    }

    public void setTintMode(int v) {
        this.tintMode = Math.max(0, Math.min(1, v));
        LiquidGlassConfig.save();
    }

    public void setTintR(float v) {
        this.tintR = Math.max(0.0f, Math.min(1.0f, v));
        LiquidGlassConfig.save();
    }

    public void setTintG(float v) {
        this.tintG = Math.max(0.0f, Math.min(1.0f, v));
        LiquidGlassConfig.save();
    }

    public void setTintB(float v) {
        this.tintB = Math.max(0.0f, Math.min(1.0f, v));
        LiquidGlassConfig.save();
    }

    public void setTintStrength(float v) {
        this.tintStrength = Math.max(0.0f, Math.min(1.0f, v));
        LiquidGlassConfig.save();
    }

    public void setNoise(float v) {
        this.noise = Math.max(0.0f, Math.min(0.3f, v));
        LiquidGlassConfig.save();
    }

    public void setGlowWeight(float v) {
        this.glowWeight = Math.max(-1.0f, Math.min(1.0f, v));
        LiquidGlassConfig.save();
    }

    public void setGlowBias(float v) {
        this.glowBias = Math.max(-1.0f, Math.min(1.0f, v));
        LiquidGlassConfig.save();
    }

    public void setGlowEdge0(float v) {
        this.glowEdge0 = Math.max(-1.0f, Math.min(1.0f, v));
        LiquidGlassConfig.save();
    }

    public void setGlowEdge1(float v) {
        this.glowEdge1 = Math.max(-1.0f, Math.min(1.0f, v));
        LiquidGlassConfig.save();
    }

    public void setChromaStrength(float v) {
        this.chromaStrength = Math.max(0.0f, Math.min(0.01f, v));
        LiquidGlassConfig.save();
    }

    public void setDarkness(float v) {
        this.darkness = Math.max(0.0f, Math.min(1.0f, v));
        LiquidGlassConfig.save();
    }
}

