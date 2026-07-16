package fku.org.example.fku.features.structure_locator;

import fku.org.example.fku.Fku;
import fku.org.example.fku.util.BaritoneBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
//? if neoforge {
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
//? }

/**
 * 结构定位功能 — 根据种子+放置规则计算最近结构坐标，支持 Baritone 前往
 * <p>
 * 参考：lexis.Hack.Hacks.L_Enders_Cataclysm_C.CataclysmLocatorHack
 *       lexis.Hack.Hacks.Baritone.StructureLocatorHack
 */
@OnlyIn(Dist.CLIENT)
//? if neoforge {
@EventBusSubscriber(modid = Fku.MOD_ID, value = Dist.CLIENT)
//? } else {
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
//? }
public class StructureLocatorFeature {

    private static final Logger LOGGER = LoggerFactory.getLogger("StructureLocator");
    private static final Minecraft mc = Minecraft.getInstance();
    private static final Pattern SEED_PATTERN = Pattern.compile("\\[\\s*(-?\\d+)\\s*\\]");
    private static volatile boolean expectingSeed = false;

    public static final List<Target> TARGETS = new ArrayList<>();
    private static final Map<String, Set<Long>> skipped = new HashMap<>();
    private static boolean hasLastTarget = false;
    private static String lastTargetKey = "";
    private static int lastTargetCx = 0, lastTargetCz = 0;
    private static final String PREFIX = "§6[§b结构定位§6] §r";

    // ──────── 标记数据结构 ────────
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
    }

    // ──────── 聊天捕获 ────────
    @SubscribeEvent
    public static void onChat(ClientChatReceivedEvent event) {
        if (!expectingSeed) return;
        String text = event.getMessage().getString();
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

    // ──────── API ────────
    public static void requestSeed() {
        if (mc.player == null || mc.player.connection == null) { msg("§c未连接服务器"); return; }
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
        if (mc.player == null || mc.level == null) return;
        Long seed = resolveSeed();
        if (seed == null) { msg("§f还没有种子。先点[取种子]或填[手动种子]"); return; }
        Target t = selectedTarget();
        boolean dimOk = currentDim() == t.dim;
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

    /** 在原版世界中标记一个光柱 + HUD 指引，不依赖 Baritone */
    public static void markLocation() {
        locate(false);
        if (!hasLastTarget) return;
        Target t = selectedTarget();
        boolean dimOk = currentDim() == t.dim;
        int mx = lastTargetCx * 16 + 8, mz = lastTargetCz * 16 + 8;
        if (!dimOk) { msg("§e当前不在[" + dimName(t.dim) + "], 无法标记"); return; }
        markedX = mx; markedZ = mz;
        markedY = (int) mc.player.getY();
        markName = t.name;
        msg("§a已标记「" + t.name + "」(" + mx + ", " + mz + ")，光柱指引到达后自动清除");
    }

    /** 清除标记 */
    public static void clearMark() {
        if (markedX < 0) { msg("§e当前没有标记"); return; }
        markedX = markedZ = -1;
        markedY = 80;
        markName = "";
        msg("§7标记已清除");
    }

    /** 标记是否活跃 */
    public static boolean hasMark() { return markedX >= 0; }

    // ──────── 内部定位 ────────
    private static void locateRandomSpread(Target t, long seed, boolean travel, boolean dimOk) {
        if (t.biomes != null && !SeedBiomeSampler.ensureReady()) { msg("§c群系数据初始化失败"); return; }
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
        RandomSource random = RandomSource.create();
        long key = (long) rx * 341873128712L + (long) rz * 132897987541L + seed + salt;
        random.setSeed(key);
        int offX = random.nextInt(spacing - separation);
        int offZ = random.nextInt(spacing - separation);
        return new ChunkPos(rx * spacing + offX, rz * spacing + offZ);
    }

    private static void locateStronghold(Target t, long seed, boolean travel, boolean dimOk) {
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

    /** 自动清除 + 粒子光柱（150 格内渲染 END_ROD 光柱） */
    @SubscribeEvent
    //? if neoforge {
        public static void onClientTick(ClientTickEvent.Post event) {
    //? } else {
        public static void onClientTick(TickEvent.ClientTickEvent event) {
    //? }
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (markedX < 0 || mc.player == null || mc.level == null) return;

        // ── 到达检测 ──
        double dx = mc.player.getX() - markedX;
        double dz = mc.player.getZ() - markedZ;
        double distSq = dx * dx + dz * dz;
        if (distSq <= 100) { // 10 格内
            markedX = markedZ = -1; markedY = 80; markName = "";
            msg("§7已到达标记位置，标记已自动清除");
            return;
        }

        // ── 光柱由 onRenderLevelStage 渲染（AFTER_LEVEL + 加性混合） ──
    }

    private static void doTravel(boolean travel, boolean dimOk, Target t, int x, int z) {
        if (!travel) return;
        if (!dimOk) { msg("§e当前不在[" + dimName(t.dim) + "], 已显示坐标"); return; }
        if (!BaritoneBridge.isAvailable()) { msg("§e未安装 Baritone, 已显示坐标"); return; }
        // 前往新位置时自动清除旧标记
        if (markedX >= 0) { markedX = markedZ = -1; }
        BaritoneBridge.gotoCoordSilent(x, 120, z);
        msg("§a已让 Baritone 前往 (goto " + x + " 120 " + z + ")");
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

    private static Dim currentDim() {
        if (mc.level == null) return Dim.OVERWORLD;
        ResourceKey<Level> d = mc.level.dimension();
        if (d == Level.NETHER) return Dim.NETHER;
        if (d == Level.END) return Dim.END;
        return Dim.OVERWORLD;
    }

    private static SeedBiomeSampler.Dim toSeedDim(Dim d) {
        return d == Dim.NETHER ? SeedBiomeSampler.Dim.NETHER : d == Dim.END ? SeedBiomeSampler.Dim.END : SeedBiomeSampler.Dim.OVERWORLD;
    }

    public static String dimName(Dim d) {
        if (d == Dim.NETHER) return "下界";
        if (d == Dim.END) return "末地";
        return "主世界";
    }

    public static void msg(String s) {
        if (mc.player != null) mc.player.displayClientMessage(Component.literal(PREFIX + s), false);
    }

    // ════════════════════════════════════════════════════════
    //  原版标记渲染：光束 + HUD 方向/距离指示器
    // ════════════════════════════════════════════════════════

    /** 标记显示名称 */
    private static String markName = "";
    static int markedY = 80;

    /** 光柱渲染已移除 — 全部指引在 HUD 上完成 */

    /** 渲染 HUD：方向指示 + 距离 + 近距离大箭头 */
    @SubscribeEvent
    public static void onRenderHUD(net.minecraftforge.client.event.RenderGuiOverlayEvent.Post event) {
        if (markedX < 0 || mc.player == null || mc.level == null) return;

        var g = event.getGuiGraphics();
        var font = mc.font;
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        // ── Vec3 3D 方向 ──
        Vec3 look = mc.player.getLookAngle();
        Vec3 toTarget = new Vec3(markedX - mc.player.getX(),
                                 (markedY + 32) - mc.player.getEyeY(),
                                 markedZ - mc.player.getZ());
        double dist = toTarget.length();
        Vec3 dir = toTarget.normalize();

        // 屏幕空间基向量
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 up = right.cross(look).normalize();

        double horiz = dir.dot(right);      // 正=右侧
        double vert  = -dir.dot(up);        // 正=上方
        double fwd   = dir.dot(look);       // 正=前方

        // ── 底部坐标 ──
        g.drawString(font, String.format("§7%d, %d", markedX, markedZ), 8, sh - 16, 0x666666);


        int cx = sw / 2, cy = sh / 2;

        // 近距离（< 50 格）放大显示
        boolean close = dist < 50;

        if (fwd > 0) {
            // 在视野前方 → 投影到屏幕
            double fovScale = 0.35;
            int sx = cx + (int)((horiz / Math.max(fwd, 0.3)) * fovScale * sw);
            int sy = cy + (int)((vert  / Math.max(fwd, 0.3)) * fovScale * sh);
            sx = Math.max(8, Math.min(sw - 16, sx));
            sy = Math.max(8, Math.min(sh - 30, sy));
            // 大字标记（近距离⛭，远距离✦），加阴影描边
            String marker = close ? "§b⛭" : "§b✦";
            g.drawString(font, marker, sx + 1, sy, 0x00000044); // 阴影
            g.drawString(font, marker, sx, sy, 0x88CCFF);
        } else {
            // 在后方 → 边缘大箭头
            int arrowX = horiz > 0 ? sw - 26 : 4;
            String a = horiz > 0 ? "§b▸" : "§b◂";
            g.drawString(font, a, arrowX + 1, cy + 1, 0x00000044);
            g.drawString(font, a, arrowX, cy, 0x88CCFF);
            if (vert > 2)  { g.drawString(font, "§b▲", cx - 3, 3, 0x88CCFF); g.drawString(font, "§b▲", cx - 2, 2, 0x00000044); }
            else if (vert < -2) { g.drawString(font, "§b▼", cx - 3, sh - 33, 0x88CCFF); g.drawString(font, "§b▼", cx - 2, sh - 32, 0x00000044); }
        }

        // ── 近距离中心超大箭头 ──
        if (close && fwd > 0) {
            int bigY = sh - 75;
            g.drawString(font, "§b⬆", cx - 7, bigY + 1, 0x00000044);
            g.drawString(font, "§b⬆", cx - 8, bigY, 0x88CCFF);
        }

        // ── HUD 指示下方显示距离 + 名称 ──
        String distText = String.format("§b%.0fm  §7%s", dist, markName);
        g.drawString(font, distText, cx - font.width(distText) / 2, sh - 110, 0x88CCFF);
    }

    // ──────── 类型 ────────
    public enum Dim { OVERWORLD, NETHER, END }
    public enum Kind { RANDOM_SPREAD, CONCENTRIC_RINGS }

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
    }
}
