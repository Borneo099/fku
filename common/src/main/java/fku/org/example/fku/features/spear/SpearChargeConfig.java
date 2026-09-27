package fku.org.example.fku.features.spear; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 矛之冲锋配置（重写：配置项完全参考 meteoruth SpearDMG，仅保留核心项 + 模式选择）
 */
public class SpearChargeConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static SpearChargeConfig instance;

    public boolean enabled = false;
    /** 突进模式：
     *  - "packet" 发包模式：注入移动包偏移（服务端侧位移，朝准星，无需索敌）—— 参考 meteoruth SpearDMG
     *  - "vanilla" 原版模式：改本地速度真实突进（需准星小范围索敌朝目标才能命中） */
    public String mode = "packet";
    /** 戳击距离：蓄力期间每 tick 服务器侧移动的格数；矛读到的速度 = 该值 × 20（meteoruth 唯一配置项） */
    public double boostDistance = 3.0;
    /** 发包模式：每 tick 发送的位移步数（力量放大；默认 1 = meteoruth 原值） */
    public int packets = 1;
    /** 原版模式：突进速度（格/tick，可调） */
    public double vanillaSpeed = 1.2;
    /** 原版模式：与目标距离 ≤ 此值(格)时暂停冲锋（不再向目标推速度），让矛在贴脸处自然戳中
     *  并借击退/余速飞出一段，从而打出伤害；否则会紧贴目标原地反复冲刺、打不出伤害。0=不暂停。 */
    public double vanillaStopDistance = 1.5;

    private static File getConfigFile() {
        File dir = new File(getGameDir(), "fku");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "spearcharge.json");
    }

    private static File getGameDir() {
        try { Minecraft mc = Minecraft.getInstance(); if (mc != null) return mc.gameDirectory; }
        catch (Exception ignored) {}
        return Paths.get("config").toAbsolutePath().normalize().getParent().toFile();
    }

    public static SpearChargeConfig getInstance() { if (instance == null) load(); return instance; }

    public static void load() {
        File f = getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f)) { instance = GSON.fromJson(r, SpearChargeConfig.class); }
            catch (IOException e) { instance = new SpearChargeConfig(); }
        } else { instance = new SpearChargeConfig(); save(); }
        if (instance == null) instance = new SpearChargeConfig();
    }

    public static void save() {
        if (instance == null) return;
        try (FileWriter w = new FileWriter(getConfigFile())) { GSON.toJson(instance, w); }
        catch (IOException e) { e.printStackTrace(); }
    }
}
