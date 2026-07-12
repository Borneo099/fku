package moze_intel.projecte.gameObjs.container.slots;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class InventoryContainerSlot extends SlotItemHandler implements IInsertableSlot {
   public InventoryContainerSlot(IItemHandler itemHandler, int index, int x, int y) {
      super(itemHandler, index, x, y);
   }

   public int m_5866_(ItemStack stack) {
      return Math.min(this.m_6641_(), stack.m_41741_());
   }
}
