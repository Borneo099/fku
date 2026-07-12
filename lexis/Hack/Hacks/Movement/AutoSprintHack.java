package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.world.entity.player.Player;

public class AutoSprintHack extends Hack implements UpdateListener {
   private HackConfig config;
   private static final String CONFIG_KEY = "自动冲刺";
   private boolean omniSprint = false;
   private boolean sprintWhileHungry = false;
   private boolean ignoreMovementRestrictions = false;

   public AutoSprintHack() {
      super("自动冲刺", "自动开启冲刺", Hack.Category.MOVEMENT, true);
      this.addSetting(new Hack.Setting("全方向冲刺", "允许向任何方向冲刺（不仅向前）", false));
      this.addSetting(new Hack.Setting("低饥饿冲刺", "即使饥饿值低也能冲刺", false));
      this.addSetting(new Hack.Setting("忽略移动限制", "在飞行、爬梯、游泳、滑翔、骑乘时也自动冲刺", false));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.omniSprint = this.config.getBooleanSetting("自动冲刺", "全方向冲刺", false);
      this.sprintWhileHungry = this.config.getBooleanSetting("自动冲刺", "低饥饿冲刺", false);
      this.ignoreMovementRestrictions = this.config.getBooleanSetting("自动冲刺", "忽略移动限制", false);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "全方向冲刺":
               setting.setValue(this.omniSprint);
               break;
            case "低饥饿冲刺":
               setting.setValue(this.sprintWhileHungry);
               break;
            case "忽略移动限制":
               setting.setValue(this.ignoreMovementRestrictions);
         }
      }

   }

   public void onEnable() {
      EventManager.add(UpdateListener.class, this);
      this.updateSprint();
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
      if (mc.f_91074_ != null && mc.f_91074_.m_20142_()) {
         mc.f_91074_.m_6858_(false);
      }

   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "全方向冲刺":
               boolean newOmni = setting.getBoolean();
               if (newOmni != this.omniSprint) {
                  this.omniSprint = newOmni;
                  needSave = true;
               }
               break;
            case "低饥饿冲刺":
               boolean newHungry = setting.getBoolean();
               if (newHungry != this.sprintWhileHungry) {
                  this.sprintWhileHungry = newHungry;
                  needSave = true;
               }
               break;
            case "忽略移动限制":
               boolean newIgnore = setting.getBoolean();
               if (newIgnore != this.ignoreMovementRestrictions) {
                  this.ignoreMovementRestrictions = newIgnore;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("自动冲刺", this.getSettings());
      }

      this.updateSprint();
   }

   private void updateSprint() {
      if (mc.f_91074_ != null) {
         Player player = mc.f_91074_;
         if (!this.ignoreMovementRestrictions && (player.m_6147_() || player.m_6069_() || player.m_150110_().f_35935_ || player.m_21255_() || player.m_20159_())) {
            if (player.m_20142_()) {
               player.m_6858_(false);
            }

         } else {
            boolean moving = player.f_20902_ != 0.0F || player.f_20900_ != 0.0F;
            if (!moving) {
               if (player.m_20142_()) {
                  player.m_6858_(false);
               }

            } else {
               boolean canSprint = this.omniSprint || player.f_20902_ > 0.0F;
               if (!canSprint) {
                  if (player.m_20142_()) {
                     player.m_6858_(false);
                  }

               } else if (!this.sprintWhileHungry && player.m_36324_().m_38702_() <= 6) {
                  if (player.m_20142_()) {
                     player.m_6858_(false);
                  }

               } else {
                  if (!player.m_20142_()) {
                     player.m_6858_(true);
                  }

               }
            }
         }
      }
   }

   public void onClick() {
      this.toggle();
   }
}
