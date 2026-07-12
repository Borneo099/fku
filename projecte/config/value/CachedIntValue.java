package moze_intel.projecte.config.value;

import java.util.function.IntSupplier;
import moze_intel.projecte.config.IPEConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public class CachedIntValue extends CachedValue implements IntSupplier {
   private boolean resolved;
   private int cachedValue;

   private CachedIntValue(IPEConfig config, ForgeConfigSpec.ConfigValue internal) {
      super(config, internal);
   }

   public static CachedIntValue wrap(IPEConfig config, ForgeConfigSpec.ConfigValue internal) {
      return new CachedIntValue(config, internal);
   }

   public int getOrDefault() {
      return !this.resolved && !this.isLoaded() ? (Integer)this.internal.getDefault() : this.get();
   }

   public int get() {
      if (!this.resolved) {
         this.cachedValue = (Integer)this.internal.get();
         this.resolved = true;
      }

      return this.cachedValue;
   }

   public int getAsInt() {
      return this.get();
   }

   public void set(int value) {
      this.internal.set(value);
      this.cachedValue = value;
   }

   protected boolean clearCachedValue(boolean checkChanged) {
      if (!this.resolved) {
         return false;
      } else {
         int oldCachedValue = this.cachedValue;
         this.resolved = false;
         return checkChanged && oldCachedValue != this.get();
      }
   }
}
