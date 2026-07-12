package lexis.Hack.Hacks.Render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.Colors.SettingColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

public class BlockSelectionHack extends Hack {
   private boolean advanced = true;
   private boolean oneSide = false;
   private ShapeMode shapeMode;
   private SettingColor sideColor;
   private SettingColor lineColor;
   private boolean hideInside;
   private HackConfig config;
   private static final String CONFIG_KEY = "方块指标";

   public BlockSelectionHack() {
      super("方块指标", "自定义方块选择框的渲染", Hack.Category.RENDER, true);
      this.shapeMode = BlockSelectionHack.ShapeMode.BOTH;
      this.sideColor = new SettingColor(255, 255, 255, 50);
      this.lineColor = new SettingColor(255, 255, 255, 255);
      this.hideInside = true;
      this.addSetting(new Hack.Setting("高级模式", "显示非完整方块的精确形状", true));
      this.addSetting(new Hack.Setting("单面模式", "仅渲染你看向的那一面", false));
      this.addSetting(new Hack.Setting("形状模式", "渲染方式", "线条+填充", new String[]{"仅线条", "仅填充", "线条+填充"}));
      this.addSetting(new Hack.Setting("侧面颜色", "填充颜色", this.sideColor.getPacked()));
      this.addSetting(new Hack.Setting("线条颜色", "线条颜色", this.lineColor.getPacked()));
      this.addSetting(new Hack.Setting("内部隐藏", "当准星在方块内部时隐藏", true));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.advanced = this.config.getBooleanSetting("方块指标", "高级模式", true);
      this.oneSide = this.config.getBooleanSetting("方块指标", "单面模式", false);
      String modeStr = this.config.getStringSetting("方块指标", "形状模式", "线条+填充");
      ShapeMode[] var2 = BlockSelectionHack.ShapeMode.values();
      int lineColorInt = var2.length;

      for(int var4 = 0; var4 < lineColorInt; ++var4) {
         ShapeMode mode = var2[var4];
         if (mode.toString().equals(modeStr)) {
            this.shapeMode = mode;
            break;
         }
      }

      int sideColorInt = this.config.getIntSetting("方块指标", "侧面颜色", this.sideColor.getPacked());
      this.sideColor = new SettingColor(sideColorInt);
      lineColorInt = this.config.getIntSetting("方块指标", "线条颜色", this.lineColor.getPacked());
      this.lineColor = new SettingColor(lineColorInt);
      this.hideInside = this.config.getBooleanSetting("方块指标", "内部隐藏", true);
      Iterator var9 = this.getSettings().iterator();

      while(var9.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var9.next();
         switch (setting.getName()) {
            case "高级模式":
               setting.setValue(this.advanced);
               break;
            case "单面模式":
               setting.setValue(this.oneSide);
               break;
            case "形状模式":
               setting.setValue(this.shapeMode.toString());
               break;
            case "侧面颜色":
               setting.setValue(this.sideColor.getPacked());
               break;
            case "线条颜色":
               setting.setValue(this.lineColor.getPacked());
               break;
            case "内部隐藏":
               setting.setValue(this.hideInside);
         }
      }

   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(true) {
         label73:
         while(var2.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var2.next();
            int newLine;
            switch (setting.getName()) {
               case "高级模式":
                  if (setting.getBoolean() != this.advanced) {
                     this.advanced = setting.getBoolean();
                     needSave = true;
                  }
                  break;
               case "单面模式":
                  if (setting.getBoolean() != this.oneSide) {
                     this.oneSide = setting.getBoolean();
                     needSave = true;
                  }
                  break;
               case "形状模式":
                  String newMode = setting.getString();
                  ShapeMode[] var11 = BlockSelectionHack.ShapeMode.values();
                  newLine = var11.length;
                  int var9 = 0;

                  while(true) {
                     if (var9 >= newLine) {
                        continue label73;
                     }

                     ShapeMode mode = var11[var9];
                     if (mode.toString().equals(newMode) && this.shapeMode != mode) {
                        this.shapeMode = mode;
                        needSave = true;
                        continue label73;
                     }

                     ++var9;
                  }
               case "侧面颜色":
                  int newSide = (Integer)setting.getValue();
                  if (newSide != this.sideColor.getPacked()) {
                     this.sideColor = new SettingColor(newSide);
                     needSave = true;
                  }
                  break;
               case "线条颜色":
                  newLine = (Integer)setting.getValue();
                  if (newLine != this.lineColor.getPacked()) {
                     this.lineColor = new SettingColor(newLine);
                     needSave = true;
                  }
                  break;
               case "内部隐藏":
                  if (setting.getBoolean() != this.hideInside) {
                     this.hideInside = setting.getBoolean();
                     needSave = true;
                  }
            }
         }

         if (needSave) {
            this.config.saveHackSettings("方块指标", this.getSettings());
         }

         return;
      }
   }

   public void onRender(PoseStack poseStack, float partialTicks) {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         if (mc.f_91077_ != null && mc.f_91077_.m_6662_() == Type.BLOCK) {
            BlockHitResult hit = (BlockHitResult)mc.f_91077_;
            if (!this.hideInside || !hit.m_82436_()) {
               BlockPos pos = hit.m_82425_();
               Direction side = hit.m_82434_();
               VoxelShape shape = mc.f_91073_.m_8055_(pos).m_60808_(mc.f_91073_, pos);
               if (!shape.m_83281_()) {
                  Vec3 cameraPos = mc.f_91063_.m_109153_().m_90583_();
                  poseStack.m_85836_();
                  poseStack.m_85837_(-cameraPos.f_82479_, -cameraPos.f_82480_, -cameraPos.f_82481_);
                  RenderSystem.enableBlend();
                  RenderSystem.defaultBlendFunc();
                  RenderSystem.disableCull();
                  RenderSystem.disableDepthTest();
                  RenderSystem.setShader(GameRenderer::m_172811_);
                  RenderSystem.depthMask(false);
                  RenderSystem.lineWidth(2.0F);
                  Tesselator tesselator = Tesselator.m_85913_();
                  BufferBuilder buffer = tesselator.m_85915_();
                  Matrix4f matrix = poseStack.m_85850_().m_252922_();
                  AABB box;
                  if (this.shapeMode == BlockSelectionHack.ShapeMode.SIDES || this.shapeMode == BlockSelectionHack.ShapeMode.BOTH) {
                     buffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85815_);
                     if (this.oneSide) {
                        this.renderOneSide(buffer, matrix, pos, side, shape, this.sideColor);
                     } else if (this.advanced) {
                        shape.m_83286_((minX, minY, minZ, maxX, maxY, maxZ) -> {
                           AABB box = (new AABB(minX, minY, minZ, maxX, maxY, maxZ)).m_82338_(pos);
                           this.renderBoxFilled(buffer, matrix, box, this.sideColor);
                        });
                     } else {
                        box = shape.m_83215_().m_82338_(pos);
                        this.renderBoxFilled(buffer, matrix, box, this.sideColor);
                     }

                     tesselator.m_85914_();
                  }

                  if (this.shapeMode == BlockSelectionHack.ShapeMode.LINES || this.shapeMode == BlockSelectionHack.ShapeMode.BOTH) {
                     buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
                     if (this.oneSide) {
                        this.renderOneSideLines(buffer, matrix, pos, side, shape, this.lineColor);
                     } else if (this.advanced) {
                        shape.m_83286_((minX, minY, minZ, maxX, maxY, maxZ) -> {
                           AABB box = (new AABB(minX, minY, minZ, maxX, maxY, maxZ)).m_82338_(pos);
                           this.renderBoxWireframe(buffer, matrix, box, this.lineColor);
                        });
                     } else {
                        box = shape.m_83215_().m_82338_(pos);
                        this.renderBoxWireframe(buffer, matrix, box, this.lineColor);
                     }

                     tesselator.m_85914_();
                  }

                  poseStack.m_85849_();
                  RenderSystem.depthMask(true);
                  RenderSystem.enableDepthTest();
                  RenderSystem.enableCull();
                  RenderSystem.disableBlend();
                  RenderSystem.lineWidth(1.0F);
               }
            }
         }
      }
   }

   private void renderOneSide(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, Direction side, VoxelShape shape, SettingColor sideColor) {
      AABB box = shape.m_83215_().m_82338_(pos);
      double x1 = box.f_82288_;
      double y1 = box.f_82289_;
      double z1 = box.f_82290_;
      double x2 = box.f_82291_;
      double y2 = box.f_82292_;
      double z2 = box.f_82293_;
      double[] points;
      switch (side) {
         case UP:
            points = new double[]{x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2};
            break;
         case DOWN:
            points = new double[]{x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2};
            break;
         case NORTH:
            points = new double[]{x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1};
            break;
         case SOUTH:
            points = new double[]{x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2};
            break;
         case WEST:
            points = new double[]{x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1};
            break;
         case EAST:
            points = new double[]{x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1};
            break;
         default:
            return;
      }

      this.renderQuadFilled(buffer, matrix, points, sideColor);
   }

   private void renderQuadFilled(BufferBuilder buffer, Matrix4f matrix, double[] points, SettingColor color) {
      float r = (float)color.r / 255.0F;
      float g = (float)color.g / 255.0F;
      float b = (float)color.b / 255.0F;
      float a = (float)color.a / 255.0F * 0.3F;
      buffer.m_252986_(matrix, (float)points[0], (float)points[1], (float)points[2]).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)points[3], (float)points[4], (float)points[5]).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)points[6], (float)points[7], (float)points[8]).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)points[9], (float)points[10], (float)points[11]).m_85950_(r, g, b, a).m_5752_();
   }

   private void renderOneSideLines(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, Direction side, VoxelShape shape, SettingColor lineColor) {
      AABB box = shape.m_83215_().m_82338_(pos);
      double x1 = box.f_82288_;
      double y1 = box.f_82289_;
      double z1 = box.f_82290_;
      double x2 = box.f_82291_;
      double y2 = box.f_82292_;
      double z2 = box.f_82293_;
      double[] points;
      switch (side) {
         case UP:
            points = new double[]{x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2};
            break;
         case DOWN:
            points = new double[]{x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2};
            break;
         case NORTH:
            points = new double[]{x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1};
            break;
         case SOUTH:
            points = new double[]{x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2};
            break;
         case WEST:
            points = new double[]{x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1};
            break;
         case EAST:
            points = new double[]{x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1};
            break;
         default:
            return;
      }

      this.renderQuadWireframe(buffer, matrix, points, lineColor);
   }

   private void renderQuadWireframe(BufferBuilder buffer, Matrix4f matrix, double[] points, SettingColor color) {
      float r = (float)color.r / 255.0F;
      float g = (float)color.g / 255.0F;
      float b = (float)color.b / 255.0F;
      float a = (float)color.a / 255.0F;
      buffer.m_252986_(matrix, (float)points[0], (float)points[1], (float)points[2]).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)points[3], (float)points[4], (float)points[5]).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)points[3], (float)points[4], (float)points[5]).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)points[6], (float)points[7], (float)points[8]).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)points[6], (float)points[7], (float)points[8]).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)points[9], (float)points[10], (float)points[11]).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)points[9], (float)points[10], (float)points[11]).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)points[0], (float)points[1], (float)points[2]).m_85950_(r, g, b, a).m_5752_();
   }

   private void renderBoxFilled(BufferBuilder buffer, Matrix4f matrix, AABB box, SettingColor sideColor) {
      float r = (float)sideColor.r / 255.0F;
      float g = (float)sideColor.g / 255.0F;
      float b = (float)sideColor.b / 255.0F;
      float a = (float)sideColor.a / 255.0F * 0.3F;
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82289_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82289_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82289_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82289_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82292_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82292_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82292_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82292_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82289_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82292_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82292_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82289_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82289_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82289_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82292_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82292_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82289_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82289_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82292_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82292_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82289_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82292_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82292_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82289_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
   }

   private void renderBoxWireframe(BufferBuilder buffer, Matrix4f matrix, AABB box, SettingColor lineColor) {
      float r = (float)lineColor.r / 255.0F;
      float g = (float)lineColor.g / 255.0F;
      float b = (float)lineColor.b / 255.0F;
      float a = (float)lineColor.a / 255.0F;
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82289_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82289_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82289_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82289_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82289_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82289_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82289_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82289_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82292_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82292_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82292_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82292_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82292_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82292_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82292_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82292_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82289_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82292_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82289_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82292_, (float)box.f_82290_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82289_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82291_, (float)box.f_82292_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82289_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)box.f_82288_, (float)box.f_82292_, (float)box.f_82293_).m_85950_(r, g, b, a).m_5752_();
   }

   public void onClick() {
      this.toggle();
   }

   public static enum ShapeMode {
      LINES("仅线条"),
      SIDES("仅填充"),
      BOTH("线条+填充");

      private final String name;

      private ShapeMode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }

      // $FF: synthetic method
      private static ShapeMode[] $values() {
         return new ShapeMode[]{LINES, SIDES, BOTH};
      }
   }
}
