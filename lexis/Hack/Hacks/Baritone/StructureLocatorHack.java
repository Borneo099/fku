package lexis.Hack.Hacks.Baritone;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.Cataclysm_C.Seed.SeedBiomeSampler;
import lexis.Hack.Utils.BaritoneBridge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public class StructureLocatorHack extends Hack {
   private static final List TARGETS = new ArrayList();
   private static volatile long capturedSeed;
   private static volatile boolean hasSeed;
   private static volatile boolean expectingSeed;
   private static final Pattern SEED_PATTERN;
   private final Hack.Setting target;
   private final Hack.Setting manualSeed;
   private final Hack.Setting radius;
   private final Map skipped = new HashMap();
   private boolean hasLastTarget = false;
   private String lastTargetKey = "";
   private int lastTargetCx = 0;
   private int lastTargetCz = 0;
   private static final String PREFIX = "[Lexis] §6[§b原版结构定位前往§6] §r";

   private static Target rs(String id, String name, SeedBiomeSampler.Dim dim, int spacing, int sep, int salt, String spread, int sampleY, Set biomes) {
      return new Target(id, name, dim, StructureLocatorHack.Kind.RANDOM_SPREAD, spacing, sep, salt, spread, 0, 0, 0, sampleY, biomes);
   }

   private static Target rings(String id, String name, int count, int dist, int spread) {
      return new Target(id, name, SeedBiomeSampler.Dim.OVERWORLD, StructureLocatorHack.Kind.CONCENTRIC_RINGS, 0, 0, 0, "linear", count, dist, spread, 64, (Set)null);
   }

   public StructureLocatorHack() {
      super("原版结构定位前往", new String[]{"用世界种子定位原版结构, 算最近坐标并交给 Baritone 前往", "用法: 1 点[取种子](自动发 /seed)或填[手动种子] → 2 下拉选结构 → 3 点[定位并前往]", "左键 = 定位并前往; 右键 = 打开设置 (本功能没有开关, 不会一直挂着)", "维度不符(如在主世界选末地城)只显示坐标不前往, 切到对应维度后再点", "概率结构(掠夺者前哨/废弃传送门等)到点没建筑时点[空的→找下一个]"}, Hack.Category.BARITONE, false);
      String[] names = new String[TARGETS.size()];

      for(int i = 0; i < TARGETS.size(); ++i) {
         names[i] = ((Target)TARGETS.get(i)).name();
      }

      this.target = new Hack.Setting("目标结构", "要定位的原版结构", names[0], names);
      this.manualSeed = new Hack.Setting("手动种子", "直接填世界种子(留空则用 /seed 抓到的)", "");
      this.radius = new Hack.Setting("搜索范围(区域)", "向四周搜索多少个结构区域, 越大越慢", 16, 1, 128);
      this.addSetting(this.target);
      this.addSetting(this.manualSeed);
      this.addSetting(this.radius);
      this.addSetting(new Hack.Setting("取种子(/seed)", "自动发送 /seed 并抓取返回的种子", "发送 /seed", this::requestSeed));
      this.addSetting(new Hack.Setting("定位并前往", "算出最近坐标并让 Baritone 前往", "定位+前往", () -> {
         this.locate(true);
      }));
      this.addSetting(new Hack.Setting("只显示坐标", "只算坐标显示在聊天, 不前往", "只定位", () -> {
         this.locate(false);
      }));
      this.addSetting(new Hack.Setting("空的→找下一个", "上一个定位点没建筑时点它: 拉黑该点并给下一个最近候选", "下一个", this::skipAndNext));
      this.addSetting(new Hack.Setting("清空跳过记录", "清空当前结构的跳过名单, 重新从最近的开始", "清空", this::clearSkips));
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.locate(true);
   }

   public static void onChatMessage(String text) {
      if (expectingSeed && text != null) {
         Matcher m = SEED_PATTERN.matcher(text);
         if (m.find()) {
            try {
               capturedSeed = Long.parseLong(m.group(1));
               hasSeed = true;
               expectingSeed = false;
            } catch (NumberFormatException var3) {
            }
         }

      }
   }

   private void requestSeed() {
      if (mc.m_91403_() == null) {
         this.msg("§c未连接服务器");
      } else {
         expectingSeed = true;
         mc.m_91403_().m_246623_("seed");
         this.msg("§7已发送 /seed, 正在等待种子...");
      }
   }

   private Long resolveSeed() {
      String ms = this.manualSeed.getString();
      if (ms != null && !ms.trim().isEmpty()) {
         try {
            return Long.parseLong(ms.trim());
         } catch (NumberFormatException var3) {
            this.msg("§c手动种子不是有效数字");
            return null;
         }
      } else {
         return hasSeed ? capturedSeed : null;
      }
   }

   private Target selectedTarget() {
      String disp = this.target.getString();
      Iterator var2 = TARGETS.iterator();

      Target t;
      do {
         if (!var2.hasNext()) {
            return (Target)TARGETS.get(0);
         }

         t = (Target)var2.next();
      } while(!t.name().equals(disp));

      return t;
   }

   private void locate(boolean travel) {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         Long seed = this.resolveSeed();
         if (seed == null) {
            this.msg("§f还没有种子。先点[取种子]或填[手动种子]");
         } else {
            Target t = this.selectedTarget();
            boolean dimOk = currentDim() == t.dim();
            if (t.kind() == StructureLocatorHack.Kind.CONCENTRIC_RINGS) {
               this.locateStronghold(t, seed, travel, dimOk);
            } else {
               this.locateRandomSpread(t, seed, travel, dimOk);
            }
         }
      }
   }

   private void locateRandomSpread(Target t, long seed, boolean travel, boolean dimOk) {
      RandomSpreadStructurePlacement placement = this.buildRandomSpread(t);
      if (placement == null) {
         this.msg("§c构建结构配置失败: " + t.id());
      } else if (t.biomes() != null && !SeedBiomeSampler.ensureReady()) {
         this.msg("§c群系数据初始化失败, 可能非原版地形");
      } else {
         int spacing = placement.m_205003_();
         int r = this.radius.getInt();
         int centerGx = Math.floorDiv(mc.f_91074_.m_146902_().f_45578_, spacing);
         int centerGz = Math.floorDiv(mc.f_91074_.m_146902_().f_45579_, spacing);
         int px = mc.f_91074_.m_146903_();
         int pz = mc.f_91074_.m_146907_();
         Set bl = (Set)this.skipped.get(blKey(seed, t.id()));
         long bestDistSq = Long.MAX_VALUE;
         int bestX = 0;
         int bestZ = 0;
         int bestCx = 0;
         int bestCz = 0;
         boolean found = false;

         int dist;
         for(dist = centerGx - r; dist <= centerGx + r; ++dist) {
            for(int gz = centerGz - r; gz <= centerGz + r; ++gz) {
               ChunkPos cand = placement.m_227008_(seed, dist * spacing, gz * spacing);
               if (bl == null || !bl.contains(packChunk(cand.f_45578_, cand.f_45579_))) {
                  int bx = cand.f_45578_ * 16 + 8;
                  int bz = cand.f_45579_ * 16 + 8;
                  if (t.biomes() != null) {
                     ResourceKey biome = SeedBiomeSampler.biomeAt(seed, t.dim(), bx, t.sampleY(), bz);
                     if (biome == null || !t.biomes().contains(biome)) {
                        continue;
                     }
                  }

                  long dx = (long)(bx - px);
                  long dz = (long)(bz - pz);
                  long d = dx * dx + dz * dz;
                  if (d < bestDistSq) {
                     bestDistSq = d;
                     bestX = bx;
                     bestZ = bz;
                     bestCx = cand.f_45578_;
                     bestCz = cand.f_45579_;
                     found = true;
                  }
               }
            }
         }

         if (!found) {
            dist = bl == null ? 0 : bl.size();
            this.msg(dist > 0 ? "§c范围内没有更多匹配的结构了 (已跳过 " + dist + " 个空点, 可点[清空跳过记录])" : "§c范围内没找到匹配的结构, 调大[搜索范围]再试");
         } else {
            this.rememberTarget(blKey(seed, t.id()), bestCx, bestCz);
            dist = (int)Math.sqrt((double)bestDistSq);
            this.msg("§f" + t.name());
            this.msg("§7维度: §f" + dimName(t.dim()) + "  §7坐标: §bX=" + bestX + " Z=" + bestZ + " §7(约 " + dist + " 格)");
            this.doTravel(travel, dimOk, t, bestX, bestZ);
         }
      }
   }

   private void locateStronghold(Target t, long seed, boolean travel, boolean dimOk) {
      List positions = strongholdPositions(seed, t.ringCount(), t.ringDist(), t.ringSpread());
      int px = mc.f_91074_.m_146903_();
      int pz = mc.f_91074_.m_146907_();
      Set bl = (Set)this.skipped.get(blKey(seed, t.id()));
      long bestDistSq = Long.MAX_VALUE;
      int bestX = 0;
      int bestZ = 0;
      int bestCx = 0;
      int bestCz = 0;
      boolean found = false;
      Iterator var17 = positions.iterator();

      while(true) {
         ChunkPos cp;
         do {
            if (!var17.hasNext()) {
               if (!found) {
                  this.msg("§c没算出要塞位置 (可点[清空跳过记录])");
                  return;
               }

               this.rememberTarget(blKey(seed, t.id()), bestCx, bestCz);
               int dist = (int)Math.sqrt((double)bestDistSq);
               this.msg("§f" + t.name() + " §7(同心环近似, 到点附近找传送门)");
               this.msg("§7维度: §f主世界  §7坐标: §bX=" + bestX + " Z=" + bestZ + " §7(约 " + dist + " 格)");
               this.doTravel(travel, dimOk, t, bestX, bestZ);
               return;
            }

            cp = (ChunkPos)var17.next();
         } while(bl != null && bl.contains(packChunk(cp.f_45578_, cp.f_45579_)));

         int bx = cp.f_45578_ * 16 + 8;
         int bz = cp.f_45579_ * 16 + 8;
         long dx = (long)(bx - px);
         long dz = (long)(bz - pz);
         long d = dx * dx + dz * dz;
         if (d < bestDistSq) {
            bestDistSq = d;
            bestX = bx;
            bestZ = bz;
            bestCx = cp.f_45578_;
            bestCz = cp.f_45579_;
            found = true;
         }
      }
   }

   private void doTravel(boolean travel, boolean dimOk, Target t, int x, int z) {
      if (travel) {
         if (!dimOk) {
            this.msg("§e当前不在[" + dimName(t.dim()) + "], 已显示坐标。请切到该维度后再开启本功能");
         } else if (!BaritoneBridge.isAvailable()) {
            this.msg("§e未安装 Baritone, 已显示坐标, 请手动前往");
         } else {
            BaritoneBridge.executeCommand("goto " + x + " " + z);
            this.msg("§a已让 Baritone 前往 (goto " + x + " " + z + ")");
         }
      }
   }

   private void rememberTarget(String key, int cx, int cz) {
      this.hasLastTarget = true;
      this.lastTargetKey = key;
      this.lastTargetCx = cx;
      this.lastTargetCz = cz;
   }

   private RandomSpreadStructurePlacement buildRandomSpread(Target t) {
      try {
         JsonObject json = new JsonObject();
         json.addProperty("type", "minecraft:random_spread");
         json.addProperty("salt", t.salt());
         json.addProperty("spacing", t.spacing());
         json.addProperty("separation", t.separation());
         if (t.spread() != null && !t.spread().isEmpty()) {
            json.addProperty("spread_type", t.spread());
         }

         StructurePlacement sp = (StructurePlacement)StructurePlacement.f_205036_.parse(JsonOps.INSTANCE, json).result().orElse((Object)null);
         return sp instanceof RandomSpreadStructurePlacement ? (RandomSpreadStructurePlacement)sp : null;
      } catch (Throwable var4) {
         return null;
      }
   }

   private static List strongholdPositions(long seed, int count, int distance, int spread) {
      List list = new ArrayList();
      WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
      random.m_190068_(seed, 0, 0);
      double angle = random.m_188500_() * Math.PI * 2.0;
      int ring = 0;
      int placedInRing = 0;
      int currentSpread = spread;

      for(int i = 0; i < count; ++i) {
         double d = 4.0 * (double)distance + (double)(distance * ring * 6) + (random.m_188500_() - 0.5) * (double)distance * 2.5;
         int cx = (int)Math.round(Math.cos(angle) * d);
         int cz = (int)Math.round(Math.sin(angle) * d);
         list.add(new ChunkPos(cx, cz));
         angle += 6.283185307179586 / (double)currentSpread;
         ++placedInRing;
         if (placedInRing == currentSpread) {
            ++ring;
            placedInRing = 0;
            currentSpread += 2 * currentSpread / (ring + 1);
            currentSpread = Math.min(currentSpread, count - i - 1);
            angle += random.m_188500_() * Math.PI * 2.0;
         }
      }

      return list;
   }

   private static String blKey(long seed, String id) {
      return "" + seed + "/" + id;
   }

   private static long packChunk(int cx, int cz) {
      return (long)cx << 32 | (long)cz & 4294967295L;
   }

   private void skipAndNext() {
      Long seed = this.resolveSeed();
      if (seed == null) {
         this.msg("§f还没有种子");
      } else if (!this.hasLastTarget) {
         this.msg("§e还没定位过, 先开启本功能或点[只显示坐标]");
      } else {
         ((Set)this.skipped.computeIfAbsent(this.lastTargetKey, (k) -> {
            return new HashSet();
         })).add(packChunk(this.lastTargetCx, this.lastTargetCz));
         this.msg("§7已拉黑空点(区块 " + this.lastTargetCx + "," + this.lastTargetCz + "), 找下一个...");
         this.locate(true);
      }
   }

   private void clearSkips() {
      Long seed = this.resolveSeed();
      if (seed != null) {
         this.skipped.remove(blKey(seed, this.selectedTarget().id()));
      }

      this.hasLastTarget = false;
      this.msg("§a已清空当前结构的跳过记录");
   }

   private static SeedBiomeSampler.Dim currentDim() {
      if (mc.f_91073_ == null) {
         return SeedBiomeSampler.Dim.OVERWORLD;
      } else {
         ResourceKey d = mc.f_91073_.m_46472_();
         if (d == Level.f_46429_) {
            return SeedBiomeSampler.Dim.NETHER;
         } else {
            return d == Level.f_46430_ ? SeedBiomeSampler.Dim.END : SeedBiomeSampler.Dim.OVERWORLD;
         }
      }
   }

   private static String dimName(SeedBiomeSampler.Dim d) {
      switch (d) {
         case NETHER:
            return "下界";
         case END:
            return "末地";
         default:
            return "主世界";
      }
   }

   private void msg(String s) {
      if (mc.f_91065_ != null) {
         mc.f_91065_.m_93076_().m_93785_(Component.m_237113_("[Lexis] §6[§b原版结构定位前往§6] §r" + s));
      }

   }

   static {
      TARGETS.add(rs("villages", "村庄", SeedBiomeSampler.Dim.OVERWORLD, 34, 8, 10387312, "linear", 64, Set.of(Biomes.f_48202_, Biomes.f_48203_, Biomes.f_48157_, Biomes.f_186761_, Biomes.f_48206_)));
      TARGETS.add(rs("desert_pyramids", "沙漠神殿", SeedBiomeSampler.Dim.OVERWORLD, 32, 8, 14357617, "linear", 64, Set.of(Biomes.f_48203_)));
      TARGETS.add(rs("igloos", "雪屋", SeedBiomeSampler.Dim.OVERWORLD, 32, 8, 14357618, "linear", 64, Set.of(Biomes.f_186761_, Biomes.f_48152_, Biomes.f_186756_)));
      TARGETS.add(rs("jungle_temples", "丛林神庙", SeedBiomeSampler.Dim.OVERWORLD, 32, 8, 14357619, "linear", 64, Set.of(Biomes.f_48222_, Biomes.f_48197_)));
      TARGETS.add(rs("swamp_huts", "女巫小屋", SeedBiomeSampler.Dim.OVERWORLD, 32, 8, 14357620, "linear", 64, Set.of(Biomes.f_48207_)));
      TARGETS.add(rs("pillager_outposts", "掠夺者前哨", SeedBiomeSampler.Dim.OVERWORLD, 32, 8, 165745296, "linear", 64, (Set)null));
      TARGETS.add(rs("woodland_mansions", "林地府邸", SeedBiomeSampler.Dim.OVERWORLD, 80, 20, 10387319, "triangular", 64, Set.of(Biomes.f_48151_)));
      TARGETS.add(rs("trail_ruins", "古迹废墟", SeedBiomeSampler.Dim.OVERWORLD, 34, 8, 83469867, "linear", 64, (Set)null));
      TARGETS.add(rs("ruined_portals", "废弃传送门(主世界)", SeedBiomeSampler.Dim.OVERWORLD, 40, 15, 34222645, "linear", 64, (Set)null));
      TARGETS.add(rs("ocean_monuments", "海底神殿", SeedBiomeSampler.Dim.OVERWORLD, 32, 5, 10387313, "triangular", 64, Set.of(Biomes.f_48225_, Biomes.f_48170_, Biomes.f_48171_, Biomes.f_48172_)));
      TARGETS.add(rs("ocean_ruins", "海底废墟", SeedBiomeSampler.Dim.OVERWORLD, 20, 8, 14357621, "linear", 64, (Set)null));
      TARGETS.add(rs("shipwrecks", "沉船", SeedBiomeSampler.Dim.OVERWORLD, 24, 4, 165745295, "linear", 64, (Set)null));
      TARGETS.add(rs("ancient_cities", "古城(监守者)", SeedBiomeSampler.Dim.OVERWORLD, 24, 8, 20083232, "linear", -50, Set.of(Biomes.f_220594_)));
      TARGETS.add(rs("nether_fossils", "下界化石", SeedBiomeSampler.Dim.NETHER, 2, 1, 14357921, "linear", 64, Set.of(Biomes.f_48199_)));
      TARGETS.add(rs("nether_complexes", "下界要塞/堡垒", SeedBiomeSampler.Dim.NETHER, 27, 4, 30084232, "linear", 64, (Set)null));
      TARGETS.add(rs("end_cities", "末地城", SeedBiomeSampler.Dim.END, 20, 11, 10387313, "triangular", 64, Set.of(Biomes.f_48164_, Biomes.f_48163_)));
      TARGETS.add(rings("strongholds", "要塞(含末地传送门)", 128, 32, 3));
      capturedSeed = 0L;
      hasSeed = false;
      expectingSeed = false;
      SEED_PATTERN = Pattern.compile("\\[\\s*(-?\\d+)\\s*\\]");
   }

   private static record Target(String id, String name, SeedBiomeSampler.Dim dim, Kind kind, int spacing, int separation, int salt, String spread, int ringCount, int ringDist, int ringSpread, int sampleY, Set biomes) {
      private Target(String id, String name, SeedBiomeSampler.Dim dim, Kind kind, int spacing, int separation, int salt, String spread, int ringCount, int ringDist, int ringSpread, int sampleY, Set biomes) {
         this.id = id;
         this.name = name;
         this.dim = dim;
         this.kind = kind;
         this.spacing = spacing;
         this.separation = separation;
         this.salt = salt;
         this.spread = spread;
         this.ringCount = ringCount;
         this.ringDist = ringDist;
         this.ringSpread = ringSpread;
         this.sampleY = sampleY;
         this.biomes = biomes;
      }

      public String id() {
         return this.id;
      }

      public String name() {
         return this.name;
      }

      public SeedBiomeSampler.Dim dim() {
         return this.dim;
      }

      public Kind kind() {
         return this.kind;
      }

      public int spacing() {
         return this.spacing;
      }

      public int separation() {
         return this.separation;
      }

      public int salt() {
         return this.salt;
      }

      public String spread() {
         return this.spread;
      }

      public int ringCount() {
         return this.ringCount;
      }

      public int ringDist() {
         return this.ringDist;
      }

      public int ringSpread() {
         return this.ringSpread;
      }

      public int sampleY() {
         return this.sampleY;
      }

      public Set biomes() {
         return this.biomes;
      }
   }

   private static enum Kind {
      RANDOM_SPREAD,
      CONCENTRIC_RINGS;

      // $FF: synthetic method
      private static Kind[] $values() {
         return new Kind[]{RANDOM_SPREAD, CONCENTRIC_RINGS};
      }
   }
}
