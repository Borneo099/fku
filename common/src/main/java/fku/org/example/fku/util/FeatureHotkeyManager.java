package fku.org.example.fku.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;

public class FeatureHotkeyManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static FeatureHotkeyManager instance;
    private Map<String, HotkeyEntry> hotkeys = new HashMap<String, HotkeyEntry>();
    private final transient Map<String, LinkedConfig> linkedConfigs = new HashMap<String, LinkedConfig>();

    private FeatureHotkeyManager() {
    }

    public static void linkConfig(String featureName, LinkedConfig cfg) {
        FeatureHotkeyManager.getInstance().linkedConfigs.put(featureName, cfg);
    }

    private static File getConfigFile() {
        File dir = new File(FeatureHotkeyManager.getGameDir(), "fku");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, "feature_hotkeys.json");
    }

    private static File getGameDir() {
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

    public static FeatureHotkeyManager getInstance() {
        if (instance == null) {
            instance = new FeatureHotkeyManager();
            instance.load();
        }
        return instance;
    }

    private void load() {
        File f = FeatureHotkeyManager.getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f);){
                FeatureHotkeyManager loaded = (FeatureHotkeyManager)GSON.fromJson(r, FeatureHotkeyManager.class);
                if (loaded != null) {
                    this.hotkeys = loaded.hotkeys;
                }
            }
            catch (IOException iOException) {
                // ignored
            }
        }
    }

    public void save() {
        try (FileWriter w = new FileWriter(FeatureHotkeyManager.getConfigFile());){
            GSON.toJson(this, w);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public IHotkeyInterface getHotkey(String featureName) {
        LinkedConfig linked = this.linkedConfigs.get(featureName);
        if (linked != null) {
            return new LinkedHotkey(linked);
        }
        return new IHotkey(featureName);
    }

    public record LinkedConfig(IntSupplier keyGetter, IntConsumer keySetter, Supplier<String> nameGetter, Consumer<String> nameSetter, Runnable saver) {
    }

    public class LinkedHotkey
    implements IHotkeyInterface {
        private final LinkedConfig cfg;

        LinkedHotkey(LinkedConfig cfg) {
            this.cfg = cfg;
        }

        @Override
        public int getHotkeyKey() {
            return this.cfg.keyGetter().getAsInt();
        }

        @Override
        public String getHotkeyName() {
            return this.cfg.nameGetter().get();
        }

        @Override
        public void setHotkeyKey(int key) {
            this.cfg.keySetter().accept(key);
        }

        @Override
        public void setHotkeyName(String name) {
            this.cfg.nameSetter().accept(name);
        }

        @Override
        public void saveConfig() {
            this.cfg.saver().run();
        }
    }

    public class IHotkey
    implements IHotkeyInterface {
        private final String featureName;

        IHotkey(String name) {
            this.featureName = name;
        }

        @Override
        public int getHotkeyKey() {
            HotkeyEntry e = FeatureHotkeyManager.this.hotkeys.get(this.featureName);
            return e != null ? e.key : -1;
        }

        @Override
        public String getHotkeyName() {
            HotkeyEntry e = FeatureHotkeyManager.this.hotkeys.get(this.featureName);
            return e != null ? e.name : "";
        }

        @Override
        public void setHotkeyKey(int key) {
            FeatureHotkeyManager.this.hotkeys.computeIfAbsent(this.featureName, k -> new HotkeyEntry()).key = key;
        }

        @Override
        public void setHotkeyName(String name) {
            FeatureHotkeyManager.this.hotkeys.computeIfAbsent(this.featureName, k -> new HotkeyEntry()).name = name;
        }

        @Override
        public void saveConfig() {
            FeatureHotkeyManager.this.save();
        }
    }

    public static interface IHotkeyInterface {
        public int getHotkeyKey();

        public String getHotkeyName();

        public void setHotkeyKey(int var1);

        public void setHotkeyName(String var1);

        public void saveConfig();
    }

    private static class HotkeyEntry {
        int key = -1;
        String name = "";

        private HotkeyEntry() {
        }
    }
}

