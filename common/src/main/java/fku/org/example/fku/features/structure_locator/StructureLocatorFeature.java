package fku.org.example.fku.features.structure_locator;

import fku.org.example.fku.features.structure_locator.SeedBiomeSampler;
import fku.org.example.fku.features.structure_locator.StructureLocatorConfig;
import fku.org.example.fku.util.BaritoneBridge;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class StructureLocatorFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"StructureLocator");
    private static final Pattern SEED_PATTERN = Pattern.compile("\\[\\s*(-?\\d+)\\s*\\]");
    private static volatile boolean expectingSeed = false;
    public static final List<Target> TARGETS = new ArrayList<Target>();
    private static final Map<String, Set<Long>> skipped = new HashMap<String, Set<Long>>();
    private static boolean hasLastTarget = false;
    private static String lastTargetKey = "";
    private static int lastTargetCx = 0;
    private static int lastTargetCz = 0;
    private static final String PREFIX = "\u00a76[\u00a7b\u7ed3\u6784\u5b9a\u4f4d\u00a76] \u00a7r";
    private static int markedX = -1;
    private static int markedZ = -1;
    private static String markName;

    private static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    @SubscribeEvent
    public static void onChat(ClientChatReceivedEvent event) {
        if (!expectingSeed) {
            return;
        }
        String text = event.getMessage().getString();
        Matcher m = SEED_PATTERN.matcher(text);
        if (m.find()) {
            try {
                StructureLocatorConfig cfg = StructureLocatorConfig.getInstance();
                cfg.capturedSeed = Long.parseLong(m.group(1));
                cfg.hasSeed = true;
                expectingSeed = false;
                cfg.save();
            }
            catch (NumberFormatException numberFormatException) {
                // ignored
            }
        }
    }

    public static void requestSeed() {
        Minecraft mc = StructureLocatorFeature.getMc();
        if (mc == null || mc.player == null || mc.player.connection == null) {
            StructureLocatorFeature.msg("\u00a7c\u672a\u8fde\u63a5\u670d\u52a1\u5668");
            return;
        }
        expectingSeed = true;
        mc.player.connection.sendCommand("seed");
        StructureLocatorFeature.msg("\u00a77\u5df2\u53d1\u9001 /seed, \u6b63\u5728\u7b49\u5f85\u79cd\u5b50.");
    }

    public static Long resolveSeed() {
        StructureLocatorConfig cfg = StructureLocatorConfig.getInstance();
        String ms = cfg.manualSeed;
        if (ms != null && !ms.trim().isEmpty()) {
            try {
                return Long.parseLong(ms.trim());
            }
            catch (NumberFormatException e) {
                StructureLocatorFeature.msg("\u00a7c\u624b\u52a8\u79cd\u5b50\u4e0d\u662f\u6709\u6548\u6570\u5b57");
                return null;
            }
        }
        return cfg.hasSeed ? Long.valueOf(cfg.capturedSeed) : null;
    }

    public static Target selectedTarget() {
        StructureLocatorConfig cfg = StructureLocatorConfig.getInstance();
        int idx = cfg.targetIndex;
        return idx >= 0 && idx < TARGETS.size() ? TARGETS.get(idx) : TARGETS.get(0);
    }

    public static void locate(boolean travel) {
        boolean dimOk;
        Minecraft mc = StructureLocatorFeature.getMc();
        if (mc == null || mc.player == null || mc.level == null) {
            return;
        }
        Long seed = StructureLocatorFeature.resolveSeed();
        if (seed == null) {
            StructureLocatorFeature.msg("\u00a7f\u8fd8\u6ca1\u6709\u79cd\u5b50\u3002\u5148\u70b9[\u53d6\u79cd\u5b50]\u6216\u586b[\u624b\u52a8\u79cd\u5b50]");
            return;
        }
        Target t = StructureLocatorFeature.selectedTarget();
        boolean bl = dimOk = StructureLocatorFeature.currentDim() == t.dim;
        if (t.kind == Kind.CONCENTRIC_RINGS) {
            StructureLocatorFeature.locateStronghold(t, seed, travel, dimOk);
        } else {
            StructureLocatorFeature.locateRandomSpread(t, seed, travel, dimOk);
        }
    }

    public static void skipAndNext() {
        Long seed = StructureLocatorFeature.resolveSeed();
        if (seed == null) {
            StructureLocatorFeature.msg("\u00a7f\u8fd8\u6ca1\u6709\u79cd\u5b50");
            return;
        }
        if (!hasLastTarget) {
            StructureLocatorFeature.msg("\u00a7e\u8fd8\u6ca1\u5b9a\u4f4d\u8fc7");
            return;
        }
        skipped.computeIfAbsent(lastTargetKey, k -> new HashSet()).add(StructureLocatorFeature.packChunk(lastTargetCx, lastTargetCz));
        StructureLocatorFeature.msg("\u00a77\u5df2\u8df3\u8fc7\u7a7a\u70b9(\u533a\u5757 " + lastTargetCx + "," + lastTargetCz + "), \u627e\u4e0b\u4e00\u4e2a.");
        StructureLocatorFeature.locate(true);
    }

    public static void clearSkips() {
        Long seed = StructureLocatorFeature.resolveSeed();
        if (seed != null) {
            skipped.remove(StructureLocatorFeature.blKey(seed, StructureLocatorFeature.selectedTarget().id));
        }
        hasLastTarget = false;
        StructureLocatorFeature.msg("\u00a7a\u5df2\u6e05\u7a7a\u8df3\u8fc7\u8bb0\u5f55");
    }

    public static void markLocation() {
        StructureLocatorFeature.locate(false);
        if (!hasLastTarget) {
            return;
        }
        Target t = StructureLocatorFeature.selectedTarget();
        boolean dimOk = StructureLocatorFeature.currentDim() == t.dim;
        int mx = lastTargetCx * 16 + 8;
        int mz = lastTargetCz * 16 + 8;
        if (!dimOk) {
            StructureLocatorFeature.msg("\u00a7e\u5f53\u524d\u4e0d\u5728[" + StructureLocatorFeature.dimName(t.dim) + "], \u65e0\u6cd5\u6807\u8bb0");
            return;
        }
        if (!BaritoneBridge.isAvailable()) {
            StructureLocatorFeature.msg("\u00a7e\u672a\u5b89\u88c5 Baritone, \u5df2\u663e\u793a\u5750\u6807");
            return;
        }
        BaritoneBridge.setGoalOnly(mx, 120, mz);
        markedX = mx;
        markedZ = mz;
        markName = t.name;
        int cd = StructureLocatorConfig.getInstance().markClearDistance;
        StructureLocatorFeature.msg("\u00a7a\u5df2\u6807\u8bb0\u300c" + t.name + "\u300d\u5230\u5c0f\u5730\u56fe (goal " + mx + " 120 " + mz + ")\uff0c" + cd + "\u683c\u5185\u81ea\u52a8\u6e05\u9664");
    }

    public static void clearMark() {
        BaritoneBridge.clearGoal();
        if (markedX >= 0) {
            markedZ = -1;
            markedX = -1;
            markName = "";
            StructureLocatorFeature.msg("\u00a77\u6807\u8bb0\u5df2\u6e05\u9664");
        } else {
            StructureLocatorFeature.msg("\u00a77\u5df2\u53d1\u9001 #goal clear\uff08\u65e0\u5185\u90e8\u6807\u8bb0\uff09");
        }
    }

    public static boolean hasMark() {
        return markedX >= 0;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        double dz;
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = StructureLocatorFeature.getMc();
        if (mc == null || markedX < 0 || mc.player == null || mc.level == null) {
            return;
        }
        int clearDist = StructureLocatorConfig.getInstance().markClearDistance;
        double dx = mc.player.getX() - markedX;
        if (dx * dx + (dz = mc.player.getZ() - markedZ) * dz <= (clearDist * clearDist)) {
            BaritoneBridge.clearGoal();
            markedZ = -1;
            markedX = -1;
            markName = "";
            StructureLocatorFeature.msg("\u00a77\u5df2\u5230\u8fbe\u6807\u8bb0\u4f4d\u7f6e(" + clearDist + "\u683c\u5185)\uff0c\u6807\u8bb0\u5df2\u81ea\u52a8\u6e05\u9664");
        }
    }

    private static void locateRandomSpread(Target t, long seed, boolean travel, boolean dimOk) {
        if (t.biomes != null && !SeedBiomeSampler.ensureReady()) {
            StructureLocatorFeature.msg("\u00a7c\u7fa4\u7cfb\u6570\u636e\u521d\u59cb\u5316\u5931\u8d25");
            return;
        }
        Minecraft mc = StructureLocatorFeature.getMc();
        if (mc == null) {
            return;
        }
        int r = StructureLocatorConfig.getInstance().searchRadius;
        int px = (int)mc.player.getX();
        int pz = (int)mc.player.getZ();
        int centerGx = Math.floorDiv(px, t.spacing);
        int centerGz = Math.floorDiv(pz, t.spacing);
        String key = StructureLocatorFeature.blKey(seed, t.id);
        Set<Long> bl = skipped.get(key);
        long bestDistSq = Long.MAX_VALUE;
        int bestX = 0;
        int bestZ = 0;
        int bestCx = 0;
        int bestCz = 0;
        boolean found = false;
        for (int gx = centerGx - r; gx <= centerGx + r; ++gx) {
            for (int gz = centerGz - r; gz <= centerGz + r; ++gz) {
                ResourceKey<Biome> biome;
                ChunkPos cand = StructureLocatorFeature.calcRandomSpreadPos(seed, t.salt, t.spacing, t.separation, t.spread, gx, gz);
                if (bl != null && bl.contains(StructureLocatorFeature.packChunk(cand.x, cand.z))) continue;
                int bx = cand.x * 16 + 8;
                int bz = cand.z * 16 + 8;
                long dx = bx - px;
                long dz = bz - pz;
                long d = dx * dx + dz * dz;
                if (t.biomes != null && ((biome = SeedBiomeSampler.biomeAt(seed, StructureLocatorFeature.toSeedDim(t.dim), bx, t.sampleY, bz)) == null || !t.biomes.contains(biome)) || d >= bestDistSq) continue;
                bestDistSq = d;
                bestX = bx;
                bestZ = bz;
                bestCx = cand.x;
                bestCz = cand.z;
                found = true;
            }
        }
        if (!found) {
            StructureLocatorFeature.msg(bl != null && !bl.isEmpty() ? "\u00a7c\u8303\u56f4\u5185\u6ca1\u6709\u66f4\u591a\u5339\u914d\u7684\u7ed3\u6784\u4e86" : "\u00a7c\u8303\u56f4\u5185\u6ca1\u627e\u5230\u5339\u914d\u7684\u7ed3\u6784, \u8c03\u5927\u641c\u7d22\u8303\u56f4\u518d\u8bd5");
            return;
        }
        StructureLocatorFeature.rememberTarget(key, bestCx, bestCz);
        int dist = (int)Math.sqrt(bestDistSq);
        StructureLocatorFeature.msg("\u00a7f" + t.name + "  \u00a77\u5750\u6807: \u00a7bX=" + bestX + " Z=" + bestZ + " \u00a77(\u7ea6" + dist + "\u683c)");
        StructureLocatorFeature.doTravel(travel, dimOk, t, bestX, bestZ);
    }

    private static ChunkPos calcRandomSpreadPos(long seed, int salt, int spacing, int separation, String spread, int rx, int rz) {
        RandomSource random = RandomSource.create();
        long key = rx * 341873128712L + rz * 132897987541L + seed + salt;
        random.setSeed(key);
        int offX = random.nextInt(spacing - separation);
        int offZ = random.nextInt(spacing - separation);
        return new ChunkPos(rx * spacing + offX, rz * spacing + offZ);
    }

    private static void locateStronghold(Target t, long seed, boolean travel, boolean dimOk) {
        Minecraft mc = StructureLocatorFeature.getMc();
        if (mc == null) {
            return;
        }
        List<ChunkPos> positions = StructureLocatorFeature.strongholdPositions(seed, t.ringCount, t.ringDist, t.ringSpread);
        int px = (int)mc.player.getX();
        int pz = (int)mc.player.getZ();
        String key = StructureLocatorFeature.blKey(seed, t.id);
        Set<Long> bl = skipped.get(key);
        long bestDistSq = Long.MAX_VALUE;
        int bestX = 0;
        int bestZ = 0;
        int bestCx = 0;
        int bestCz = 0;
        boolean found = false;
        for (ChunkPos cp : positions) {
            int bz;
            int bx;
            long d;
            if (bl != null && bl.contains(StructureLocatorFeature.packChunk(cp.x, cp.z)) || (d = ((bx = cp.x * 16 + 8) - px) * (bx - px) + ((bz = cp.z * 16 + 8) - pz) * (bz - pz)) >= bestDistSq) continue;
            bestDistSq = d;
            bestX = bx;
            bestZ = bz;
            bestCx = cp.x;
            bestCz = cp.z;
            found = true;
        }
        if (!found) {
            StructureLocatorFeature.msg("\u00a7c\u6ca1\u7b97\u51fa\u8981\u4f4d\u7f6e");
            return;
        }
        StructureLocatorFeature.rememberTarget(key, bestCx, bestCz);
        int dist = (int)Math.sqrt(bestDistSq);
        StructureLocatorFeature.msg("\u00a7f" + t.name + " \u00a77(\u540c\u5fc3\u73af, \u5230\u70b9\u9644\u8fd1\u627e\u4f20\u9001\u95e8)  \u00a77\u5750\u6807: \u00a7bX=" + bestX + " Z=" + bestZ + " \u00a77(\u7ea6" + dist + "\u683c)");
        StructureLocatorFeature.doTravel(travel, dimOk, t, bestX, bestZ);
    }

    private static void doTravel(boolean travel, boolean dimOk, Target t, int x, int z) {
        if (!travel) {
            return;
        }
        if (!dimOk) {
            StructureLocatorFeature.msg("\u00a7e\u5f53\u524d\u4e0d\u5728[" + StructureLocatorFeature.dimName(t.dim) + "], \u5df2\u663e\u793a\u5750\u6807");
            return;
        }
        if (!BaritoneBridge.isAvailable()) {
            StructureLocatorFeature.msg("\u00a7e\u672a\u5b89\u88c5 Baritone, \u5df2\u663e\u793a\u5750\u6807");
            return;
        }
        if (markedX >= 0) {
            markedZ = -1;
            markedX = -1;
        }
        BaritoneBridge.gotoCoordSilent(x, 120, z);
        StructureLocatorFeature.msg("\u00a7a\u5df2\u8ba9 Baritone \u524d\u5f80 (goto " + x + " 120 " + z + ")");
    }

    private static void rememberTarget(String key, int cx, int cz) {
        hasLastTarget = true;
        lastTargetKey = key;
        lastTargetCx = cx;
        lastTargetCz = cz;
    }

    private static List<ChunkPos> strongholdPositions(long seed, int count, int distance, int spread) {
        ArrayList<ChunkPos> list = new ArrayList<ChunkPos>();
        WorldgenRandom random = new WorldgenRandom((RandomSource)new LegacyRandomSource(0L));
        random.setLargeFeatureSeed(seed, 0, 0);
        double angle = random.nextDouble() * Math.PI * 2.0;
        int ring = 0;
        int placed = 0;
        int curSpread = spread;
        for (int i = 0; i < count; ++i) {
            double d = 4.0 * distance + (distance * ring * 6) + (random.nextDouble() - 0.5) * distance * 2.5;
            int cx = (int)Math.round(Math.cos(angle) * d);
                        int cz = (int)Math.round(Math.sin(angle) * d);
            list.add(new ChunkPos(cx, cz));
            angle += Math.PI * 2 / curSpread;
            if (++placed != curSpread) continue;
            placed = 0;
            curSpread += 2 * curSpread / (++ring + 1);
            curSpread = Math.min(curSpread, count - i - 1);
            angle += random.nextDouble() * Math.PI * 2.0;
        }
        return list;
    }

    private static String blKey(long seed, String id) {
        return seed + "/" + id;
    }

    private static long packChunk(int cx, int cz) {
        return cx << 32 | cz & 0xFFFFFFFFL;
    }

    private static Dim currentDim() {
        Minecraft mc = StructureLocatorFeature.getMc();
        if (mc == null || mc.level == null) {
            return Dim.OVERWORLD;
        }
        ResourceKey d = mc.level.dimension();
        if (d == Level.NETHER) {
            return Dim.NETHER;
        }
        if (d == Level.END) {
            return Dim.END;
        }
        return Dim.OVERWORLD;
    }

    private static SeedBiomeSampler.Dim toSeedDim(Dim d) {
        return d == Dim.NETHER ? SeedBiomeSampler.Dim.NETHER : (d == Dim.END ? SeedBiomeSampler.Dim.END : SeedBiomeSampler.Dim.OVERWORLD);
    }

    public static String dimName(Dim d) {
        if (d == Dim.NETHER) {
            return "\u4e0b\u754c";
        }
        if (d == Dim.END) {
            return "\u672b\u5730";
        }
        return "\u4e3b\u4e16\u754c";
    }

    public static void msg(String s) {
        Minecraft mc = StructureLocatorFeature.getMc();
        if (mc != null && mc.player != null) {
            mc.player.displayClientMessage(Component.literal((String)(PREFIX + s)), false);
        }
    }

    static {
        TARGETS.add(Target.rs("villages", "\u6751\u5e84", Dim.OVERWORLD, 34, 8, 10387312, "linear", 64, Set.of(Biomes.PLAINS, Biomes.DESERT, Biomes.SAVANNA, Biomes.SNOWY_PLAINS, Biomes.TAIGA)));
        TARGETS.add(Target.rs("desert_pyramids", "\u6c99\u6f20\u795e\u6bbf", Dim.OVERWORLD, 32, 8, 14357617, "linear", 64, Set.of(Biomes.DESERT)));
        TARGETS.add(Target.rs("igloos", "\u96ea\u5c4b", Dim.OVERWORLD, 32, 8, 14357618, "linear", 64, Set.of(Biomes.SNOWY_PLAINS, Biomes.ICE_SPIKES, Biomes.SNOWY_TAIGA)));
        TARGETS.add(Target.rs("jungle_temples", "\u4e1b\u6797\u795e\u5e99", Dim.OVERWORLD, 32, 8, 14357619, "linear", 64, Set.of(Biomes.JUNGLE, Biomes.BAMBOO_JUNGLE)));
        TARGETS.add(Target.rs("swamp_huts", "\u5973\u5deb\u5c0f\u5c4b", Dim.OVERWORLD, 32, 8, 14357620, "linear", 64, Set.of(Biomes.SWAMP)));
        TARGETS.add(Target.rs("pillager_outposts", "\u63a0\u593a\u8005\u524d\u54e8", Dim.OVERWORLD, 32, 8, 165745296, "linear", 64, null));
        TARGETS.add(Target.rs("woodland_mansions", "\u6797\u5730\u5e9c\u90b8", Dim.OVERWORLD, 80, 20, 10387319, "triangular", 64, Set.of(Biomes.DARK_FOREST)));
        TARGETS.add(Target.rs("trail_ruins", "\u53e4\u8ff9\u5e9f\u589f", Dim.OVERWORLD, 34, 8, 83469867, "linear", 64, null));
        TARGETS.add(Target.rs("ruined_portals", "\u5e9f\u5f03\u4f20\u9001\u95e8(\u4e3b\u4e16\u754c)", Dim.OVERWORLD, 40, 15, 34222645, "linear", 64, null));
        TARGETS.add(Target.rs("ocean_monuments", "\u6d77\u5e95\u795e\u6bbf", Dim.OVERWORLD, 32, 5, 10387313, "triangular", 64, Set.of(Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_FROZEN_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_OCEAN)));
        TARGETS.add(Target.rs("ocean_ruins", "\u6d77\u5e95\u5e9f\u589f", Dim.OVERWORLD, 20, 8, 14357621, "linear", 64, null));
        TARGETS.add(Target.rs("shipwrecks", "\u6c89\u8239", Dim.OVERWORLD, 24, 4, 165745295, "linear", 64, null));
        TARGETS.add(Target.rs("ancient_cities", "\u53e4\u57ce(\u76d1\u5b88\u8005)", Dim.OVERWORLD, 24, 8, 20083232, "linear", -50, Set.of(Biomes.DEEP_DARK)));
        TARGETS.add(Target.rs("nether_fossils", "\u4e0b\u754c\u5316\u77f3", Dim.NETHER, 2, 1, 14357921, "linear", 64, Set.of(Biomes.SOUL_SAND_VALLEY)));
        TARGETS.add(Target.rs("nether_complexes", "\u4e0b\u754c\u8981\u585e/\u5821\u5792", Dim.NETHER, 27, 4, 30084232, "linear", 64, null));
        TARGETS.add(Target.rs("end_cities", "\u672b\u5730\u57ce", Dim.END, 20, 11, 10387313, "triangular", 64, Set.of(Biomes.END_MIDLANDS, Biomes.END_HIGHLANDS)));
        TARGETS.add(Target.rings("strongholds", "\u8981\u585e(\u542b\u672b\u5730\u4f20\u9001\u95e8)", 128, 32, 3));
        TARGETS.add(Target.rs("acropolis", "\u00a7c\u536b\u57ce", Dim.OVERWORLD, 80, 50, 913530101, "linear", 64, null));
        TARGETS.add(Target.rs("ancient_factory", "\u00a7c\u8fdc\u53e4\u5de5\u5382", Dim.OVERWORLD, 112, 70, 319514301, "linear", 64, null));
        TARGETS.add(Target.rs("burning_arena", "\u00a7c\u71c3\u70e7\u7ade\u6280\u573a(\u706b\u7130\u5de8\u50cf)", Dim.NETHER, 80, 50, 533522197, "linear", 64, null));
        TARGETS.add(Target.rs("cursed_pyramid", "\u00a7c\u8bc5\u5492\u91d1\u5b57\u5854", Dim.OVERWORLD, 80, 50, 577299997, "linear", 64, null));
        TARGETS.add(Target.rs("frosted_prison", "\u00a7c\u51b0\u5c01\u76d1\u72f1", Dim.OVERWORLD, 80, 50, 1577172143, "linear", 64, null));
        TARGETS.add(Target.rs("ruined_citadel", "\u00a7c\u5e9f\u5f03\u57ce\u5821(\u672b\u5f71\u5b88\u536b)", Dim.END, 50, 25, 367895146, "linear", 64, null));
        TARGETS.add(Target.rs("soul_black_smith", "\u00a7c\u7075\u9b42\u94c1\u5320\u94fa", Dim.NETHER, 60, 50, 1984567320, "linear", 64, null));
        TARGETS.add(Target.rs("sunken_city", "\u00a7c\u6c89\u6ca1\u4e4b\u57ce(\u5229\u7ef4\u5766)", Dim.OVERWORLD, 100, 70, 1673928450, "triangular", 64, null));
        markName = "";
    }

    public static final class Target {
        public final String id;
        public final String name;
        public final String spread;
        public final Dim dim;
        public final Kind kind;
        public final int spacing;
        public final int separation;
        public final int salt;
        public final int ringCount;
        public final int ringDist;
        public final int ringSpread;
        public final int sampleY;
        public final Set<ResourceKey<Biome>> biomes;

        public Target(String id, String name, Dim dim, Kind kind, int spacing, int sep, int salt, String spread, int ringCount, int ringDist, int ringSpread, int sampleY, Set<ResourceKey<Biome>> biomes) {
            this.id = id;
            this.name = name;
            this.dim = dim;
            this.kind = kind;
            this.spacing = spacing;
            this.separation = sep;
            this.salt = salt;
            this.spread = spread;
            this.ringCount = ringCount;
            this.ringDist = ringDist;
            this.ringSpread = ringSpread;
            this.sampleY = sampleY;
            this.biomes = biomes;
        }

        static Target rs(String id, String name, Dim dim, int s, int sep, int salt, String spread, int sy, Set<ResourceKey<Biome>> b) {
            return new Target(id, name, dim, Kind.RANDOM_SPREAD, s, sep, salt, spread, 0, 0, 0, sy, b);
        }

        static Target rings(String id, String name, int c, int d, int sp) {
            return new Target(id, name, Dim.OVERWORLD, Kind.CONCENTRIC_RINGS, 0, 0, 0, "linear", c, d, sp, 64, null);
        }
    }

    public static enum Dim {
        OVERWORLD,
        NETHER,
        END;

    }

    public static enum Kind {
        RANDOM_SPREAD,
        CONCENTRIC_RINGS;

    }
}

