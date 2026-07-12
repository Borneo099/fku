package lexis.Hack.Hacks.Combat;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.FriendsManager;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult.Type;

public class AutoAttackHack extends Hack implements UpdateListener {
   private HackConfig config;
   private static final String CONFIG_KEY = "自动攻击";
   private int extraTicks = 0;
   private boolean swingAnimation = true;
   private boolean onlyWhenHoldingAttack = true;

   public AutoAttackHack() {
      super("自动攻击", "按住左键(默认)时自动攻击目标", Hack.Category.COMBAT, true);
      this.addSetting(new Hack.Setting("额外刻数", "攻击冷却偏移(正数延迟，负数提前)", 0.0, -5.0, 5.0));
      this.addSetting(new Hack.Setting("撸手动画", "攻击时是否显示撸手动画", true));
      this.addSetting(new Hack.Setting("仅按住左键", "是否必须按住左键才触发自动攻击", true));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.extraTicks = (int)this.config.getDoubleSetting("自动攻击", "额外刻数", 0.0);
      this.swingAnimation = this.config.getBooleanSetting("自动攻击", "撸手动画", true);
      this.onlyWhenHoldingAttack = this.config.getBooleanSetting("自动攻击", "仅按住左键", true);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "额外刻数":
               setting.setValue((double)this.extraTicks);
               break;
            case "撸手动画":
               setting.setValue(this.swingAnimation);
               break;
            case "仅按住左键":
               setting.setValue(this.onlyWhenHoldingAttack);
         }
      }

   }

   public void onEnable() {
      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "额外刻数":
               int newTicks = (int)setting.getDouble();
               if (newTicks != this.extraTicks) {
                  this.extraTicks = newTicks;
                  needSave = true;
               }
               break;
            case "撸手动画":
               boolean newSwing = setting.getBoolean();
               if (newSwing != this.swingAnimation) {
                  this.swingAnimation = newSwing;
                  needSave = true;
               }
               break;
            case "仅按住左键":
               boolean newOnly = setting.getBoolean();
               if (newOnly != this.onlyWhenHoldingAttack) {
                  this.onlyWhenHoldingAttack = newOnly;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("自动攻击", this.getSettings());
      }

      if (mc.f_91074_ != null) {
         if (!this.onlyWhenHoldingAttack || mc.f_91066_.f_92096_.m_90857_()) {
            if (mc.f_91077_ != null && mc.f_91077_.m_6662_() == Type.ENTITY) {
               if (mc.f_91074_.m_36403_((float)this.extraTicks) == 1.0F) {
                  Entity target = ((EntityHitResult)mc.f_91077_).m_82443_();
                  if (target != null) {
                     if (mc.f_91074_.canReach(target, 0.0)) {
                        if (!(target instanceof Player) || !FriendsManager.getInstance().isFriend((Player)target)) {
                           mc.f_91072_.m_105223_(mc.f_91074_, target);
                           if (this.swingAnimation) {
                              mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
                           }

                        }
                     }
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
