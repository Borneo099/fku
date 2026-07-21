package fku.org.example.fku.features.nofall;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class NoFallConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static NoFallConfig instance;
    public boolean enabled = false;
    public double minFallDistance = 3.0;
    public boolean immune = true;
    public boolean onlyWhenFlying = false;

    private static File getConfigFile() {
        File configDir = new File(NoFallConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "nofall.json");
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

    public static NoFallConfig getInstance() {
        if (instance == null) {
            NoFallConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = NoFallConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (NoFallConfig)GSON.fromJson(reader, NoFallConfig.class);
            }
            catch (IOException e) {
                instance = new NoFallConfig();
            }
        } else {
            instance = new NoFallConfig();
            NoFallConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        File configFile = NoFallConfig.getConfigFile();
        try (FileWriter writer = new FileWriter(configFile);){
            GSON.toJson(instance, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        NoFallConfig.save();
    }

    public void setMinFallDistance(double v) {
        this.minFallDistance = Math.max(0.0, v);
        NoFallConfig.save();
    }

    public void setImmune(boolean v) {
        this.immune = v;
        NoFallConfig.save();
    }

    public void setOnlyWhenFlying(boolean v) {
        this.onlyWhenFlying = v;
        NoFallConfig.save();
    }
}

