package fku.org.example.fku.features.bedrockbreaker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class BedrockBreakerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static BedrockBreakerConfig instance;
    public static final String DEFAULT_HELPER_BLOCK_LIST = "minecraft:cobbled_deepslate,minecraft:andesite,minecraft:granite,minecraft:diorite,minecraft:netherrack,minecraft:tuff,minecraft:sandstone,minecraft:cobblestone,minecraft:dirt";
    public boolean allBlocks = false;
    public boolean enabled = false;
    public String targetBlockId = "minecraft:bedrock";
    public String replaceBlockId = "minecraft:diamond_block";
    public boolean scanMode = false;
    public int autoFindRange = 0;
    public int breakTimeout = 50;
    public int extendTimeout = 10;
    public int leverBreakTimeout = 30;
    public String triggerKey = "";
    public boolean enableHelperBlocks = true;
    public String helperBlockList = "minecraft:cobbled_deepslate,minecraft:andesite,minecraft:granite,minecraft:diorite,minecraft:netherrack,minecraft:tuff,minecraft:sandstone,minecraft:cobblestone,minecraft:dirt";
    public boolean cleanupHelpers = true;
    public String pistonDirectionPriority = "HORIZONTAL_FIRST";
    public int ghostCleanupInterval = 1;

    private static File getConfigFile() {
        File configDir = new File(BedrockBreakerConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "bedrock_breaker.json");
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

    public static BedrockBreakerConfig getInstance() {
        if (instance == null) {
            BedrockBreakerConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = BedrockBreakerConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (BedrockBreakerConfig)GSON.fromJson(reader, BedrockBreakerConfig.class);
            }
            catch (IOException e) {
                instance = new BedrockBreakerConfig();
            }
        } else {
            instance = new BedrockBreakerConfig();
            BedrockBreakerConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        File configFile = BedrockBreakerConfig.getConfigFile();
        try (FileWriter writer = new FileWriter(configFile);){
            GSON.toJson(instance, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        BedrockBreakerConfig.save();
    }

    public void setTargetBlockId(String v) {
        this.targetBlockId = v != null ? v : "minecraft:bedrock";
        BedrockBreakerConfig.save();
    }

    public void setReplaceBlockId(String v) {
        this.replaceBlockId = v;
        BedrockBreakerConfig.save();
    }

    public void setScanMode(boolean v) {
        this.scanMode = v;
        BedrockBreakerConfig.save();
    }

    public void setAllBlocks(boolean v) {
        this.allBlocks = v;
        BedrockBreakerConfig.save();
    }

    public void setAutoFindRange(int v) {
        this.autoFindRange = Math.max(0, Math.min(10, v));
        BedrockBreakerConfig.save();
    }

    public void setBreakTimeout(int v) {
        this.breakTimeout = Math.max(10, Math.min(200, v));
        BedrockBreakerConfig.save();
    }

    public void setExtendTimeout(int v) {
        this.extendTimeout = Math.max(5, Math.min(50, v));
        BedrockBreakerConfig.save();
    }

    public void setLeverBreakTimeout(int v) {
        this.leverBreakTimeout = Math.max(5, Math.min(100, v));
        BedrockBreakerConfig.save();
    }

    public void setTriggerKey(String v) {
        this.triggerKey = v != null ? v : "";
        BedrockBreakerConfig.save();
    }

    public void setEnableHelperBlocks(boolean v) {
        this.enableHelperBlocks = v;
        BedrockBreakerConfig.save();
    }

    public void setHelperBlockList(String v) {
        this.helperBlockList = v != null ? v : DEFAULT_HELPER_BLOCK_LIST;
        BedrockBreakerConfig.save();
    }

    public void setCleanupHelpers(boolean v) {
        this.cleanupHelpers = v;
        BedrockBreakerConfig.save();
    }
}

