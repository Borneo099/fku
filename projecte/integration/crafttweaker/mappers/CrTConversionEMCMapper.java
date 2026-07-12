package moze_intel.projecte.integration.crafttweaker.mappers;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
public class CrTConversionEMCMapper implements IEMCMapper {
   private static final List storedConversions = new ArrayList();

   public static void addConversion(@NotNull @NotNull CrTConversion conversion) {
      storedConversions.add(conversion);
   }

   public static void removeConversion(@NotNull @NotNull CrTConversion conversion) {
      storedConversions.remove(conversion);
   }

   public void addMappings(IMappingCollector mapper, CommentedFileConfig config, ReloadableServerResources serverResources, RegistryAccess registryAccess, ResourceManager resourceManager) {
      CrTConversion apiConversion;
      for(Iterator var6 = storedConversions.iterator(); var6.hasNext(); PECore.debugLog("CraftTweaker adding conversion for {}", apiConversion.output)) {
         apiConversion = (CrTConversion)var6.next();
         if (apiConversion.propagateTags) {
            NormalizedSimpleStack var9 = apiConversion.output;
            if (var9 instanceof NSSTag) {
               NSSTag output = (NSSTag)var9;
               output.forEachElement((normalizedSimpleStack) -> {
                  if (apiConversion.set) {
                     mapper.setValueFromConversion(apiConversion.amount, normalizedSimpleStack, (Map)apiConversion.ingredients);
                  } else {
                     mapper.addConversion(apiConversion.amount, normalizedSimpleStack, (Map)apiConversion.ingredients);
                  }

               });
            }
         }

         if (apiConversion.set) {
            mapper.setValueFromConversion(apiConversion.amount, apiConversion.output, (Map)apiConversion.ingredients);
         } else {
            mapper.addConversion(apiConversion.amount, apiConversion.output, (Map)apiConversion.ingredients);
         }
      }

   }

   public String getName() {
      return "CrTConversionEMCMapper";
   }

   public String getDescription() {
      return "Allows adding custom conversions through CraftTweaker. This behaves similarly to if someone used a custom conversion file instead.";
   }

   public static record CrTConversion(NormalizedSimpleStack output, int amount, boolean propagateTags, boolean set, Map ingredients) {
      public CrTConversion(NormalizedSimpleStack output, int amount, boolean propagateTags, boolean set, Map ingredients) {
         this.output = output;
         this.amount = amount;
         this.propagateTags = propagateTags;
         this.set = set;
         this.ingredients = ingredients;
      }

      public NormalizedSimpleStack output() {
         return this.output;
      }

      public int amount() {
         return this.amount;
      }

      public boolean propagateTags() {
         return this.propagateTags;
      }

      public boolean set() {
         return this.set;
      }

      public Map ingredients() {
         return this.ingredients;
      }
   }
}
