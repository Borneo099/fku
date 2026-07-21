package fku.org.example.fku.features.selfdamage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class SelfDamageConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static SelfDamageConfig instance;
    public int damageAmount = 7;
    public int hotkeyKey = -1;
    public String hotkeyName = "";

    private SelfDamageConfig() {
    }

    private static File getConfigFile() {
        File configDir = new File(SelfDamageConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "selfdamage.json");
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

    public static SelfDamageConfig getInstance() {
        if (instance == null) {
            SelfDamageConfig.load();
        }
        return instance;
    }

    public static void load() {
        File f = SelfDamageConfig.getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f);){
                instance = (SelfDamageConfig)GSON.fromJson(r, SelfDamageConfig.class);
            }
            catch (IOException e) {
                instance = new SelfDamageConfig();
            }
        } else {
            instance = new SelfDamageConfig();
            SelfDamageConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        try (FileWriter w = new FileWriter(SelfDamageConfig.getConfigFile());){
            GSON.toJson(instance, w);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

