package moze_intel.projecte.gameObjs.container.slots.transmutation;

import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.gameObjs.container.slots.InventoryContainerSlot;
import moze_intel.projecte.gameObjs.items.Tome;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotUnlearn extends InventoryContainerSlot {
   private final TransmutationInventory inv;

   public SlotUnlearn(TransmutationInventory inv, int index, int x, int y) {
      super(inv, index, x, y);
      this.inv = inv;
   }

   public boolean m_5857_(@NotNull ItemStack stack) {
      return !this.m_6657_() && (EMCHelper.doesItemHaveEmc(stack) || stack.m_41720_() instanceof Tome);
   }

   public void initialize(@NotNull ItemStack stack) {
      super.initialize(stack);
   }

   public void m_5852_(@NotNull ItemStack stack) {
      if (this.inv.isServer() && !stack.m_41619_()) {
         this.inv.handleUnlearn(stack.m_41777_());
      }

      super.m_5852_(stack);
   }

   public int m_6641_() {
      return 1;
   }
}
