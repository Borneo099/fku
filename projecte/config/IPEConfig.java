package moze_intel.projecte.config;

import moze_intel.projecte.config.value.CachedValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public interface IPEConfig {
   String getFileName();

   ForgeConfigSpec getConfigSpec();

   default boolean isLoaded() {
      return this.getConfigSpec().isLoaded();
   }

   ModConfig.Type getConfigType();

   void clearCache(boolean var1);

   void addCachedValue(CachedValue var1);

   default boolean addToContainer() {
      return true;
   }
}
