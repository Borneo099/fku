package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class FlightHack extends Hack {
   private float flySpeed = 4.5F;
   private boolean antiKick = true;
   private float antiKickDistance = 0.05F;
   private int antiKickInterval = 30;
   private int tickCounter = 0;
   private boolean wasFlying = false;
   private boolean wasMayfly = false;
   private float originalFlySpeed = 0.07F;
   private HackConfig config;

   public FlightHack() {
      super("幻飞行", new String[]{"自由幻飞行，带有反踢出"}, Hack.Category.MOVEMENT, true);
      this.addSetting(new Hack.Setting("速度", "飞行速度", 4.5, 1.0, 500.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("反踢出", "防止被服务器踢出", true));
      this.addSetting(new Hack.Setting("反踢距离", "反踢出下落回弹距离", 0.05, 0.01, 0.5, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("反踢间隔", "反踢出间隔", 30, 1, 100, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.flySpeed = (float)this.config.getDoubleSetting("幻飞行", "速度", 4.5);
      this.antiKick = this.config.getBooleanSetting("幻飞行", "反踢出", true);
      this.antiKickDistance = (float)this.config.getDoubleSetting("幻飞行", "反踢距离", 0.05);
      this.antiKickInterval = this.config.getIntSetting("幻飞行", "反踢间隔", 30);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "速度":
               setting.setValue((double)this.flySpeed);
               break;
            case "反踢出":
               setting.setValue(this.antiKick);
               break;
            case "反踢距离":
               setting.setValue((double)this.antiKickDistance);
               break;
            case "反踢间隔":
               setting.setValue(this.antiKickInterval);
         }
      }

   }

   public void onEnable() {
      if (mc.f_91074_ != null) {
         LocalPlayer player = mc.f_91074_;
         this.tickCounter = 0;
         Iterator var2 = this.getSettings().iterator();

         while(var2.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var2.next();
            switch (setting.getName()) {
               case "速度":
                  this.flySpeed = (float)setting.getDouble();
                  break;
               case "反踢出":
                  this.antiKick = setting.getBoolean();
                  break;
               case "反踢距离":
                  this.antiKickDistance = (float)setting.getDouble();
                  break;
               case "反踢间隔":
                  this.antiKickInterval = setting.getInt();
            }
         }

         this.wasFlying = player.m_150110_().f_35935_;
         this.wasMayfly = player.m_150110_().f_35936_;
         this.originalFlySpeed = player.m_150110_().m_35942_();
         player.m_150110_().f_35936_ = true;
         player.m_6885_();
      }
   }

   public void onDisable() {
      if (mc.f_91074_ != null) {
         LocalPlayer player = mc.f_91074_;
         if (player.m_7500_()) {
            player.m_150110_().f_35935_ = this.wasFlying;
            player.m_150110_().m_35943_(this.originalFlySpeed);
         } else {
            player.m_150110_().f_35936_ = this.wasMayfly;
            player.m_150110_().f_35935_ = false;
            player.m_150110_().m_35943_(this.originalFlySpeed);
         }

         player.m_6885_();
      }
   }

   public String getDisplayName() {
      return this.antiKick ? String.format("%s [%.1f,%.2f,%d]", this.getName(), this.flySpeed, this.antiKickDistance, this.antiKickInterval) : String.format("%s [%.1f]", this.getName(), this.flySpeed);
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && this.isEnabled()) {
         Iterator var1 = this.getSettings().iterator();

         while(var1.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var1.next();
            switch (setting.getName()) {
               case "速度":
                  this.flySpeed = (float)setting.getDouble();
                  break;
               case "反踢出":
                  this.antiKick = setting.getBoolean();
                  break;
               case "反踢距离":
                  this.antiKickDistance = (float)setting.getDouble();
                  break;
               case "反踢间隔":
                  this.antiKickInterval = setting.getInt();
            }
         }

         this.handleFlightMode();
      }
   }

   public void onClick() {
      this.toggle();
   }

   private void handleFlightMode() {
      LocalPlayer player = mc.f_91074_;
      if (!player.m_150110_().f_35936_) {
         player.m_150110_().f_35936_ = true;
      }

      float actualSpeed = this.flySpeed * 0.05F;
      player.m_150110_().m_35943_(actualSpeed);
      float forward = 0.0F;
      float strafe = 0.0F;
      float vertical = 0.0F;
      if (mc.f_91066_.f_92085_.m_90857_()) {
         ++forward;
      }

      if (mc.f_91066_.f_92087_.m_90857_()) {
         --forward;
      }

      if (mc.f_91066_.f_92086_.m_90857_()) {
         ++strafe;
      }

      if (mc.f_91066_.f_92088_.m_90857_()) {
         --strafe;
      }

      if (mc.f_91066_.f_92089_.m_90857_()) {
         ++vertical;
      }

      if (mc.f_91066_.f_92090_.m_90857_()) {
         --vertical;
      }

      Vec3 movement = this.calculateMovement(forward, strafe, vertical, actualSpeed);
      player.m_20256_(movement);
      if (this.antiKick) {
         this.doAntiKick(movement);
      }

   }

   private Vec3 calculateMovement(float forward, float strafe, float vertical, float speed) {
      LocalPlayer player = mc.f_91074_;
      if (forward == 0.0F && strafe == 0.0F && vertical == 0.0F) {
         return Vec3.f_82478_;
      } else {
         float yaw = player.m_146908_();
         double rad = Math.toRadians((double)yaw);
         double moveX = 0.0;
         double moveZ = 0.0;
         if (forward != 0.0F) {
            moveX += -Math.sin(rad) * (double)forward;
            moveZ += Math.cos(rad) * (double)forward;
         }

         if (strafe != 0.0F) {
            moveX += -Math.sin(rad - 1.5707963267948966) * (double)strafe;
            moveZ += Math.cos(rad - 1.5707963267948966) * (double)strafe;
         }

         double length = Math.sqrt(moveX * moveX + moveZ * moveZ);
         if (length > 0.0) {
            moveX = moveX / length * (double)speed * 20.0;
            moveZ = moveZ / length * (double)speed * 20.0;
         }

         double moveY = (double)(vertical * speed * 20.0F);
         return new Vec3(moveX, moveY, moveZ);
      }
   }

   private void doAntiKick(Vec3 velocity) {
      if (this.tickCounter > this.antiKickInterval + 1) {
         this.tickCounter = 0;
      }

      switch (this.tickCounter) {
         case 0:
            if (mc.f_91066_.f_92090_.m_90857_()) {
               this.tickCounter = 2;
            } else {
               mc.f_91074_.m_20334_(velocity.f_82479_, (double)(-this.antiKickDistance), velocity.f_82481_);
            }
            break;
         case 1:
            mc.f_91074_.m_20334_(velocity.f_82479_, (double)this.antiKickDistance, velocity.f_82481_);
      }

      ++this.tickCounter;
   }
}
