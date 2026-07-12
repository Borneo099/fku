package lexis.Hack.Hacks.Items;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.NotificationManager;

public class HeadlessPistonHack extends Hack {
   public HeadlessPistonHack() {
      super("自动无头活塞", new String[]{"这自动无头活塞，右键对方块会自动放置无头活塞", "需要创造模式", "注意：空手也会自动放置，手上有物品方块 会自动放置无头活塞 不会被清除手上物品"}, Hack.Category.ITEMS, true);
   }

   public void onEnable() {
      if (mc.f_91074_ != null && !mc.f_91074_.m_7500_()) {
         NotificationManager.error("自动无头活塞", "仅创造模式可使用！", 3);
         this.setEnabled(false);
      }

   }

   public void onDisable() {
   }

   public void onUpdate() {
   }

   public void onClick() {
      if (mc.f_91074_ != null && mc.f_91074_.m_7500_()) {
         this.toggle();
      } else {
         NotificationManager.error("自动无头活塞", "仅创造模式可使用！", 3);
         if (this.isEnabled()) {
            this.setEnabled(false);
         }

      }
   }
}
