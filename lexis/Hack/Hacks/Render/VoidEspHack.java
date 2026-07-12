package lexis.Hack.Hacks.Render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class VoidEspHack extends Hack {
   private static final Direction[] SIDES;
   private static final String CONFIG_KEY = "虚空透视";
   private final List voidHoles = new ArrayList();
   private HackConfig config;
   private int horizontalRadius = 64;
   private int holeHeight = 1;
   private boolean airOnly = false;
   private int fillColor = 853612825;
   private int lineColor = -2024961;
   private float lineWidth = 2.0F;

   public VoidEspHack() {
      super("虚空透视", new String[]{"高亮显示虚空危险区域"}, Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("水平半径", "扫描半径", this.horizontalRadius, 0, 256, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("仅空气", "只检测空气方块(默认检测非基岩)", false));
      this.addSetting(new Hack.Setting("洞高度", "最少连续非基岩层数", this.holeHeight, 1, 5, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("填充颜色", "方框填充色", this.fillColor));
      this.addSetting(new Hack.Setting("线颜色", "线框颜色", this.lineColor));
      this.addSetting(new Hack.Setting("线宽", "边框线宽", (double)this.lineWidth, 1.0, 5.0, Hack.ValueDisplay.DECIMAL));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.horizontalRadius = this.config.getIntSetting("虚空透视", "水平半径", 64);
      this.airOnly = this.config.getBooleanSetting("虚空透视", "仅空气", false);
      this.holeHeight = this.config.getIntSetting("虚空透视", "洞高度", 1);
      this.fillColor = this.config.getIntSetting("虚空透视", "填充颜色", 853612825);
      this.lineColor = this.config.getIntSetting("虚空透视", "线颜色", -2024961);
      this.lineWidth = (float)this.config.getDoubleSetting("虚空透视", "线宽", 2.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "水平半径":
               setting.setValue(this.horizontalRadius);
               break;
            case "仅空气":
               setting.setValue(this.airOnly);
               break;
            case "洞高度":
               setting.setValue(this.holeHeight);
               break;
            case "填充颜色":
               setting.setValue(this.fillColor);
               break;
            case "线颜色":
               setting.setValue(this.lineColor);
               break;
            case "线宽":
               setting.setValue((double)this.lineWidth);
         }
      }

   }

   public void onEnable() {
      this.voidHoles.clear();
   }

   public void onDisable() {
      this.voidHoles.clear();
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      int bottomY;
      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "水平半径":
               if (setting.getInt() != this.horizontalRadius) {
                  this.horizontalRadius = setting.getInt();
                  needSave = true;
               }
               break;
            case "仅空气":
               if (setting.getBoolean() != this.airOnly) {
                  this.airOnly = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "洞高度":
               if (setting.getInt() != this.holeHeight) {
                  this.holeHeight = setting.getInt();
                  needSave = true;
               }
               break;
            case "填充颜色":
               if ((Integer)setting.getValue() != this.fillColor) {
                  this.fillColor = (Integer)setting.getValue();
                  needSave = true;
               }
               break;
            case "线颜色":
               if ((Integer)setting.getValue() != this.lineColor) {
                  this.lineColor = (Integer)setting.getValue();
                  needSave = true;
               }
               break;
            case "线宽":
               if ((float)setting.getDouble() != this.lineWidth) {
                  this.lineWidth = (float)setting.getDouble();
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("虚空透视", this.getSettings());
      }

      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         this.voidHoles.clear();
         int px = mc.f_91074_.m_20183_().m_123341_();
         int pz = mc.f_91074_.m_20183_().m_123343_();
         int r = this.horizontalRadius;
         bottomY = mc.f_91073_.m_141937_();
         BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

         for(int x = px - r; x <= px + r; ++x) {
            for(int z = pz - r; z <= pz + r; ++z) {
               pos.m_122178_(x, bottomY, z);
               if (this.isHole(pos)) {
                  int exclude = this.computeExcludeDir(pos);
                  this.voidHoles.add(new VoidHole(x, bottomY, z, exclude));
               }
            }
         }

      }
   }

   public void onClick() {
      this.toggle();
   }

   private boolean isHole(BlockPos.MutableBlockPos pos) {
      for(int i = 0; i < this.holeHeight; ++i) {
         pos.m_142448_(pos.m_123342_() + i);
         if (this.isBlockWrong(pos)) {
            return false;
         }

         pos.m_142448_(pos.m_123342_() - i);
      }

      return true;
   }

   private boolean isBlockWrong(BlockPos pos) {
      ChunkAccess chunk = mc.f_91073_.m_6522_(pos.m_123341_() >> 4, pos.m_123343_() >> 4, ChunkStatus.f_62326_, false);
      if (chunk == null) {
         return true;
      } else {
         Block block = chunk.m_8055_(pos).m_60734_();
         if (this.airOnly) {
            return block != Blocks.f_50016_;
         } else {
            return block == Blocks.f_50752_;
         }
      }
   }

   private int computeExcludeDir(BlockPos.MutableBlockPos pos) {
      int exclude = 0;
      Direction[] var3 = SIDES;
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Direction side = var3[var5];
         pos.m_122173_(side);
         if (this.isHole(pos)) {
            switch (side) {
               case EAST:
                  exclude |= 1;
                  break;
               case NORTH:
                  exclude |= 2;
                  break;
               case SOUTH:
                  exclude |= 4;
                  break;
               case WEST:
                  exclude |= 8;
            }
         }

         pos.m_122173_(side.m_122424_());
      }

      return exclude;
   }

   public void onRender(PoseStack poseStack, float partialTicks) {
      if (this.isEnabled() && mc.f_91074_ != null && !this.voidHoles.isEmpty()) {
         Vec3 cam = mc.f_91063_.m_109153_().m_90583_();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.setShader(GameRenderer::m_172811_);
         RenderSystem.depthMask(false);
         RenderSystem.lineWidth(this.lineWidth);
         poseStack.m_85836_();
         poseStack.m_85837_(-cam.f_82479_, -cam.f_82480_, -cam.f_82481_);
         Tesselator tess = Tesselator.m_85913_();
         BufferBuilder buf = tess.m_85915_();
         Matrix4f mat = poseStack.m_85850_().m_252922_();
         float fr = (float)(this.fillColor >> 16 & 255) / 255.0F;
         float fg = (float)(this.fillColor >> 8 & 255) / 255.0F;
         float fb = (float)(this.fillColor & 255) / 255.0F;
         float fa = (float)(this.fillColor >> 24 & 255) / 255.0F;
         float lr = (float)(this.lineColor >> 16 & 255) / 255.0F;
         float lg = (float)(this.lineColor >> 8 & 255) / 255.0F;
         float lb = (float)(this.lineColor & 255) / 255.0F;
         float la = (float)(this.lineColor >> 24 & 255) / 255.0F;
         Iterator var15 = this.voidHoles.iterator();

         while(var15.hasNext()) {
            VoidHole hole = (VoidHole)var15.next();
            float x1 = (float)hole.x;
            float y1 = (float)hole.y;
            float z1 = (float)hole.z;
            float x2 = (float)(hole.x + 1);
            float y2 = (float)(hole.y + 1);
            float z2 = (float)(hole.z + 1);
            int ex = hole.excludeDir;
            buf.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85815_);
            renderFaces(buf, mat, x1, y1, z1, x2, y2, z2, fr, fg, fb, fa, ex);
            tess.m_85914_();
            buf.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
            renderWire(buf, mat, x1, y1, z1, x2, y2, z2, lr, lg, lb, la, ex);
            tess.m_85914_();
         }

         poseStack.m_85849_();
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.lineWidth(1.0F);
      }
   }

   private static void renderFaces(BufferBuilder buf, Matrix4f mat, float x1, float y1, float z1, float x2, float y2, float z2, float cr, float cg, float cb, float ca, int ex) {
      if ((ex & 1) == 0) {
         buf.m_252986_(mat, x2, y1, z1).m_85950_(cr, cg, cb, ca).m_5752_();
         buf.m_252986_(mat, x2, y2, z1).m_85950_(cr, cg, cb, ca).m_5752_();
         buf.m_252986_(mat, x2, y2, z2).m_85950_(cr, cg, cb, ca).m_5752_();
         buf.m_252986_(mat, x2, y1, z2).m_85950_(cr, cg, cb, ca).m_5752_();
      }

      if ((ex & 2) == 0) {
         buf.m_252986_(mat, x1, y1, z1).m_85950_(cr, cg, cb, ca).m_5752_();
         buf.m_252986_(mat, x1, y2, z1).m_85950_(cr, cg, cb, ca).m_5752_();
         buf.m_252986_(mat, x2, y2, z1).m_85950_(cr, cg, cb, ca).m_5752_();
         buf.m_252986_(mat, x2, y1, z1).m_85950_(cr, cg, cb, ca).m_5752_();
      }

      if ((ex & 4) == 0) {
         buf.m_252986_(mat, x1, y1, z2).m_85950_(cr, cg, cb, ca).m_5752_();
         buf.m_252986_(mat, x2, y1, z2).m_85950_(cr, cg, cb, ca).m_5752_();
         buf.m_252986_(mat, x2, y2, z2).m_85950_(cr, cg, cb, ca).m_5752_();
         buf.m_252986_(mat, x1, y2, z2).m_85950_(cr, cg, cb, ca).m_5752_();
      }

      if ((ex & 8) == 0) {
         buf.m_252986_(mat, x1, y1, z1).m_85950_(cr, cg, cb, ca).m_5752_();
         buf.m_252986_(mat, x1, y1, z2).m_85950_(cr, cg, cb, ca).m_5752_();
         buf.m_252986_(mat, x1, y2, z2).m_85950_(cr, cg, cb, ca).m_5752_();
         buf.m_252986_(mat, x1, y2, z1).m_85950_(cr, cg, cb, ca).m_5752_();
      }

      buf.m_252986_(mat, x1, y1, z1).m_85950_(cr, cg, cb, ca).m_5752_();
      buf.m_252986_(mat, x2, y1, z1).m_85950_(cr, cg, cb, ca).m_5752_();
      buf.m_252986_(mat, x2, y1, z2).m_85950_(cr, cg, cb, ca).m_5752_();
      buf.m_252986_(mat, x1, y1, z2).m_85950_(cr, cg, cb, ca).m_5752_();
      buf.m_252986_(mat, x1, y2, z1).m_85950_(cr, cg, cb, ca).m_5752_();
      buf.m_252986_(mat, x1, y2, z2).m_85950_(cr, cg, cb, ca).m_5752_();
      buf.m_252986_(mat, x2, y2, z2).m_85950_(cr, cg, cb, ca).m_5752_();
      buf.m_252986_(mat, x2, y2, z1).m_85950_(cr, cg, cb, ca).m_5752_();
   }

   private static void renderWire(BufferBuilder buf, Matrix4f mat, float x1, float y1, float z1, float x2, float y2, float z2, float cr, float cg, float cb, float ca, int ex) {
      if ((ex & 2) == 0) {
         lin(buf, mat, x1, y1, z1, x2, y1, z1, cr, cg, cb, ca);
      }

      if ((ex & 1) == 0) {
         lin(buf, mat, x2, y1, z1, x2, y1, z2, cr, cg, cb, ca);
      }

      if ((ex & 4) == 0) {
         lin(buf, mat, x2, y1, z2, x1, y1, z2, cr, cg, cb, ca);
      }

      if ((ex & 8) == 0) {
         lin(buf, mat, x1, y1, z2, x1, y1, z1, cr, cg, cb, ca);
      }

      if ((ex & 2) == 0) {
         lin(buf, mat, x1, y2, z1, x2, y2, z1, cr, cg, cb, ca);
      }

      if ((ex & 1) == 0) {
         lin(buf, mat, x2, y2, z1, x2, y2, z2, cr, cg, cb, ca);
      }

      if ((ex & 4) == 0) {
         lin(buf, mat, x2, y2, z2, x1, y2, z2, cr, cg, cb, ca);
      }

      if ((ex & 8) == 0) {
         lin(buf, mat, x1, y2, z2, x1, y2, z1, cr, cg, cb, ca);
      }

      if ((ex & 8) == 0 && (ex & 2) == 0) {
         lin(buf, mat, x1, y1, z1, x1, y2, z1, cr, cg, cb, ca);
      }

      if ((ex & 1) == 0 && (ex & 2) == 0) {
         lin(buf, mat, x2, y1, z1, x2, y2, z1, cr, cg, cb, ca);
      }

      if ((ex & 1) == 0 && (ex & 4) == 0) {
         lin(buf, mat, x2, y1, z2, x2, y2, z2, cr, cg, cb, ca);
      }

      if ((ex & 8) == 0 && (ex & 4) == 0) {
         lin(buf, mat, x1, y1, z2, x1, y2, z2, cr, cg, cb, ca);
      }

   }

   private static void lin(BufferBuilder buf, Matrix4f mat, float x1, float y1, float z1, float x2, float y2, float z2, float cr, float cg, float cb, float ca) {
      buf.m_252986_(mat, x1, y1, z1).m_85950_(cr, cg, cb, ca).m_5752_();
      buf.m_252986_(mat, x2, y2, z2).m_85950_(cr, cg, cb, ca).m_5752_();
   }

   static {
      SIDES = new Direction[]{Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST};
   }

   private static class VoidHole {
      final int x;
      final int y;
      final int z;
      final int excludeDir;

      VoidHole(int x, int y, int z, int ex) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.excludeDir = ex;
      }
   }
}
