package lexis.Hack.Hacks.Combat;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoTotemHack extends Hack implements UpdateListener {
   private HackConfig config;
   private static final String CONFIG_KEY = "自动图腾";
   private boolean showCounter = true;
   private int delay = 0;
   private float healthThreshold = 0.0F;
   private int timer = 0;
   private int nextTickSlot = -1;
   private int totems = 0;
   private boolean wasTotemInOffhand = false;

   public AutoTotemHack() {
      super("自动图腾", new String[]{"自动图腾放入在副手"}, Hack.Category.COMBAT, true);
      this.addSetting(new Hack.Setting("显示计数器", "在名称后显示图腾数量", true));
      this.addSetting(new Hack.Setting("延迟", "交换后的等时间", 0, 0, 20, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("生命值阈值", "低于生命值才自动图腾（0=总是）", 0, 0, 10, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.showCounter = this.config.getBooleanSetting("自动图腾", "显示计数器", true);
      this.delay = (int)this.config.getDoubleSetting("自动图腾", "延迟", 0.0);
      this.healthThreshold = (float)this.config.getDoubleSetting("自动图腾", "生命值阈值", 0.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "显示计数器":
               setting.setValue(this.showCounter);
               break;
            case "延迟":
               setting.setValue((double)this.delay);
               break;
            case "生命值阈值":
               setting.setValue((double)this.healthThreshold);
         }
      }

   }

   public String getDisplayName() {
      if (!this.showCounter) {
         return super.getDisplayName();
      } else {
         String var10000 = super.getDisplayName();
         return var10000 + " [" + this.totems + "]";
      }
   }

   public void onEnable() {
      this.nextTickSlot = -1;
      this.totems = 0;
      this.timer = 0;
      this.wasTotemInOffhand = false;
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
            case "显示计数器":
               if (setting.getBoolean() != this.showCounter) {
                  this.showCounter = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "延迟":
               int newDelay = (int)setting.getDouble();
               if (newDelay != this.delay) {
                  this.delay = newDelay;
                  needSave = true;
               }
               break;
            case "生命值阈值":
               float newHealth = (float)setting.getDouble();
               if (newHealth != this.healthThreshold) {
                  this.healthThreshold = newHealth;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("自动图腾", this.getSettings());
      }

      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null) {
         this.finishMovingTotem();
         int nextTotemSlot = this.searchForTotems();
         ItemStack offhandStack = mc.f_91074_.m_21206_();
         if (this.isTotem(offhandStack)) {
            ++this.totems;
            this.wasTotemInOffhand = true;
         } else {
            if (this.wasTotemInOffhand) {
               this.timer = this.delay;
               this.wasTotemInOffhand = false;
            }

            float health = mc.f_91074_.m_21223_();
            if ((this.healthThreshold <= 0.0F || health <= this.healthThreshold * 2.0F) && (mc.f_91080_ == null || mc.f_91080_ instanceof InventoryScreen) && nextTotemSlot != -1) {
               if (this.timer > 0) {
                  --this.timer;
               } else {
                  this.moveTotem(nextTotemSlot, offhandStack);
               }
            }
         }

      }
   }

   private void moveTotem(int nextTotemSlot, ItemStack offhandStack) {
      boolean offhandEmpty = offhandStack.m_41619_();
      this.windowClick(nextTotemSlot, 0, ClickType.PICKUP);
      this.windowClick(45, 0, ClickType.PICKUP);
      if (!offhandEmpty) {
         this.nextTickSlot = nextTotemSlot;
      }

   }

   private void finishMovingTotem() {
      if (this.nextTickSlot != -1) {
         this.windowClick(this.nextTickSlot, 0, ClickType.PICKUP);
         this.nextTickSlot = -1;
      }

   }

   private int searchForTotems() {
      this.totems = 0;
      int nextTotemSlot = -1;

      for(int i = 0; i < 36; ++i) {
         ItemStack stack = mc.f_91074_.m_150109_().m_8020_(i);
         if (this.isTotem(stack)) {
            ++this.totems;
            if (nextTotemSlot == -1) {
               nextTotemSlot = i < 9 ? i + 36 : i;
            }
         }
      }

      return nextTotemSlot;
   }

   private boolean isTotem(ItemStack stack) {
      return !stack.m_41619_() && stack.m_41720_() == Items.f_42747_;
   }

   private void windowClick(int slot, int button, ClickType type) {
      if (mc.f_91072_ != null && mc.f_91074_ != null) {
         mc.f_91072_.m_171799_(mc.f_91074_.f_36096_.f_38840_, slot, button, type, mc.f_91074_);
      }
   }

   public void onClick() {
      this.toggle();
   }
}
