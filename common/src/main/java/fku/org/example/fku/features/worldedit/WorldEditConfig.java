package fku.org.example.fku.features.worldedit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class WorldEditConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static WorldEditConfig instance;
    public boolean enabled = false;
    public String toolItem = "minecraft:wooden_axe";
    public int rangeMultiplier = 114514;
    public int maxPacketsPerTick = 50;
    public int maxUndoSteps = 50;
    public boolean renderSelection = true;
    public String selectionColor = "#00FF00";
    public boolean autoRestoreSlot = true;
    public boolean safeMode = true;
    public boolean enableClipboard = true;
    public String schematicsFolder = "fku/schematics";
    public transient boolean taskRunning = false;
    public transient String taskStatus = "";

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
        return new File(configDir, "worldedit.json");
    }

    public static WorldEditConfig getInstance() {
        if (instance == null) {
            WorldEditConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = WorldEditConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (WorldEditConfig)GSON.fromJson(reader, WorldEditConfig.class);
            }
            catch (IOException e) {
                instance = new WorldEditConfig();
            }
        } else {
            instance = new WorldEditConfig();
            WorldEditConfig.save();
        }
        WorldEditConfig.instance.taskRunning = false;
        WorldEditConfig.instance.taskStatus = "";
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        File configFile = WorldEditConfig.getConfigFile();
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
            this.taskRunning = false;
            this.taskStatus = "";
        }
        WorldEditConfig.save();
    }

    public void setToolItem(String v) {
        this.toolItem = v != null ? v : "minecraft:wooden_axe";
        WorldEditConfig.save();
    }

    public void setRangeMultiplier(int v) {
        this.rangeMultiplier = Math.max(1, Math.min(999999, v));
        WorldEditConfig.save();
    }

    public void setMaxPacketsPerTick(int v) {
        this.maxPacketsPerTick = Math.max(1, Math.min(5000, v));
        WorldEditConfig.save();
    }

    public void setMaxUndoSteps(int v) {
        this.maxUndoSteps = Math.max(1, Math.min(500, v));
        WorldEditConfig.save();
    }

    public void setRenderSelection(boolean v) {
        this.renderSelection = v;
        WorldEditConfig.save();
    }

    public void setSelectionColor(String v) {
        this.selectionColor = v != null ? v : "#00FF00";
        WorldEditConfig.save();
    }

    public void setAutoRestoreSlot(boolean v) {
        this.autoRestoreSlot = v;
        WorldEditConfig.save();
    }

    public void setSafeMode(boolean v) {
        this.safeMode = v;
        WorldEditConfig.save();
    }

    public void setEnableClipboard(boolean v) {
        this.enableClipboard = v;
        WorldEditConfig.save();
    }

    public void setSchematicsFolder(String v) {
        this.schematicsFolder = v != null ? v : "fku/schematics";
        WorldEditConfig.save();
    }
}

