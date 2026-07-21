package fku.org.example.fku.features.teleport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class TeleportConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static TeleportConfig instance;
    public boolean enabled = true;
    public double maxDistance = 100.0;
    public boolean snapToBlock = true;

    private static File getConfigFile() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.gameDirectory != null) {
                return new File(new File(mc.gameDirectory, "fku"), "teleport.json");
            }
        }
        catch (Exception exception) {
            // ignored
        }
        return new File(Paths.get("config", new String[0]).toAbsolutePath().normalize().getParent().toFile(), "fku/teleport.json");
    }

    public static TeleportConfig getInstance() {
        if (instance == null) {
            TeleportConfig.load();
        }
        return instance;
    }

    public static void load() {
        File f = TeleportConfig.getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f);){
                instance = (TeleportConfig)GSON.fromJson(r, TeleportConfig.class);
            }
            catch (Exception e) {
                instance = new TeleportConfig();
            }
        } else {
            instance = new TeleportConfig();
            TeleportConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        TeleportConfig.getConfigFile().getParentFile().mkdirs();
        try (FileWriter w = new FileWriter(TeleportConfig.getConfigFile());){
            GSON.toJson(instance, w);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        TeleportConfig.save();
    }

    public void setMaxDistance(double v) {
        this.maxDistance = Math.max(10.0, Math.min(500.0, v));
        TeleportConfig.save();
    }

    public void setSnapToBlock(boolean v) {
        this.snapToBlock = v;
        TeleportConfig.save();
    }
}

