package moze_intel.projecte.api.nbt;

import moze_intel.projecte.api.ItemInfo;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface INBTProcessor {
   String getName();

   String getDescription();

   default boolean isAvailable() {
      return true;
   }

   default boolean hasPersistentNBT() {
      return false;
   }

   default boolean usePersistentNBT() {
      return this.hasPersistentNBT();
   }

   long recalculateEMC(@NotNull ItemInfo var1, long var2) throws ArithmeticException;

   default @Nullable CompoundTag getPersistentNBT(@NotNull ItemInfo info) {
      return null;
   }
}
