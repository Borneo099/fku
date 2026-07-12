package lexis.Hack.Hacks.Render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.util.Mth;

public class BlockAnimationHack extends Hack {
   public static BlockAnimationHack INSTANCE;
   private String mode = "1.7";
   private double spinSpeed = 1.5;

   public BlockAnimationHack() {
      super("防砍动画", "1.7/希格玛/转圈 防砍动画（右键持剑时生效）", Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("动画样式", "选择防砍动画风格", this.mode, new String[]{"1.7", "希格玛", "转圈"}));
      this.addSetting(new Hack.Setting("转圈速度", "转一圈的秒数（越小越快）", this.spinSpeed, 0.1, 5.0, Hack.ValueDisplay.DECIMAL));
      INSTANCE = this;
      this.loadConfig();
   }

   private void loadConfig() {
      this.mode = HackConfig.getInstance().getStringSetting("防砍动画", "动画样式", "1.7");
      this.spinSpeed = HackConfig.getInstance().getDoubleSetting("防砍动画", "转圈速度", 1.5);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "动画样式":
               setting.setValue(this.mode);
               break;
            case "转圈速度":
               setting.setValue(this.spinSpeed);
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

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "动画样式":
               if (!setting.getString().equals(this.mode)) {
                  this.mode = setting.getString();
                  needSave = true;
               }
               break;
            case "转圈速度":
               if (setting.getDouble() != this.spinSpeed) {
                  this.spinSpeed = setting.getDouble();
                  needSave = true;
               }
         }
      }

      if (needSave) {
         HackConfig.getInstance().saveHackSettings("防砍动画", this.getSettings());
      }

   }

   public String getMode() {
      return this.mode;
   }

   public static void animation1_7(PoseStack poseStack, float equippedProg, float swingProgress) {
      poseStack.m_252880_(0.56F, -0.52F + equippedProg * -0.6F, -0.71999997F);
      float f = Mth.m_14031_(swingProgress * swingProgress * 3.1415927F);
      float f1 = Mth.m_14031_(Mth.m_14116_(swingProgress) * 3.1415927F);
      poseStack.m_252781_(Axis.f_252436_.m_252977_(45.0F + f * -20.0F));
      poseStack.m_252781_(Axis.f_252403_.m_252977_(f1 * -20.0F));
      poseStack.m_252781_(Axis.f_252529_.m_252977_(f1 * -80.0F));
      poseStack.m_252781_(Axis.f_252436_.m_252977_(-45.0F));
      poseStack.m_252880_(-0.2F, 0.126F, 0.2F);
      poseStack.m_252781_(Axis.f_252529_.m_252977_(-102.25F));
      poseStack.m_252781_(Axis.f_252436_.m_252977_(15.0F));
      poseStack.m_252781_(Axis.f_252403_.m_252977_(80.0F));
   }

   public static void animationSpin(PoseStack poseStack, float equippedProg, float swingProgress) {
      animation1_7(poseStack, equippedProg, swingProgress);
      long period = (long)(INSTANCE.spinSpeed * 1000.0);
      if (period < 50L) {
         period = 50L;
      }

      float time = (float)(System.currentTimeMillis() % period) / (float)period;
      float angle = time * 360.0F;
      poseStack.m_252781_(Axis.f_252529_.m_252977_(angle));
   }

   public static void animationSigma(PoseStack poseStack, float equippedProg, float swingProgress) {
      poseStack.m_85837_(0.56, -0.52, -0.72);
      poseStack.m_85837_(-0.1414214, 0.08, 0.1414214);
      poseStack.m_252781_(Axis.f_252529_.m_252977_(-102.25F));
      poseStack.m_252781_(Axis.f_252436_.m_252977_(7.365F));
      poseStack.m_252781_(Axis.f_252403_.m_252977_(78.05F));
      double f1 = Math.sin(Math.sqrt((double)swingProgress) * Math.PI);
      poseStack.m_252781_(Axis.f_252529_.m_252977_((float)(f1 * -10.0)));
      poseStack.m_252781_(Axis.f_252403_.m_252977_((float)(f1 * 30.0)));
      poseStack.m_252781_(Axis.f_252436_.m_252977_((float)(f1 * -13.0)));
   }

   public void onClick() {
      this.toggle();
   }
}
