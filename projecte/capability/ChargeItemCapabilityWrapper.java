package moze_intel.projecte.capability;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

public class ChargeItemCapabilityWrapper extends BasicItemCapability implements IItemCharge {
   public Capability getCapability() {
      return PECapabilities.CHARGE_ITEM_CAPABILITY;
   }

   public int getNumCharges(@NotNull ItemStack stack) {
      return ((IItemCharge)this.getItem()).getNumCharges(stack);
   }
}
