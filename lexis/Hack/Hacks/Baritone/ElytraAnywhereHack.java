package lexis.Hack.Hacks.Baritone;

import lexis.Hack.Hack;
import lexis.Hack.Utils.BaritoneBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;

public class ElytraAnywhereHack extends Hack {
   public static boolean enabled = false;
   public static volatile boolean rendering = false;
   public static int goalX;
   public static int goalY;
   public static int goalZ;
   public static boolean hasGoal = false;
   private boolean wasElytraEquipped = false;

   public ElytraAnywhereHack() {
      super("Baritone允许任意维度鞘翅", new String[]{"强制Baritone鞘翅在所有维度可用，解除Y限制", "§c§l运行鞘翅任务时别乱换装备！会自动停止！因为这是baritone问题会卡死游戏 就是加上防护了"}, Hack.Category.BARITONE, true);
   }

   public static void setGoal(int x, int y, int z) {
      goalX = x;
      goalY = y;
      goalZ = z;
      hasGoal = true;
   }

   public void onEnable() {
      enabled = true;
      this.wasElytraEquipped = false;
      if (!Hack.isLoading()) {
         BaritoneBridge.suppressNextSetMessage();
         BaritoneBridge.executeCommand("set elytraTermsAccepted true");
      }
   }

   public void onDisable() {
      enabled = false;
      hasGoal = false;
      this.wasElytraEquipped = false;
   }

   public void onUpdate() {
      if (enabled) {
         Minecraft mc = Minecraft.m_91087_();
         if (mc.f_91074_ != null) {
            if (hasGoal) {
               double dx = mc.f_91074_.m_20185_() - (double)goalX;
               double dy = mc.f_91074_.m_20186_() - (double)goalY;
               double dz = mc.f_91074_.m_20189_() - (double)goalZ;
               if (Math.sqrt(dx * dx + dy * dy + dz * dz) < 5.0) {
                  BaritoneBridge.stop();
                  hasGoal = false;
                  this.wasElytraEquipped = false;
                  return;
               }
            }

            boolean hasElytra = mc.f_91074_.m_6844_(EquipmentSlot.CHEST).m_150930_(Items.f_42741_);
            if (hasGoal && this.wasElytraEquipped && !hasElytra && BaritoneBridge.isActive()) {
               BaritoneBridge.stop();
               hasGoal = false;
               this.wasElytraEquipped = false;
            } else {
               this.wasElytraEquipped = hasElytra;
            }
         }
      }
   }

   public static boolean isProtecting() {
      return enabled && hasGoal && BaritoneBridge.isElytraActive();
   }

   public static void emergencyStop() {
      hasGoal = false;
   }

   public void onClick() {
      this.toggle();
   }
}
