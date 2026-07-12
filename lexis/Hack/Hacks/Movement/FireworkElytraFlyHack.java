package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.InventoryUtils;
import lexis.Hack.Utils.Timer;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class FireworkElytraFlyHack extends Hack implements UpdateListener {
   private HackConfig config;
   private static final String CONFIG_KEY = "烟花鞘翅飞行";
   private Timer fireworkTimer = new Timer();
   private int delay = 1000;
   private double checkSpeed = 90.0;
   private boolean inventorySwap = true;
   private boolean debug = false;
   private int cachedFireworks = 0;
   private long lastFireworkCalc = 0L;

   public FireworkElytraFlyHack() {
      super("烟花鞘翅飞行", "自动使用烟花火箭加速鞘翅飞行", Hack.Category.MOVEMENT, true);
      this.addSetting(new Hack.Setting("延迟(ms)", "使用烟花后的冷却时间", 1000, 100, 3000, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("速度阈值", "低于此速度时触发烟花", 90.0, 10.0, 120.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("背包交换", "允许从背包中寻找烟花", true));
      this.addSetting(new Hack.Setting("调试", "显示当前速度", false));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.delay = (int)this.config.getDoubleSetting("烟花鞘翅飞行", "延迟(ms)", 1000.0);
      this.checkSpeed = this.config.getDoubleSetting("烟花鞘翅飞行", "速度阈值", 90.0);
      this.inventorySwap = this.config.getBooleanSetting("烟花鞘翅飞行", "背包交换", true);
      this.debug = this.config.getBooleanSetting("烟花鞘翅飞行", "调试", false);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "延迟(ms)":
               setting.setValue((double)this.delay);
               break;
            case "速度阈值":
               setting.setValue(this.checkSpeed);
               break;
            case "背包交换":
               setting.setValue(this.inventorySwap);
               break;
            case "调试":
               setting.setValue(this.debug);
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
            case "延迟(ms)":
               int newDelay = (int)setting.getDouble();
               if (newDelay != this.delay) {
                  this.delay = newDelay;
                  needSave = true;
               }
               break;
            case "速度阈值":
               double newSpeed = setting.getDouble();
               if (newSpeed != this.checkSpeed) {
                  this.checkSpeed = newSpeed;
                  needSave = true;
               }
               break;
            case "背包交换":
               boolean newSwap = setting.getBoolean();
               if (newSwap != this.inventorySwap) {
                  this.inventorySwap = newSwap;
                  needSave = true;
               }
               break;
            case "调试":
               boolean newDebug = setting.getBoolean();
               if (newDebug != this.debug) {
                  this.debug = newDebug;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("烟花鞘翅飞行", this.getSettings());
      }

      if (mc.f_91074_ != null) {
         if (this.debug && mc.f_91074_.f_19797_ % 20 == 0) {
            mc.f_91074_.m_213846_(Component.m_237113_("Speed: " + this.getSpeed()));
         }

         ItemStack chest = mc.f_91074_.m_6844_(EquipmentSlot.CHEST);
         boolean wearingElytra = chest.m_41720_() == Items.f_42741_ && ElytraItem.m_41140_(chest);
         if (wearingElytra && !mc.f_91074_.m_20096_()) {
            if (!mc.f_91074_.m_21255_() && !mc.f_91074_.m_20096_() && mc.f_91066_.f_92089_.m_90857_()) {
               mc.f_91074_.m_36320_();
               mc.m_91403_().m_104955_(new ServerboundPlayerCommandPacket(mc.f_91074_, Action.START_FALL_FLYING));
            }

            if (this.fireworkTimer.passedMs((long)this.delay)) {
               if (this.wanToMove() && this.getSpeed() <= this.checkSpeed) {
                  this.useFirework();
               }

            }
         } else {
            if (mc.f_91074_.m_21255_()) {
               mc.f_91074_.m_36321_();
            }

         }
      }
   }

   private void useFirework() {
      if (mc.f_91074_.m_21205_().m_41720_() == Items.f_42688_) {
         mc.f_91072_.m_233721_(mc.f_91074_, InteractionHand.MAIN_HAND);
         this.fireworkTimer.reset();
      } else if (mc.f_91074_.m_21206_().m_41720_() == Items.f_42688_) {
         mc.f_91072_.m_233721_(mc.f_91074_, InteractionHand.OFF_HAND);
         this.fireworkTimer.reset();
      } else if (this.inventorySwap) {
         int fireworkSlot = InventoryUtils.findItem(Items.f_42688_);
         if (fireworkSlot != -1) {
            int oldSlot = mc.f_91074_.m_150109_().f_35977_;
            InventoryUtils.switchToSlot(fireworkSlot);
            mc.f_91072_.m_233721_(mc.f_91074_, InteractionHand.MAIN_HAND);
            InventoryUtils.switchToSlot(oldSlot);
            mc.m_91403_().m_104955_(new ServerboundContainerClosePacket(mc.f_91074_.f_36096_.f_38840_));
            this.fireworkTimer.reset();
         }
      }

   }

   private boolean wanToMove() {
      return mc.f_91066_.f_92085_.m_90857_() || mc.f_91066_.f_92087_.m_90857_() || mc.f_91066_.f_92086_.m_90857_() || mc.f_91066_.f_92088_.m_90857_();
   }

   private double getSpeed() {
      Vec3 vel = mc.f_91074_.m_20184_();
      double horizontalSpeed = Math.sqrt(vel.f_82479_ * vel.f_82479_ + vel.f_82481_ * vel.f_82481_);
      return horizontalSpeed * 20.0;
   }

   public String getDisplayName() {
      if (mc.f_91074_ == null) {
         return super.getDisplayName();
      } else {
         long now = System.currentTimeMillis();
         if (now - this.lastFireworkCalc >= 250L) {
            this.lastFireworkCalc = now;
            int fireworks = 0;
            int slots = this.inventorySwap ? 36 : 9;

            for(int i = 0; i < slots; ++i) {
               ItemStack stack = mc.f_91074_.m_150109_().m_8020_(i);
               if (stack.m_41720_() == Items.f_42688_) {
                  fireworks += stack.m_41613_();
               }
            }

            this.cachedFireworks = fireworks;
         }

         String var10000 = super.getDisplayName();
         return var10000 + " [F:" + this.cachedFireworks + "]";
      }
   }

   public void onClick() {
      this.toggle();
   }
}
