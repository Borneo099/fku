package fku.org.example.fku.features.knockback;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class KnockbackConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static KnockbackConfig instance;
    public boolean enabled = false;
    public String mode = "PUSHBACK";
    public float customYaw = 0.0f;
    public int cliffSearchRadius = 5;
    public int rotationDelay = 0;
    public boolean aggressiveMode = false;
    public boolean smoothRotation = true;
    public int smoothSteps = 5;

    private static File getConfigFile() {
        File configDir = new File(KnockbackConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "knockback.json");
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
        return Paths.get("config", new String[0]).toAbsolutePath().normalize().getParent().toFile();
    }

    public static KnockbackConfig getInstance() {
        if (instance == null) {
            KnockbackConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = KnockbackConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (KnockbackConfig)GSON.fromJson(reader, KnockbackConfig.class);
            }
            catch (IOException e) {
                instance = new KnockbackConfig();
            }
        } else {
            instance = new KnockbackConfig();
            KnockbackConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        File configFile = KnockbackConfig.getConfigFile();
        try (FileWriter writer = new FileWriter(configFile);){
            GSON.toJson(instance, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        KnockbackConfig.save();
    }

    public void setMode(String v) {
        this.mode = v != null ? v : "PUSHBACK";
        KnockbackConfig.save();
    }

    public void setCustomYaw(float v) {
        this.customYaw = v;
        KnockbackConfig.save();
    }

    public void setCliffSearchRadius(int v) {
        this.cliffSearchRadius = Math.max(1, Math.min(20, v));
        KnockbackConfig.save();
    }

    public void setRotationDelay(int v) {
        this.rotationDelay = Math.max(0, Math.min(5, v));
        KnockbackConfig.save();
    }

    public void setAggressiveMode(boolean v) {
        this.aggressiveMode = v;
        KnockbackConfig.save();
    }

    public void setSmoothRotation(boolean v) {
        this.smoothRotation = v;
        KnockbackConfig.save();
    }

    public void setSmoothSteps(int v) {
        this.smoothSteps = Math.max(2, Math.min(10, v));
        KnockbackConfig.save();
    }
}

