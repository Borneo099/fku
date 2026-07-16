package fku.org.example.fku.features.duplicator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class DuplicatorConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static DuplicatorConfig instance;

    public int dupeDelay = 5;
    public int holdDuration = 10;
    public boolean enableTrident = true;
    public boolean dropTridents = true;
    public boolean durabilityManagement = true;
    /** 绕过 Grim V3 反作弊检测 */
    public boolean bypassGrim = false;
    /** 受伤自动关闭功能 */
    public boolean autoCloseOnDamage = true;
    /** 自动清理背包中过多的三叉戟 */
    public boolean autoCleanInventory = true;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "duplicator.json");
    }

    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }

    public static DuplicatorConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File f = getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f)) { instance = GSON.fromJson(r, DuplicatorConfig.class); }
            catch (IOException e) { instance = new DuplicatorConfig(); }
        } else { instance = new DuplicatorConfig(); save(); }
    }

    public static void save() {
        if (instance == null) return;
        try (FileWriter w = new FileWriter(getConfigFile())) { GSON.toJson(instance, w); }
        catch (IOException e) { e.printStackTrace(); }
    }

    // setters
    public void setDupeDelay(int v) { this.dupeDelay = Math.max(1, Math.min(200, v)); save(); }
    public void setHoldDuration(int v) { this.holdDuration = Math.max(1, Math.min(200, v)); save(); }
    public void setDropTridents(boolean v) { this.dropTridents = v; save(); }
    public void setDurabilityManagement(boolean v) { this.durabilityManagement = v; save(); }
    public void setBypassGrim(boolean v) { this.bypassGrim = v; save(); }
    public void setAutoCloseOnDamage(boolean v) { this.autoCloseOnDamage = v; save(); }
    public void setAutoCleanInventory(boolean v) { this.autoCleanInventory = v; save(); }
}
