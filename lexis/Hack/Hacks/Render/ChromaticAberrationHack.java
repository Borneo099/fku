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

public class ChromaticAberrationHack extends Hack {
   public static ChromaticAberrationHack INSTANCE;
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final ResourceLocation SHADER_LOCATION = new ResourceLocation("lexis", "shaders/post/chromatic_aberration.json");
   private PostChain postChain;
   private double intensity = 0.05;
   private boolean dynamicMode = false;
   private double dynamicSpeed = 4.0;
   private long animStartTime;
   private int prevWidth = -1;
   private int prevHeight = -1;
   private boolean shaderLoadFailed = false;

   public ChromaticAberrationHack() {
      super("色差效果", new String[]{"屏幕边缘红蓝通道分离", "色差的§d凋零风暴§f模组 效果"}, Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("强度", "色差强度（0=关闭效果, 3=最大偏移）", this.intensity, 0.0, 3.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("是否动态效果", "强度三角波振荡循环（像恐怖炸弹渐强渐弱）", false));
      this.addSetting(new Hack.Setting("动态速度", "一个完整振荡周期的秒数", this.dynamicSpeed, 1.0, 20.0, Hack.ValueDisplay.DECIMAL));
      INSTANCE = this;
      this.loadConfig();
   }

   private void loadConfig() {
      this.intensity = HackConfig.getInstance().getDoubleSetting("色差效果", "强度", 0.05);
      this.dynamicMode = HackConfig.getInstance().getBooleanSetting("色差效果", "是否动态效果", false);
      this.dynamicSpeed = HackConfig.getInstance().getDoubleSetting("色差效果", "动态速度", 4.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "强度":
               setting.setValue(this.intensity);
               break;
            case "是否动态效果":
               setting.setValue(this.dynamicMode);
               break;
            case "动态速度":
               setting.setValue(this.dynamicSpeed);
         }
      }

   }

   public void onEnable() {
      this.shaderLoadFailed = false;
      this.animStartTime = System.currentTimeMillis();
   }

   public void onDisable() {
      this.closePostChain();
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "强度":
               if (setting.getDouble() != this.intensity) {
                  this.intensity = setting.getDouble();
                  needSave = true;
               }
               break;
            case "是否动态效果":
               if (setting.getBoolean() != this.dynamicMode) {
                  this.dynamicMode = setting.getBoolean();
                  if (this.dynamicMode) {
                     this.animStartTime = System.currentTimeMillis();
                  }

                  needSave = true;
               }
               break;
            case "动态速度":
               if (setting.getDouble() != this.dynamicSpeed) {
                  this.dynamicSpeed = setting.getDouble();
                  needSave = true;
               }
         }
      }

      if (needSave) {
         HackConfig.getInstance().saveHackSettings("色差效果", this.getSettings());
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

               float multiplier = (float)this.intensity;
               if (this.dynamicMode && this.intensity > 0.0) {
                  double elapsed = (double)(System.currentTimeMillis() - this.animStartTime) / 1000.0;
                  double cyclePos = elapsed % this.dynamicSpeed / this.dynamicSpeed;
                  double oscillation = cyclePos < 0.5 ? cyclePos * 2.0 : (1.0 - cyclePos) * 2.0;
                  multiplier = (float)(this.intensity * oscillation);
               }

               if ((double)multiplier > 0.0) {
                  Iterator var11 = ((PostChainAccessor)this.postChain).getPasses().iterator();

                  while(var11.hasNext()) {
                     PostPass pass = (PostPass)var11.next();
                     if (pass.m_110074_() != null) {
                        pass.m_110074_().m_108960_("Multiplier").m_5985_(multiplier);
                     }
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
         System.err.println("[Lexis] 色差效果着色器加载失败: " + var2.getMessage());
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
