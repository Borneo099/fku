package fku.org.example.fku.features.criticals; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Paths;

/**
 * 刀刀暴击配置 — 借鉴 Wurst 的 Criticals
 */
public class CriticalsConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = getConfigFile();
    private static CriticalsConfig instance;

    public boolean enabled = false;
    public String mode = "PACKET";   // PACKET / MINI_JUMP / JITTER
    public boolean silentSave = true; // 静默保存：开关状态/配置静默持久化（关闭时切换会弹提示）

    public static CriticalsConfig getInstance() {
        if (instance == null) instance = load();
        return instance;
    }

    public static CriticalsConfig load() {
        CriticalsConfig cfg = new CriticalsConfig();
        try {
            if (FILE.exists()) {
                JsonObject obj = GSON.fromJson(new FileReader(FILE), JsonObject.class);
                if (obj != null) {
                    if (obj.has("enabled")) cfg.enabled = obj.get("enabled").getAsBoolean();
                    if (obj.has("mode")) cfg.mode = obj.get("mode").getAsString();
                    if (obj.has("silentSave")) cfg.silentSave = obj.get("silentSave").getAsBoolean();
                }
            }
        } catch (Exception ignored) {}
        instance = cfg;
        return cfg;
    }

    public void saveConfig() {
        try {
            if (!FILE.getParentFile().exists()) FILE.getParentFile().mkdirs();
            JsonObject obj = new JsonObject();
            obj.addProperty("enabled", enabled);
            obj.addProperty("mode", mode);
            obj.addProperty("silentSave", silentSave);
            try (FileWriter w = new FileWriter(FILE)) {
                GSON.toJson(obj, w);
            }
        } catch (Exception ignored) {}
    }

    private static File getConfigFile() {
        File dir = new File(getGameDirectory(), "fku");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "criticals.json");
    }

    private static File getGameDirectory() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get(".").toAbsolutePath().normalize().toFile();
    }
}
