package lexis.Hack.Hacks.TaCZ;

import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import lexis.Hack.Hack;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.world.item.ItemStack;

public class AutoReloadHack extends Hack implements UpdateListener {
   private static AutoReloadHack instance;
   private int reloadCooldown = 0;

   public AutoReloadHack() {
      super("自动换弹", "弹匣打空就自动换弹", Hack.Category.TACZ, true);
      instance = this;
   }

   public void onEnable() {
      this.reloadCooldown = 0;
      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
   }

   public void onUpdate() {
      if (mc.f_91074_ != null) {
         if (this.reloadCooldown > 0) {
            --this.reloadCooldown;
         } else {
            ItemStack stack = mc.f_91074_.m_21205_();
            IGun gun = IGun.getIGunOrNull(stack);
            if (gun != null) {
               int ammo = gun.getCurrentAmmoCount(stack);
               boolean barrelLoaded = gun.hasBulletInBarrel(stack);
               if (ammo <= 1) {
                  if (ammo != 1 || !barrelLoaded) {
                     IClientPlayerGunOperator operator = IClientPlayerGunOperator.fromLocalPlayer(mc.f_91074_);
                     operator.reload();
                     this.reloadCooldown = 4;
                  }
               }
            }
         }
      }
   }

   public void onClick() {
      this.toggle();
   }

   public static AutoReloadHack getInstance() {
      return instance;
   }
}
