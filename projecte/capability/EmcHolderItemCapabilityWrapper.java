package moze_intel.projecte.capability;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class EmcHolderItemCapabilityWrapper extends BasicItemCapability implements IItemEmcHolder {
   public Capability getCapability() {
      return PECapabilities.EMC_HOLDER_ITEM_CAPABILITY;
   }

   public long insertEmc(@NotNull ItemStack stack, long toInsert, IEmcStorage.EmcAction action) {
      return ((IItemEmcHolder)this.getItem()).insertEmc(stack, toInsert, action);
   }

   public long extractEmc(@NotNull ItemStack stack, long toExtract, IEmcStorage.EmcAction action) {
      return ((IItemEmcHolder)this.getItem()).extractEmc(stack, toExtract, action);
   }

   public @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getStoredEmc(@NotNull ItemStack stack) {
      return ((IItemEmcHolder)this.getItem()).getStoredEmc(stack);
   }

   public @Range(
   from = 1L,
   to = Long.MAX_VALUE
) long getMaximumEmc(@NotNull ItemStack stack) {
      return ((IItemEmcHolder)this.getItem()).getMaximumEmc(stack);
   }
}
