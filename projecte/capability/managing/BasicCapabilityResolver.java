package moze_intel.projecte.capability.managing;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BasicCapabilityResolver implements ICapabilityResolver {
   private final NonNullSupplier supplier;
   private LazyOptional cachedCapability;

   public static ICapabilityResolver getBasicItemHandlerResolver(NonNullSupplier supplier) {
      return new BasicCapabilityResolver(supplier) {
         public @NotNull Capability getMatchingCapability() {
            return ForgeCapabilities.ITEM_HANDLER;
         }
      };
   }

   public static ICapabilityResolver getBasicItemHandlerResolver(IItemHandler handler) {
      return new BasicCapabilityResolver(handler) {
         public @NotNull Capability getMatchingCapability() {
            return ForgeCapabilities.ITEM_HANDLER;
         }
      };
   }

   protected BasicCapabilityResolver(Object constant) {
      this.supplier = () -> {
         return constant;
      };
   }

   protected BasicCapabilityResolver(NonNullSupplier supplier) {
      this.supplier = (NonNullSupplier)(supplier instanceof NonNullLazy ? supplier : NonNullLazy.of(supplier));
   }

   public @NotNull LazyOptional getCapabilityUnchecked(@NotNull Capability capability, @Nullable Direction side) {
      if (this.cachedCapability == null || !this.cachedCapability.isPresent()) {
         this.cachedCapability = LazyOptional.of(this.supplier);
      }

      return this.cachedCapability.cast();
   }

   public void invalidate(@NotNull Capability capability, @Nullable Direction side) {
      this.invalidateAll();
   }

   public void invalidateAll() {
      if (this.cachedCapability != null && this.cachedCapability.isPresent()) {
         this.cachedCapability.invalidate();
         this.cachedCapability = null;
      }

   }
}
