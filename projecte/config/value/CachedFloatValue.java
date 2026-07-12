package moze_intel.projecte.config.value;

import moze_intel.projecte.config.IPEConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public class CachedFloatValue extends CachedValue {
   private boolean resolved;
   private float cachedValue;

   private CachedFloatValue(IPEConfig config, ForgeConfigSpec.ConfigValue internal) {
      super(config, internal);
   }

   public static CachedFloatValue wrap(IPEConfig config, ForgeConfigSpec.ConfigValue internal) {
      return new CachedFloatValue(config, internal);
   }

   public float getOrDefault() {
      return !this.resolved && !this.isLoaded() ? this.clampInternal((Double)this.internal.getDefault()) : this.get();
   }

   public float get() {
      if (!this.resolved) {
         this.cachedValue = this.clampInternal((Double)this.internal.get());
         this.resolved = true;
      }

      return this.cachedValue;
   }

   private float clampInternal(Double val) {
      if (val == null) {
         return 0.0F;
      } else if (val > 3.4028234663852886E38) {
         return Float.MAX_VALUE;
      } else {
         return val < -3.4028234663852886E38 ? -3.4028235E38F : val.floatValue();
      }
   }

   public void set(float value) {
      this.internal.set((double)value);
      this.cachedValue = value;
   }

   protected boolean clearCachedValue(boolean checkChanged) {
      if (!this.resolved) {
         return false;
      } else {
         float oldCachedValue = this.cachedValue;
         this.resolved = false;
         return checkChanged && oldCachedValue != this.get();
      }
   }
}
