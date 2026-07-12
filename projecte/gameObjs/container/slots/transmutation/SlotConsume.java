package moze_intel.projecte.gameObjs.container.slots.transmutation;

import java.math.BigInteger;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.gameObjs.container.slots.InventoryContainerSlot;
import moze_intel.projecte.gameObjs.items.Tome;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotConsume extends InventoryContainerSlot {
   private final TransmutationInventory inv;

   public SlotConsume(TransmutationInventory inv, int index, int x, int y) {
      super(inv, index, x, y);
      this.inv = inv;
   }

   public void initialize(@NotNull ItemStack stack) {
   }

   public void m_5852_(@NotNull ItemStack stack) {
      if (this.inv.isServer() && !stack.m_41619_()) {
         this.inv.handleKnowledge(stack);
         this.inv.addEmc(BigInteger.valueOf(EMCHelper.getEmcSellValue(stack)).multiply(BigInteger.valueOf((long)stack.m_41613_())));
         this.m_6654_();
      }

   }

   public boolean m_5857_(@NotNull ItemStack stack) {
      return EMCHelper.doesItemHaveEmc(stack) || stack.m_41720_() instanceof Tome;
   }
}
