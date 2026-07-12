package moze_intel.projecte.config.value;

import java.util.function.DoubleSupplier;
import moze_intel.projecte.config.IPEConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public class CachedDoubleValue extends CachedValue implements DoubleSupplier {
   private boolean resolved;
   private double cachedValue;

   private CachedDoubleValue(IPEConfig config, ForgeConfigSpec.ConfigValue internal) {
      super(config, internal);
   }

   public static CachedDoubleValue wrap(IPEConfig config, ForgeConfigSpec.ConfigValue internal) {
      return new CachedDoubleValue(config, internal);
   }

   public double getOrDefault() {
      return !this.resolved && !this.isLoaded() ? (Double)this.internal.getDefault() : this.get();
   }

   public double get() {
      if (!this.resolved) {
         this.cachedValue = (Double)this.internal.get();
         this.resolved = true;
      }

      return this.cachedValue;
   }

   public double getAsDouble() {
      return this.get();
   }

   public void set(double value) {
      this.internal.set(value);
      this.cachedValue = value;
   }

   protected boolean clearCachedValue(boolean checkChanged) {
      if (!this.resolved) {
         return false;
      } else {
         double oldCachedValue = this.cachedValue;
         this.resolved = false;
         return checkChanged && oldCachedValue != this.get();
      }
   }
}
