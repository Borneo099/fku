package fku.org.example.fku.features.fakeplayer; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 假人（FakePlayer）配置类
 *
 * ★ 职责：
 *   存储假人功能的所有配置项，持久化到 config/fku/fake_player.json。
 *
 * ★ 配置项说明：
 *   - enabled: 功能是否启用
 *   - name: 假人显示名称
 *   - health: 初始生命值
 *   - copyInv: 复制玩家背包
 *   - simulateDamage: 是否模拟伤害
 *   - invulnerableTicks: 受击后无敌 Tick
 *   - autoTotem: 自动副手图腾
 *   - showDamage: 聊天栏显示伤害
 *   - respawn: 假人死后是否自动重生
 *
 * ★ 参考来源：
 *   AdvancedFakePlayer.java / IMGFakePlayer.java (InvincibleMachineGun)
 */
public class FakePlayerConfig {

    private static File getConfigFile() {
        try {
            Minecraft mc = Minecraft.getInstance();
            File configDir = new File(mc.gameDirectory, "fku");
            if (!configDir.exists()) configDir.mkdirs();
            return new File(configDir, "fake_player.json");
        } catch (Exception ignored) {}
        return Paths.get(".", "config", "fku", "fake_player.json").toAbsolutePath().normalize().toFile();
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ===== 开关 =====
    /** 功能是否启用 */
    public boolean enabled = false;

    // ===== 假人属性 =====
    /** 假人名称 */
    public String name = "FakePlayer";
    /** 初始生命值 */
    public int health = 20;
    /** 复制玩家背包物品 */
    public boolean copyInv = true;

    // ===== 伤害模拟 =====
    /** 是否模拟伤害（攻击/爆炸） */
    public boolean simulateDamage = true;
    /** 受击后无敌 Tick（原版 20） */
    public int invulnerableTicks = 20;
    /** 自动副手图腾 */
    public boolean autoTotem = true;
    /** 聊天栏显示伤害数值 */
    public boolean showDamage = true;

    // ===== 额外 =====
    /** 死亡后自动重生 */
    public boolean respawn = false;

    // ===== 单例 =====
    private static FakePlayerConfig instance;

    public static FakePlayerConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, FakePlayerConfig.class);
                System.out.println("[FakePlayer] 配置已加载: " + configFile.getAbsolutePath());
            } catch (IOException e) {
                System.out.println("[FakePlayer] 配置加载失败，使用默认值");
                instance = new FakePlayerConfig();
            }
        } else {
            instance = new FakePlayerConfig();
            save();
        }
    }

    public static void save() {
        if (instance == null) return;
        File configFile = getConfigFile();
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(instance, writer);
            System.out.println("[FakePlayer] 配置已保存: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        save();
    }

    public void setName(String v) {
        this.name = v;
        save();
    }

    public void setHealth(int v) {
        this.health = v;
        save();
    }

    public void setCopyInv(boolean v) {
        this.copyInv = v;
        save();
    }

    public void setSimulateDamage(boolean v) {
        this.simulateDamage = v;
        save();
    }

    public void setAutoTotem(boolean v) {
        this.autoTotem = v;
        save();
    }

    public void setShowDamage(boolean v) {
        this.showDamage = v;
        save();
    }

    public void setInvulnerableTicks(int v) {
        this.invulnerableTicks = v;
        save();
    }

    public void setRespawn(boolean v) {
        this.respawn = v;
        save();
    }
}