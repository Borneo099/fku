package lexis.Hack.Hacks.Render;

import java.io.IOException;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.mixin.accessor.PostChainAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;

public class MotionBlurHack extends Hack {
   public static MotionBlurHack INSTANCE;
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final ResourceLocation SHADER_LOCATION = new ResourceLocation("lexis", "shaders/post/motion_blur.json");
   private PostChain postChain;
   private double strength = 0.5;
   private int prevWidth = -1;
   private int prevHeight = -1;
   private boolean shaderLoadFailed = false;

   public MotionBlurHack() {
      super("动态模糊", new String[]{"移动/转视角时拖影效果", "别玩太久了会头痛！"}, Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("模糊强度", "拖影长度（0=无, 0.9=最强拖影）", this.strength, 0.0, 0.9, Hack.ValueDisplay.DECIMAL));
      INSTANCE = this;
      this.loadConfig();
   }

   private void loadConfig() {
      this.strength = HackConfig.getInstance().getDoubleSetting("动态模糊", "模糊强度", 0.5);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("模糊强度")) {
            setting.setValue(this.strength);
         }
      }

   }

   public void onEnable() {
      this.shaderLoadFailed = false;
   }

   public void onDisable() {
      this.closePostChain();
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         if (setting.getName().equals("模糊强度") && setting.getDouble() != this.strength) {
            this.strength = setting.getDouble();
            needSave = true;
         }
      }

      if (needSave) {
         HackConfig.getInstance().saveHackSettings("动态模糊", this.getSettings());
      }

   }

   public void renderShaders(float partialTick) {
      if (this.isEnabled()) {
         if (this.postChain == null && !this.shaderLoadFailed) {
            this.loadPostChain();
         }

         if (this.postChain != null) {
            if (mc.m_91385_() != null) {
               int w = mc.m_91268_().m_85441_();
               int h = mc.m_91268_().m_85442_();
               if (w != this.prevWidth || h != this.prevHeight) {
                  this.postChain.m_110025_(w, h);
                  this.prevWidth = w;
                  this.prevHeight = h;
               }

               float amount = (float)this.strength;
               Iterator var5 = ((PostChainAccessor)this.postChain).getPasses().iterator();

               while(var5.hasNext()) {
                  PostPass pass = (PostPass)var5.next();
                  if (pass.m_110074_() != null) {
                     pass.m_110074_().m_108960_("BlurAmount").m_5985_(amount);
                  }
               }

               this.postChain.m_110023_(partialTick);
               mc.m_91385_().m_83947_(false);
            }
         }
      }
   }

   private void loadPostChain() {
      try {
         this.postChain = new PostChain(mc.m_91097_(), mc.m_91098_(), mc.m_91385_(), SHADER_LOCATION);
         this.postChain.m_110025_(mc.m_91268_().m_85441_(), mc.m_91268_().m_85442_());
         this.prevWidth = mc.m_91268_().m_85441_();
         this.prevHeight = mc.m_91268_().m_85442_();
      } catch (IOException var2) {
         System.err.println("[Lexis] 动态模糊着色器加载失败: " + var2.getMessage());
         this.shaderLoadFailed = true;
         this.postChain = null;
      }

   }

   private void closePostChain() {
      if (this.postChain != null) {
         this.postChain.close();
         this.postChain = null;
      }

      this.prevWidth = -1;
      this.prevHeight = -1;
   }

   public void onClick() {
      this.toggle();
   }
}
