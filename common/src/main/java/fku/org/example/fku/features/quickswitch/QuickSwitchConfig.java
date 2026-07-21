package fku.org.example.fku.features.quickswitch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class QuickSwitchConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static QuickSwitchConfig instance;
    public boolean enabled = false;
    public String mode = "OFF";
    public String customItems = "minecraft:diamond_sword,minecraft:diamond_axe,minecraft:mace";
    public boolean visualFeedback = true;
    public int[] prioritySlots = new int[]{1, 2, 3};
    public int rttDelay = 80;

    private QuickSwitchConfig() {
    }

    private static File getConfigFile() {
        File configDir = new File(QuickSwitchConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "quickswitch.json");
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

    public static QuickSwitchConfig getInstance() {
        if (instance == null) {
            QuickSwitchConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = QuickSwitchConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (QuickSwitchConfig)GSON.fromJson(reader, QuickSwitchConfig.class);
            }
            catch (IOException e) {
                instance = new QuickSwitchConfig();
            }
        } else {
            instance = new QuickSwitchConfig();
            QuickSwitchConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        File configFile = QuickSwitchConfig.getConfigFile();
        try (FileWriter writer = new FileWriter(configFile);){
            GSON.toJson(instance, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        QuickSwitchConfig.save();
    }

    public boolean isActiveMode() {
        return !"OFF".equals(this.mode);
    }

    public static String[] getAvailableModes() {
        return new String[]{"OFF", "SMART", "CUSTOM"};
    }
}

