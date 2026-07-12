package lexis.Hack.Hacks.Fun;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class StareAtPlayerHack extends Hack {
   private double maxDistance = 5.0;
   private Player targetPlayer = null;
   private HackConfig config;

   public StareAtPlayerHack() {
      super("老老实看盯玩家", "就什么无聊就无聊，老老实看盯玩家", Hack.Category.FUN, true);
      this.addSetting(new Hack.Setting("最大距离", "看盯玩家的距离", 5.0, 1.0, 10.0, Hack.ValueDisplay.DECIMAL));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.maxDistance = this.config.getDoubleSetting("老老实看盯玩家", "最大距离", 5.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("最大距离")) {
            setting.setValue(this.maxDistance);
            break;
         }
      }

   }

   public String getDisplayName() {
      if (this.targetPlayer != null) {
         String var10000 = super.getDisplayName();
         return var10000 + " [正在看 " + this.targetPlayer.m_7755_().getString() + "]";
      } else {
         return super.getDisplayName();
      }
   }

   public void onEnable() {
   }

   public void onDisable() {
      this.targetPlayer = null;
      HeadOnlyLook.stopRotation();
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         Iterator var1 = this.getSettings().iterator();

         while(var1.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var1.next();
            if (setting.getName().equals("最大距离")) {
               this.maxDistance = setting.getDouble();
               break;
            }
         }

         Player oldTarget = this.targetPlayer;
         this.targetPlayer = null;
         double closestDistance = this.maxDistance;
         Iterator var4 = mc.f_91073_.m_6907_().iterator();

         while(var4.hasNext()) {
            Player player = (Player)var4.next();
            if (player != mc.f_91074_ && !player.m_213877_() && !(player.m_21223_() <= 0.0F)) {
               double distance = (double)mc.f_91074_.m_20270_(player);
               if (distance <= closestDistance) {
                  closestDistance = distance;
                  this.targetPlayer = player;
               }
            }
         }

         if (this.targetPlayer != null) {
            this.lookAt(this.targetPlayer);
         } else {
            HeadOnlyLook.stopRotation();
         }

      }
   }

   private void lookAt(Player target) {
      Vec3 targetPos = target.m_146892_();
      Vec3 playerPos = mc.f_91074_.m_146892_();
      double diffX = targetPos.f_82479_ - playerPos.f_82479_;
      double diffY = targetPos.f_82480_ - playerPos.f_82480_;
      double diffZ = targetPos.f_82481_ - playerPos.f_82481_;
      double distance = Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);
      float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, distance)));
      if (!HeadOnlyLook.isRotating()) {
         HeadOnlyLook.startRotation(yaw, pitch);
      } else {
         HeadOnlyLook.updateRotation(yaw, pitch);
      }

   }

   public void onClick() {
      this.toggle();
   }
}
