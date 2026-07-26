package fku.org.example.fku.features.structure_locator;

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.tpgoto.TpGotoPosFeature;
import fku.org.example.fku.util.BaritoneBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 结构定位功能 — 根据种子+放置规则计算最近结构坐标，支持 Baritone 前往
 * <p>
 * 原版结构使用种子+spacing/separation/salt 计算（random_spread 算法）。
 * 暮色森林结构使用自定义 forced_landmark 放置系统，无法通过种子计算，
 * 改用 /locate structure 命令捕获方式定位。
 * <p>
 * 该功能由赛博教员实现
 * <p>
 * 参考：lexis.Hack.Hacks.L_Enders_Cataclysm_C.CataclysmLocatorHack
 *       lexis.Hack.Hacks.Baritone.StructureLocatorHack
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class StructureLocatorFeature {

    private static final Logger LOGGER = LoggerFactory.getLogger("StructureLocator");
    private static Minecraft getMc() { return Minecraft.getInstance(); }
    private static final Pattern SEED_PATTERN = Pattern.compile("\\[\\s*(-?\\d+)\\s*\\]");
    private static volatile boolean expectingSeed = false;
    /**
     * 用于捕获 /locate structure 命令返回的坐标格式
     * 例如: [结构定位] 娜迦庭院 位于 [x: 123, y: 64, z: 456] (约 1000 格) 或类似格式
     */
    private static final Pattern LOCATE_PATTERN = Pattern.compile("位于\\s*\\[?\\s*x:\\s*(-?\\d+).*?y:\\s*(-?\\d+).*?z:\\s*(-?\\d+)", Pattern.CASE_INSENSITIVE);
    /** 简化版 locate 坐标正则：直接匹配数字组合 */
    private static final Pattern LOCATE_XYZ_PATTERN = Pattern.compile("(-?\\d+)\\s*/\\s*(-?\\d+)\\s*/\\s*(-?\\d+)");
    private static volatile boolean expectingLocate = false;
    private static volatile String locateTargetId = "";
    private static volatile boolean pendingTravel = false;
    private static volatile boolean pendingDimOk = false;
    private static volatile Target pendingTarget = null;

    public static final List<Target> TARGETS = new ArrayList<>();
    private static final Map<String, Set<Long>> skipped = new HashMap<>();
    private static boolean hasLastTarget = false;
    private static String lastTargetKey = "";
    private static int lastTargetCx = 0, lastTargetCz = 0;
    private static final String PREFIX = "§6[§b结构定位§6] §r";

    // ──────── 标记数据结构（通过 Baritone #goal 实现） ────────
    /** 当前标记的坐标（-1 = 未标记） */
    private static int markedX = -1, markedZ = -1;

    // ──────── 目标结构定义 ────────
    static {
        // ★ 原版
        TARGETS.add(Target.rs("villages", "村庄", Dim.OVERWORLD, 34, 8, 10387312, "linear", 64, Set.of(Biomes.PLAINS, Biomes.DESERT, Biomes.SAVANNA, Biomes.SNOWY_PLAINS, Biomes.TAIGA)));
        TARGETS.add(Target.rs("desert_pyramids", "沙漠神殿", Dim.OVERWORLD, 32, 8, 14357617, "linear", 64, Set.of(Biomes.DESERT)));
        TARGETS.add(Target.rs("igloos", "雪屋", Dim.OVERWORLD, 32, 8, 14357618, "linear", 64, Set.of(Biomes.SNOWY_PLAINS, Biomes.ICE_SPIKES, Biomes.SNOWY_TAIGA)));
        TARGETS.add(Target.rs("jungle_temples", "丛林神庙", Dim.OVERWORLD, 32, 8, 14357619, "linear", 64, Set.of(Biomes.JUNGLE, Biomes.BAMBOO_JUNGLE)));
        TARGETS.add(Target.rs("swamp_huts", "女巫小屋", Dim.OVERWORLD, 32, 8, 14357620, "linear", 64, Set.of(Biomes.SWAMP)));
        TARGETS.add(Target.rs("pillager_outposts", "掠夺者前哨", Dim.OVERWORLD, 32, 8, 165745296, "linear", 64, null));
        TARGETS.add(Target.rs("woodland_mansions", "林地府邸", Dim.OVERWORLD, 80, 20, 10387319, "triangular", 64, Set.of(Biomes.DARK_FOREST)));
        TARGETS.add(Target.rs("trail_ruins", "古迹废墟", Dim.OVERWORLD, 34, 8, 83469867, "linear", 64, null));
        TARGETS.add(Target.rs("ruined_portals", "废弃传送门(主世界)", Dim.OVERWORLD, 40, 15, 34222645, "linear", 64, null));
        TARGETS.add(Target.rs("ocean_monuments", "海底神殿", Dim.OVERWORLD, 32, 5, 10387313, "triangular", 64, Set.of(Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_FROZEN_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_OCEAN)));
        TARGETS.add(Target.rs("ocean_ruins", "海底废墟", Dim.OVERWORLD, 20, 8, 14357621, "linear", 64, null));
        TARGETS.add(Target.rs("shipwrecks", "沉船", Dim.OVERWORLD, 24, 4, 165745295, "linear", 64, null));
        TARGETS.add(Target.rs("ancient_cities", "古城(监守者)", Dim.OVERWORLD, 24, 8, 20083232, "linear", -50, Set.of(Biomes.DEEP_DARK)));
        TARGETS.add(Target.rs("nether_fossils", "下界化石", Dim.NETHER, 2, 1, 14357921, "linear", 64, Set.of(Biomes.SOUL_SAND_VALLEY)));
        TARGETS.add(Target.rs("nether_complexes", "下界要塞/堡垒", Dim.NETHER, 27, 4, 30084232, "linear", 64, null));
        TARGETS.add(Target.rs("end_cities", "末地城", Dim.END, 20, 11, 10387313, "triangular", 64, Set.of(Biomes.END_MIDLANDS, Biomes.END_HIGHLANDS)));
        TARGETS.add(Target.rings("strongholds", "要塞(含末地传送门)", 128, 32, 3));

        // ★ 灾变
        TARGETS.add(Target.rs("acropolis", "§c卫城", Dim.OVERWORLD, 80, 50, 913530101, "linear", 64, null));
        TARGETS.add(Target.rs("ancient_factory", "§c远古工厂", Dim.OVERWORLD, 112, 70, 319514301, "linear", 64, null));
        TARGETS.add(Target.rs("burning_arena", "§c燃烧竞技场(火焰巨像)", Dim.NETHER, 80, 50, (int)9123456789L, "linear", 64, null));
        TARGETS.add(Target.rs("cursed_pyramid", "§c诅咒金字塔", Dim.OVERWORLD, 80, 50, (int)9167234589L, "linear", 64, null));
        TARGETS.add(Target.rs("frosted_prison", "§c冰封监狱", Dim.OVERWORLD, 80, 50, (int)5872139439L, "linear", 64, null));
        TARGETS.add(Target.rs("ruined_citadel", "§c废弃城堡(末影守卫)", Dim.END, 50, 25, 367895146, "linear", 64, null));
        TARGETS.add(Target.rs("soul_black_smith", "§c灵魂铁匠铺", Dim.NETHER, 60, 50, (int)1984567320L, "linear", 64, null));
        TARGETS.add(Target.rs("sunken_city", "§c沉没之城(利维坦)", Dim.OVERWORLD, 100, 70, (int)1673928450L, "triangular", 64, null));

        // ★ 暮色森林 — 使用 RANDOM_SPREAD 模式（与灾变结构一致），通过种子+盐值计算位置
        // 注意：暮色森林实际使用自定义 landmark_grid 放置系统，此处参数为近似值
        TARGETS.add(Target.rs("naga_courtyard", "§6娜迦庭院", Dim.TWILIGHT, 32, 8, 10293847, "linear", 64, null));
        TARGETS.add(Target.rs("lich_tower", "§5巫妖塔", Dim.TWILIGHT, 32, 8, 20394857, "linear", 64, null));
        TARGETS.add(Target.rs("small_hollow_hill", "§2小型空心山丘", Dim.TWILIGHT, 32, 8, 30495867, "linear", 64, null));
        TARGETS.add(Target.rs("medium_hollow_hill", "§a中型空心山丘", Dim.TWILIGHT, 32, 8, 40596877, "linear", 64, null));
        TARGETS.add(Target.rs("large_hollow_hill", "§b大型空心山丘", Dim.TWILIGHT, 32, 8, 50697887, "linear", 64, null));
        TARGETS.add(Target.rs("hedge_maze", "§e树篱迷宫", Dim.TWILIGHT, 32, 8, 60798897, "linear", 64, null));
        TARGETS.add(Target.rs("quest_grove", "§7探索树林", Dim.TWILIGHT, 48, 12, 70899907, "linear", 64, null));
        TARGETS.add(Target.rs("hydra_lair", "§4九头蛇巢穴", Dim.TWILIGHT, 48, 12, 80901017, "linear", 64, null));
        TARGETS.add(Target.rs("labyrinth", "§8米诺陶迷宫", Dim.TWILIGHT, 48, 12, 91002027, "linear", 64, null));
        TARGETS.add(Target.rs("knight_stronghold", "§d骑士要塞", Dim.TWILIGHT, 48, 12, 101103037, "linear", 64, null));
        TARGETS.add(Target.rs("dark_tower", "§0黑暗高塔", Dim.TWILIGHT, 48, 12, 112104047, "linear", 64, null));
        TARGETS.add(Target.rs("yeti_cave", "§f雪怪洞穴", Dim.TWILIGHT, 48, 12, 123105057, "linear", 64, null));
        TARGETS.add(Target.rs("aurora_palace", "§d极光宫殿", Dim.TWILIGHT, 64, 16, 134106067, "linear", 64, null));
        TARGETS.add(Target.rs("troll_cave", "§8洞穴巨魔", Dim.TWILIGHT, 64, 16, 145107077, "linear", 64, null));
        TARGETS.add(Target.rs("mushroom_tower", "§d蘑菇塔", Dim.TWILIGHT, 64, 16, 156108087, "linear", 64, null));
        TARGETS.add(Target.rs("final_castle", "§c最终城堡", Dim.TWILIGHT, 128, 32, 167109097, "linear", 64, null));
    }

    // ──────── 聊天捕获 ────────
    @SubscribeEvent
    public static void onChat(ClientChatReceivedEvent event) {
        String text = event.getMessage().getString();
        // 处理种子捕获
        if (expectingSeed) {
            Matcher m = SEED_PATTERN.matcher(text);
            if (m.find()) {
                try {
                    StructureLocatorConfig cfg = StructureLocatorConfig.getInstance();
                    cfg.capturedSeed = Long.parseLong(m.group(1));
                    cfg.hasSeed = true;
                    expectingSeed = false;
                    cfg.save();
                } catch (NumberFormatException ignored) {}
            }
        }
        // 处理 /locate 响应捕获
        if (expectingLocate) {
            // 匹配 "位于 [x: 123, y: 64, z: 456]" 格式
            Matcher m = LOCATE_PATTERN.matcher(text);
            if (m.find()) {
                try {
                    int x = Integer.parseInt(m.group(1));
                    int y = Integer.parseInt(m.group(2));
                    int z = Integer.parseInt(m.group(3));
                    expectingLocate = false;
                    handleLocateResult(x, y, z);
                    return;
                } catch (NumberFormatException ignored) {}
            }
            // 匹配简化格式 "123 / 64 / 456"
            m = LOCATE_XYZ_PATTERN.matcher(text);
            if (m.find()) {
                try {
                    int x = Integer.parseInt(m.group(1));
                    int y = Integer.parseInt(m.group(2));
                    int z = Integer.parseInt(m.group(3));
                    expectingLocate = false;
                    handleLocateResult(x, y, z);
                    return;
                } catch (NumberFormatException ignored) {}
            }
            // 检查错误信息
            if (text.contains("无法定位") || text.contains("Could not locate") || text.contains("找不到")) {
                expectingLocate = false;
                msg("§c/locate 无法定位该结构，可能未生成或不存在");
            }
        }
    }

    // ──────── API ────────
    public static void requestSeed() {
        Minecraft mc = getMc();
        if (mc == null || mc.player == null || mc.player.connection == null) { msg("§c未连接服务器"); return; }
        expectingSeed = true;
        mc.player.connection.sendCommand("seed");
        msg("§7已发送 /seed, 正在等待种子...");
    }

    public static Long resolveSeed() {
        StructureLocatorConfig cfg = StructureLocatorConfig.getInstance();
        String ms = cfg.manualSeed;
        if (ms != null && !ms.trim().isEmpty()) {
            try { return Long.parseLong(ms.trim()); }
            catch (NumberFormatException e) { msg("§c手动种子不是有效数字"); return null; }
        }
        return cfg.hasSeed ? cfg.capturedSeed : null;
    }

    public static Target selectedTarget() {
        StructureLocatorConfig cfg = StructureLocatorConfig.getInstance();
        int idx = cfg.targetIndex;
        return (idx >= 0 && idx < TARGETS.size()) ? TARGETS.get(idx) : TARGETS.get(0);
    }

    public static void locate(boolean travel) {
        Minecraft mc = getMc();
        if (mc == null || mc.player == null || mc.level == null) return;
        Target t = selectedTarget();
        boolean dimOk = currentDim() == t.dim;
        // LOCATE 类型（暮色森林等）通过 /locate 命令定位，不需要种子
        if (t.kind == Kind.LOCATE) {
            locateViaCommand(t, travel, dimOk);
            return;
        }
        Long seed = resolveSeed();
        if (seed == null) { msg("§f还没有种子。先点[取种子]或填[手动种子]"); return; }
        if (t.kind == Kind.CONCENTRIC_RINGS) locateStronghold(t, seed, travel, dimOk);
        else locateRandomSpread(t, seed, travel, dimOk);
    }

    public static void skipAndNext() {
        Long seed = resolveSeed();
        if (seed == null) { msg("§f还没有种子"); return; }
        if (!hasLastTarget) { msg("§e还没定位过"); return; }
        skipped.computeIfAbsent(lastTargetKey, k -> new HashSet<>()).add(packChunk(lastTargetCx, lastTargetCz));
        msg("§7已跳过空点(区块 " + lastTargetCx + "," + lastTargetCz + "), 找下一个...");
        locate(true);
    }

    public static void clearSkips() {
        Long seed = resolveSeed();
        if (seed != null) skipped.remove(blKey(seed, selectedTarget().id));
        hasLastTarget = false;
        msg("§a已清空跳过记录");
    }

    /** 使用 Baritone #goal 标记目标点（不寻路，仅在小地图/路径点显示） */
    public static void markLocation() {
        locate(false);
        if (!hasLastTarget) return;
        Target t = selectedTarget();
        boolean dimOk = currentDim() == t.dim;
        int mx = lastTargetCx * 16 + 8, mz = lastTargetCz * 16 + 8;
        if (!dimOk) { msg("§e当前不在[" + dimName(t.dim) + "], 无法标记"); return; }
        if (!BaritoneBridge.isAvailable()) { msg("§e未安装 Baritone, 已显示坐标"); return; }
        BaritoneBridge.setGoalOnly(mx, 120, mz);
        markedX = mx; markedZ = mz;
        markName = t.name;
        int cd = StructureLocatorConfig.getInstance().markClearDistance;
        msg("§a已标记「" + t.name + "」到小地图 (goal " + mx + " 120 " + mz + ")，" + cd + "格内自动清除");
    }

    /** 清除标记 — 始终发送 #goal clear，不论是否追踪了内部标记 */
    public static void clearMark() {
        BaritoneBridge.clearGoal();
        if (markedX >= 0) {
            markedX = markedZ = -1;
            markName = "";
            msg("§7标记已清除");
        } else {
            msg("§7已发送 #goal clear（无内部标记）");
        }
    }

    /** 标记是否活跃 */
    public static boolean hasMark() { return markedX >= 0; }

    /** 自动清除：玩家进入配置距离时自动 #goal clear */
    @SubscribeEvent
    public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        Minecraft mc = getMc();
        if (mc == null || markedX < 0 || mc.player == null || mc.level == null) return;

        int clearDist = StructureLocatorConfig.getInstance().markClearDistance;
        double dx = mc.player.getX() - markedX;
        double dz = mc.player.getZ() - markedZ;
        if (dx * dx + dz * dz <= clearDist * clearDist) {
            BaritoneBridge.clearGoal();
            markedX = markedZ = -1;
            markName = "";
            msg("§7已到达标记位置(" + clearDist + "格内)，标记已自动清除");
        }
    }
    private static void locateRandomSpread(Target t, long seed, boolean travel, boolean dimOk) {
        if (t.biomes != null && !SeedBiomeSampler.ensureReady()) { msg("§c群系数据初始化失败"); return; }
        Minecraft mc = getMc();
        if (mc == null) return;
        int r = StructureLocatorConfig.getInstance().searchRadius;
        int px = (int) mc.player.getX(), pz = (int) mc.player.getZ();
        int centerGx = Math.floorDiv(px, t.spacing);
        int centerGz = Math.floorDiv(pz, t.spacing);
        String key = blKey(seed, t.id);
        Set<Long> bl = skipped.get(key);
        long bestDistSq = Long.MAX_VALUE;
        int bestX = 0, bestZ = 0, bestCx = 0, bestCz = 0;
        boolean found = false;

        for (int gx = centerGx - r; gx <= centerGx + r; gx++) {
            for (int gz = centerGz - r; gz <= centerGz + r; gz++) {
                ChunkPos cand = calcRandomSpreadPos(seed, t.salt, t.spacing, t.separation, t.spread, gx, gz);
                if (bl != null && bl.contains(packChunk(cand.x, cand.z))) continue;
                int bx = cand.x * 16 + 8, bz = cand.z * 16 + 8;
                long dx = (long) bx - px, dz = (long) bz - pz, d = dx * dx + dz * dz;
                if (t.biomes != null) {
                    ResourceKey<Biome> biome = SeedBiomeSampler.biomeAt(seed, toSeedDim(t.dim), bx, t.sampleY, bz);
                    if (biome == null || !t.biomes.contains(biome)) continue;
                }
                if (d >= bestDistSq) continue;
                bestDistSq = d; bestX = bx; bestZ = bz; bestCx = cand.x; bestCz = cand.z; found = true;
            }
        }
        if (!found) { msg(bl != null && !bl.isEmpty() ? "§c范围内没有更多匹配的结构了" : "§c范围内没找到匹配的结构, 调大搜索范围再试"); return; }
        rememberTarget(key, bestCx, bestCz);
        int dist = (int) Math.sqrt(bestDistSq);
        msg("§f" + t.name + "  §7坐标: §bX=" + bestX + " Z=" + bestZ + " §7(约" + dist + "格)");
        doTravel(travel, dimOk, t, bestX, bestZ);
    }

    private static ChunkPos calcRandomSpreadPos(long seed, int salt, int spacing, int separation, String spread, int rx, int rz) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        long key = (long) rx * 341873128712L + (long) rz * 132897987541L + seed + salt;
        random.setLargeFeatureWithSalt(seed, salt, rx, rz);
        int offX = random.nextInt(spacing - separation);
        int offZ = random.nextInt(spacing - separation);
        return new ChunkPos(rx * spacing + offX, rz * spacing + offZ);
    }

    private static void locateStronghold(Target t, long seed, boolean travel, boolean dimOk) {
        Minecraft mc = getMc();
        if (mc == null) return;
        List<ChunkPos> positions = strongholdPositions(seed, t.ringCount, t.ringDist, t.ringSpread);
        int px = (int) mc.player.getX(), pz = (int) mc.player.getZ();
        String key = blKey(seed, t.id);
        Set<Long> bl = skipped.get(key);
        long bestDistSq = Long.MAX_VALUE;
        int bestX = 0, bestZ = 0, bestCx = 0, bestCz = 0;
        boolean found = false;
        for (ChunkPos cp : positions) {
            if (bl != null && bl.contains(packChunk(cp.x, cp.z))) continue;
            int bx = cp.x * 16 + 8, bz = cp.z * 16 + 8;
            long d = (long)(bx - px) * (bx - px) + (long)(bz - pz) * (bz - pz);
            if (d >= bestDistSq) continue;
            bestDistSq = d; bestX = bx; bestZ = bz; bestCx = cp.x; bestCz = cp.z; found = true;
        }
        if (!found) { msg("§c没算出要位置"); return; }
        rememberTarget(key, bestCx, bestCz);
        int dist = (int) Math.sqrt(bestDistSq);
        msg("§f" + t.name + " §7(同心环, 到点附近找传送门)  §7坐标: §bX=" + bestX + " Z=" + bestZ + " §7(约" + dist + "格)");
        doTravel(travel, dimOk, t, bestX, bestZ);
    }

    private static void doTravel(boolean travel, boolean dimOk, Target t, int x, int z) {
        if (!travel) return;
        if (!dimOk) { msg("§e当前不在[" + dimName(t.dim) + "], 已显示坐标"); return; }
        String mode = StructureLocatorConfig.getInstance().gotoMode;
        if ("tpgoto".equals(mode)) {
            // 使用 TpGotoPos 传送前往
            TpGotoPosFeature.startTeleport(x, 120, z);
            msg("§a已让 TpGotoPos 前往坐标 (" + x + " 120 " + z + ")");
            return;
        }
        // 默认使用 Baritone
        if (!BaritoneBridge.isAvailable()) { msg("§e未安装 Baritone, 已显示坐标"); return; }
        // 前往新位置时自动清除旧标记
        if (markedX >= 0) { markedX = markedZ = -1; }
        BaritoneBridge.gotoCoordSilent(x, 120, z);
        msg("§a已让 Baritone 前往 (goto " + x + " 120 " + z + ")");
    }

    /** 通过 /locate 命令定位暮色森林等结构 */
    private static void locateViaCommand(Target t, boolean travel, boolean dimOk) {
        Minecraft mc = getMc();
        if (mc == null || mc.player == null || mc.player.connection == null) return;
        if (!dimOk) {
            msg("§e当前不在[" + dimName(t.dim) + "], 请先进入暮色森林再定位");
            return;
        }
        expectingLocate = true;
        locateTargetId = t.id;
        pendingTravel = travel;
        pendingDimOk = dimOk;
        pendingTarget = t;
        mc.player.connection.sendCommand("locate structure twilightforest:" + t.id);
        msg("§7已发送 /locate, 正在等待响应...");
    }

    /** 处理 /locate 命令返回的坐标结果 */
    private static void handleLocateResult(int x, int y, int z) {
        Minecraft mc = getMc();
        if (mc == null) return;
        // 查找目标结构名称
        String targetName = "";
        Target t = pendingTarget;
        if (t != null) {
            targetName = t.name;
        } else {
            for (Target ct : TARGETS) {
                if (ct.id.equals(locateTargetId)) {
                    targetName = ct.name;
                    t = ct;
                    break;
                }
            }
        }
        int cx = x >> 4, cz = z >> 4;
        rememberTarget(blKey(0, locateTargetId), cx, cz);
        int dist = (int) Math.sqrt((long)(x - mc.player.getX()) * (x - mc.player.getX()) + (long)(z - mc.player.getZ()) * (z - mc.player.getZ()));
        msg("§f" + targetName + "  §7坐标: §bX=" + x + " Y=" + y + " Z=" + z + " §7(约" + dist + "格)");
        // 执行 pending 的 travel
        if (pendingTravel && pendingDimOk && t != null) {
            String mode = StructureLocatorConfig.getInstance().gotoMode;
            if ("tpgoto".equals(mode)) {
                TpGotoPosFeature.startTeleport(x, y, z);
                msg("§a已让 TpGotoPos 前往坐标 (" + x + " " + y + " " + z + ")");
            } else if (!BaritoneBridge.isAvailable()) {
                msg("§e未安装 Baritone, 已显示坐标");
            } else {
                BaritoneBridge.gotoCoordSilent(x, y, z);
                msg("§a已让 Baritone 前往 (goto " + x + " " + y + " " + z + ")");
            }
        }
        pendingTarget = null;
    }

    private static void rememberTarget(String key, int cx, int cz) {
        hasLastTarget = true; lastTargetKey = key; lastTargetCx = cx; lastTargetCz = cz;
    }

    private static List<ChunkPos> strongholdPositions(long seed, int count, int distance, int spread) {
        List<ChunkPos> list = new ArrayList<>();
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureSeed(seed, 0, 0);
        double angle = random.nextDouble() * Math.PI * 2.0;
        int ring = 0, placed = 0, curSpread = spread;
        for (int i = 0; i < count; i++) {
            double d = 4.0 * distance + (distance * ring * 6) + (random.nextDouble() - 0.5) * distance * 2.5;
            int cx = (int) Math.round(Math.cos(angle) * d);
            int cz = (int) Math.round(Math.sin(angle) * d);
            list.add(new ChunkPos(cx, cz));
            angle += Math.PI * 2.0 / curSpread;
            if (++placed != curSpread) continue;
            placed = 0; curSpread += 2 * curSpread / (++ring + 1);
            curSpread = Math.min(curSpread, count - i - 1);
            angle += random.nextDouble() * Math.PI * 2.0;
        }
        return list;
    }

    private static String blKey(long seed, String id) { return seed + "/" + id; }
    private static long packChunk(int cx, int cz) { return (long) cx << 32 | (long) cz & 0xFFFFFFFFL; }

    private static final ResourceKey<Level> TWILIGHT_DIM = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("twilightforest", "twilight_forest"));

    private static Dim currentDim() {
        Minecraft mc = getMc();
        if (mc == null || mc.level == null) return Dim.OVERWORLD;
        ResourceKey<Level> d = mc.level.dimension();
        if (d == Level.NETHER) return Dim.NETHER;
        if (d == Level.END) return Dim.END;
        if (d == TWILIGHT_DIM) return Dim.TWILIGHT;
        return Dim.OVERWORLD;
    }

    private static SeedBiomeSampler.Dim toSeedDim(Dim d) {
        return d == Dim.NETHER ? SeedBiomeSampler.Dim.NETHER : d == Dim.END ? SeedBiomeSampler.Dim.END : SeedBiomeSampler.Dim.OVERWORLD;
    }

    public static String dimName(Dim d) {
        if (d == Dim.NETHER) return "下界";
        if (d == Dim.END) return "末地";
        if (d == Dim.TWILIGHT) return "暮色森林";
        return "主世界";
    }

    public static void msg(String s) {
        Minecraft mc = getMc();
        if (mc != null && mc.player != null) mc.player.displayClientMessage(Component.literal(PREFIX + s), false);
    }

    /** 标记显示名称 */
    private static String markName = "";

    // ──────── 类型 ────────
    public enum Dim { OVERWORLD, NETHER, END, TWILIGHT }
    public enum Kind { RANDOM_SPREAD, CONCENTRIC_RINGS, LOCATE }

    public static final class Target {
        public final String id, name, spread;
        public final Dim dim;
        public final Kind kind;
        public final int spacing, separation, salt, ringCount, ringDist, ringSpread, sampleY;
        public final Set<ResourceKey<Biome>> biomes;

        public Target(String id, String name, Dim dim, Kind kind, int spacing, int sep, int salt, String spread,
                      int ringCount, int ringDist, int ringSpread, int sampleY, Set<ResourceKey<Biome>> biomes) {
            this.id = id; this.name = name; this.dim = dim; this.kind = kind;
            this.spacing = spacing; this.separation = sep; this.salt = salt; this.spread = spread;
            this.ringCount = ringCount; this.ringDist = ringDist; this.ringSpread = ringSpread;
            this.sampleY = sampleY; this.biomes = biomes;
        }
        static Target rs(String id, String name, Dim dim, int s, int sep, int salt, String spread, int sy, Set<ResourceKey<Biome>> b) {
            return new Target(id, name, dim, Kind.RANDOM_SPREAD, s, sep, salt, spread, 0, 0, 0, sy, b);
        }
        static Target rings(String id, String name, int c, int d, int sp) {
            return new Target(id, name, Dim.OVERWORLD, Kind.CONCENTRIC_RINGS, 0, 0, 0, "linear", c, d, sp, 64, null);
        }
        /** 通过 /locate 命令定位的结构（暮色森林等） */
        static Target locate(String id, String name, Dim dim) {
            return new Target(id, name, dim, Kind.LOCATE, 0, 0, 0, "linear", 0, 0, 0, 64, null);
        }
    }
}
