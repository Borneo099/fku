package fku.org.example.fku.features.killfx; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * KillFX（击杀特效）配置类
 * 配置持久化使用JSON，遵循FKU现有配置模式（参考HealthTagConfig）
 */
public class KillFXConfig {
    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "killfx.json");
    }
    
    private static File getGameDirectory() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null) return mc.gameDirectory;
        } catch (Exception ignored) {}
        return Paths.get(".").toAbsolutePath().normalize().toFile();
    }
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ===== 通用设置 =====
    /** 功能总开关 */
    public boolean enabled = false;
    /** 仅限攻击目标：只有玩家攻击过的生物死亡时才触发特效 */
    public boolean onlyTargeted = true;
    /** 记忆时间（秒）：记住攻击目标的秒数 */
    public double targetTimeout = 3.5;

    // ===== 闪电设置 =====
    /** 启用闪电 */
    public boolean useLightning = true;
    /** 闪电数量 */
    public int lightningAmount = 1;
    /** 闪电音效（关闭后仅保留视觉闪光，不播放雷声） */
    public boolean useLightningSound = true;

    // ===== 粒子设置 =====
    /** 启用粒子 */
    public boolean useParticles = true;
    /** 粒子分类: Combat, Magic, Fire, Nature, Update121, Misc */
    public String particleCategory = "Magic";
    /** 具体粒子类型字符串（如 "END_ROD"），按分类分别存储 */
    public String combatParticle = "CRIT";
    public String magicParticle = "END_ROD";
    public String fireParticle = "FLAME";
    public String natureParticle = "HEART";
    public String updateParticle = "DRAGON_BREATH";
    public String miscParticle = "SCULK_SOUL";
    /** 粒子形状: Burst, Sphere, Spiral, Column, Halo, Heart, Helix, Star, Ring */
    public String particleShape = "Burst";
    /** 粒子数量 */
    public int particleCount = 40;
    /** 粒子速度 */
    public double particleSpeed = 0.2;

    // ===== 音效设置 =====
    /** 启用声音 */
    public boolean useSound = true;
    /** 音效分类: Combat, Magic, Creature, Fun */
    public String soundGroup = "Combat";
    /** 具体音效字符串 */
    public String combatSound = "THUNDER";
    public String magicSound = "ANCHOR_CHARGE";
    public String creatureSound = "WARDEN";
    public String funSound = "PLING";
    /** 音量 */
    public double volume = 1.0;
    /** 音调 */
    public double pitch = 1.0;

    // ===== 额外视觉 =====
    /** 生成烟花 */
    public boolean useFirework = false;
    /** 生成爆炸烟雾 */
    public boolean useExplosion = false;

    private static KillFXConfig instance;

    public static KillFXConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, KillFXConfig.class);
            } catch (IOException e) {
                instance = new KillFXConfig();
            }
        } else {
            instance = new KillFXConfig();
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