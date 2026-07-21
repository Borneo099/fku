package fku.org.example.fku.features.duplicator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class DuplicatorConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static DuplicatorConfig instance;
    public int dupeDelay = 5;
    public int holdDuration = 10;
    public boolean enableTrident = true;
    public boolean dropTridents = true;
    public boolean durabilityManagement = true;
    public boolean bypassGrim = false;
    public boolean autoCloseOnDamage = true;
    public boolean autoCleanInventory = true;

    private static File getConfigFile() {
        File configDir = new File(DuplicatorConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "duplicator.json");
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

    public static DuplicatorConfig getInstance() {
        if (instance == null) {
            DuplicatorConfig.load();
        }
        return instance;
    }

    public static void load() {
        File f = DuplicatorConfig.getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f);){
                instance = (DuplicatorConfig)GSON.fromJson(r, DuplicatorConfig.class);
            }
            catch (IOException e) {
                instance = new DuplicatorConfig();
            }
        } else {
            instance = new DuplicatorConfig();
            DuplicatorConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        try (FileWriter w = new FileWriter(DuplicatorConfig.getConfigFile());){
            GSON.toJson(instance, w);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDupeDelay(int v) {
        this.dupeDelay = Math.max(1, Math.min(200, v));
        DuplicatorConfig.save();
    }

    public void setHoldDuration(int v) {
        this.holdDuration = Math.max(1, Math.min(200, v));
        DuplicatorConfig.save();
    }

    public void setDropTridents(boolean v) {
        this.dropTridents = v;
        DuplicatorConfig.save();
    }

    public void setDurabilityManagement(boolean v) {
        this.durabilityManagement = v;
        DuplicatorConfig.save();
    }

    public void setBypassGrim(boolean v) {
        this.bypassGrim = v;
        DuplicatorConfig.save();
    }

    public void setAutoCloseOnDamage(boolean v) {
        this.autoCloseOnDamage = v;
        DuplicatorConfig.save();
    }

    public void setAutoCleanInventory(boolean v) {
        this.autoCleanInventory = v;
        DuplicatorConfig.save();
    }
}

