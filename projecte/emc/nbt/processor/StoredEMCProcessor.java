package moze_intel.projecte.emc.nbt.processor;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.api.nbt.NBTProcessor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NBTProcessor
public class StoredEMCProcessor implements INBTProcessor {
   public String getName() {
      return "StoredEMCProcessor";
   }

   public String getDescription() {
      return "Increases the EMC value of the item to take into account any EMC the item has stored.";
   }

   public long recalculateEMC(@NotNull ItemInfo info, long currentEMC) throws ArithmeticException {
      ItemStack stack = info.createStack();
      return (Long)stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).map((emcHolder) -> {
         return Math.addExact(currentEMC, emcHolder.getStoredEmc(stack));
      }).orElse(currentEMC);
   }
}
