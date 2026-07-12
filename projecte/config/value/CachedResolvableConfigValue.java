package moze_intel.projecte.config.value;

import java.util.function.Supplier;
import moze_intel.projecte.config.IPEConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CachedResolvableConfigValue extends CachedValue implements Supplier {
   private @Nullable Object cachedValue;

   protected CachedResolvableConfigValue(IPEConfig config, ForgeConfigSpec.ConfigValue internal) {
      super(config, internal);
   }

   protected abstract Object resolve(Object var1);

   protected abstract Object encode(Object var1);

   public @NotNull Object getOrDefault() {
      return this.cachedValue == null && !this.isLoaded() ? this.resolve(this.internal.getDefault()) : this.get();
   }

   public @NotNull Object get() {
      if (this.cachedValue == null) {
         this.cachedValue = this.resolve(this.internal.get());
      }

      return this.cachedValue;
   }

   public void set(Object value) {
      this.internal.set(this.encode(value));
      this.cachedValue = value;
   }

   protected boolean clearCachedValue(boolean checkChanged) {
      if (this.cachedValue == null) {
         return false;
      } else {
         Object oldCachedValue = this.cachedValue;
         this.cachedValue = null;
         return checkChanged && !oldCachedValue.equals(this.get());
      }
   }
}
