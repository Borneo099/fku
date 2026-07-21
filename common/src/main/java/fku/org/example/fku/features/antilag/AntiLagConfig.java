package fku.org.example.fku.features.antilag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class AntiLagConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static AntiLagConfig instance;
    public boolean enabled = false;
    public String serverVersionMode = "MC1_16";
    public double range = 100.0;
    public int limitPerSecond = 100;
    public double moveDistance = 0.5;
    public String searchVclipMode = "OnlyUp";
    public double searchFindStep = 1.8;
    public boolean back = false;
    public boolean allowIntoVoid = false;
    public boolean printWhenTooManyPacket = true;
    public transient int packetCounter = 0;
    public transient long lastResetTime = System.currentTimeMillis();
    public transient boolean rateLimited = false;

    private static File getConfigFile() {
        File configDir;
        try {
            Minecraft mc = Minecraft.getInstance();
            configDir = mc != null ? new File(mc.gameDirectory, "fku") : Paths.get("config", new String[0]).toAbsolutePath().normalize().getParent().toFile();
        }
        catch (Exception e) {
            configDir = Paths.get("config", new String[0]).toAbsolutePath().normalize().getParent().toFile();
        }
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "antilag.json");
    }

    public static AntiLagConfig getInstance() {
        if (instance == null) {
            AntiLagConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = AntiLagConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (AntiLagConfig)GSON.fromJson(reader, AntiLagConfig.class);
            }
            catch (IOException e) {
                instance = new AntiLagConfig();
            }
        } else {
            instance = new AntiLagConfig();
            AntiLagConfig.save();
        }
        AntiLagConfig.instance.packetCounter = 0;
        AntiLagConfig.instance.lastResetTime = System.currentTimeMillis();
        AntiLagConfig.instance.rateLimited = false;
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        File configFile = AntiLagConfig.getConfigFile();
        try (FileWriter writer = new FileWriter(configFile);){
            GSON.toJson(instance, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        if (!v) {
            this.resetState();
        }
        AntiLagConfig.save();
    }

    public void setServerVersionMode(String v) {
        this.serverVersionMode = "MC1_16".equals(v) || "MC1_9".equals(v) ? v : "MC1_16";
        AntiLagConfig.save();
    }

    public void setRange(double v) {
        this.range = Math.max(0.1, Math.min(2000.0, v));
        AntiLagConfig.save();
    }

    public void setLimitPerSecond(int v) {
        this.limitPerSecond = Math.max(1, Math.min(10000, v));
        AntiLagConfig.save();
    }

    public void setMoveDistance(double v) {
        this.moveDistance = Math.max(0.01, Math.min(1.0, v));
        AntiLagConfig.save();
    }

    public void setSearchVclipMode(String v) {
        this.searchVclipMode = "OnlyUp".equals(v) || "Down".equals(v) || "Both".equals(v) ? v : "OnlyUp";
        AntiLagConfig.save();
    }

    public void setSearchFindStep(double v) {
        this.searchFindStep = Math.max(0.1, Math.min(5.0, v));
        AntiLagConfig.save();
    }

    public void setBack(boolean v) {
        this.back = v;
        AntiLagConfig.save();
    }

    public void setAllowIntoVoid(boolean v) {
        this.allowIntoVoid = v;
        AntiLagConfig.save();
    }

    public void setPrintWhenTooManyPacket(boolean v) {
        this.printWhenTooManyPacket = v;
        AntiLagConfig.save();
    }

    public void resetState() {
        this.packetCounter = 0;
        this.lastResetTime = System.currentTimeMillis();
        this.rateLimited = false;
    }
}

