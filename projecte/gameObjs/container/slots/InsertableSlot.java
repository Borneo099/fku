package moze_intel.projecte.gameObjs.container.slots;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class InsertableSlot extends Slot implements IInsertableSlot {
   public InsertableSlot(Container inventory, int index, int x, int y) {
      super(inventory, index, x, y);
   }

   public int m_5866_(ItemStack stack) {
      return Math.min(this.m_6641_(), stack.m_41741_());
   }
}
