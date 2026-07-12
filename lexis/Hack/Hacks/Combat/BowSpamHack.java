package lexis.Hack.Hacks.Combat;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BowSpamHack extends Hack implements UpdateListener {
   private static final Minecraft mc = Minecraft.m_91087_();
   private HackConfig config;
   private static final String CONFIG_KEY = "弓连射";
   private int charge = 5;
   private boolean onlyWhenHoldingRightClick = false;
   private boolean wasBow = false;
   private boolean wasHoldingRightClick = false;

   public BowSpamHack() {
      super("弓连射", "自动连续发射弓", Hack.Category.COMBAT, true);
      this.addSetting(new Hack.Setting("蓄力时间", "拉弓蓄力时间(tick) (21 有暴击 20没有暴击 伤害是一样)", 5, 4, 21));
      this.addSetting(new Hack.Setting("仅右键时", "只有按住右键时才工作", false));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.charge = this.config.getIntSetting("弓连射", "蓄力时间", 5);
      this.onlyWhenHoldingRightClick = this.config.getBooleanSetting("弓连射", "仅右键时", false);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "蓄力时间":
               setting.setValue(this.charge);
               break;
            case "仅右键时":
               setting.setValue(this.onlyWhenHoldingRightClick);
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("弓连射", this.getSettings());
   }

   public void onEnable() {
      this.wasBow = false;
      this.wasHoldingRightClick = false;
      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
      this.setPressed(false);
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "蓄力时间":
               if (setting.getInt() != this.charge) {
                  this.charge = setting.getInt();
                  needSave = true;
               }
               break;
            case "仅右键时":
               if (setting.getBoolean() != this.onlyWhenHoldingRightClick) {
                  this.onlyWhenHoldingRightClick = setting.getBoolean();
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.saveConfig();
      }

      if (mc.f_91074_ != null && mc.f_91072_ != null) {
         if (mc.f_91074_.m_150110_().f_35937_ || this.hasArrow()) {
            if (this.onlyWhenHoldingRightClick && !mc.f_91066_.f_92095_.m_90857_()) {
               if (this.wasHoldingRightClick) {
                  this.setPressed(false);
                  this.wasHoldingRightClick = false;
               }
            } else {
               boolean isBow = this.isHoldingBow();
               if (!isBow && this.wasBow) {
                  this.setPressed(false);
               }

               this.wasBow = isBow;
               if (!isBow) {
                  return;
               }

               if (mc.f_91074_.m_21252_() >= this.charge) {
                  mc.f_91072_.m_105277_(mc.f_91074_);
               } else {
                  this.setPressed(true);
               }

               this.wasHoldingRightClick = mc.f_91066_.f_92095_.m_90857_();
            }

         }
      }
   }

   private boolean isHoldingBow() {
      ItemStack main = mc.f_91074_.m_21205_();
      return main.m_41720_() == Items.f_42411_;
   }

   private boolean hasArrow() {
      for(int i = 0; i < mc.f_91074_.m_150109_().m_6643_(); ++i) {
         ItemStack stack = mc.f_91074_.m_150109_().m_8020_(i);
         if (stack.m_41720_() instanceof ArrowItem) {
            return true;
         }
      }

      return false;
   }

   private void setPressed(boolean pressed) {
      mc.f_91066_.f_92095_.m_7249_(pressed);
   }

   public void onClick() {
      this.toggle();
   }
}
