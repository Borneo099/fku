package lexis.Hack.Hacks.Fun;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import net.minecraft.util.Mth;

public class SpinHack extends Hack {
   private double yawSpeed = 180.0;
   private double pitchSpeed = 0.0;
   private long lastTime = 0L;
   private float targetYaw = 0.0F;
   private float targetPitch = 0.0F;
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "旋转";

   public SpinHack() {
      super("旋转", new String[]{"装逼。。。"}, Hack.Category.FUN, true);
      this.addSetting(new Hack.Setting("水平转速", "左右速度", 180.0, -360.0, 360.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("垂直上下", "改变上下", 0.0, -90.0, 90.0, Hack.ValueDisplay.DECIMAL));
      this.loadConfig();
   }

   private void loadConfig() {
      this.yawSpeed = this.config.getDoubleSetting("旋转", "水平转速", 180.0);
      this.pitchSpeed = this.config.getDoubleSetting("旋转", "垂直上下", 0.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         switch (s.getName()) {
            case "水平转速":
               s.setValue(this.yawSpeed);
               break;
            case "垂直上下":
               s.setValue(this.pitchSpeed);
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("旋转", this.getSettings());
   }

   public void onEnable() {
      if (mc.f_91074_ != null) {
         this.loadConfig();
         this.lastTime = System.currentTimeMillis();
         this.targetYaw = mc.f_91074_.m_146908_();
         this.targetPitch = mc.f_91074_.m_146909_();
         HeadOnlyLook.startRotation(this.targetYaw, this.targetPitch, Long.MAX_VALUE);
      }
   }

   public void onDisable() {
      HeadOnlyLook.stopRotation();
   }

   public void onUpdate() {
      if (mc.f_91074_ != null) {
         boolean needSave = false;
         Iterator var2 = this.getSettings().iterator();

         while(var2.hasNext()) {
            Hack.Setting s = (Hack.Setting)var2.next();
            switch (s.getName()) {
               case "水平转速":
                  double ny = s.getDouble();
                  if (ny != this.yawSpeed) {
                     this.yawSpeed = ny;
                     needSave = true;
                  }
                  break;
               case "垂直上下":
                  double np = s.getDouble();
                  if (np != this.pitchSpeed) {
                     this.pitchSpeed = np;
                     needSave = true;
                  }
            }
         }

         if (needSave) {
            this.saveConfig();
         }

         long now = System.currentTimeMillis();
         float delta = (float)(now - this.lastTime) / 600.0F;
         this.lastTime = now;
         if (delta > 0.0F) {
            this.targetYaw += (float)(this.yawSpeed * (double)delta);
            this.targetPitch += (float)(this.pitchSpeed * (double)delta);
            this.targetYaw = Mth.m_14177_(this.targetYaw);
            this.targetPitch = Mth.m_14036_(this.targetPitch, -90.0F, 90.0F);
            HeadOnlyLook.updateRotation(this.targetYaw, this.targetPitch);
         }

         HeadOnlyLook.onClientTick();
      }
   }

   public void onClick() {
      this.toggle();
   }
}
