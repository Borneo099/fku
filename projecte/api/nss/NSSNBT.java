package moze_intel.projecte.api.nss;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public interface NSSNBT extends NormalizedSimpleStack {
   @Nullable CompoundTag getNBT();

   default boolean hasNBT() {
      return this.getNBT() != null;
   }
}
