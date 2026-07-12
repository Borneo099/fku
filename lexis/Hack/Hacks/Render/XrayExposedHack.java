package lexis.Hack.Hacks.Render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lexis.Hack.Hack;
import lexis.Hack.Utils.Render.RenderUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class XrayExposedHack extends Hack {
   public static final Set TARGETS = Collections.newSetFromMap(new IdentityHashMap());
   private static final Block[] DEFAULT_ORES;
   private int renderDistance = 64;
   private double opacity = 0.4;
   public final Map renderPositions = new ConcurrentHashMap();
   private final ConcurrentLinkedQueue chunksToScan = new ConcurrentLinkedQueue();
   private final Map lastScanTick = new ConcurrentHashMap();

   public XrayExposedHack() {
      super("Xray(露出版)", new String[]{"真矿石露出六面透视 不管什么反矿透模组和插件"}, Hack.Category.RENDER);
      this.addSetting(new Hack.Setting("渲染距离", "扫描矿石方块的范围", 64, 16, 256));
      this.addSetting(new Hack.Setting("不透明度", "矿石面不透明度", 0.4, 0.05, 1.0));
      MinecraftForge.EVENT_BUS.register(this);
   }

   public void onEnable() {
      this.renderPositions.clear();
      this.lastScanTick.clear();
      this.chunksToScan.clear();
      this.submitNearbyChunks();
   }

   public void onDisable() {
      this.renderPositions.clear();
      this.lastScanTick.clear();
      this.chunksToScan.clear();
   }

   private void submitNearbyChunks() {
      if (mc.f_91073_ != null && mc.f_91074_ != null) {
         int cr = (this.renderDistance >> 4) + 1;
         int pcx = mc.f_91074_.m_20183_().m_123341_() >> 4;
         int pcz = mc.f_91074_.m_20183_().m_123343_() >> 4;

         for(int dx = -cr; dx <= cr; ++dx) {
            for(int dz = -cr; dz <= cr; ++dz) {
               int cx = pcx + dx;
               int cz = pcz + dz;
               long key = (long)cx << 32 | (long)cz & 4294967295L;
               Integer lastTick = (Integer)this.lastScanTick.get(key);
               if ((lastTick == null || mc.f_91074_.f_19797_ - lastTick >= 60) && mc.f_91073_.m_7232_(cx, cz)) {
                  this.chunksToScan.add(mc.f_91073_.m_6325_(cx, cz));
               }
            }
         }

      }
   }

   @SubscribeEvent
   public void onChunkLoad(ChunkEvent.Load event) {
      if (mc.f_91073_ != null) {
         if (event.getLevel() instanceof ClientLevel && event.getChunk() instanceof LevelChunk) {
            this.chunksToScan.add((LevelChunk)event.getChunk());
         }

      }
   }

   @SubscribeEvent
   public void onChunkUnload(ChunkEvent.Unload event) {
      if (event.getChunk() instanceof LevelChunk) {
         LevelChunk chunk = (LevelChunk)event.getChunk();
         int cx = chunk.m_7697_().f_45578_;
         int cz = chunk.m_7697_().f_45579_;
         long ck = (long)cx << 32 | (long)cz & 4294967295L;
         this.lastScanTick.remove(ck);
         this.renderPositions.keySet().removeIf((p) -> {
            return p.m_123341_() >> 4 == cx && p.m_123343_() >> 4 == cz;
         });
      }

   }

   public void onUpdate() {
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         if (s.getName().equals("渲染距离")) {
            this.renderDistance = s.getInt();
         }

         if (s.getName().equals("不透明度")) {
            this.opacity = s.getDouble();
         }
      }

      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         if (mc.f_91074_.f_19797_ % 20 == 0) {
            this.submitNearbyChunks();
         }

         int scanned = 0;

         while(scanned < 3) {
            LevelChunk chunk = (LevelChunk)this.chunksToScan.poll();
            if (chunk == null) {
               break;
            }

            long chunkKey = (long)chunk.m_7697_().f_45578_ << 32 | (long)chunk.m_7697_().f_45579_ & 4294967295L;
            Integer lastTick = (Integer)this.lastScanTick.put(chunkKey, mc.f_91074_.f_19797_);
            if (lastTick == null || mc.f_91074_.f_19797_ - lastTick >= 60) {
               this.scanChunk(chunk);
               ++scanned;
            }
         }

         if (scanned > 0 || mc.f_91074_.f_19797_ % 20 == 0) {
            double maxDistSq = (double)this.renderDistance * (double)this.renderDistance;
            BlockPos playerPos = mc.f_91074_.m_20183_();
            this.renderPositions.entrySet().removeIf((e) -> {
               return ((BlockPos)e.getKey()).m_123331_(playerPos) > maxDistSq;
            });
         }

      }
   }

   private void scanChunk(LevelChunk chunk) {
      int chunkX = chunk.m_7697_().f_45578_ << 4;
      int chunkZ = chunk.m_7697_().f_45579_ << 4;
      int range = this.renderDistance;
      int px = mc.f_91074_.m_20183_().m_123341_();
      int pz = mc.f_91074_.m_20183_().m_123343_();
      int minY = mc.f_91073_.m_141937_();
      int maxY = mc.f_91073_.m_151558_() - 1;
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

      for(int x = 0; x < 16; ++x) {
         int wx = chunkX + x;
         if (Math.abs(wx - px) <= range) {
            for(int z = 0; z < 16; ++z) {
               int wz = chunkZ + z;
               if (Math.abs(wz - pz) <= range) {
                  for(int y = minY; y <= maxY; ++y) {
                     pos.m_122178_(wx, y, wz);
                     Block block = mc.f_91073_.m_8055_(pos).m_60734_();
                     if (TARGETS.contains(block) && (!mc.f_91073_.m_8055_(pos.m_122178_(wx, y - 1, wz)).m_280296_() || !mc.f_91073_.m_8055_(pos.m_122178_(wx, y + 1, wz)).m_280296_() || !mc.f_91073_.m_8055_(pos.m_122178_(wx, y, wz - 1)).m_280296_() || !mc.f_91073_.m_8055_(pos.m_122178_(wx, y, wz + 1)).m_280296_() || !mc.f_91073_.m_8055_(pos.m_122178_(wx - 1, y, wz)).m_280296_() || !mc.f_91073_.m_8055_(pos.m_122178_(wx + 1, y, wz)).m_280296_())) {
                        this.renderPositions.put(new BlockPos(wx, y, wz), getOreColor(block));
                     }
                  }
               }
            }
         }
      }

   }

   public void onClick() {
      this.toggle();
   }

   public void onRender(PoseStack poseStack) {
      if (this.isEnabled() && mc.f_91074_ != null && !this.renderPositions.isEmpty()) {
         Map byColor = new HashMap();
         Iterator var3 = this.renderPositions.entrySet().iterator();

         Map.Entry group;
         while(var3.hasNext()) {
            group = (Map.Entry)var3.next();
            BlockPos pos = (BlockPos)group.getKey();
            int color = (Integer)group.getValue();
            int alpha = (int)(this.opacity * 255.0);
            int packed = (alpha & 255) << 24 | color & 16777215;
            ((List)byColor.computeIfAbsent(packed, (k) -> {
               return new ArrayList();
            })).add(new AABB((double)pos.m_123341_(), (double)pos.m_123342_(), (double)pos.m_123343_(), (double)(pos.m_123341_() + 1), (double)(pos.m_123342_() + 1), (double)(pos.m_123343_() + 1)));
         }

         var3 = byColor.entrySet().iterator();

         while(var3.hasNext()) {
            group = (Map.Entry)var3.next();
            RenderUtils.drawOutlinedBoxes(poseStack, (List)group.getValue(), (Integer)group.getKey(), false);
         }

      }
   }

   private static int getOreColor(Block b) {
      if (b != Blocks.f_50089_ && b != Blocks.f_152474_) {
         if (b != Blocks.f_49995_ && b != Blocks.f_152467_ && b != Blocks.f_49998_) {
            if (b != Blocks.f_49996_ && b != Blocks.f_152468_) {
               if (b != Blocks.f_50264_ && b != Blocks.f_152479_) {
                  if (b != Blocks.f_49997_ && b != Blocks.f_152469_) {
                     if (b != Blocks.f_152505_ && b != Blocks.f_152506_) {
                        if (b != Blocks.f_50173_ && b != Blocks.f_152473_) {
                           if (b != Blocks.f_50059_ && b != Blocks.f_152472_) {
                              if (b == Blocks.f_50331_) {
                                 return -1;
                              } else {
                                 return b == Blocks.f_50722_ ? -3904968 : -65281;
                              }
                           } else {
                              return -14531329;
                           }
                        } else {
                           return -65536;
                        }
                     } else {
                        return -29630;
                     }
                  } else {
                     return -7829368;
                  }
               } else {
                  return -11468976;
               }
            } else {
               return -2841228;
            }
         } else {
            return -10496;
         }
      } else {
         return -11866919;
      }
   }

   static {
      DEFAULT_ORES = new Block[]{Blocks.f_50089_, Blocks.f_152474_, Blocks.f_49995_, Blocks.f_152467_, Blocks.f_49996_, Blocks.f_152468_, Blocks.f_50264_, Blocks.f_152479_, Blocks.f_49997_, Blocks.f_152469_, Blocks.f_152505_, Blocks.f_152506_, Blocks.f_50173_, Blocks.f_152473_, Blocks.f_50059_, Blocks.f_152472_, Blocks.f_49998_, Blocks.f_50331_, Blocks.f_50722_};
      Block[] var0 = DEFAULT_ORES;
      int var1 = var0.length;

      for(int var2 = 0; var2 < var1; ++var2) {
         Block b = var0[var2];
         TARGETS.add(b);
      }

   }
}
