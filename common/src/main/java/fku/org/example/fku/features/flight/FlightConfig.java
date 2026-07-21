package fku.org.example.fku.features.flight;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class FlightConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static FlightConfig instance;
    public boolean enabled = false;
    public double flySpeed = 0.1;
    public double verticalSpeed = 0.1;
    public int doubleTapWindow = 500;
    public boolean disableCollision = false;
    public boolean onlyInCreative = false;
    public boolean consumeHunger = false;
    public int hungerCost = 1;
    public boolean allowSprint = true;
    public boolean smoothAcceleration = true;
    public boolean particleEffect = true;
    public boolean soundFeedback = true;
    public boolean antiKick = true;
    public int antiKickInterval = 70;
    public double antiKickDistance = 0.07;

    private static File getConfigFile() {
        File configDir = new File(FlightConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "flight.json");
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

    public static FlightConfig getInstance() {
        if (instance == null) {
            FlightConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = FlightConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (FlightConfig)GSON.fromJson(reader, FlightConfig.class);
            }
            catch (IOException e) {
                instance = new FlightConfig();
            }
        } else {
            instance = new FlightConfig();
            FlightConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        File configFile = FlightConfig.getConfigFile();
        try (FileWriter writer = new FileWriter(configFile);){
            GSON.toJson(instance, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        FlightConfig.save();
    }

    public void setFlySpeed(double v) {
        this.flySpeed = Math.max(0.01, Math.min(2.0, v));
        FlightConfig.save();
    }

    public void setVerticalSpeed(double v) {
        this.verticalSpeed = Math.max(0.01, Math.min(2.0, v));
        FlightConfig.save();
    }

    public void setDoubleTapWindow(int v) {
        this.doubleTapWindow = Math.max(100, Math.min(2000, v));
        FlightConfig.save();
    }

    public void setDisableCollision(boolean v) {
        this.disableCollision = v;
        FlightConfig.save();
    }

    public void setOnlyInCreative(boolean v) {
        this.onlyInCreative = v;
        FlightConfig.save();
    }

    public void setConsumeHunger(boolean v) {
        this.consumeHunger = v;
        FlightConfig.save();
    }

    public void setHungerCost(int v) {
        this.hungerCost = Math.max(1, Math.min(20, v));
        FlightConfig.save();
    }

    public void setAllowSprint(boolean v) {
        this.allowSprint = v;
        FlightConfig.save();
    }

    public void setSmoothAcceleration(boolean v) {
        this.smoothAcceleration = v;
        FlightConfig.save();
    }

    public void setParticleEffect(boolean v) {
        this.particleEffect = v;
        FlightConfig.save();
    }

    public void setSoundFeedback(boolean v) {
        this.soundFeedback = v;
        FlightConfig.save();
    }

    public void setAntiKick(boolean v) {
        this.antiKick = v;
        FlightConfig.save();
    }

    public void setAntiKickInterval(int v) {
        this.antiKickInterval = Math.max(10, Math.min(200, v));
        FlightConfig.save();
    }

    public void setAntiKickDistance(double v) {
        this.antiKickDistance = Math.max(0.01, Math.min(0.5, v));
        FlightConfig.save();
    }
}

