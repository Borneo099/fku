package fku.org.example.fku.features.antipush;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class AntiPushConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static AntiPushConfig instance;
    public boolean enabled = false;

    private static File getConfigFile() {
        File configDir = new File(AntiPushConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "antipush.json");
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

    public static AntiPushConfig getInstance() {
        if (instance == null) {
            AntiPushConfig.load();
        }
        return instance;
    }

    public static void load() {
        File f = AntiPushConfig.getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f);){
                instance = (AntiPushConfig)GSON.fromJson(r, AntiPushConfig.class);
            }
            catch (IOException e) {
                instance = new AntiPushConfig();
            }
        } else {
            instance = new AntiPushConfig();
            AntiPushConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        try (FileWriter w = new FileWriter(AntiPushConfig.getConfigFile());){
            GSON.toJson(instance, w);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        AntiPushConfig.save();
    }
}

