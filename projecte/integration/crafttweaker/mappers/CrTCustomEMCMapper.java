package moze_intel.projecte.integration.crafttweaker.mappers;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

@EMCMapper(
   requiredMods = {"crafttweaker"}
)
public class CrTCustomEMCMapper implements IEMCMapper {
   private static final Map customEmcValues = new HashMap();

   public static void registerCustomEMC(@NotNull NormalizedSimpleStack stack, long emcValue) {
      customEmcValues.put(stack, emcValue);
   }

   public static void unregisterNSS(@NotNull NormalizedSimpleStack stack) {
      customEmcValues.remove(stack);
   }

   public void addMappings(IMappingCollector mapper, CommentedFileConfig config, ReloadableServerResources serverResources, RegistryAccess registryAccess, ResourceManager resourceManager) {
      NormalizedSimpleStack normStack;
      long value;
      for(Iterator var6 = customEmcValues.entrySet().iterator(); var6.hasNext(); PECore.debugLog("CraftTweaker setting value for {} to {}", normStack, value)) {
         Map.Entry entry = (Map.Entry)var6.next();
         normStack = (NormalizedSimpleStack)entry.getKey();
         value = (Long)entry.getValue();
         mapper.setValueBefore(normStack, value);
         if (normStack instanceof NSSTag nssTag) {
            nssTag.forEachElement((normalizedSimpleStack) -> {
               mapper.setValueBefore(normalizedSimpleStack, value);
            });
         }
      }

   }

   public String getName() {
      return "CrTCustomEMCMapper";
   }

   public String getDescription() {
      return "Allows setting EMC values through CraftTweaker. This behaves similarly to if someone used the custom emc file instead.";
   }
}
