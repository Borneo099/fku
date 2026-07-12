package lexis.Hack.Hacks.Fun;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;

public class MileyCyrusHack extends Hack {
   private int speed = 20;
   private int tickCounter = 0;
   private boolean sneaking = false;
   private HackConfig config;

   public MileyCyrusHack() {
      super("无限蹲起", new String[]{"这里魔怔玩家？破防玩家？", "注：其地玩家能看见你在疯狂蹲起，自己看不到"}, Hack.Category.FUN, true);
      this.addSetting(new Hack.Setting("蹲起速度", "速度", 20.0, 1.0, 100.0, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.speed = (int)this.config.getDoubleSetting("无限蹲起", "蹲起速度", 20.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("蹲起速度")) {
            setting.setValue((double)this.speed);
            break;
         }
      }

   }

   public void onEnable() {
      this.tickCounter = 0;
      this.sneaking = false;
   }

   public void onDisable() {
      if (mc.f_91074_ != null && mc.m_91403_() != null && this.sneaking) {
         mc.m_91403_().m_104955_(new ServerboundPlayerCommandPacket(mc.f_91074_, Action.RELEASE_SHIFT_KEY));
         this.sneaking = false;
      }

   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.m_91403_() != null) {
         Iterator var1 = this.getSettings().iterator();

         while(var1.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var1.next();
            if (setting.getName().equals("蹲起速度")) {
               this.speed = (int)setting.getDouble();
               break;
            }
         }

         ++this.tickCounter;
         int interval = 101 - this.speed;
         if (interval <= 0) {
            interval = 1;
         }

         if (this.tickCounter >= interval) {
            if (this.sneaking) {
               mc.m_91403_().m_104955_(new ServerboundPlayerCommandPacket(mc.f_91074_, Action.RELEASE_SHIFT_KEY));
            } else {
               mc.m_91403_().m_104955_(new ServerboundPlayerCommandPacket(mc.f_91074_, Action.PRESS_SHIFT_KEY));
            }

            this.sneaking = !this.sneaking;
            this.tickCounter = 0;
         }

      }
   }

   public void onClick() {
      this.toggle();
   }
}
