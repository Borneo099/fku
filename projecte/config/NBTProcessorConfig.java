package moze_intel.projecte.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.config.value.CachedBooleanValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NBTProcessorConfig extends BasePEConfig {
   private static NBTProcessorConfig INSTANCE;
   private static final String ENABLED = "enabled";
   private static final String PERSISTENT = "persistent";
   private static final String MAIN_KEY = "processors";
   private final ForgeConfigSpec configSpec;
   private final Map processorConfigs = new HashMap();

   public static void setup(@NotNull List processors) {
      if (INSTANCE == null) {
         ProjectEConfig.registerConfig(INSTANCE = new NBTProcessorConfig(processors));
      }

   }

   private NBTProcessorConfig(@NotNull List processors) {
      ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
      builder.comment(new String[]{"This config is used to control which NBT Processors get used, and which ones actually contribute to the persistent NBT data that gets saved to knowledge/copied in a condenser.", "To disable an NBT Processor set the 'enabled' option for it to false.", "To disable an NBT Processor from contributing to the persistent data set the 'persistent' option for it to false. Note: that if there is no persistent' config option, the NBT Processor never has any persistent data.", "The config options in this file are synced from server to client, as the processors get used dynamically to calculate/preview EMC values for items and are not included in the synced EMC mappings."}).push("processors");
      Iterator var3 = processors.iterator();

      while(var3.hasNext()) {
         INBTProcessor processor = (INBTProcessor)var3.next();
         this.processorConfigs.put(processor.getName(), new ProcessorConfig(this, builder, processor));
      }

      builder.pop();
      this.configSpec = builder.build();
   }

   public static boolean isEnabled(INBTProcessor processor) {
      if (INSTANCE == null) {
         return true;
      } else {
         String name = processor.getName();
         ProcessorConfig processorConfig = (ProcessorConfig)INSTANCE.processorConfigs.get(name);
         if (processorConfig == null) {
            PECore.LOGGER.warn("Processor Config: '{}' is missing from the config.", name);
            return false;
         } else {
            return processorConfig.enabled.get();
         }
      }
   }

   public static boolean hasPersistent(INBTProcessor processor) {
      if (INSTANCE == null) {
         return false;
      } else {
         String name = processor.getName();
         ProcessorConfig processorConfig = (ProcessorConfig)INSTANCE.processorConfigs.get(name);
         if (processorConfig == null) {
            PECore.LOGGER.warn("Processor Config: '{}' is missing from the config.", name);
            return false;
         } else if (processorConfig.persistent == null) {
            if (processor.hasPersistentNBT()) {
               PECore.LOGGER.warn("Processor Config: '{}' has persistent NBT but is missing the config option.", name);
            }

            return false;
         } else {
            return processorConfig.persistent.get();
         }
      }
   }

   public String getFileName() {
      return "processing";
   }

   public ForgeConfigSpec getConfigSpec() {
      return this.configSpec;
   }

   public ModConfig.Type getConfigType() {
      return Type.SERVER;
   }

   public boolean addToContainer() {
      return false;
   }

   private static class ProcessorConfig {
      public final CachedBooleanValue enabled;
      public final @Nullable CachedBooleanValue persistent;

      private ProcessorConfig(IPEConfig config, ForgeConfigSpec.Builder builder, INBTProcessor processor) {
         builder.comment(processor.getDescription()).push(processor.getName());
         this.enabled = CachedBooleanValue.wrap(config, builder.define("enabled", processor.isAvailable()));
         if (processor.hasPersistentNBT()) {
            this.persistent = CachedBooleanValue.wrap(config, builder.define("persistent", processor.usePersistentNBT()));
         } else {
            this.persistent = null;
         }

         builder.pop();
      }
   }
}
