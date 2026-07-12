package moze_intel.projecte.capability;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IAlchChestItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

public class AlchChestItemCapabilityWrapper extends BasicItemCapability implements IAlchChestItem {
   public Capability getCapability() {
      return PECapabilities.ALCH_CHEST_ITEM_CAPABILITY;
   }

   public boolean updateInAlchChest(@NotNull Level level, @NotNull BlockPos pos, @NotNull ItemStack stack) {
      return ((IAlchChestItem)this.getItem()).updateInAlchChest(level, pos, stack);
   }
}
