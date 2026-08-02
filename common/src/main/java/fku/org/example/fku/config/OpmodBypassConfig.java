package fku.org.example.fku.config; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * OpMod 绕过功能配置 — 模组目录路径
 *
 * 默认指向 D 盘，用户可修改为其他盘符（如 C 盘）。
 * 该功能由赛博教员实现
 */
public class OpmodBypassConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static OpmodBypassConfig instance;

    /** 目录1：服务器自动安装的模组（cache） */
    public String modsDir1 = "D:\\MCLDownload\\cache\\game\\V_1_20\\mods";

    /** 目录2：客户端本地模组（.minecraft） */
    public String modsDir2 = "D:\\MCLDownload\\Game\\.minecraft\\mods";

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "opmod_bypass.json");
    }

    private static File getGameDirectory() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                return mc.gameDirectory;
            }
        } catch (Exception ignored) {}
        return new File(".");
    }

    public static OpmodBypassConfig getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, OpmodBypassConfig.class);
            } catch (IOException e) {
                instance = new OpmodBypassConfig();
            }
        } else {
            instance = new OpmodBypassConfig();
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
}