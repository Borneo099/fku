package lexis.Hack.Hacks.TaCZ;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class BulletTracersHack extends Hack implements UpdateListener {
   private static BulletTracersHack instance;
   public static boolean tracersActive = false;
   private static Class kineticBulletClass;
   private int tracerColor = -65536;
   private float lineWidth = 2.0F;
   private int maxDistance = 128;
   private HackConfig config;
   private static final String CONFIG_KEY = "子弹透视";

   public BulletTracersHack() {
      super("子弹透视", "渲染 TaCZ 子弹弹道轨迹线", Hack.Category.TACZ, true);
      this.addSetting(new Hack.Setting("颜色", "追踪线颜色", -65536));
      this.addSetting(new Hack.Setting("线宽", "线条粗细", 2.0, 1.0, 10.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("最大距离", "最大渲染距离", 128, 16, 512, Hack.ValueDisplay.INTEGER));
      instance = this;
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.tracerColor = this.config.getIntSetting("子弹透视", "颜色", -65536);
      this.lineWidth = (float)this.config.getDoubleSetting("子弹透视", "线宽", 2.0);
      this.maxDistance = this.config.getIntSetting("子弹透视", "最大距离", 128);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "颜色":
               setting.setValue(this.tracerColor);
               break;
            case "线宽":
               setting.setValue((double)this.lineWidth);
               break;
            case "最大距离":
               setting.setValue(this.maxDistance);
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("子弹透视", this.getSettings());
   }

   public void onEnable() {
      tracersActive = true;
      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      tracersActive = false;
      EventManager.remove(UpdateListener.class, this);
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "颜色":
               if ((Integer)setting.getValue() != this.tracerColor) {
                  this.tracerColor = (Integer)setting.getValue();
                  needSave = true;
               }
               break;
            case "线宽":
               if ((float)setting.getDouble() != this.lineWidth) {
                  this.lineWidth = (float)setting.getDouble();
                  needSave = true;
               }
               break;
            case "最大距离":
               if (setting.getInt() != this.maxDistance) {
                  this.maxDistance = setting.getInt();
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.saveConfig();
      }

   }

   public void onRender(PoseStack poseStack, float partialTicks) {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         Vec3 cameraPos = mc.f_91063_.m_109153_().m_90583_();
         double maxDistSq = (double)this.maxDistance * (double)this.maxDistance;
         float r = (float)(this.tracerColor >> 16 & 255) / 255.0F;
         float g = (float)(this.tracerColor >> 8 & 255) / 255.0F;
         float b = (float)(this.tracerColor & 255) / 255.0F;
         float a = (float)(this.tracerColor >> 24 & 255) / 255.0F;
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.setShader(GameRenderer::m_172811_);
         RenderSystem.lineWidth(this.lineWidth);
         RenderSystem.depthMask(false);
         poseStack.m_85836_();
         poseStack.m_85837_(-cameraPos.f_82479_, -cameraPos.f_82480_, -cameraPos.f_82481_);
         Tesselator tesselator = Tesselator.m_85913_();
         BufferBuilder buffer = tesselator.m_85915_();
         Matrix4f matrix = poseStack.m_85850_().m_252922_();
         buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
         Iterator var13 = mc.f_91073_.m_104735_().iterator();

         while(var13.hasNext()) {
            Entity entity = (Entity)var13.next();
            if (kineticBulletClass != null && kineticBulletClass.isInstance(entity) && !(mc.f_91074_.m_20280_(entity) > maxDistSq)) {
               double px = entity.f_19790_ + (entity.m_20185_() - entity.f_19790_) * (double)partialTicks;
               double py = entity.f_19791_ + (entity.m_20186_() - entity.f_19791_) * (double)partialTicks;
               double pz = entity.f_19792_ + (entity.m_20189_() - entity.f_19792_) * (double)partialTicks;
               Vec3 delta = entity.m_20184_();
               float extX = (float)(px + delta.f_82479_ * 2.0);
               float extY = (float)(py + delta.f_82480_ * 2.0);
               float extZ = (float)(pz + delta.f_82481_ * 2.0);
               buffer.m_252986_(matrix, (float)entity.f_19790_, (float)entity.f_19791_, (float)entity.f_19792_).m_85950_(r, g, b, a).m_5752_();
               buffer.m_252986_(matrix, (float)px, (float)py, (float)pz).m_85950_(r, g, b, a).m_5752_();
               buffer.m_252986_(matrix, (float)px, (float)py, (float)pz).m_85950_(r, g, b, a * 0.5F).m_5752_();
               buffer.m_252986_(matrix, extX, extY, extZ).m_85950_(r, g, b, a * 0.2F).m_5752_();
            }
         }

         tesselator.m_85914_();
         poseStack.m_85849_();
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         RenderSystem.lineWidth(1.0F);
      }
   }

   public void onClick() {
      this.toggle();
   }

   public static BulletTracersHack getInstance() {
      return instance;
   }

   static {
      try {
         kineticBulletClass = Class.forName("com.tacz.guns.entity.EntityKineticBullet", false, BulletTracersHack.class.getClassLoader());
      } catch (Throwable var1) {
      }

   }
}
