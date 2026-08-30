package fku.org.example.fku.features.tacz; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * TaCZ 枪械辅助功能 — 统一配置类（JSON 持久化）
 * 移植自 Lexis 的 TaCZ 系列 Hack
 * 该功能由赛博教员实现
 */
public class TaCZConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static TaCZConfig instance;

    // ★ 主开关（关闭则所有功能停用）
    public boolean masterEnabled = true;

    // 功能开关
    public boolean aimbotEnabled = false;
    public boolean autoReloadEnabled = false;
    public boolean bulletTracersEnabled = false;
    public boolean endlessAimbotEnabled = false;
    public boolean instantAimEnabled = false;
    public boolean noRecoilEnabled = false;
    public boolean noSprintInterruptEnabled = false;
    public boolean sniperFullAutoEnabled = false;
    public boolean fullAutoEnabled = false;

    // ★ 以下三个功能由赛博教员参考 NoSpread 02 版本实现
    public boolean noSpreadEnabled = false;   // 无扩散
    public boolean antiShakeEnabled = false;  // 防抖

    // Aimbot 参数
    public int aimbotCircleSize = 100;
    public float aimbotRotationSpeed = 30.0f;
    public int aimbotCircleColor = 0xFFFF0000;
    public int aimbotLockColor = 0xFF00FF00;
    public int aimbotFovColor = 0x40808080;
    public boolean aimbotOnlyWhenAiming = true;
    public boolean aimbotAllowThroughWalls = false;
    public String aimbotBodyPart = "身体";
    // ★ 自瞄对象选择器：全部实体 / 仅玩家 / 自定义
    public String aimbotTargetMode = "全部实体";
    // 自定义模式下生效：逗号分隔的实体 registry id（如 minecraft:zombie, tacz:xxx）
    public String aimbotCustomEntities = "";

    // BulletTracers 参数
    public int tracerColor = 0xFFFF0000;
    public float tracerLineWidth = 2.0f;
    public int tracerMaxDistance = 128;

    // NoRecoil 参数
    public float recoilReduction = 1.0f;

    // EndlessAimbot 参数
    public float endlessRotationSpeed = 360f;
    public boolean endlessOnlyWhenAiming = true;
    public boolean endlessAllowThroughWalls = false;
    public String endlessBodyPart = "身体";
    public boolean endlessOnlyOnLeftClick = false;

    private static File getConfigFile() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null && mc.gameDirectory != null) {
                File dir = new File(mc.gameDirectory, "fku");
                if (!dir.exists()) dir.mkdirs();
                return new File(dir, "tacz.json");
            }
        } catch (Exception ignored) {}
        File dir = new File(Paths.get("config").toAbsolutePath().normalize().getParent().toFile(), "fku");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "tacz.json");
    }

    public static TaCZConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File f = getConfigFile();
        if (f.exists()) {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
                instance = GSON.fromJson(r, TaCZConfig.class);
            } catch (Exception e) { instance = new TaCZConfig(); }
        } else { instance = new TaCZConfig(); save(); }
    }

    public static void save() {
        if (instance == null) return;
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getConfigFile()), StandardCharsets.UTF_8))) {
            GSON.toJson(instance, w);
        } catch (IOException e) { e.printStackTrace(); }
    }
}