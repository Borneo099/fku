package lexis.Hack.Hacks.Blocks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.Iterator;
import java.util.Random;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.RenderListener;
import lexis.mixin.accessor.MultiPlayerGameModeAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.joml.Matrix4f;

public class FastBreakHack extends Hack implements RenderListener {
   private HackConfig config;
   private static final String CONFIG_KEY = "快速破坏";
   private static final Minecraft mc = Minecraft.m_91087_();
   private double activationChance = 1.0;
   private boolean legitMode = false;
   private boolean lookAtBlock = true;
   private boolean renderBlock = true;
   private final Random random = new Random();
   private BlockPos lastBlockPos;
   private BlockPos currentTarget;
   private boolean fastBreakBlock;
   private boolean wasDestroying = false;
   private int tickCounter = 0;

   public FastBreakHack() {
      super("快速破坏", "加快方块破坏速度", Hack.Category.BLOCKS, true);
      this.addSetting(new Hack.Setting("激活几率", "触发快速破坏的几率 (0-1)", 1.0, 0.0, 1.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("合法模式", "只移除破坏延迟，不加速", false));
      this.addSetting(new Hack.Setting("看向方块", "自动看向破坏的方块", true));
      this.addSetting(new Hack.Setting("渲染方块", "显示当前破坏的方块", true));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.activationChance = this.config.getDoubleSetting("快速破坏", "激活几率", 1.0);
      this.legitMode = this.config.getBooleanSetting("快速破坏", "合法模式", false);
      this.lookAtBlock = this.config.getBooleanSetting("快速破坏", "看向方块", true);
      this.renderBlock = this.config.getBooleanSetting("快速破坏", "渲染方块", true);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "激活几率":
               setting.setValue(this.activationChance);
               break;
            case "合法模式":
               setting.setValue(this.legitMode);
               break;
            case "看向方块":
               setting.setValue(this.lookAtBlock);
               break;
            case "渲染方块":
               setting.setValue(this.renderBlock);
         }
      }

   }

   public void onEnable() {
      EventManager.add(RenderListener.class, this);
      this.lastBlockPos = null;
      this.currentTarget = null;
      this.fastBreakBlock = false;
      this.wasDestroying = false;
      this.tickCounter = 0;
   }

   public void onDisable() {
      EventManager.remove(RenderListener.class, this);
      this.lastBlockPos = null;
      this.currentTarget = null;
      if (this.lookAtBlock) {
         HeadOnlyLook.stopLooking();
      }

   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "激活几率":
               double newChance = setting.getDouble();
               if (newChance != this.activationChance) {
                  this.activationChance = newChance;
                  needSave = true;
               }
               break;
            case "合法模式":
               boolean newLegit = setting.getBoolean();
               if (newLegit != this.legitMode) {
                  this.legitMode = newLegit;
                  needSave = true;
               }
               break;
            case "看向方块":
               boolean newLook = setting.getBoolean();
               if (newLook != this.lookAtBlock) {
                  this.lookAtBlock = newLook;
                  needSave = true;
               }
               break;
            case "渲染方块":
               boolean newRender = setting.getBoolean();
               if (newRender != this.renderBlock) {
                  this.renderBlock = newRender;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("快速破坏", this.getSettings());
      }

      ++this.tickCounter;
      if (this.tickCounter % 2 == 0) {
         boolean isDestroying = mc.f_91072_ != null && mc.f_91072_.m_105296_();
         BlockPos currentBlockPos = null;
         Direction currentDirection = Direction.UP;
         if (mc.f_91077_ != null && mc.f_91077_.m_6662_() == Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult)mc.f_91077_;
            currentBlockPos = blockHit.m_82425_();
            currentDirection = blockHit.m_82434_();
         }

         if (isDestroying && currentBlockPos != null) {
            this.currentTarget = currentBlockPos;
            if (this.lookAtBlock) {
               HeadOnlyLook.startLookingAt(currentBlockPos);
            }

            if (!currentBlockPos.equals(this.lastBlockPos)) {
               this.lastBlockPos = currentBlockPos;
               this.fastBreakBlock = this.random.nextDouble() <= this.activationChance;
            }

            if (this.fastBreakBlock && !this.legitMode && mc.m_91403_() != null) {
               mc.m_91403_().m_104955_(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, currentBlockPos, currentDirection));
            }

            this.wasDestroying = true;
         } else {
            if (this.wasDestroying) {
               this.lastBlockPos = null;
               this.fastBreakBlock = false;
               this.wasDestroying = false;
            }

            this.currentTarget = null;
         }

         if (this.legitMode && mc.f_91072_ != null) {
            ((MultiPlayerGameModeAccessor)mc.f_91072_).setDestroyDelay(0);
         }

      }
   }

   public void onRender(PoseStack poseStack, float partialTicks) {
      if (this.renderBlock && this.currentTarget != null && this.isEnabled()) {
         Vec3 cameraPos = mc.f_91063_.m_109153_().m_90583_();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.setShader(GameRenderer::m_172811_);
         RenderSystem.lineWidth(3.0F);
         poseStack.m_85836_();
         poseStack.m_85837_(-cameraPos.f_82479_, -cameraPos.f_82480_, -cameraPos.f_82481_);
         Tesselator tesselator = Tesselator.m_85913_();
         BufferBuilder buffer = tesselator.m_85915_();
         Matrix4f matrix = poseStack.m_85850_().m_252922_();
         buffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85815_);
         this.renderBoxFaces(buffer, matrix, this.currentTarget, 1.0F, 0.0F, 0.0F, 0.3F);
         tesselator.m_85914_();
         buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
         this.renderBoxWireframe(buffer, matrix, this.currentTarget, 1.0F, 0.0F, 0.0F, 1.0F);
         tesselator.m_85914_();
         poseStack.m_85849_();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         RenderSystem.lineWidth(1.0F);
      }
   }

   private void renderBoxFaces(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, float r, float g, float b, float a) {
      float minX = (float)pos.m_123341_();
      float minY = (float)pos.m_123342_();
      float minZ = (float)pos.m_123343_();
      float maxX = (float)(pos.m_123341_() + 1);
      float maxY = (float)(pos.m_123342_() + 1);
      float maxZ = (float)(pos.m_123343_() + 1);
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

   private void renderBoxWireframe(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, float r, float g, float b, float a) {
      float minX = (float)pos.m_123341_();
      float minY = (float)pos.m_123342_();
      float minZ = (float)pos.m_123343_();
      float maxX = (float)(pos.m_123341_() + 1);
      float maxY = (float)(pos.m_123342_() + 1);
      float maxZ = (float)(pos.m_123343_() + 1);
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
}
