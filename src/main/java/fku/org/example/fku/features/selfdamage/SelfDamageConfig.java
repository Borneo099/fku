package fku.org.example.fku.features.selfdamage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/** 自伤配置（JSON 持久化） */
public class SelfDamageConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static SelfDamageConfig instance;

    public int damageAmount = 7;

    /** GLFW 热键按键码（-1=未设置） */
    public int hotkeyKey = -1;
    /** 热键名称（展示用） */
    public String hotkeyName = "";

    private SelfDamageConfig() {}

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "selfdamage.json");
    }

    private static File getGameDirectory() {
        try { net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance(); if (mc != null) return mc.gameDirectory; } catch (Exception ignored) {}
        return Paths.get(".").toAbsolutePath().normalize().toFile();
    }

    public static SelfDamageConfig getInstance() { if (instance == null) load(); return instance; }

    public static void load() {
        File f = getConfigFile();
        if (f.exists()) { try (FileReader r = new FileReader(f)) { instance = GSON.fromJson(r, SelfDamageConfig.class); } catch (IOException e) { instance = new SelfDamageConfig(); } }
        else { instance = new SelfDamageConfig(); save(); }
    }

    public static void save() { if (instance == null) return; try (FileWriter w = new FileWriter(getConfigFile())) { GSON.toJson(instance, w); } catch (IOException e) { e.printStackTrace(); } }
}
