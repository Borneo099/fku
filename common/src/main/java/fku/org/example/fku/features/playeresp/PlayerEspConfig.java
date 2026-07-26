package fku.org.example.fku.features.playeresp; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 玩家ESP功能配置类（JSON 持久化）
 * 移植自 Lexis PlayerEspHack
 * 该功能由赛博教员实现
 */
public class PlayerEspConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static PlayerEspConfig instance;

    public boolean enabled = false;
    /** 显示模式 */
    public String mode = "ALL"; // BOX_ONLY, LINES_ONLY, SIDES_ONLY, BOX_LINES, BOX_SIDES, LINES_SIDES, ALL
    /** 方框颜色 (ARGB packed int) */
    public int boxColor = 0xB4FF0000;
    /** 连线颜色 (ARGB packed int) */
    public int linesColor = 0xB400FF00;
    /** 六面填充颜色 (ARGB packed int) */
    public int sidesColor = 0xB40000FF;
    /** 使用队伍颜色 */
    public boolean forceTeamColor = false;
    /** 最大显示距离 */
    public int maxDistance = 128;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "player_esp.json");
    }

    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }

    public static PlayerEspConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, PlayerEspConfig.class);
            } catch (IOException e) {
                instance = new PlayerEspConfig();
            }
        } else {
            instance = new PlayerEspConfig();
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

    // Setter 即时保存
    public void setEnabled(boolean v) { this.enabled = v; save(); }
    public void setMode(String v) { this.mode = v; save(); }
    public void setBoxColor(int v) { this.boxColor = v; save(); }
    public void setLinesColor(int v) { this.linesColor = v; save(); }
    public void setSidesColor(int v) { this.sidesColor = v; save(); }
    public void setForceTeamColor(boolean v) { this.forceTeamColor = v; save(); }
    public void setMaxDistance(int v) { this.maxDistance = Math.max(8, Math.min(1024, v)); save(); }
}