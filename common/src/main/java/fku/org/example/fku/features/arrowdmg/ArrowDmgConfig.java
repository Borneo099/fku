package fku.org.example.fku.features.arrowdmg;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class ArrowDmgConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ArrowDmgConfig instance;
    public boolean enabled = false;
    public double packets = 50.0;
    public boolean useOffset = true;
    public boolean yeetTridents = false;
    public boolean arrowDmgFly = true;
    public boolean vClip = true;
    public double expandHitbox = 0.5;
    public boolean yCalibrate = false;
    public boolean autoCrouch = false;
    public boolean autoShoot = false;
    public int charge = 4;
    public boolean onlyWhenHoldingRightClick = true;
    public boolean totemBypass = false;
    public double bypassStrength = 20.0;
    public int bypassDelay = 4;
    public boolean aimbot = false;
    public String priority = "Angle";
    public double aimRange = 40.0;
    public boolean aimOnlyWhenHoldingRightClick = true;
    public boolean ignoreWalls = true;
    public String entities = "PLAYER";
    public boolean renderEnabled = true;
    public int renderMaxDistance = 0;
    public boolean showBox = true;
    public int boxColor = -65536;
    public String customBowIds = "";

    private static File getConfigFile() {
        File configDir = new File(ArrowDmgConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "arrowdmg.json");
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

    public static ArrowDmgConfig getInstance() {
        if (instance == null) {
            ArrowDmgConfig.load();
        }
        return instance;
    }

    public static void load() {
        File f = ArrowDmgConfig.getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f);){
                instance = (ArrowDmgConfig)GSON.fromJson(r, ArrowDmgConfig.class);
            }
            catch (IOException e) {
                instance = new ArrowDmgConfig();
            }
        } else {
            instance = new ArrowDmgConfig();
            ArrowDmgConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        try (FileWriter w = new FileWriter(ArrowDmgConfig.getConfigFile());){
            GSON.toJson(instance, w);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        ArrowDmgConfig.save();
    }
}

