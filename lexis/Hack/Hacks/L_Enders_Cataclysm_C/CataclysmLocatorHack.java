package lexis.Hack.Hacks.L_Enders_Cataclysm_C;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
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
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public class CataclysmLocatorHack extends Hack {
   private static final String[][] TARGETS = new String[][]{{"burning_arena", "燃烧竞技场(火焰巨像)", "下界"}, {"sunken_city", "沉没之城(利维坦)", "主世界·海洋"}, {"cursed_pyramid", "诅咒金字塔", "主世界·沙漠"}, {"frosted_prison", "冰封监狱", "主世界·雪地"}, {"ruined_citadel", "废弃城堡(末影守卫)", "末地"}, {"ancient_factory", "远古工厂", "主世界·地下"}, {"acropolis", "卫城", "主世界"}, {"soul_black_smith", "灵魂铁匠铺", "下界"}};
   private static final Map CFG = new HashMap();
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
   private static final String PREFIX = "[Lexis] §6[§d灾变的快速找到Boss位置§6] §r";

   public CataclysmLocatorHack() {
      super("灾变的快速找到Boss位置", new String[]{"用世界种子定位灾变 Boss 结构, 算出最近坐标并交给 Baritone(如果你没这模组就会给你坐标手动前往) 前往", "用法: 1 点[取种子](自动发 /seed) → 2 选目标 → 3 点[定位并前往]", "/seed 无权限时, 在[手动种子]里直接填数字", "左键 = 定位并前往; 右键 = 打开设置"}, Hack.Category.CATACLYSM, false);
      String[] names = new String[TARGETS.length];

      for(int i = 0; i < TARGETS.length; ++i) {
         names[i] = TARGETS[i][1];
      }

      this.target = new Hack.Setting("目标结构", "要定位的灾变 Boss 结构", names[0], names);
      this.manualSeed = new Hack.Setting("手动种子", "直接填世界种子(留空则用 /seed 抓到的)", "");
      this.radius = new Hack.Setting("搜索范围(区域)", "向四周搜索多少个结构区域, 越大越慢（会卡 等一会就行了）", 16, 1, 128);
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
      this.addSetting(new Hack.Setting("空的→找下一个", "上一个定位点没建筑时点它: 拉黑该点并给出下一个最近候选", "下一个", this::skipAndNext));
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

   private void requestSeed() {
      if (mc.m_91403_() == null) {
         this.msg("§c未连接服务器");
      } else {
         expectingSeed = true;
         mc.m_91403_().m_246623_("seed");
         this.msg("已发送 /seed, 正在等一下的种子...");
      }
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

   private Long resolveSeed() {
      String ms = this.manualSeed.getString();
      if (ms != null && !ms.trim().isEmpty()) {
         try {
            return Long.parseLong(ms.trim());
         } catch (NumberFormatException var3) {
            this.msg("§c手动种子不是有效数字！！！");
            return null;
         }
      } else {
         return hasSeed ? capturedSeed : null;
      }
   }

   private String selectedSetName() {
      String disp = this.target.getString();
      String[][] var2 = TARGETS;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String[] t = var2[var4];
         if (t[1].equals(disp)) {
            return t[0];
         }
      }

      return TARGETS[0][0];
   }

   private String selectedDimHint() {
      String disp = this.target.getString();
      String[][] var2 = TARGETS;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String[] t = var2[var4];
         if (t[1].equals(disp)) {
            return t[2];
         }
      }

      return "";
   }

   private void locate(boolean travel) {
      if (mc.f_91074_ != null) {
         Long seed = this.resolveSeed();
         if (seed == null) {
            this.msg("§f还没有种子。先点[取种子]或填[手动种子]");
         } else {
            String setName = this.selectedSetName();
            StructCfg cfg = (StructCfg)CFG.get(setName);
            String key = blKey(seed, setName);
            Set bl = (Set)this.skipped.get(key);
            RandomSpreadStructurePlacement placement = this.loadPlacement(setName);
            if (placement == null) {
               this.msg("§c读取结构配置失败: " + setName);
            } else if (!SeedBiomeSampler.ensureReady()) {
               this.msg("§c群系数据初始化失败, 可能非原版地形");
            } else {
               int spacing = placement.m_205003_();
               int r = this.radius.getInt();
               int centerGx = Math.floorDiv(mc.f_91074_.m_146902_().f_45578_, spacing);
               int centerGz = Math.floorDiv(mc.f_91074_.m_146902_().f_45579_, spacing);
               int px = mc.f_91074_.m_146903_();
               int pz = mc.f_91074_.m_146907_();
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
                        if (cfg != null) {
                           ResourceKey biome = SeedBiomeSampler.biomeAt(seed, cfg.dim(), bx, cfg.sampleY(), bz);
                           if (biome == null || !cfg.biomes().contains(biome)) {
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
                  this.msg(dist > 0 ? "§c范围内没有更多匹配的结构了 (已跳过 " + dist + " 个空点, 可点[⑤清空])" : "§c范围内没找到匹配群系的结构, 调大[搜索范围]再试");
               } else {
                  this.hasLastTarget = true;
                  this.lastTargetKey = key;
                  this.lastTargetCx = bestCx;
                  this.lastTargetCz = bestCz;
                  dist = (int)Math.sqrt((double)bestDistSq);
                  this.msg("§f" + this.target.getString());
                  this.msg("§7维度: §f" + this.selectedDimHint() + "  §7坐标: §bX=" + bestX + " Z=" + bestZ + " §7(约 " + dist + " 格)");
                  this.msg("§7到点没建筑就点[④空的→找下一个]");
                  if (travel) {
                     if (!BaritoneBridge.isAvailable()) {
                        this.msg("§e未安装 Baritone, 已显示坐标, 你自己搞手动前往 一直天天不安装baritone 你好有意思啊");
                        return;
                     }

                     BaritoneBridge.executeCommand("goto " + bestX + " " + bestZ);
                     this.msg("§f已让 Baritone 前往 (goto " + bestX + " " + bestZ + ")");
                  }

               }
            }
         }
      }
   }

   private static String blKey(long seed, String setName) {
      return "" + seed + "/" + setName;
   }

   private static long packChunk(int cx, int cz) {
      return (long)cx << 32 | (long)cz & 4294967295L;
   }

   private void skipAndNext() {
      Long seed = this.resolveSeed();
      if (seed == null) {
         this.msg("§f还没有种子");
      } else if (!this.hasLastTarget) {
         this.msg("§e还没定位过, 先点[②定位并前往]");
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
         this.skipped.remove(blKey(seed, this.selectedSetName()));
      }

      this.hasLastTarget = false;
      this.msg("§a已清空当前结构的跳过记录");
   }

   private RandomSpreadStructurePlacement loadPlacement(String setName) {
      try {
         InputStream is = CataclysmLocatorHack.class.getResourceAsStream("/cataclysm_locator/" + setName + ".json");

         String json;
         label52: {
            RandomSpreadStructurePlacement var7;
            try {
               if (is == null) {
                  json = null;
                  break label52;
               }

               json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
               JsonObject root = JsonParser.parseString(json).getAsJsonObject();
               JsonObject placement = root.getAsJsonObject("placement");
               placement.remove("super_exclusion_zone");
               placement.remove("exclusion_zone");
               StructurePlacement sp = (StructurePlacement)StructurePlacement.f_205036_.parse(JsonOps.INSTANCE, placement).result().orElse((Object)null);
               var7 = sp instanceof RandomSpreadStructurePlacement ? (RandomSpreadStructurePlacement)sp : null;
            } catch (Throwable var9) {
               if (is != null) {
                  try {
                     is.close();
                  } catch (Throwable var8) {
                     var9.addSuppressed(var8);
                  }
               }

               throw var9;
            }

            if (is != null) {
               is.close();
            }

            return var7;
         }

         if (is != null) {
            is.close();
         }

         return json;
      } catch (Throwable var10) {
         return null;
      }
   }

   private void msg(String s) {
      if (mc.f_91065_ != null) {
         mc.f_91065_.m_93076_().m_93785_(Component.m_237113_("[Lexis] §6[§d灾变的快速找到Boss位置§6] §r" + s));
      }

   }

   static {
      CFG.put("burning_arena", new StructCfg(SeedBiomeSampler.Dim.NETHER, 64, Set.of(Biomes.f_48209_)));
      CFG.put("soul_black_smith", new StructCfg(SeedBiomeSampler.Dim.NETHER, 64, Set.of(Biomes.f_48200_, Biomes.f_48209_, Biomes.f_48199_, Biomes.f_48201_)));
      CFG.put("sunken_city", new StructCfg(SeedBiomeSampler.Dim.OVERWORLD, 64, Set.of(Biomes.f_48225_, Biomes.f_48170_, Biomes.f_48171_, Biomes.f_48172_)));
      CFG.put("cursed_pyramid", new StructCfg(SeedBiomeSampler.Dim.OVERWORLD, 64, Set.of(Biomes.f_48203_)));
      CFG.put("frosted_prison", new StructCfg(SeedBiomeSampler.Dim.OVERWORLD, 64, Set.of(Biomes.f_186761_)));
      CFG.put("acropolis", new StructCfg(SeedBiomeSampler.Dim.OVERWORLD, 64, Set.of(Biomes.f_48166_)));
      CFG.put("ancient_factory", new StructCfg(SeedBiomeSampler.Dim.OVERWORLD, -16, Set.of(Biomes.f_151784_, Biomes.f_151785_, Biomes.f_220594_)));
      CFG.put("ruined_citadel", new StructCfg(SeedBiomeSampler.Dim.END, 64, Set.of(Biomes.f_48164_, Biomes.f_48163_)));
      capturedSeed = 0L;
      hasSeed = false;
      expectingSeed = false;
      SEED_PATTERN = Pattern.compile("\\[\\s*(-?\\d+)\\s*\\]");
   }

   private static record StructCfg(SeedBiomeSampler.Dim dim, int sampleY, Set biomes) {
      private StructCfg(SeedBiomeSampler.Dim dim, int sampleY, Set biomes) {
         this.dim = dim;
         this.sampleY = sampleY;
         this.biomes = biomes;
      }

      public SeedBiomeSampler.Dim dim() {
         return this.dim;
      }

      public int sampleY() {
         return this.sampleY;
      }

      public Set biomes() {
         return this.biomes;
      }
   }
}
