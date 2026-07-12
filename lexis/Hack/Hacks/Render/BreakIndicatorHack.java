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
import lexis.Hack.events.EventManager;
import lexis.Hack.events.RenderListener;
import lexis.mixin.accessor.ClientPlayerInteractionManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

public class BreakIndicatorHack extends Hack implements RenderListener {
   private static final Minecraft mc = Minecraft.m_91087_();
   private HackConfig config;
   private static final String CONFIG_KEY = "破坏进度";
   private SettingColor startColor = new SettingColor(25, 252, 25, 150);
   private SettingColor endColor = new SettingColor(255, 25, 25, 150);

   public BreakIndicatorHack() {
      super("破坏进度", "显示方块破坏进度", Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("起始颜色", "破坏开始时的颜色", this.startColor.getPacked()));
      this.addSetting(new Hack.Setting("结束颜色", "破坏结束时的颜色", this.endColor.getPacked()));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      int startPacked = this.config.getIntSetting("破坏进度", "起始颜色", this.startColor.getPacked());
      int endPacked = this.config.getIntSetting("破坏进度", "结束颜色", this.endColor.getPacked());
      this.startColor = new SettingColor(startPacked);
      this.endColor = new SettingColor(endPacked);
      Iterator var3 = this.getSettings().iterator();

      while(var3.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var3.next();
         switch (setting.getName()) {
            case "起始颜色":
               setting.setValue(this.startColor.getPacked());
               break;
            case "结束颜色":
               setting.setValue(this.endColor.getPacked());
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("破坏进度", this.getSettings());
   }

   public void onEnable() {
      EventManager.add(RenderListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(RenderListener.class, this);
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "起始颜色":
               int newStart = (Integer)setting.getValue();
               if (newStart != this.startColor.getPacked()) {
                  this.startColor = new SettingColor(newStart);
                  needSave = true;
               }
               break;
            case "结束颜色":
               int newEnd = (Integer)setting.getValue();
               if (newEnd != this.endColor.getPacked()) {
                  this.endColor = new SettingColor(newEnd);
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.saveConfig();
      }

   }

   public void onRender(PoseStack poseStack, float partialTicks) {
      if (mc.f_91073_ != null && mc.f_91074_ != null) {
         float progress = ((ClientPlayerInteractionManagerAccessor)mc.f_91072_).getDestroyProgress();
         BlockPos pos = ((ClientPlayerInteractionManagerAccessor)mc.f_91072_).getDestroyBlockPos();
         if (!(progress <= 0.0F) && pos != null) {
            double shrinkFactor = 1.0 - (double)progress;
            BlockState state = mc.f_91073_.m_8055_(pos);
            VoxelShape shape = state.m_60808_(mc.f_91073_, pos);
            if (!shape.m_83281_()) {
               AABB orig = shape.m_83215_().m_82338_(pos);
               AABB box = orig.m_82310_(orig.m_82362_() * shrinkFactor, orig.m_82376_() * shrinkFactor, orig.m_82385_() * shrinkFactor);
               double xOffset = orig.m_82362_() * shrinkFactor / 2.0;
               double yOffset = orig.m_82376_() * shrinkFactor / 2.0;
               double zOffset = orig.m_82385_() * shrinkFactor / 2.0;
               double x1 = box.f_82288_ + xOffset;
               double y1 = box.f_82289_ + yOffset;
               double z1 = box.f_82290_ + zOffset;
               double x2 = box.f_82291_ + xOffset;
               double y2 = box.f_82292_ + yOffset;
               double z2 = box.f_82293_ + zOffset;
               float startR = (float)this.startColor.r / 255.0F;
               float startG = (float)this.startColor.g / 255.0F;
               float startB = (float)this.startColor.b / 255.0F;
               float startA = (float)this.startColor.a / 255.0F;
               float endR = (float)this.endColor.r / 255.0F;
               float endG = (float)this.endColor.g / 255.0F;
               float endB = (float)this.endColor.b / 255.0F;
               float endA = (float)this.endColor.a / 255.0F;
               float r = startR + (endR - startR) * progress;
               float g = startG + (endG - startG) * progress;
               float b = startB + (endB - startB) * progress;
               float a = startA + (endA - startA) * progress;
               Vec3 cameraPos = mc.f_91063_.m_109153_().m_90583_();
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               RenderSystem.disableCull();
               RenderSystem.disableDepthTest();
               RenderSystem.setShader(GameRenderer::m_172811_);
               RenderSystem.depthMask(false);
               RenderSystem.lineWidth(2.0F);
               poseStack.m_85836_();
               poseStack.m_85837_(-cameraPos.f_82479_, -cameraPos.f_82480_, -cameraPos.f_82481_);
               Tesselator tesselator = Tesselator.m_85913_();
               BufferBuilder buffer = tesselator.m_85915_();
               Matrix4f matrix = poseStack.m_85850_().m_252922_();
               buffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85815_);
               this.renderBoxFaces(buffer, matrix, x1, y1, z1, x2, y2, z2, r, g, b, a * 0.3F);
               tesselator.m_85914_();
               buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
               this.renderBoxWireframe(buffer, matrix, x1, y1, z1, x2, y2, z2, r, g, b, a);
               tesselator.m_85914_();
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

   private void renderBoxFaces(BufferBuilder buffer, Matrix4f matrix, double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float a) {
      buffer.m_252986_(matrix, (float)x1, (float)y1, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y1, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y1, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y1, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y2, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y2, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y2, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y2, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y1, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y2, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y2, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y1, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y1, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y1, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y2, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y2, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y1, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y1, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y2, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y2, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y1, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y2, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y2, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y1, (float)z2).m_85950_(r, g, b, a).m_5752_();
   }

   private void renderBoxWireframe(BufferBuilder buffer, Matrix4f matrix, double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float a) {
      buffer.m_252986_(matrix, (float)x1, (float)y1, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y1, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y1, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y1, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y1, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y1, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y1, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y1, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y2, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y2, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y2, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y2, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y2, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y2, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y2, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y2, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y1, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y2, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y1, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y2, (float)z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y1, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y2, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y1, (float)z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x1, (float)y2, (float)z2).m_85950_(r, g, b, a).m_5752_();
   }

   public void onClick() {
      this.toggle();
   }
}
