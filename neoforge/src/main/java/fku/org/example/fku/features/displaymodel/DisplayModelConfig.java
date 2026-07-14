package fku.org.example.fku.features.displaymodel; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 实体模型展示配置类
 * 使用JSON持久化，支持运行时实时修改
 *
 * 设计思想：
 * - 继承GuiStyleConfig的JSON配置模式，保持一致风格
 * - 所有字段自动保存，修改即保存
 * - 放置坐标 placeX/Y/Z：若全部为0，运行时使用玩家当前位置
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

    /** 实体可视距离（0=使用默认值），对应 EntityTag.view_range NBT */
    public double viewRange = 0.0;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "display_model.json");
    }

    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) {
                return mc.gameDirectory;
            }
        } catch (Exception e) {
            // 初始化失败时回退到当前目录
        }
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }

    public static DisplayModelConfig getInstance() {
        if (instance == null) {
            load();
        }
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
        File configFile = getConfigFile();
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ============ Setter方法（自动保存） ============

    public void setPlaceDelay(double value) {
        this.placeDelay = Math.max(0, Math.min(5000, value));
        save();
    }

    public void setGenerationDelay(double value) {
        this.generationDelay = Math.max(0, Math.min(5000, value));
        save();
    }

    public void setEntitySpacing(double value) {
        this.entitySpacing = Math.max(0, Math.min(10, value));
        save();
    }

    public void setPlaceX(double value) {
        this.placeX = value;
        save();
    }

    public void setPlaceY(double value) {
        this.placeY = value;
        save();
    }

    public void setPlaceZ(double value) {
        this.placeZ = value;
        save();
    }

    public void setViewRange(double value) {
        this.viewRange = Math.max(0, Math.min(10000, value));
        save();
    }
}