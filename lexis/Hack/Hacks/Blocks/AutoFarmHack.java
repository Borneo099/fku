package lexis.Hack.Hacks.Blocks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.BlockBreaker;
import lexis.Hack.Utils.BlockPlacer;
import lexis.Hack.Utils.BlockUtils;
import lexis.Hack.Utils.InventoryUtils;
import lexis.Hack.Utils.Colors.SettingColor;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.RenderListener;
import lexis.Hack.events.UpdateListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

public class AutoFarmHack extends Hack implements UpdateListener, RenderListener {
   private HackConfig config;
   private static final String CONFIG_KEY = "自动农场";
   private double range = 5.0;
   private boolean replant = true;
   private boolean renderBoxes = true;
   private SettingColor harvestColor = new SettingColor(255, 0, 0, 180);
   private SettingColor plantColor = new SettingColor(0, 255, 0, 180);
   private int operationsPerTick = 1;
   private boolean superFastMode = false;
   private final Map seeds = new HashMap();
   private final Map plants = new HashMap();
   private BlockPos currentTarget = null;
   private boolean isPlanting = false;
   private final Map renderTargets = new HashMap();
   private static final int MAX_RENDER_TARGETS = 30;

   public AutoFarmHack() {
      super("自动农场", "自动收获并重新种植作物", Hack.Category.BLOCKS, true);
      this.addSetting(new Hack.Setting("范围", "检测作物的范围", 5.0, 1.0, 6.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("重新种植", "收获后自动重新种植", true));
      this.addSetting(new Hack.Setting("渲染方框", "显示破坏/放置的方框", true));
      this.addSetting(new Hack.Setting("破坏颜色", "破坏方块的方框颜色", this.harvestColor.getPacked()));
      this.addSetting(new Hack.Setting("放置颜色", "放置方块的方框颜色", this.plantColor.getPacked()));
      this.addSetting(new Hack.Setting("速度", "速度破坏/放置", 1.0, 1.0, 256.0, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("超速模式", "没有速度限制", false));
      this.seeds.put(Blocks.f_50092_, Items.f_42404_);
      this.seeds.put(Blocks.f_50249_, Items.f_42619_);
      this.seeds.put(Blocks.f_50250_, Items.f_42620_);
      this.seeds.put(Blocks.f_50444_, Items.f_42733_);
      this.seeds.put(Blocks.f_50200_, Items.f_42588_);
      this.seeds.put(Blocks.f_50262_, Items.f_42533_);
      this.seeds.put(Blocks.f_50685_, Items.f_42780_);
      this.seeds.put(Blocks.f_50190_, Items.f_42578_);
      this.seeds.put(Blocks.f_50189_, Items.f_42577_);
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.range = this.config.getDoubleSetting("自动农场", "范围", 5.0);
      this.replant = this.config.getBooleanSetting("自动农场", "重新种植", true);
      this.renderBoxes = this.config.getBooleanSetting("自动农场", "渲染方框", true);
      int harvestPacked = this.config.getIntSetting("自动农场", "破坏颜色", this.harvestColor.getPacked());
      int plantPacked = this.config.getIntSetting("自动农场", "放置颜色", this.plantColor.getPacked());
      this.harvestColor = new SettingColor(harvestPacked);
      this.plantColor = new SettingColor(plantPacked);
      this.operationsPerTick = (int)this.config.getDoubleSetting("自动农场", "速度", 1.0);
      this.superFastMode = this.config.getBooleanSetting("自动农场", "超速模式", false);
      Iterator var3 = this.getSettings().iterator();

      while(var3.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var3.next();
         switch (setting.getName()) {
            case "范围":
               setting.setValue(this.range);
               break;
            case "重新种植":
               setting.setValue(this.replant);
               break;
            case "渲染方框":
               setting.setValue(this.renderBoxes);
               break;
            case "破坏颜色":
               setting.setValue(this.harvestColor.getPacked());
               break;
            case "放置颜色":
               setting.setValue(this.plantColor.getPacked());
               break;
            case "速度":
               setting.setValue((double)this.operationsPerTick);
               break;
            case "超速模式":
               setting.setValue(this.superFastMode);
         }
      }

   }

   public void onEnable() {
      EventManager.add(UpdateListener.class, this);
      EventManager.add(RenderListener.class, this);
      this.plants.clear();
      this.renderTargets.clear();
      this.currentTarget = null;
      this.isPlanting = false;
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
      EventManager.remove(RenderListener.class, this);
      this.renderTargets.clear();
      this.currentTarget = null;
      this.isPlanting = false;
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "范围":
               double newRange = setting.getDouble();
               if (newRange != this.range) {
                  this.range = newRange;
                  needSave = true;
               }
               break;
            case "重新种植":
               boolean newReplant = setting.getBoolean();
               if (newReplant != this.replant) {
                  this.replant = newReplant;
                  needSave = true;
               }
               break;
            case "渲染方框":
               boolean newRender = setting.getBoolean();
               if (newRender != this.renderBoxes) {
                  this.renderBoxes = newRender;
                  needSave = true;
               }
               break;
            case "破坏颜色":
               int newHarvest = (Integer)setting.getValue();
               if (newHarvest != this.harvestColor.getPacked()) {
                  this.harvestColor = new SettingColor(newHarvest);
                  needSave = true;
               }
               break;
            case "放置颜色":
               int newPlant = (Integer)setting.getValue();
               if (newPlant != this.plantColor.getPacked()) {
                  this.plantColor = new SettingColor(newPlant);
                  needSave = true;
               }
               break;
            case "速度":
               int newOps = (int)setting.getDouble();
               if (newOps != this.operationsPerTick) {
                  this.operationsPerTick = newOps;
                  needSave = true;
               }
               break;
            case "超速模式":
               boolean newFast = setting.getBoolean();
               if (newFast != this.superFastMode) {
                  this.superFastMode = newFast;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("自动农场", this.getSettings());
      }

      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         int maxOps = this.superFastMode ? 256 : this.operationsPerTick;
         if (maxOps <= 0) {
            maxOps = 1;
         }

         for(int op = 0; op < maxOps; ++op) {
            BlockPos plantPos;
            if (this.replant) {
               plantPos = this.findReplantPosition();
               if (plantPos != null) {
                  if (!this.plantCrop(plantPos)) {
                     break;
                  }
                  continue;
               }
            }

            plantPos = this.findHarvestableCrop();
            if (plantPos == null) {
               break;
            }

            this.harvestCrop(plantPos);
         }

         this.renderTargets.entrySet().removeIf((entry) -> {
            RenderTarget var10000 = (RenderTarget)entry.getValue();
            var10000.progress -= 0.1F;
            return ((RenderTarget)entry.getValue()).progress <= 0.0F;
         });
      }
   }

   public void onRender(PoseStack poseStack, float partialTicks) {
      if (this.isEnabled() && this.renderBoxes && !this.renderTargets.isEmpty()) {
         Vec3 cameraPos = mc.f_91063_.m_109153_().m_90583_();
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShader(GameRenderer::m_172811_);
         RenderSystem.lineWidth(2.0F);
         Tesselator tesselator = Tesselator.m_85913_();
         BufferBuilder buffer = tesselator.m_85915_();
         Iterator var6 = this.renderTargets.entrySet().iterator();

         while(var6.hasNext()) {
            Map.Entry entry = (Map.Entry)var6.next();
            RenderTarget target = (RenderTarget)entry.getValue();
            BlockPos pos = (BlockPos)entry.getKey();
            float anim = target.progress;
            int colorPacked = target.isHarvest ? this.harvestColor.getPacked() : this.plantColor.getPacked();
            float r = (float)(colorPacked >> 16 & 255) / 255.0F;
            float g = (float)(colorPacked >> 8 & 255) / 255.0F;
            float b = (float)(colorPacked & 255) / 255.0F;
            float colorAlpha = (float)(colorPacked >> 24 & 255) / 255.0F;
            float finalAlpha = colorAlpha * anim;
            AABB box = target.box;
            double minX = box.f_82288_;
            double minY = box.f_82289_;
            double minZ = box.f_82290_;
            double maxX = box.f_82291_;
            double maxY = box.f_82292_;
            double maxZ = box.f_82293_;
            poseStack.m_85836_();
            poseStack.m_85837_(-cameraPos.f_82479_, -cameraPos.f_82480_, -cameraPos.f_82481_);
            Matrix4f matrix = poseStack.m_85850_().m_252922_();
            buffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85815_);
            this.renderBoxFaces(buffer, matrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, finalAlpha * 0.3F);
            tesselator.m_85914_();
            buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
            this.renderBoxWireframe(buffer, matrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, finalAlpha);
            tesselator.m_85914_();
            poseStack.m_85849_();
         }

         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         RenderSystem.lineWidth(1.0F);
      }
   }

   private void renderBoxFaces(BufferBuilder buffer, Matrix4f matrix, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a) {
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
   }

   private void renderBoxWireframe(BufferBuilder buffer, Matrix4f matrix, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a) {
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
   }

   private void addRenderTarget(BlockPos pos, boolean isHarvest, AABB box) {
      RenderTarget existing = (RenderTarget)this.renderTargets.get(pos);
      if (existing != null) {
         existing.progress = 1.0F;
         existing.isHarvest = isHarvest;
         existing.box = box;
      } else {
         if (this.renderTargets.size() >= 30) {
            BlockPos oldest = null;
            float minProgress = 1.0F;
            Iterator var7 = this.renderTargets.entrySet().iterator();

            while(var7.hasNext()) {
               Map.Entry entry = (Map.Entry)var7.next();
               if (((RenderTarget)entry.getValue()).progress < minProgress) {
                  minProgress = ((RenderTarget)entry.getValue()).progress;
                  oldest = (BlockPos)entry.getKey();
               }
            }

            if (oldest != null) {
               this.renderTargets.remove(oldest);
            }
         }

         this.renderTargets.put(pos, new RenderTarget(isHarvest, box));
      }

   }

   private BlockPos findHarvestableCrop() {
      Vec3 eyePos = mc.f_91074_.m_146892_();
      double rangeSq = this.range * this.range;
      int blockRange = (int)Math.ceil(this.range);
      List candidates = (List)BlockUtils.getAllInBox(mc.f_91074_.m_20183_(), blockRange).stream().filter((pos) -> {
         return eyePos.m_82557_(Vec3.m_82512_(pos)) <= rangeSq;
      }).filter(this::isHarvestable).sorted(Comparator.comparingDouble((pos) -> {
         return eyePos.m_82557_(Vec3.m_82512_(pos));
      })).collect(Collectors.toList());
      return candidates.isEmpty() ? null : (BlockPos)candidates.get(0);
   }

   private boolean isHarvestable(BlockPos pos) {
      BlockState state = mc.f_91073_.m_8055_(pos);
      Block block = state.m_60734_();
      if (block instanceof CropBlock) {
         return ((CropBlock)block).m_52307_(state);
      } else if (block instanceof NetherWartBlock) {
         return (Integer)state.m_61143_(NetherWartBlock.f_54967_) >= 3;
      } else if (block instanceof CocoaBlock) {
         return (Integer)state.m_61143_(CocoaBlock.f_51736_) >= 2;
      } else if (block instanceof SweetBerryBushBlock) {
         return (Integer)state.m_61143_(SweetBerryBushBlock.f_57244_) >= 3;
      } else {
         if (block instanceof StemBlock) {
            Iterator var4 = Plane.HORIZONTAL.iterator();

            while(var4.hasNext()) {
               Direction dir = (Direction)var4.next();
               BlockPos fruitPos = pos.m_121945_(dir);
               Block fruit = mc.f_91073_.m_8055_(fruitPos).m_60734_();
               if (fruit == Blocks.f_50186_ || fruit == Blocks.f_50133_) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private void harvestCrop(BlockPos pos) {
      HeadOnlyLook.startLookingAt(pos, 200L);
      this.currentTarget = pos;
      this.isPlanting = false;
      if (BlockBreaker.breakOneBlock(pos, true)) {
         AABB box = this.getBlockBoundingBox(pos);
         this.addRenderTarget(pos, true, box);
         if (this.replant && this.seeds.containsKey(mc.f_91073_.m_8055_(pos).m_60734_())) {
            this.plants.remove(pos);
         }
      } else {
         this.currentTarget = null;
      }

   }

   private BlockPos findReplantPosition() {
      Vec3 eyePos = mc.f_91074_.m_146892_();
      double rangeSq = this.range * this.range;
      int blockRange = (int)Math.ceil(this.range);
      List candidates = (List)BlockUtils.getAllInBox(mc.f_91074_.m_20183_(), blockRange).stream().filter((posx) -> {
         return eyePos.m_82557_(Vec3.m_82512_(posx)) <= rangeSq;
      }).filter((posx) -> {
         return mc.f_91073_.m_8055_(posx).m_60795_();
      }).filter((posx) -> {
         BlockPos below = posx.m_7495_();
         return mc.f_91073_.m_8055_(below).m_60713_(Blocks.f_50093_);
      }).sorted(Comparator.comparingDouble((posx) -> {
         return eyePos.m_82557_(Vec3.m_82512_(posx));
      })).collect(Collectors.toList());
      Iterator var6 = candidates.iterator();

      BlockPos pos;
      Item seed;
      do {
         if (!var6.hasNext()) {
            return null;
         }

         pos = (BlockPos)var6.next();
         seed = (Item)this.plants.get(pos);
         if (seed == null) {
            Iterator var9 = this.seeds.values().iterator();

            while(var9.hasNext()) {
               Item s = (Item)var9.next();
               if (InventoryUtils.hasItem(s)) {
                  seed = s;
                  this.plants.put(pos, s);
                  break;
               }
            }
         }
      } while(seed == null || !InventoryUtils.hasItem(seed));

      return pos;
   }

   private boolean plantCrop(BlockPos pos) {
      Item seed = (Item)this.plants.get(pos);
      if (seed == null) {
         return false;
      } else if (!InventoryUtils.hasItem(seed)) {
         this.plants.remove(pos);
         return false;
      } else if (!InventoryUtils.selectItem(seed)) {
         return false;
      } else {
         HeadOnlyLook.startLookingAt(pos, 200L);
         this.currentTarget = pos;
         this.isPlanting = true;
         BlockHitResult hitResult = BlockPlacer.getBlockPlacingParams(pos);
         if (hitResult != null && mc.f_91074_.m_20238_(hitResult.m_82450_()) <= this.range * this.range) {
            mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, hitResult);
            mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
            AABB box = new AABB((double)pos.m_123341_(), (double)pos.m_123342_(), (double)pos.m_123343_(), (double)(pos.m_123341_() + 1), (double)(pos.m_123342_() + 1), (double)(pos.m_123343_() + 1));
            this.addRenderTarget(pos, false, box);
            return true;
         } else {
            this.currentTarget = null;
            return false;
         }
      }
   }

   private AABB getBlockBoundingBox(BlockPos pos) {
      BlockState state = mc.f_91073_.m_8055_(pos);
      VoxelShape shape = state.m_60808_(mc.f_91073_, pos);
      if (shape.m_83281_()) {
         return new AABB((double)pos.m_123341_(), (double)pos.m_123342_(), (double)pos.m_123343_(), (double)(pos.m_123341_() + 1), (double)(pos.m_123342_() + 1), (double)(pos.m_123343_() + 1));
      } else {
         AABB bounds = shape.m_83215_();
         return new AABB((double)pos.m_123341_() + bounds.f_82288_, (double)pos.m_123342_() + bounds.f_82289_, (double)pos.m_123343_() + bounds.f_82290_, (double)pos.m_123341_() + bounds.f_82291_, (double)pos.m_123342_() + bounds.f_82292_, (double)pos.m_123343_() + bounds.f_82293_);
      }
   }

   public void onClick() {
      this.toggle();
   }

   public static class RenderTarget {
      public float progress = 1.0F;
      public boolean isHarvest;
      public AABB box;

      public RenderTarget(boolean isHarvest, AABB box) {
         this.isHarvest = isHarvest;
         this.box = box;
      }
   }
}
