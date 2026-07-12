package lexis.Hack.Hacks.Misc;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.NotificationManager;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public class DamageHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "受伤";
   private int amount = 1;

   public DamageHack() {
      super("受伤", "对你自己造成伤害 右键打开可设置", Hack.Category.MISC, false);
      this.addSetting(new Hack.Setting("伤害值", "伤害值。", 7, 1, 7, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.amount = (int)this.config.getDoubleSetting("受伤", "伤害值", 1.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("伤害值")) {
            setting.setValue((double)this.amount);
            break;
         }
      }

   }

   public void onEnable() {
      this.applyDamage();
      this.setEnabled(false);
   }

   public void onDisable() {
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         if (setting.getName().equals("伤害值")) {
            int newAmount = (int)setting.getDouble();
            if (newAmount != this.amount) {
               this.amount = newAmount;
               needSave = true;
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("受伤", this.getSettings());
      }

   }

   public void onClick() {
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("伤害值")) {
            this.amount = (int)setting.getDouble();
            break;
         }
      }

      this.applyDamage();
   }

   private void applyDamage() {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null && mc.f_91072_ != null) {
         if (mc.f_91074_.m_150110_().f_35937_) {
            NotificationManager.error("受伤", "你不能是创造模式！");
         } else {
            Vec3 pos = mc.f_91074_.m_20182_();
            int loops = 100;

            for(int i = 0; i < loops; ++i) {
               this.sendPosition(pos.f_82479_, pos.f_82480_ + (double)this.amount + 2.1, pos.f_82481_, false);
               this.sendPosition(pos.f_82479_, pos.f_82480_ + 0.05, pos.f_82481_, false);
            }

            this.sendPosition(pos.f_82479_, pos.f_82480_, pos.f_82481_, true);
            NotificationManager.info("受伤", "已造成 " + this.amount + " 点伤害");
         }
      }
   }

   private void sendPosition(double x, double y, double z, boolean onGround) {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.m_91403_() != null) {
         mc.m_91403_().m_104955_(new ServerboundMovePlayerPacket.Pos(x, y, z, onGround));
      }

   }
}
