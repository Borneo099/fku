package moze_intel.projecte.capability.managing;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SidedItemHandlerResolver implements ICapabilityResolver {
   protected abstract ICapabilityResolver getResolver(@Nullable Direction var1);

   public @NotNull Capability getMatchingCapability() {
      return ForgeCapabilities.ITEM_HANDLER;
   }

   public @NotNull LazyOptional getCapabilityUnchecked(@NotNull Capability capability, @Nullable Direction side) {
      return this.getResolver(side).getCapabilityUnchecked(capability, side);
   }

   public void invalidate(@NotNull Capability capability, @Nullable Direction side) {
      this.getResolver(side).invalidate(capability, side);
   }
}
