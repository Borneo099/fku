package moze_intel.projecte.capability;

import java.util.List;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

public class PedestalItemCapabilityWrapper extends BasicItemCapability implements IPedestalItem {
   public Capability getCapability() {
      return PECapabilities.PEDESTAL_ITEM_CAPABILITY;
   }

   public boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockEntity pedestal) {
      return ((IPedestalItem)this.getItem()).updateInPedestal(stack, level, pos, pedestal);
   }

   public @NotNull List getPedestalDescription() {
      return ((IPedestalItem)this.getItem()).getPedestalDescription();
   }
}
