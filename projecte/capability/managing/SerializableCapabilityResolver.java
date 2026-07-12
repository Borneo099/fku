package moze_intel.projecte.capability.managing;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class SerializableCapabilityResolver extends BasicCapabilityResolver implements ICapabilitySerializable {
   protected final INBTSerializable internal;

   protected SerializableCapabilityResolver(INBTSerializable internal) {
      super((Object)internal);
      this.internal = internal;
   }

   public CompoundTag serializeNBT() {
      return (CompoundTag)this.internal.serializeNBT();
   }

   public void deserializeNBT(CompoundTag nbt) {
      this.internal.deserializeNBT(nbt);
   }
}
