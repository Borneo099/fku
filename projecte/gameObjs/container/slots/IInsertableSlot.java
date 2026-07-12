package moze_intel.projecte.gameObjs.container.slots;

import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

public interface IInsertableSlot {
   private Slot self() {
      return (Slot)this;
   }

   default @NotNull ItemStack insertItem(@NotNull ItemStack stack, boolean simulate) {
      Slot self = this.self();
      if (!stack.m_41619_() && self.m_5857_(stack)) {
         ItemStack current = self.m_7993_();
         int needed = self.m_5866_(stack) - current.m_41613_();
         if (needed <= 0) {
            return stack;
         } else if (!current.m_41619_() && !ItemHandlerHelper.canItemStacksStack(current, stack)) {
            return stack;
         } else {
            int toAdd = Math.min(stack.m_41613_(), needed);
            if (!simulate) {
               self.m_5852_(stack.m_255036_(current.m_41613_() + toAdd));
            }

            return ItemHelper.size(stack, stack.m_41613_() - toAdd);
         }
      } else {
         return stack;
      }
   }
}
