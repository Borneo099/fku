package fku.org.example.fku.features.arrowdmg; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * ArrowDmg（32k弓）配置 — JSON 持久化
 */
public class ArrowDmgConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ArrowDmgConfig instance;

    public boolean enabled = false;

    // sgGeneral
    /** 发包数 (1~7000) */
    public double packets = 50;
    public boolean useOffset = true;
    /** 垂直修正：开启后包含 Y 方向偏移（自由角度射击），关闭则仅水平位移（参考 ArrowDmg.java） */
    public boolean vertical = true;
    public boolean yeetTridents = false;
    /** 箭伤飞行：蓄力弓时开启创造飞行，稳定箭矢伤害 */
    public boolean arrowDmgFly = true;
    /** VClip瞬移：射箭前瞬移到目标Y高度，解决高度差问题 */
    public boolean vClip = true;
    /** 命中辅助：放大瞄准框 (0~5)，瞄准时偏移使箭更容易命中 */
    public double expandHitbox = 0.5;
    /** Y校准：蓄力时自动传送Y坐标与目标一致（开启后自动关闭VClip） */
    public boolean yCalibrate = false;
    /** 自动下蹲：目标模型<2格高时自动蹲下降低射击高度，提高命中率 */
    public boolean autoCrouch = false;

    // sgAuto
    public boolean autoShoot = false;
    public int charge = 4;
    public boolean onlyWhenHoldingRightClick = true;

    // sgTotem
    public boolean totemBypass = false;
    public double bypassStrength = 20.0;
    public int bypassDelay = 4;

    // sgAim
    public boolean aimbot = false;
    public String priority = "Angle";
    public double aimRange = 40.0;
    public boolean aimOnlyWhenHoldingRightClick = true;
    public boolean ignoreWalls = true;
    public String entities = "PLAYER";

    // sgRender
    public boolean renderEnabled = true;
    public int renderMaxDistance = 0;
    public boolean showBox = true;
    public int boxColor = 0xFFFF0000;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "arrowdmg.json");
    }
    private static File getGameDirectory() {
        try { Minecraft mc = Minecraft.getInstance(); if (mc != null) return mc.gameDirectory; }
        catch (Exception ignored) {}
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }
    public static ArrowDmgConfig getInstance() { if (instance == null) load(); return instance; }
    public static void load() {
        File f = getConfigFile();
        if (f.exists()) { try (FileReader r = new FileReader(f)) { instance = GSON.fromJson(r, ArrowDmgConfig.class); } catch (IOException e) { instance = new ArrowDmgConfig(); } }
        else { instance = new ArrowDmgConfig(); save(); }
    }
    public static void save() {
        if (instance == null) return;
        try (FileWriter w = new FileWriter(getConfigFile())) { GSON.toJson(instance, w); } catch (IOException e) { e.printStackTrace(); }
    }
    public void setEnabled(boolean v) { this.enabled = v; save(); }
}
