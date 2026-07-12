package lexis.Hack.Hacks.Render;

import com.mojang.blaze3d.shaders.AbstractUniform;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.mixin.accessor.PostChainAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;

public class VirtualShaderHack extends Hack {
   public static VirtualShaderHack INSTANCE;
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final ResourceLocation SHADER_LOCATION = new ResourceLocation("lexis", "shaders/post/virtual_shader.json");
   private PostChain postChain;
   private double bloomStrength = 0.6;
   private double vignetteStrength = 0.5;
   private double saturation = 1.15;
   private double contrast = 1.1;
   private double splitTone = 0.5;
   private double gamma = 1.05;
   private boolean dynamicMode = false;
   private double dynamicSpeed = 6.0;
   private long animStartTime;
   private int prevWidth = -1;
   private int prevHeight = -1;
   private boolean shaderLoadFailed = false;

   public VirtualShaderHack() {
      super("虚拟光影", new String[]{"伪光影效果", "如果不好看，你可以右键打开设置"}, Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("辉光强度", "亮区发光扩散强度", this.bloomStrength, 0.0, 1.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("暗角强度", "屏幕边缘变暗", this.vignetteStrength, 0.0, 1.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("饱和度", "颜色饱和度（1.0=原始）", this.saturation, 0.0, 2.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("对比度", "明暗对比（1.0=原始）", this.contrast, 0.5, 2.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("分色调色", "橙蓝分色调色强度（暖高光+冷阴影）", this.splitTone, 0.0, 1.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("伽马值", "画面亮度曲线", this.gamma, 0.5, 2.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("是否动态效果", "所有参数做三角波振荡循环", false));
      this.addSetting(new Hack.Setting("动态速度", "一个完整振荡周期的秒数", this.dynamicSpeed, 1.0, 20.0, Hack.ValueDisplay.DECIMAL));
      INSTANCE = this;
      this.loadConfig();
   }

   private void loadConfig() {
      this.bloomStrength = HackConfig.getInstance().getDoubleSetting("虚拟光影", "辉光强度", 0.6);
      this.vignetteStrength = HackConfig.getInstance().getDoubleSetting("虚拟光影", "暗角强度", 0.5);
      this.saturation = HackConfig.getInstance().getDoubleSetting("虚拟光影", "饱和度", 1.15);
      this.contrast = HackConfig.getInstance().getDoubleSetting("虚拟光影", "对比度", 1.1);
      this.splitTone = HackConfig.getInstance().getDoubleSetting("虚拟光影", "分色调色", 0.5);
      this.gamma = HackConfig.getInstance().getDoubleSetting("虚拟光影", "伽马值", 1.05);
      this.dynamicMode = HackConfig.getInstance().getBooleanSetting("虚拟光影", "是否动态效果", false);
      this.dynamicSpeed = HackConfig.getInstance().getDoubleSetting("虚拟光影", "动态速度", 6.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "辉光强度":
               setting.setValue(this.bloomStrength);
               break;
            case "暗角强度":
               setting.setValue(this.vignetteStrength);
               break;
            case "饱和度":
               setting.setValue(this.saturation);
               break;
            case "对比度":
               setting.setValue(this.contrast);
               break;
            case "分色调色":
               setting.setValue(this.splitTone);
               break;
            case "伽马值":
               setting.setValue(this.gamma);
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
            case "辉光强度":
               if (setting.getDouble() != this.bloomStrength) {
                  this.bloomStrength = setting.getDouble();
                  needSave = true;
               }
               break;
            case "暗角强度":
               if (setting.getDouble() != this.vignetteStrength) {
                  this.vignetteStrength = setting.getDouble();
                  needSave = true;
               }
               break;
            case "饱和度":
               if (setting.getDouble() != this.saturation) {
                  this.saturation = setting.getDouble();
                  needSave = true;
               }
               break;
            case "对比度":
               if (setting.getDouble() != this.contrast) {
                  this.contrast = setting.getDouble();
                  needSave = true;
               }
               break;
            case "分色调色":
               if (setting.getDouble() != this.splitTone) {
                  this.splitTone = setting.getDouble();
                  needSave = true;
               }
               break;
            case "伽马值":
               if (setting.getDouble() != this.gamma) {
                  this.gamma = setting.getDouble();
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
         HackConfig.getInstance().saveHackSettings("虚拟光影", this.getSettings());
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

               double oscillation = 1.0;
               if (this.dynamicMode) {
                  double elapsed = (double)(System.currentTimeMillis() - this.animStartTime) / 1000.0;
                  double cyclePos = elapsed % this.dynamicSpeed / this.dynamicSpeed;
                  oscillation = cyclePos < 0.5 ? cyclePos * 2.0 : (1.0 - cyclePos) * 2.0;
               }

               List passes = ((PostChainAccessor)this.postChain).getPasses();
               Iterator var7 = passes.iterator();

               while(var7.hasNext()) {
                  PostPass pass = (PostPass)var7.next();
                  if (pass.m_110074_() != null) {
                     AbstractUniform u = pass.m_110074_().m_108960_("BloomStrength");
                     if (u != null) {
                        u.m_5985_((float)(this.bloomStrength * oscillation));
                     }

                     u = pass.m_110074_().m_108960_("VignetteStrength");
                     if (u != null) {
                        u.m_5985_((float)(this.vignetteStrength * oscillation));
                     }

                     u = pass.m_110074_().m_108960_("Saturation");
                     if (u != null) {
                        u.m_5985_((float)(1.0 + (this.saturation - 1.0) * oscillation));
                     }

                     u = pass.m_110074_().m_108960_("Contrast");
                     if (u != null) {
                        u.m_5985_((float)(1.0 + (this.contrast - 1.0) * oscillation));
                     }

                     u = pass.m_110074_().m_108960_("SplitTone");
                     if (u != null) {
                        u.m_5985_((float)(this.splitTone * oscillation));
                     }

                     u = pass.m_110074_().m_108960_("Gamma");
                     if (u != null) {
                        u.m_5985_((float)(1.0 + (this.gamma - 1.0) * oscillation));
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
         System.err.println("[Lexis] 虚拟光影着色器加载失败: " + var2.getMessage());
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
