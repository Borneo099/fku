package moze_intel.projecte.gameObjs.container.slots.transmutation;

import java.math.BigInteger;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.gameObjs.container.slots.InventoryContainerSlot;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotOutput extends InventoryContainerSlot {
   private final TransmutationInventory inv;

   public SlotOutput(TransmutationInventory inv, int index, int x, int y) {
      super(inv, index, x, y);
      this.inv = inv;
   }

   protected void m_6405_(int amount) {
      this.m_6201_(amount);
   }

   public @NotNull ItemStack m_6201_(int amount) {
      ItemStack stack = ItemHelper.size(this.m_7993_(), amount);
      BigInteger emcValue = BigInteger.valueOf(EMCHelper.getEmcValue(stack));
      if (amount > 1) {
         emcValue = emcValue.multiply(BigInteger.valueOf((long)amount));
      }

      if (emcValue.compareTo(this.inv.getAvailableEmc()) > 0) {
         return ItemStack.f_41583_;
      } else {
         if (this.inv.isServer()) {
            this.inv.removeEmc(emcValue);
         }

         return stack;
      }
   }

   public void initialize(@NotNull ItemStack stack) {
   }

   public void m_5852_(@NotNull ItemStack stack) {
   }

   public boolean m_5857_(@NotNull ItemStack stack) {
      return false;
   }

   public boolean m_8010_(Player player) {
      return !this.m_6657_() || BigInteger.valueOf(EMCHelper.getEmcValue(this.m_7993_())).compareTo(this.inv.getAvailableEmc()) <= 0;
   }
}
