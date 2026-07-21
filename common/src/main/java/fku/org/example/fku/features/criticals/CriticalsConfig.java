package fku.org.example.fku.features.criticals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import net.minecraftforge.fml.loading.FMLPaths;

public class CriticalsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = FMLPaths.CONFIGDIR.get().resolve("fku_criticals.json").toFile();
    private static CriticalsConfig instance;
    public boolean enabled = false;
    public String mode = "PACKET";
    public boolean silentSave = true;

    public static CriticalsConfig getInstance() {
        if (instance == null) {
            instance = CriticalsConfig.load();
        }
        return instance;
    }

    public static CriticalsConfig load() {
        CriticalsConfig cfg = new CriticalsConfig();
        try {
            JsonObject obj;
            if (FILE.exists() && (obj = (JsonObject)GSON.fromJson(new FileReader(FILE), JsonObject.class)) != null) {
                if (obj.has("enabled")) {
                    cfg.enabled = obj.get("enabled").getAsBoolean();
                }
                if (obj.has("mode")) {
                    cfg.mode = obj.get("mode").getAsString();
                }
                if (obj.has("silentSave")) {
                    cfg.silentSave = obj.get("silentSave").getAsBoolean();
                }
            }
        }
        catch (Exception exception) {
            // ignored
        }
        instance = cfg;
        return cfg;
    }

    public void saveConfig() {
        try {
            if (!FILE.getParentFile().exists()) {
                FILE.getParentFile().mkdirs();
            }
            JsonObject obj = new JsonObject();
            obj.addProperty("enabled", Boolean.valueOf(this.enabled));
            obj.addProperty("mode", this.mode);
            obj.addProperty("silentSave", Boolean.valueOf(this.silentSave));
            try (FileWriter w = new FileWriter(FILE);){
                GSON.toJson((JsonElement)obj, w);
            }
        }
        catch (Exception exception) {
            // ignored
        }
    }
}

