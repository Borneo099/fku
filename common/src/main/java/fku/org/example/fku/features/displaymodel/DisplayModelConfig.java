package fku.org.example.fku.features.displaymodel; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 实体模型展示配置类
 * 使用JSON持久化，支持运行时实时修改
 *
 * 新增：保存指令行、GUI位置、预设系统
 */
public class DisplayModelConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static DisplayModelConfig instance;

    /** 同步等待延迟（毫秒）- 默认50 */
    public double placeDelay = 50.0;
    /** 实体间生成间隔（毫秒）- 默认50 */
    public double generationDelay = 50.0;
    /** 实体间距（格）- 默认0.5 */
    public double entitySpacing = 0.5;
    /** 放置坐标 X（0=使用玩家位置） */
    public double placeX = 0.0;
    /** 放置坐标 Y（0=使用玩家位置） */
    public double placeY = 0.0;
    /** 放置坐标 Z（0=使用玩家位置） */
    public double placeZ = 0.0;
    /** 实体可视距离（0=使用默认值） */
    public double viewRange = 0.0;

    /** ★ 保存的指令行（多行），重启游戏后恢复 */
    public List<String> commandLines = new ArrayList<>();

    /** ★ GUI窗口X位置（-1=居中） */
    public int guiX = -1;
    /** ★ GUI窗口Y位置（-1=居中） */
    public int guiY = -1;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "display_model.json");
    }

    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }

    public static DisplayModelConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, DisplayModelConfig.class);
            } catch (IOException e) {
                instance = new DisplayModelConfig();
            }
        } else {
            instance = new DisplayModelConfig();
            save();
        }
    }

    public static void save() {
        if (instance == null) return;
        try (FileWriter writer = new FileWriter(getConfigFile())) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ════════════ 预设系统 ════════════

    /** 预设文件存储目录 */
    private static File getPresetsDir() {
        File dir = new File(getGameDirectory(), "fku/display_presets");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    /** 保存为预设 */
    public static void savePreset(String name, List<String> commands) {
        File file = new File(getPresetsDir(), name + ".json");
        try (FileWriter w = new FileWriter(file)) {
            GSON.toJson(commands, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 加载预设 */
    public static List<String> loadPreset(String name) {
        File file = new File(getPresetsDir(), name + ".json");
        if (!file.exists()) return new ArrayList<>();
        try (FileReader r = new FileReader(file)) {
            Type type = new TypeToken<List<String>>(){}.getType();
            List<String> cmds = GSON.fromJson(r, type);
            return cmds != null ? cmds : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /** 列出所有预设 */
    public static String[] listPresets() {
        File[] files = getPresetsDir().listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return new String[0];
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = files[i].getName().replaceAll("\\.json$", "");
        }
        return names;
    }

    /** 删除预设 */
    public static void deletePreset(String name) {
        new File(getPresetsDir(), name + ".json").delete();
    }

    // ════════ Setter方法 ════════
    public void setPlaceDelay(double value) { this.placeDelay = Math.max(0, Math.min(5000, value)); save(); }
    public void setGenerationDelay(double value) { this.generationDelay = Math.max(0, Math.min(5000, value)); save(); }
    public void setEntitySpacing(double value) { this.entitySpacing = Math.max(0, Math.min(10, value)); save(); }
    public void setPlaceX(double value) { this.placeX = value; save(); }
    public void setPlaceY(double value) { this.placeY = value; save(); }
    public void setPlaceZ(double value) { this.placeZ = value; save(); }
    public void setViewRange(double value) { this.viewRange = Math.max(0, Math.min(10000, value)); save(); }
}
