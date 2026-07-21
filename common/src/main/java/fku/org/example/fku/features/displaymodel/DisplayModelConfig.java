package fku.org.example.fku.features.displaymodel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

public class DisplayModelConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static DisplayModelConfig instance;
    public double placeDelay = 50.0;
    public double generationDelay = 50.0;
    public double entitySpacing = 0.5;
    public double placeX = 0.0;
    public double placeY = 0.0;
    public double placeZ = 0.0;
    public double viewRange = 0.0;
    public List<String> commandLines = new ArrayList<String>();
    public int guiX = -1;
    public int guiY = -1;

    private static File getConfigFile() {
        File configDir = new File(DisplayModelConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "display_model.json");
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

    public static DisplayModelConfig getInstance() {
        if (instance == null) {
            DisplayModelConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = DisplayModelConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (DisplayModelConfig)GSON.fromJson(reader, DisplayModelConfig.class);
            }
            catch (IOException e) {
                instance = new DisplayModelConfig();
            }
        } else {
            instance = new DisplayModelConfig();
            DisplayModelConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        try (FileWriter writer = new FileWriter(DisplayModelConfig.getConfigFile());){
            GSON.toJson(instance, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getPresetsDir() {
        File dir = new File(DisplayModelConfig.getGameDirectory(), "fku/display_presets");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static void savePreset(String name, List<String> commands) {
        File file = new File(DisplayModelConfig.getPresetsDir(), name + ".json");
        try (FileWriter w = new FileWriter(file);){
            GSON.toJson(commands, w);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> loadPreset(String name) {
        List list;
        File file = new File(DisplayModelConfig.getPresetsDir(), name + ".json");
        if (!file.exists()) {
            return new ArrayList<String>();
        }
        FileReader r = new FileReader(file);
        try {
            Type type = new TypeToken<List<String>>(){}.getType();
            List cmds = (List)GSON.fromJson(r, type);
            list = cmds != null ? cmds : new ArrayList();
        }
        catch (Throwable throwable) {
            try {
                try {
                    r.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException e) {
                return new ArrayList<String>();
            }
        }
        r.close();
        return list;
    }

    public static String[] listPresets() {
        File[] files = DisplayModelConfig.getPresetsDir().listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            return new String[0];
        }
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; ++i) {
            names[i] = files[i].getName().replaceAll("\\.json$", "");
        }
        return names;
    }

    public static void deletePreset(String name) {
        new File(DisplayModelConfig.getPresetsDir(), name + ".json").delete();
    }

    public void setPlaceDelay(double value) {
        this.placeDelay = Math.max(0.0, Math.min(5000.0, value));
        DisplayModelConfig.save();
    }

    public void setGenerationDelay(double value) {
        this.generationDelay = Math.max(0.0, Math.min(5000.0, value));
        DisplayModelConfig.save();
    }

    public void setEntitySpacing(double value) {
        this.entitySpacing = Math.max(0.0, Math.min(10.0, value));
        DisplayModelConfig.save();
    }

    public void setPlaceX(double value) {
        this.placeX = value;
        DisplayModelConfig.save();
    }

    public void setPlaceY(double value) {
        this.placeY = value;
        DisplayModelConfig.save();
    }

    public void setPlaceZ(double value) {
        this.placeZ = value;
        DisplayModelConfig.save();
    }

    public void setViewRange(double value) {
        this.viewRange = Math.max(0.0, Math.min(10000.0, value));
        DisplayModelConfig.save();
    }
}

