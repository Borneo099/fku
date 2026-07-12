package lexis.mixin.baritone;

import lexis.Hack.Hacks.Baritone.ElytraAnywhereHack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MultiPlayerGameMode.class})
public class BaritoneElytraArmorMixin {
   @Inject(
      method = {"handleInventoryMouseClick"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onHandleInventoryMouseClick(int containerId, int slotId, int mouseButton, ClickType clickType, Player player, CallbackInfo ci) {
      if (ElytraAnywhereHack.isProtecting()) {
         Minecraft mc = Minecraft.m_91087_();
         if (mc.f_91074_ != null) {
            if (slotId == 6) {
               if (mc.f_91074_.f_36096_ instanceof InventoryMenu) {
                  if (mc.f_91074_.m_6844_(EquipmentSlot.CHEST).m_150930_(Items.f_42741_)) {
                     ci.cancel();
                     mc.f_91074_.m_5661_(Component.m_237113_("[Lexis] 别动鞘翅！Baritone正在用鞘翅飞行中，换掉会卡死游戏！已拦截操作！"), true);
                  }
               }
            }
         }
      }
   }
}
