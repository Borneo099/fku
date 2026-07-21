package fku.org.example.fku.features.loot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class LootConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static LootConfig instance;
    public boolean enabled = false;
    public int radius = 8;
    public int clickDelay = 20;
    public int containerDelay = 200;
    public boolean dropOverflow = false;
    public boolean autoCloseGUI = true;
    public int scanRefreshInterval = 20;
    public int hotkeyKey = -1;
    public String hotkeyName = "";

    private static File getConfigFile() {
        File configDir = new File(LootConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "loot.json");
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

    public static LootConfig getInstance() {
        if (instance == null) {
            LootConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = LootConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (LootConfig)GSON.fromJson(reader, LootConfig.class);
            }
            catch (IOException e) {
                instance = new LootConfig();
            }
        } else {
            instance = new LootConfig();
            LootConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        File configFile = LootConfig.getConfigFile();
        try (FileWriter writer = new FileWriter(configFile);){
            GSON.toJson(instance, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        LootConfig.save();
    }

    public void setRadius(int v) {
        this.radius = Math.max(1, Math.min(16, v));
        LootConfig.save();
    }

    public void setClickDelay(int v) {
        this.clickDelay = Math.max(0, Math.min(200, v));
        LootConfig.save();
    }

    public void setContainerDelay(int v) {
        this.containerDelay = Math.max(0, Math.min(2000, v));
        LootConfig.save();
    }

    public void setDropOverflow(boolean v) {
        this.dropOverflow = v;
        LootConfig.save();
    }

    public void setAutoCloseGUI(boolean v) {
        this.autoCloseGUI = v;
        LootConfig.save();
    }

    public void setScanRefreshInterval(int v) {
        this.scanRefreshInterval = Math.max(5, Math.min(200, v));
        LootConfig.save();
    }

    public void setHotkeyKey(int v) {
        this.hotkeyKey = v;
        LootConfig.save();
    }

    public void setHotkeyName(String v) {
        this.hotkeyName = v != null ? v : "";
        LootConfig.save();
    }
}

