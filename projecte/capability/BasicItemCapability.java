package moze_intel.projecte.capability;

import net.minecraftforge.common.util.LazyOptional;

public abstract class BasicItemCapability extends ItemCapability {
   private final LazyOptional capability = LazyOptional.of(() -> {
      return this;
   });

   public LazyOptional getLazyCapability() {
      return this.capability;
   }
}
