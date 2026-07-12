package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class AirJumpHack extends Hack implements UpdateListener {
   private static final Minecraft mc = Minecraft.m_91087_();
   private HackConfig config;
   private static final String CONFIG_KEY = "空中跳跃";
   private boolean maintainLevel = false;
   private int level = 0;
   private boolean wasJumpKeyPressed = false;
   private boolean wasSneakKeyPressed = false;

   public AirJumpHack() {
      super("空中跳跃", "允许在空中跳跃", Hack.Category.MOVEMENT, true);
      this.addSetting(new Hack.Setting("保持水平", "按住跳跃键时维持当前高度", false));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.maintainLevel = this.config.getBooleanSetting("空中跳跃", "保持水平", false);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("保持水平")) {
            setting.setValue(this.maintainLevel);
            break;
         }
      }

   }

   public void onEnable() {
      if (mc.f_91074_ != null) {
         this.level = mc.f_91074_.m_20183_().m_123342_();
      }

      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
   }

   public void onUpdate() {
      Iterator var1 = this.getSettings().iterator();

      boolean sneakPressed;
      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("保持水平")) {
            sneakPressed = setting.getBoolean();
            if (sneakPressed != this.maintainLevel) {
               this.maintainLevel = sneakPressed;
               this.saveConfig();
            }
            break;
         }
      }

      if (mc.f_91074_ != null) {
         Player player = mc.f_91074_;
         if (mc.f_91080_ == null) {
            boolean jumpPressed = mc.f_91066_.f_92089_.m_90857_();
            sneakPressed = mc.f_91066_.f_92090_.m_90857_();
            if (!player.m_20096_()) {
               if (jumpPressed && !this.wasJumpKeyPressed) {
                  this.doJump(player);
                  if (this.maintainLevel) {
                     this.level = player.m_20183_().m_123342_();
                  }
               }

               if (this.maintainLevel && player.m_20183_().m_123342_() == this.level && jumpPressed) {
                  this.doJump(player);
               }

               if (sneakPressed && !this.wasSneakKeyPressed && this.maintainLevel) {
                  --this.level;
               }
            } else if (this.maintainLevel) {
               this.level = player.m_20183_().m_123342_();
            }

            this.wasJumpKeyPressed = jumpPressed;
            this.wasSneakKeyPressed = sneakPressed;
         }
      }
   }

   private void doJump(Player player) {
      double jumpSpeed = 0.42;
      Vec3 vel = player.m_20184_();
      player.m_20334_(vel.f_82479_, jumpSpeed, vel.f_82481_);
   }

   private void saveConfig() {
      this.config.saveHackSettings("空中跳跃", this.getSettings());
   }

   public void onClick() {
      this.toggle();
   }
}
