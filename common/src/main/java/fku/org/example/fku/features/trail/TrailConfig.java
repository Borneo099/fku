package fku.org.example.fku.features.trail; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

/**
 * 拖尾特效配置类 — 持久化所有拖尾参数
 * 配置文件路径：./config/fku/trail.json
 * 该配置类由赛博教员实现
 */
public class TrailConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ═══════ 分组1：通用设置 ═══════
    public boolean enabled = false;
    /** 拖尾模式：GHOST / PARTICLE / LIGHT_STREAK / ENERGY_RIPPLE / VOID_FISSURE / ELEMENTAL_FOOTPRINT / STARDUST */
    public String trailMode = "PARTICLE";
    /** 触发条件：ALWAYS / SPRINTING / FLYING / JUMPING / COMBAT */
    public String triggerMode = "ALWAYS";
    /** 触发条件失效后拖尾消失的过渡Tick数 */
    public int fadeOutTicks = 10;

    // ═══════ 分组2：残影模式参数 ═══════
    /** 每隔多少Tick生成一个残影 */
    public int ghostInterval = 2;
    /** 最大残影数量 */
    public int ghostMaxCount = 20;
    /** 残影初始透明度 */
    public double ghostAlphaStart = 0.6;
    /** 残影消失透明度 */
    public double ghostAlphaEnd = 0.0;

    // ═══════ 分组3：粒子模式参数 ═══════
    /** 粒子类型：FLAME / DRAGON_BREATH / END_ROD / FIREWORK / PORTAL / SOUL */
    public String particleType = "END_ROD";
    /** 每Tick发射粒子数 */
    public int particlesPerTick = 3;
    /** 粒子存在Tick数 */
    public int particleLifetime = 20;
    /** 粒子初始速度 */
    public double particleSpeed = 0.3;
    /** 粒子扩散范围 */
    public double particleSpread = 0.5;

    // ═══════ 分组4：流光模式参数 ═══════
    /** 最大路径点数 */
    public int streakMaxPoints = 60;
    /** 光轨宽度 */
    public double streakWidth = 0.1;
    /** 曲线平滑插值步数 */
    public int streakSmoothness = 4;
    /** 起点颜色 */
    public String streakColorStart = "00FFAA";
    /** 终点颜色 */
    public String streakColorEnd = "FF00AA";

    // ═══════ 分组5：颜色与外观 ═══════
    /** 主色调 */
    public String mainColor = "00FFAA";
    /** 是否启用颜色渐变 */
    public boolean colorGradient = true;
    /** 次要颜色（渐变终点） */
    public String secondaryColor = "FF00AA";
    /** 发光强度 */
    public double glowIntensity = 0.8;

    // ═══════ 分组6：性能限制 ═══════
    /** 最大粒子数 */
    public int maxParticles = 200;
    /** 最大残影数 */
    public int maxGhosts = 30;
    /** 帧率低于30fps时自动禁用拖尾 */
    public boolean disableInLowFps = true;
    /** 超过此距离的玩家不显示拖尾 */
    public double lodDistance = 32.0;

    private static TrailConfig instance;

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "trail.json");
    }

    private static File getGameDirectory() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                return mc.gameDirectory;
            }
        } catch (Exception e) {
            // 静默降级
        }
        return Paths.get(".").toAbsolutePath().normalize().toFile();
    }

    public static TrailConfig getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, TrailConfig.class);
            } catch (IOException e) {
                instance = new TrailConfig();
            }
        } else {
            instance = new TrailConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(getConfigFile())) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}