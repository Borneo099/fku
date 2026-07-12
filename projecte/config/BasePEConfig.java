package moze_intel.projecte.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import moze_intel.projecte.config.value.CachedValue;

public abstract class BasePEConfig implements IPEConfig {
   private final List cachedConfigValues = new ArrayList();

   public void clearCache(boolean unloading) {
      Iterator var2 = this.cachedConfigValues.iterator();

      while(var2.hasNext()) {
         CachedValue cachedConfigValue = (CachedValue)var2.next();
         cachedConfigValue.clearCache(unloading);
      }

   }

   public void addCachedValue(CachedValue configValue) {
      this.cachedConfigValues.add(configValue);
   }
}
