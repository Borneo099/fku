package lexis.Hack.Hacks.Render;

import lexis.Hack.Hack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SelfNametagHack extends Hack {
   public SelfNametagHack() {
      super("显示自己名称", new String[]{"第三人称头顶显示自己名称"}, Hack.Category.RENDER, true);
   }

   public void onEnable() {
      MinecraftForge.EVENT_BUS.register(this);
   }

   public void onDisable() {
      MinecraftForge.EVENT_BUS.unregister(this);
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }

   @SubscribeEvent
   public void onRenderGui(RenderGuiEvent.Post event) {
      if (this.isEnabled()) {
         Minecraft mc = Minecraft.m_91087_();
         if (mc.f_91074_ != null && mc.f_91073_ != null) {
            if (mc.f_91066_ != null && !mc.f_91066_.m_92176_().m_90612_()) {
               float pt = event.getPartialTick();
               GuiGraphics gfx = event.getGuiGraphics();
               Camera cam = mc.f_91063_.m_109153_();
               Vec3 camPos = cam.m_90583_();
               int sw = mc.m_91268_().m_85445_();
               int sh = mc.m_91268_().m_85446_();
               float camYaw = cam.m_90590_();
               float camPitch = cam.m_90589_();
               float cosYaw = Mth.m_14089_(-camYaw * 0.017453292F - 3.1415927F);
               float sinYaw = Mth.m_14031_(-camYaw * 0.017453292F - 3.1415927F);
               float cosPitch = -Mth.m_14089_(-camPitch * 0.017453292F);
               float sinPitch = Mth.m_14031_(-camPitch * 0.017453292F);
               float fx = sinYaw * cosPitch;
               float fz = cosYaw * cosPitch;
               float ry = 0.0F;
               float rz = -sinYaw;
               float ux = ry * fz - rz * sinPitch;
               float uy = rz * fx - cosYaw * fz;
               float uz = cosYaw * sinPitch - ry * fx;
               double fovTan = Math.tan(Math.toRadians((double)(Integer)mc.f_91066_.m_231837_().m_231551_() / 2.0));
               double fovScale = (double)sh / 2.0 / fovTan;
               Font font = mc.f_91062_;
               Vec3 pos = mc.f_91074_.m_20318_(pt).m_82520_(0.0, (double)mc.f_91074_.m_20206_() + 0.5, 0.0);
               Vec3 rel = pos.m_82546_(camPos);
               float rlx = (float)rel.f_82479_;
               float rly = (float)rel.f_82480_;
               float rlz = (float)rel.f_82481_;
               double dotFwd = (double)(rlx * fx + rly * sinPitch + rlz * fz);
               if (!(dotFwd < 0.2)) {
                  double dotRight = (double)(rlx * cosYaw + rly * ry + rlz * rz);
                  double dotUpf = (double)(rlx * ux + rly * uy + rlz * uz);
                  double scale = fovScale / dotFwd;
                  float sx = (float)((double)sw / 2.0 + dotRight * scale);
                  float sy = (float)((double)sh / 2.0 - dotUpf * scale);
                  String name = mc.f_91074_.m_36316_().getName();
                  int w = font.m_92895_(name);
                  gfx.m_280056_(font, name, (int)(sx - (float)w / 2.0F) + 1, (int)sy + 1, Integer.MIN_VALUE, false);
                  gfx.m_280056_(font, name, (int)(sx - (float)w / 2.0F), (int)sy, -1, false);
               }
            }
         }
      }
   }
}
