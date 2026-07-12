package lexis.Hack.Hacks.Render;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.Colors.SettingColor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;

public class PortalEspHack extends Hack {
   private EspStyle style;
   private final Map portalGroups;
   private final Map renderPositions;
   private HackConfig config;
   private static final String CONFIG_KEY = "传送门透视";
   private static final File CONFIG_DIR = new File("C:/karucn/Lexis/config/hack/");
   private static final File CONFIG_FILE;
   private static final Gson GSON;
   private final ConcurrentLinkedQueue chunksToScan;
   private final Set scannedChunks;

   public PortalEspHack() {
      super("传送门透视", new String[]{"透视下界传送门、末地传送门等"}, Hack.Category.RENDER, true);
      this.style = PortalEspHack.EspStyle.LINES_AND_BOXES;
      this.portalGroups = new ConcurrentHashMap();
      this.renderPositions = new ConcurrentHashMap();
      this.chunksToScan = new ConcurrentLinkedQueue();
      this.scannedChunks = ConcurrentHashMap.newKeySet();
      this.addSetting(new Hack.Setting("显示样式", "ESP显示样式", "连线+方框", new String[]{"仅方框", "仅连线", "连线+方框", "仅六面", "六面+方框", "六面+连线", "全部"}));
      this.portalGroups.put("minecraft:nether_portal", new PortalGroup("minecraft:nether_portal", -65536));
      this.portalGroups.put("minecraft:end_portal", new PortalGroup("minecraft:end_portal", -16711936));
      this.portalGroups.put("minecraft:end_portal_frame", new PortalGroup("minecraft:end_portal_frame", -16776961));
      this.portalGroups.put("minecraft:end_gateway", new PortalGroup("minecraft:end_gateway", -256));
      this.addSetting(new Hack.Setting("下界传送门颜色", "下界传送门的颜色", ((PortalGroup)this.portalGroups.get("minecraft:nether_portal")).color.getPacked()));
      this.addSetting(new Hack.Setting("末地传送门颜色", "末地传送门的颜色", ((PortalGroup)this.portalGroups.get("minecraft:end_portal")).color.getPacked()));
      this.addSetting(new Hack.Setting("末地传送门框架颜色", "末地传送门框架的颜色", ((PortalGroup)this.portalGroups.get("minecraft:end_portal_frame")).color.getPacked()));
      this.addSetting(new Hack.Setting("末地折跃门颜色", "末地折跃门的颜色", ((PortalGroup)this.portalGroups.get("minecraft:end_gateway")).color.getPacked()));
      this.config = HackConfig.getInstance();
      this.loadConfig();
      this.loadPortalSettings();
      MinecraftForge.EVENT_BUS.register(this);
      this.startScannerThread();
   }

   private void loadConfig() {
      String styleStr = this.config.getStringSetting("传送门透视", "显示样式", "连线+方框");
      this.style = this.getStyleFromString(styleStr);
   }

   private void loadPortalSettings() {
      try {
         if (!CONFIG_FILE.exists()) {
            return;
         }

         FileReader reader = new FileReader(CONFIG_FILE);
         Map saveData = (Map)GSON.fromJson(reader, Map.class);
         reader.close();
         Iterator var4;
         if (saveData.containsKey("显示样式")) {
            String styleStr = (String)saveData.get("显示样式");
            this.style = this.getStyleFromString(styleStr);
            var4 = this.getSettings().iterator();

            while(var4.hasNext()) {
               Hack.Setting setting = (Hack.Setting)var4.next();
               if (setting.getName().equals("显示样式")) {
                  setting.setValue(styleStr);
                  break;
               }
            }
         }

         if (saveData.containsKey("portals")) {
            Map portals = (Map)saveData.get("portals");
            var4 = portals.entrySet().iterator();

            while(var4.hasNext()) {
               Map.Entry entry = (Map.Entry)var4.next();
               PortalGroup group = (PortalGroup)this.portalGroups.get(entry.getKey());
               if (group != null) {
                  group.enabled = (Boolean)((Map)entry.getValue()).getOrDefault("enabled", true);
                  group.color = new SettingColor(((Number)((Map)entry.getValue()).getOrDefault("color", group.color.getPacked())).intValue());
               }
            }
         }
      } catch (Exception var7) {
      }

   }

   public void savePortalSettings() {
      try {
         CONFIG_DIR.mkdirs();
         Map saveData = new HashMap();
         saveData.put("显示样式", this.style.toString());
         Map portals = new HashMap();
         Iterator var3 = this.portalGroups.entrySet().iterator();

         while(var3.hasNext()) {
            Map.Entry entry = (Map.Entry)var3.next();
            Map portalData = new HashMap();
            portalData.put("enabled", ((PortalGroup)entry.getValue()).enabled);
            portalData.put("color", ((PortalGroup)entry.getValue()).color.getPacked());
            portals.put((String)entry.getKey(), portalData);
         }

         saveData.put("portals", portals);
         FileWriter writer = new FileWriter(CONFIG_FILE);
         GSON.toJson(saveData, writer);
         writer.close();
      } catch (Exception var6) {
      }

   }

   private EspStyle getStyleFromString(String name) {
      EspStyle[] var2 = PortalEspHack.EspStyle.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         EspStyle s = var2[var4];
         if (s.toString().equals(name)) {
            return s;
         }
      }

      return PortalEspHack.EspStyle.LINES_AND_BOXES;
   }

   public void onEnable() {
      this.renderPositions.clear();
      this.scannedChunks.clear();
      this.chunksToScan.clear();
      if (mc.f_91073_ != null && mc.f_91074_ != null) {
         int renderDistance = (Integer)mc.f_91066_.m_231984_().m_231551_();
         BlockPos playerPos = mc.f_91074_.m_20183_();
         int playerChunkX = playerPos.m_123341_() >> 4;
         int playerChunkZ = playerPos.m_123343_() >> 4;

         for(int x = -renderDistance; x <= renderDistance; ++x) {
            for(int z = -renderDistance; z <= renderDistance; ++z) {
               int chunkX = playerChunkX + x;
               int chunkZ = playerChunkZ + z;
               if (mc.f_91073_.m_7232_(chunkX, chunkZ)) {
                  LevelChunk chunk = mc.f_91073_.m_6325_(chunkX, chunkZ);
                  this.chunksToScan.add(chunk);
               }
            }
         }
      }

   }

   public void onDisable() {
      this.renderPositions.clear();
      this.scannedChunks.clear();
      this.chunksToScan.clear();
   }

   private void startScannerThread() {
      Thread scanner = new Thread(() -> {
         while(true) {
            while(true) {
               while(true) {
                  try {
                     LevelChunk chunk = (LevelChunk)this.chunksToScan.poll();
                     if (chunk == null) {
                        Thread.sleep(1L);
                     } else {
                        long chunkKey = (long)chunk.m_7697_().f_45578_ << 32 | (long)chunk.m_7697_().f_45579_ & 4294967295L;
                        if (this.scannedChunks.add(chunkKey)) {
                           int chunkX = chunk.m_7697_().f_45578_ << 4;
                           int chunkZ = chunk.m_7697_().f_45579_ << 4;
                           int minY = mc.f_91073_.m_141937_();
                           int maxY = mc.f_91073_.m_151558_();
                           BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

                           for(int x = 0; x < 16; ++x) {
                              for(int z = 0; z < 16; ++z) {
                                 for(int y = minY; y < maxY; ++y) {
                                    pos.m_122178_(chunkX + x, y, chunkZ + z);
                                    BlockState state = mc.f_91073_.m_8055_(pos);
                                    String blockId = ForgeRegistries.BLOCKS.getKey(state.m_60734_()).toString();
                                    PortalGroup group = (PortalGroup)this.portalGroups.get(blockId);
                                    if (group != null && group.enabled) {
                                       this.renderPositions.put(pos.m_7949_(), group);
                                    }
                                 }
                              }
                           }
                        }
                     }
                  } catch (Exception var15) {
                     var15.printStackTrace();
                  }
               }
            }
         }
      }, "PortalScanner");
      scanner.setDaemon(true);
      scanner.start();
   }

   public void updateBlock(BlockPos pos, BlockState state) {
      String blockId = ForgeRegistries.BLOCKS.getKey(state.m_60734_()).toString();
      PortalGroup group = (PortalGroup)this.portalGroups.get(blockId);
      if (group != null && group.enabled) {
         this.renderPositions.put(pos.m_7949_(), group);
         int chunkX = pos.m_123341_() >> 4;
         int chunkZ = pos.m_123343_() >> 4;
         long chunkKey = (long)chunkX << 32 | (long)chunkZ & 4294967295L;
         this.scannedChunks.remove(chunkKey);
         if (mc.f_91073_ != null && mc.f_91073_.m_7232_(chunkX, chunkZ)) {
            LevelChunk chunk = mc.f_91073_.m_6325_(chunkX, chunkZ);
            this.chunksToScan.add(chunk);
         }

      } else {
         this.renderPositions.remove(pos);
      }
   }

   @SubscribeEvent
   public void onChunkLoad(ChunkEvent.Load event) {
      if (event.getLevel() instanceof ClientLevel && event.getChunk() instanceof LevelChunk) {
         this.chunksToScan.add((LevelChunk)event.getChunk());
      }

   }

   @SubscribeEvent
   public void onChunkUnload(ChunkEvent.Unload event) {
      if (event.getChunk() instanceof LevelChunk) {
         LevelChunk chunk = (LevelChunk)event.getChunk();
         int chunkX = chunk.m_7697_().f_45578_;
         int chunkZ = chunk.m_7697_().f_45579_;
         long chunkKey = (long)chunkX << 32 | (long)chunkZ & 4294967295L;
         this.scannedChunks.remove(chunkKey);
         this.renderPositions.keySet().removeIf((pos) -> {
            return pos.m_123341_() >> 4 == chunkX && pos.m_123343_() >> 4 == chunkZ;
         });
      }

   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         Iterator var1 = this.getSettings().iterator();

         while(var1.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var1.next();
            if (setting.getName().equals("显示样式")) {
               String newStyle = setting.getString();
               EspStyle newEspStyle = this.getStyleFromString(newStyle);
               if (this.style != newEspStyle) {
                  this.style = newEspStyle;
                  this.savePortalSettings();
               }
               break;
            }
         }

         boolean needSave = false;
         Iterator var7 = this.getSettings().iterator();

         while(var7.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var7.next();
            PortalGroup group;
            int newColor;
            if (setting.getName().equals("下界传送门颜色")) {
               newColor = (Integer)setting.getValue();
               group = (PortalGroup)this.portalGroups.get("minecraft:nether_portal");
               if (group != null && group.color.getPacked() != newColor) {
                  group.color = new SettingColor(newColor);
                  needSave = true;
               }
            } else if (setting.getName().equals("末地传送门颜色")) {
               newColor = (Integer)setting.getValue();
               group = (PortalGroup)this.portalGroups.get("minecraft:end_portal");
               if (group != null && group.color.getPacked() != newColor) {
                  group.color = new SettingColor(newColor);
                  needSave = true;
               }
            } else if (setting.getName().equals("末地传送门框架颜色")) {
               newColor = (Integer)setting.getValue();
               group = (PortalGroup)this.portalGroups.get("minecraft:end_portal_frame");
               if (group != null && group.color.getPacked() != newColor) {
                  group.color = new SettingColor(newColor);
                  needSave = true;
               }
            } else if (setting.getName().equals("末地折跃门颜色")) {
               newColor = (Integer)setting.getValue();
               group = (PortalGroup)this.portalGroups.get("minecraft:end_gateway");
               if (group != null && group.color.getPacked() != newColor) {
                  group.color = new SettingColor(newColor);
                  needSave = true;
               }
            }
         }

         if (needSave) {
            this.savePortalSettings();
         }

      }
   }

   public void onRender(PoseStack poseStack, float partialTicks) {
      if (this.isEnabled() && mc.f_91074_ != null) {
         Vec3 cameraPos = mc.f_91063_.m_109153_().m_90583_();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.setShader(GameRenderer::m_172811_);
         RenderSystem.depthMask(false);
         RenderSystem.lineWidth(3.0F);
         poseStack.m_85836_();
         poseStack.m_85837_(-cameraPos.f_82479_, -cameraPos.f_82480_, -cameraPos.f_82481_);
         Tesselator tesselator = Tesselator.m_85913_();
         BufferBuilder buffer = tesselator.m_85915_();
         Matrix4f matrix = poseStack.m_85850_().m_252922_();
         Iterator var7 = this.renderPositions.entrySet().iterator();

         while(true) {
            BlockPos pos;
            PortalGroup group;
            VoxelShape shape;
            do {
               if (!var7.hasNext()) {
                  poseStack.m_85849_();
                  RenderSystem.depthMask(true);
                  RenderSystem.enableDepthTest();
                  RenderSystem.enableCull();
                  RenderSystem.disableBlend();
                  RenderSystem.lineWidth(1.0F);
                  return;
               }

               Map.Entry entry = (Map.Entry)var7.next();
               pos = (BlockPos)entry.getKey();
               group = (PortalGroup)entry.getValue();
               BlockState state = mc.f_91073_.m_8055_(pos);
               shape = state.m_60808_(mc.f_91073_, pos);
            } while(shape.m_83281_());

            float[] color = group.getColorF();
            List boxes = new ArrayList();
            Iterator var15 = shape.m_83299_().iterator();

            AABB box;
            while(var15.hasNext()) {
               box = (AABB)var15.next();
               boxes.add(box.m_82386_((double)pos.m_123341_(), (double)pos.m_123342_(), (double)pos.m_123343_()));
            }

            if (this.style.hasSides()) {
               buffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85815_);
               var15 = boxes.iterator();

               while(var15.hasNext()) {
                  box = (AABB)var15.next();
                  this.renderSides(buffer, matrix, box, color[0], color[1], color[2], color[3] * 0.3F);
               }

               tesselator.m_85914_();
            }

            if (this.style.hasBoxes()) {
               buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
               var15 = boxes.iterator();

               while(var15.hasNext()) {
                  box = (AABB)var15.next();
                  this.renderWireframeBox(buffer, matrix, box, color[0], color[1], color[2], color[3]);
               }

               tesselator.m_85914_();
            }

            if (this.style.hasLines()) {
               buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
               Vec3 center = ((Vec3)boxes.stream().map(AABB::m_82399_).reduce(Vec3.f_82478_, Vec3::m_82549_)).m_82490_(1.0 / (double)boxes.size());
               Vec3 eyePos = mc.f_91074_.m_20299_(partialTicks);
               Vec3 crosshairPos = eyePos.m_82549_(mc.f_91074_.m_20154_().m_82490_(2.0));
               buffer.m_252986_(matrix, (float)crosshairPos.f_82479_, (float)crosshairPos.f_82480_, (float)crosshairPos.f_82481_).m_85950_(color[0], color[1], color[2], color[3]).m_5752_();
               buffer.m_252986_(matrix, (float)center.f_82479_, (float)center.f_82480_, (float)center.f_82481_).m_85950_(color[0], color[1], color[2], color[3]).m_5752_();
               tesselator.m_85914_();
            }
         }
      }
   }

   private void renderSides(BufferBuilder buffer, Matrix4f matrix, AABB box, float r, float g, float b, float a) {
      float minX = (float)box.f_82288_;
      float minY = (float)box.f_82289_;
      float minZ = (float)box.f_82290_;
      float maxX = (float)box.f_82291_;
      float maxY = (float)box.f_82292_;
      float maxZ = (float)box.f_82293_;
      buffer.m_252986_(matrix, minX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
   }

   private void renderWireframeBox(BufferBuilder buffer, Matrix4f matrix, AABB box, float r, float g, float b, float a) {
      float minX = (float)box.f_82288_;
      float minY = (float)box.f_82289_;
      float minZ = (float)box.f_82290_;
      float maxX = (float)box.f_82291_;
      float maxY = (float)box.f_82292_;
      float maxZ = (float)box.f_82293_;
      buffer.m_252986_(matrix, minX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
   }

   public void onClick() {
      this.toggle();
   }

   static {
      CONFIG_FILE = new File(CONFIG_DIR, "portalesp.json");
      GSON = (new GsonBuilder()).setPrettyPrinting().create();
   }

   public static enum EspStyle {
      BOXES("仅方框", true, false, false),
      LINES("仅连线", false, true, false),
      LINES_AND_BOXES("连线+方框", true, true, false),
      SIDES_ONLY("仅六面", false, false, true),
      SIDES_AND_BOXES("六面+方框", true, false, true),
      SIDES_AND_LINES("六面+连线", false, true, true),
      ALL("全部", true, true, true);

      private final String name;
      private final boolean boxes;
      private final boolean lines;
      private final boolean sides;

      private EspStyle(String name, boolean boxes, boolean lines, boolean sides) {
         this.name = name;
         this.boxes = boxes;
         this.lines = lines;
         this.sides = sides;
      }

      public String toString() {
         return this.name;
      }

      public boolean hasBoxes() {
         return this.boxes;
      }

      public boolean hasLines() {
         return this.lines;
      }

      public boolean hasSides() {
         return this.sides;
      }

      // $FF: synthetic method
      private static EspStyle[] $values() {
         return new EspStyle[]{BOXES, LINES, LINES_AND_BOXES, SIDES_ONLY, SIDES_AND_BOXES, SIDES_AND_LINES, ALL};
      }
   }

   public static class PortalGroup {
      public boolean enabled = true;
      public SettingColor color = new SettingColor(255, 0, 0, 180);
      public final String blockId;

      public PortalGroup(String blockId, int defaultColor) {
         this.blockId = blockId;
         this.color = new SettingColor(defaultColor);
      }

      public float[] getColorF() {
         return new float[]{(float)this.color.r / 255.0F, (float)this.color.g / 255.0F, (float)this.color.b / 255.0F, (float)this.color.a / 255.0F};
      }
   }
}
