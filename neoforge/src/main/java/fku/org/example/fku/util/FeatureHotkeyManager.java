package fku.org.example.fku.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * 全局功能热键管理器
 * <p>
 * 无内置配置的功能 → 存储在 feature_hotkeys.json
 * 有内置配置的功能（如 TpAura、Loot、SelfDamage）→ 代理到其自身的 Config 类
 */
public class FeatureHotkeyManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static FeatureHotkeyManager instance;
    private Map<String, HotkeyEntry> hotkeys = new HashMap<>();

    /** 关联到外部 Config 的代理（transient → GSON 跳过，只由 linkConfig() 设置） */
    private transient final Map<String, LinkedConfig> linkedConfigs = new HashMap<>();

    private static class HotkeyEntry {
        int key = -1;
        String name = "";
    }

    /** 外部 Config 代理描述 */
    public record LinkedConfig(IntSupplier keyGetter, IntConsumer keySetter,
                                Supplier<String> nameGetter, java.util.function.Consumer<String> nameSetter,
                                Runnable saver) {}

    private FeatureHotkeyManager() {}

    /** 注册一个代理：该功能的热键由外部 Config 管理 */
    public static void linkConfig(String featureName, LinkedConfig cfg) {
        getInstance().linkedConfigs.put(featureName, cfg);
    }

    private static File getConfigFile() {
        File dir = new File(getGameDir(), "fku");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "feature_hotkeys.json");
    }

    private static File getGameDir() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get(".").toAbsolutePath().normalize().toFile();
    }

    public static FeatureHotkeyManager getInstance() {
        if (instance == null) { instance = new FeatureHotkeyManager(); instance.load(); }
        return instance;
    }

    private void load() {
        File f = getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f)) {
                FeatureHotkeyManager loaded = GSON.fromJson(r, FeatureHotkeyManager.class);
                if (loaded != null) this.hotkeys = loaded.hotkeys;
            } catch (IOException ignored) {}
        }
    }

    public void save() {
        try (FileWriter w = new FileWriter(getConfigFile())) { GSON.toJson(this, w); }
        catch (IOException e) { e.printStackTrace(); }
    }

    /** 获取某个功能的热键配置 */
    public IHotkeyInterface getHotkey(String featureName) {
        LinkedConfig linked = linkedConfigs.get(featureName);
        if (linked != null) return new LinkedHotkey(linked);
        return new IHotkey(featureName);
    }

    // ───────── 内置存储热键 ─────────

    public class IHotkey implements IHotkeyInterface {
        private final String featureName;
        IHotkey(String name) { this.featureName = name; }
        public int getHotkeyKey() { HotkeyEntry e = hotkeys.get(featureName); return e != null ? e.key : -1; }
        public String getHotkeyName() { HotkeyEntry e = hotkeys.get(featureName); return e != null ? e.name : ""; }
        public void setHotkeyKey(int key) { hotkeys.computeIfAbsent(featureName, k -> new HotkeyEntry()).key = key; }
        public void setHotkeyName(String name) { hotkeys.computeIfAbsent(featureName, k -> new HotkeyEntry()).name = name; }
        public void saveConfig() { save(); }
    }

    // ───────── 代理到外部 Config ─────────

    public class LinkedHotkey implements IHotkeyInterface {
        private final LinkedConfig cfg;
        LinkedHotkey(LinkedConfig cfg) { this.cfg = cfg; }
        public int getHotkeyKey() { return cfg.keyGetter().getAsInt(); }
        public String getHotkeyName() { return cfg.nameGetter().get(); }
        public void setHotkeyKey(int key) { cfg.keySetter().accept(key); }
        public void setHotkeyName(String name) { cfg.nameSetter().accept(name); }
        public void saveConfig() { cfg.saver().run(); }
    }

    public interface IHotkeyInterface {
        int getHotkeyKey();
        String getHotkeyName();
        void setHotkeyKey(int key);
        void setHotkeyName(String name);
        void saveConfig();
    }
}
