package lexis.Hack.Hacks.Blocks;

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
import lexis.Hack.Utils.Colors.SettingColor;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.RenderListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ScaffoldWalkHack extends Hack implements RenderListener {
   private static final Minecraft mc = Minecraft.m_91087_();
   private HackConfig config;
   private BlockPos currentTarget = null;
   private SettingColor espColor = new SettingColor(255, 0, 0, 255);
   private boolean showESP = true;
   private List renderTargets = new ArrayList();

   public ScaffoldWalkHack() {
      super("简单搭路", new String[]{"简单搭路。。。"}, Hack.Category.BLOCKS, true);
      this.addSetting(new Hack.Setting("显示ESP", "显示目标方框", true));
      this.addSetting(new Hack.Setting("ESP颜色", "方框颜色", this.espColor.getPacked()));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.showESP = this.config.getBooleanSetting("简单搭路", "显示ESP", true);
      int packed = this.config.getIntSetting("简单搭路", "ESP颜色", this.espColor.getPacked());
      this.espColor = new SettingColor(packed);
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "显示ESP":
               setting.setValue(this.showESP);
               break;
            case "ESP颜色":
               setting.setValue(this.espColor.getPacked());
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("简单搭路", this.getSettings());
   }

   public void onEnable() {
      EventManager.add(RenderListener.class, this);
      this.currentTarget = null;
      this.renderTargets.clear();
   }

   public void onDisable() {
      EventManager.remove(RenderListener.class, this);
      HeadOnlyLook.stopLooking();
      this.currentTarget = null;
      this.renderTargets.clear();
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         boolean needSave = false;
         Iterator var2 = this.getSettings().iterator();

         while(var2.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var2.next();
            switch (setting.getName()) {
               case "显示ESP":
                  boolean newShow = setting.getBoolean();
                  if (newShow != this.showESP) {
                     this.showESP = newShow;
                     needSave = true;
                  }
                  break;
               case "ESP颜色":
                  int newPacked = (Integer)setting.getValue();
                  if (newPacked != this.espColor.getPacked()) {
                     this.espColor = new SettingColor(newPacked);
                     needSave = true;
                  }
            }
         }

         if (needSave) {
            this.saveConfig();
         }

         int blockSlot = this.findBlockSlot();
         boolean hasBlocks = blockSlot != -1;
         if (!hasBlocks) {
            if (this.currentTarget != null) {
               this.currentTarget = null;
            }

         } else {
            BlockPos belowPlayer = BlockPos.m_274561_(mc.f_91074_.m_20185_(), mc.f_91074_.m_20186_() - 1.0, mc.f_91074_.m_20189_());
            BlockPos placePos = this.findPlacePos(belowPlayer);
            if (mc.f_91073_.m_8055_(belowPlayer).m_60795_() && placePos != null) {
               if (this.currentTarget == null || !this.currentTarget.equals(placePos)) {
                  this.currentTarget = placePos;
                  Vec3 lookPos = this.getLookPosition(placePos);
                  float[] rotations = this.getNeededRotations(lookPos);
                  HeadOnlyLook.startRotation(rotations[0], rotations[1]);
               }

               int oldSlot = mc.f_91074_.m_150109_().f_35977_;
               mc.f_91074_.m_150109_().f_35977_ = blockSlot;
               this.placeBlock(placePos);
               mc.f_91074_.m_150109_().f_35977_ = oldSlot;
               this.renderTargets.add(new RenderTarget(placePos));
            }

            Iterator it = this.renderTargets.iterator();

            while(it.hasNext()) {
               RenderTarget rt = (RenderTarget)it.next();
               rt.progress -= 0.06F;
               if (rt.progress <= 0.0F) {
                  it.remove();
               }
            }

         }
      }
   }

   public void onRender(PoseStack poseStack, float partialTicks) {
      if (this.showESP && !this.renderTargets.isEmpty()) {
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
         float r = (float)this.espColor.r / 255.0F;
         float g = (float)this.espColor.g / 255.0F;
         float b = (float)this.espColor.b / 255.0F;
         float baseAlpha = (float)this.espColor.a / 255.0F;
         Iterator var10 = this.renderTargets.iterator();

         while(var10.hasNext()) {
            RenderTarget target = (RenderTarget)var10.next();
            BlockPos pos = target.pos;
            float anim = target.progress;
            float a = baseAlpha * anim;
            poseStack.m_85836_();
            poseStack.m_252880_((float)pos.m_123341_(), (float)pos.m_123342_(), (float)pos.m_123343_());
            Matrix4f matrix = poseStack.m_85850_().m_252922_();
            buffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85815_);
            renderBoxFilled(buffer, matrix, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, r, g, b, a * 0.3F);
            tesselator.m_85914_();
            buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
            renderBoxWireframe(buffer, matrix, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, r, g, b, a);
            tesselator.m_85914_();
            poseStack.m_85849_();
         }

         poseStack.m_85849_();
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.lineWidth(1.0F);
      }
   }

   private static void renderBoxFilled(BufferBuilder buffer, Matrix4f matrix, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a) {
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

   private static void renderBoxWireframe(BufferBuilder buffer, Matrix4f matrix, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a) {
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

   private int findBlockSlot() {
      for(int i = 0; i < 9; ++i) {
         ItemStack stack = mc.f_91074_.m_150109_().m_8020_(i);
         if (!stack.m_41619_() && stack.m_41720_() instanceof BlockItem) {
            return i;
         }
      }

      return -1;
   }

   private BlockPos findPlacePos(BlockPos target) {
      if (this.canPlaceAt(target)) {
         return target;
      } else {
         Direction[] var2 = Direction.values();
         int var3 = var2.length;

         int var4;
         Direction side1;
         for(var4 = 0; var4 < var3; ++var4) {
            side1 = var2[var4];
            BlockPos neighbor = target.m_121945_(side1);
            if (this.canPlaceAt(neighbor)) {
               return neighbor;
            }
         }

         var2 = Direction.values();
         var3 = var2.length;

         for(var4 = 0; var4 < var3; ++var4) {
            side1 = var2[var4];
            Direction[] var11 = Direction.values();
            int var7 = var11.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               Direction side2 = var11[var8];
               if (side1.m_122434_() != side2.m_122434_() && side1.m_122434_() != Axis.Y) {
                  BlockPos neighbor = target.m_121945_(side1).m_121945_(side2);
                  if (this.canPlaceAt(neighbor)) {
                     return neighbor;
                  }
               }
            }
         }

         return null;
      }
   }

   private boolean canPlaceAt(BlockPos pos) {
      if (!mc.f_91073_.m_8055_(pos).m_60795_()) {
         return false;
      } else {
         Direction[] var2 = Direction.values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Direction side = var2[var4];
            if (!mc.f_91073_.m_8055_(pos.m_121945_(side)).m_60795_()) {
               return true;
            }
         }

         return false;
      }
   }

   private Vec3 getLookPosition(BlockPos pos) {
      Direction[] var2 = Direction.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Direction side = var2[var4];
         BlockPos neighbor = pos.m_121945_(side);
         if (!mc.f_91073_.m_8055_(neighbor).m_60795_()) {
            return Vec3.m_82512_(neighbor).m_82549_(Vec3.m_82528_(side.m_122424_().m_122436_()).m_82490_(0.5));
         }
      }

      return Vec3.m_82512_(pos);
   }

   private void placeBlock(BlockPos pos) {
      Direction[] var2 = Direction.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Direction side = var2[var4];
         BlockPos neighbor = pos.m_121945_(side);
         if (!mc.f_91073_.m_8055_(neighbor).m_60795_()) {
            Vec3 hitVec = Vec3.m_82512_(neighbor).m_82549_(Vec3.m_82528_(side.m_122424_().m_122436_()).m_82490_(0.5));
            mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, side.m_122424_(), neighbor, false));
            mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
            return;
         }
      }

   }

   private float[] getNeededRotations(Vec3 target) {
      Vec3 eyesPos = mc.f_91074_.m_146892_();
      double diffX = target.f_82479_ - eyesPos.f_82479_;
      double diffY = target.f_82480_ - eyesPos.f_82480_;
      double diffZ = target.f_82481_ - eyesPos.f_82481_;
      double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
      yaw = this.normalizeAngle(yaw);
      pitch = Math.max(-90.0F, Math.min(90.0F, pitch));
      return new float[]{yaw, pitch};
   }

   private float normalizeAngle(float angle) {
      angle %= 360.0F;
      if (angle > 180.0F) {
         angle -= 360.0F;
      }

      if (angle < -180.0F) {
         angle += 360.0F;
      }

      return angle;
   }

   public List getRenderTargets() {
      return this.renderTargets;
   }

   public int getEspColor() {
      return this.espColor.getPacked();
   }

   public boolean shouldShowESP() {
      return this.showESP && !this.renderTargets.isEmpty();
   }

   public void onClick() {
      this.toggle();
   }

   public static class RenderTarget {
      public BlockPos pos;
      public float progress;

      RenderTarget(BlockPos pos) {
         this.pos = pos;
         this.progress = 1.0F;
      }
   }
}
