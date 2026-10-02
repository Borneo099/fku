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
    public boolean aimbotTriggerEnabled = false;  // 扳机：自瞄框变绿（锁定敌人）即自动左键开火
    public int aimbotTriggerDelay = 0;             // 扳机开火前间隔(ms)：锁定后延迟再按左键，避免狙击枪首枪空枪（0=立即）
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
    // ★ 不打队友：队伍名字颜色与自己相同的玩家不进入自瞄目标（默认开）
    public boolean dontHitTeammates = true;

    // ★ Ghost Peek（幽灵窥视）：按住指定按键，瞬移到身侧绕过墙角偷瞄/偷射后瞬回
    public boolean ghostPeekEnabled = false;
    public int ghostPeekKey = -1;                  // 激活按键：-1=未绑定；>=1000 表示鼠标按键(值-1000)；否则键盘按键码
    public double ghostPeekBlocks = 3.0;           // 身侧(左右)瞬移距离（格）
    public double ghostPeekUpBlocks = 1.0;         // 上(垂直+)窥视偏移（格）：翻越矮墙 y+1
    public double ghostPeekDownBlocks = 3.0;       // 下(垂直−)窥视偏移（格）：钻地缝 y-3（下窥视点通常需更深）
    public boolean ghostPeekOriginEnabled = true;  // 启用“原地原点”窥视点：玩家原地若能命中则最优先（无需位移）
    public double ghostPeekBacktrack = 0.0;          // 反向提前量(刻)：补偿网络延迟，瞄向目标“延迟前”的位置；0=直接锁当前点（近距离不需要）
    public boolean ghostPeekSinglePlayer = false;  // true=单 tick 内瞬移+开火+瞬回（最稳）；false=分两 tick（stage1 瞬移 / stage2 开火瞬回）
    public double ghostPeekRange = 100.0;         // 索敌范围（格）
    public double ghostPeekFov = 360.0;           // 索敌半锥角(度)：>=360=不限（与 NoSpread 一致）
    public boolean ghostPeekPlayersOnly = true;    // 仅锁定玩家
    public String ghostPeekAimPart = "头部";        // 窥视开火瞄准部位："头部"(默认，与 NoSpread 一致) / "身体"
    public boolean ghostPeekNoSpread = true;       // 开火时多发 PosRot 包抵消扩散/防回弹
    public int ghostPeekNoSpreadPackets = 6;
    public int ghostPeekFrequency = 2;             // 每轮“瞬移→开火→回位”之间的间隔刻数（越小越快，1=最快）；控制窥视/开火频率
    public boolean ghostPeekWallBang = false;      // 开启则近距离穿墙也能瞄准
    public double ghostPeekWallBangRange = 3.0;

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