package fku.org.example.fku.features.pearlphase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class PearlPhaseConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static PearlPhaseConfig instance;
    public boolean enabled = false;
    public boolean autoThrow = true;
    public boolean noClipEnabled = true;
    public double speed = 5.0;
    public double baseSpeed = 1.0E-4;
    public int aimTime = 100;
    public int maxWaitTicks = 100;
    public double edgeOffset = 0.001;
    public boolean removeOverlay = true;
    public boolean noFront = false;

    private PearlPhaseConfig() {
    }

    public static PearlPhaseConfig getInstance() {
        if (instance == null) {
            PearlPhaseConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = PearlPhaseConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (PearlPhaseConfig)GSON.fromJson(reader, PearlPhaseConfig.class);
            }
            catch (IOException e) {
                instance = new PearlPhaseConfig();
            }
        } else {
            instance = new PearlPhaseConfig();
            PearlPhaseConfig.save();
        }
        if (instance == null) {
            instance = new PearlPhaseConfig();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        File configFile = PearlPhaseConfig.getConfigFile();
        try (FileWriter writer = new FileWriter(configFile);){
            GSON.toJson(instance, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getConfigFile() {
        File dir = PearlPhaseConfig.getConfigDir();
        return new File(dir, "pearl_phase.json");
    }

    private static File getConfigDir() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.gameDirectory != null) {
                File fkuDir = new File(mc.gameDirectory, "fku");
                if (!fkuDir.exists()) {
                    fkuDir.mkdirs();
                }
                return fkuDir;
            }
        }
        catch (Exception mc) {
            // ignored
        }
        File fallback = new File(Paths.get("config", new String[0]).toAbsolutePath().normalize().toFile(), "fku");
        if (!fallback.exists()) {
            fallback.mkdirs();
        }
        return fallback;
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        PearlPhaseConfig.save();
    }

    public void setAutoThrow(boolean v) {
        this.autoThrow = v;
        PearlPhaseConfig.save();
    }

    public void setNoClipEnabled(boolean v) {
        this.noClipEnabled = v;
        PearlPhaseConfig.save();
    }

    public void setSpeed(double v) {
        this.speed = Math.max(0.0, Math.min(20.0, v));
        PearlPhaseConfig.save();
    }

    public void setBaseSpeed(double v) {
        this.baseSpeed = Math.max(1.0E-5, Math.min(0.1, v));
        PearlPhaseConfig.save();
    }

    public void setAimTime(int v) {
        this.aimTime = Math.max(0, Math.min(1000, v));
        PearlPhaseConfig.save();
    }

    public void setMaxWaitTicks(int v) {
        this.maxWaitTicks = Math.max(20, Math.min(600, v));
        PearlPhaseConfig.save();
    }

    public void setEdgeOffset(double v) {
        this.edgeOffset = Math.max(1.0E-4, Math.min(0.1, v));
        PearlPhaseConfig.save();
    }

    public void setRemoveOverlay(boolean v) {
        this.removeOverlay = v;
        PearlPhaseConfig.save();
    }

    public void setNoFront(boolean v) {
        this.noFront = v;
        PearlPhaseConfig.save();
    }
}

