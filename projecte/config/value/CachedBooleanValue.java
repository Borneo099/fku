package moze_intel.projecte.config.value;

import java.util.function.BooleanSupplier;
import moze_intel.projecte.config.IPEConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public class CachedBooleanValue extends CachedValue implements BooleanSupplier {
   private boolean resolved;
   private boolean cachedValue;

   private CachedBooleanValue(IPEConfig config, ForgeConfigSpec.ConfigValue internal) {
      super(config, internal);
   }

   public static CachedBooleanValue wrap(IPEConfig config, ForgeConfigSpec.ConfigValue internal) {
      return new CachedBooleanValue(config, internal);
   }

   public boolean getOrDefault() {
      return !this.resolved && !this.isLoaded() ? (Boolean)this.internal.getDefault() : this.get();
   }

   public boolean get() {
      if (!this.resolved) {
         this.cachedValue = (Boolean)this.internal.get();
         this.resolved = true;
      }

      return this.cachedValue;
   }

   public boolean getAsBoolean() {
      return this.get();
   }

   public void set(boolean value) {
      this.internal.set(value);
      this.cachedValue = value;
   }

   protected boolean clearCachedValue(boolean checkChanged) {
      if (!this.resolved) {
         return false;
      } else {
         boolean oldCachedValue = this.cachedValue;
         this.resolved = false;
         return checkChanged && oldCachedValue != this.get();
      }
   }
}
