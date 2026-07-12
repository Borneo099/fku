package lexis.mixin.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({CreativeModeInventoryScreen.class})
public class CreativeModeInventoryScreenMixin {
   private static final Minecraft mc = Minecraft.m_91087_();

   @Inject(
      method = {"slotClicked"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSlotClicked(Slot p_98556_, int p_98557_, int p_98558_, ClickType p_98559_, CallbackInfo ci) {
      if (p_98556_ != null) {
         ItemStack stack = p_98556_.m_7993_();
         if (!stack.m_41619_()) {
            CompoundTag tag = stack.m_41783_();
            if (tag != null && tag.m_128471_("NoLexisItems")) {
               ci.cancel();
               if (mc.f_91074_ != null) {
                  mc.f_91074_.m_5661_(Component.m_237113_("§c[§6Lexis§c] §f禁止拿起在Lexis创造页的物品中"), false);
               }
            }

         }
      }
   }
}
