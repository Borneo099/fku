package moze_intel.projecte.capability.managing;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICapabilityResolver extends ICapabilityProvider {
   @NotNull Capability getMatchingCapability();

   @NotNull LazyOptional getCapabilityUnchecked(@NotNull Capability var1, @Nullable Direction var2);

   default @NotNull LazyOptional getCapability(@NotNull Capability capability, @Nullable Direction side) {
      return capability == this.getMatchingCapability() ? this.getCapabilityUnchecked(capability, side) : LazyOptional.empty();
   }

   void invalidate(@NotNull Capability var1, @Nullable Direction var2);

   void invalidateAll();
}
