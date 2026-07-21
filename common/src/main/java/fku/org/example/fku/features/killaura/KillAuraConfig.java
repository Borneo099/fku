package fku.org.example.fku.features.killaura;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

public class KillAuraConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static KillAuraConfig instance;
    public boolean enabled = false;
    public double range = 4.5;
    public int delay = 2;
    public boolean autoSwitch = true;
    public boolean autoRotate = true;
    public boolean playersOnly = false;
    public int targetMode = 0;
    public boolean attackCooldown = false;
    public boolean multiTarget = false;
    public List<String> whitelist = new ArrayList<String>();

    private static File getConfigFile() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.gameDirectory != null) {
                return new File(new File(mc.gameDirectory, "fku"), "kill_aura.json");
            }
        }
        catch (Exception exception) {
            // ignored
        }
        return new File(Paths.get("config", new String[0]).toAbsolutePath().normalize().getParent().toFile(), "fku/kill_aura.json");
    }

    public static KillAuraConfig getInstance() {
        if (instance == null) {
            KillAuraConfig.load();
        }
        return instance;
    }

    public static void load() {
        File f = KillAuraConfig.getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f);){
                instance = (KillAuraConfig)GSON.fromJson(r, KillAuraConfig.class);
            }
            catch (Exception e) {
                instance = new KillAuraConfig();
            }
        } else {
            instance = new KillAuraConfig();
            KillAuraConfig.save();
        }
        if (KillAuraConfig.instance.whitelist == null) {
            KillAuraConfig.instance.whitelist = new ArrayList<String>();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        KillAuraConfig.getConfigFile().getParentFile().mkdirs();
        try (FileWriter w = new FileWriter(KillAuraConfig.getConfigFile());){
            GSON.toJson(instance, w);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        KillAuraConfig.save();
    }

    public void setRange(double v) {
        this.range = Math.max(1.0, Math.min(10.0, v));
        KillAuraConfig.save();
    }

    public void setDelay(int v) {
        this.delay = Math.max(0, Math.min(10, v));
        KillAuraConfig.save();
    }

    public void setAutoSwitch(boolean v) {
        this.autoSwitch = v;
        KillAuraConfig.save();
    }

    public void setAutoRotate(boolean v) {
        this.autoRotate = v;
        KillAuraConfig.save();
    }

    public void setPlayersOnly(boolean v) {
        this.playersOnly = v;
        KillAuraConfig.save();
    }

    public void setTargetMode(int v) {
        this.targetMode = Math.max(0, Math.min(1, v));
        KillAuraConfig.save();
    }

    public void setAttackCooldown(boolean v) {
        this.attackCooldown = v;
        KillAuraConfig.save();
    }

    public void setMultiTarget(boolean v) {
        this.multiTarget = v;
        KillAuraConfig.save();
    }
}

