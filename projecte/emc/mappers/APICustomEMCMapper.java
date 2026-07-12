package moze_intel.projecte.emc.mappers;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.imc.CustomEMCRegistration;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.emc.EMCMappingHandler;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;

@EMCMapper
public class APICustomEMCMapper implements IEMCMapper {
   @EMCMapper.Instance
   public static final APICustomEMCMapper INSTANCE = new APICustomEMCMapper();
   private static final int PRIORITY_MIN_VALUE = 0;
   private static final int PRIORITY_MAX_VALUE = 512;
   private static final int PRIORITY_DEFAULT_VALUE = 1;
   private final Map customEMCforMod = new HashMap();

   private APICustomEMCMapper() {
   }

   public void registerCustomEMC(String modid, CustomEMCRegistration customEMCRegistration) {
      NormalizedSimpleStack stack = customEMCRegistration.stack();
      if (stack != null) {
         long emcValue = customEMCRegistration.value();
         if (emcValue < 0L) {
            emcValue = 0L;
         }

         PECore.debugLog("Mod: '{}' registered a custom EMC value of: '{}' for the NormalizedSimpleStack: '{}'", modid, emcValue, stack);
         ((Map)this.customEMCforMod.computeIfAbsent(modid, (k) -> {
            return new HashMap();
         })).put(stack, emcValue);
      }
   }

   public String getName() {
      return "APICustomEMCMapper";
   }

   public String getDescription() {
      return "Allows other mods to easily set EMC values using the ProjectEAPI";
   }

   public void addMappings(IMappingCollector mapper, CommentedFileConfig config, ReloadableServerResources serverResources, RegistryAccess registryAccess, ResourceManager resourceManager) {
      Map priorityMap = new HashMap();
      Iterator var7 = this.customEMCforMod.keySet().iterator();

      String modId;
      while(var7.hasNext()) {
         String modId = (String)var7.next();
         modId = this.getName() + ".priority." + (modId == null ? "__no_modid" : modId);
         int priority = (Integer)EMCMappingHandler.getOrSetDefault(config, modId, "Priority for this mod", 1);
         priorityMap.put(modId, priority);
      }

      List modIds = new ArrayList(this.customEMCforMod.keySet());
      Objects.requireNonNull(priorityMap);
      modIds.sort(Comparator.comparingInt(priorityMap::get).reversed());
      Iterator var18 = modIds.iterator();

      while(true) {
         String modIdOrUnknown;
         do {
            if (!var18.hasNext()) {
               return;
            }

            modId = (String)var18.next();
            modIdOrUnknown = modId == null ? "unknown mod" : modId;
         } while(!this.customEMCforMod.containsKey(modId));

         Iterator var11 = ((Map)this.customEMCforMod.get(modId)).entrySet().iterator();

         while(var11.hasNext()) {
            Map.Entry entry = (Map.Entry)var11.next();
            NormalizedSimpleStack normStack = (NormalizedSimpleStack)entry.getKey();
            long emc = (Long)entry.getValue();
            if (this.isAllowedToSet(modId, normStack, emc, config)) {
               mapper.setValueBefore(normStack, emc);
               if (normStack instanceof NSSTag) {
                  NSSTag nssTag = (NSSTag)normStack;
                  nssTag.forEachElement((normalizedSimpleStack) -> {
                     mapper.setValueBefore(normalizedSimpleStack, emc);
                  });
               }

               PECore.debugLog("{} setting value for {} to {}", modIdOrUnknown, normStack, emc);
            } else {
               PECore.debugLog("Disallowed {} to set the value for {} to {}", modIdOrUnknown, normStack, emc);
            }
         }
      }
   }

   private boolean isAllowedToSet(String modId, NormalizedSimpleStack stack, Long value, CommentedFileConfig config) {
      String resourceLocation;
      if (stack instanceof NSSItem nssItem) {
         resourceLocation = nssItem.getResourceLocation().toString();
      } else {
         resourceLocation = "IntermediateFakeItemsUsedInRecipes:";
      }

      String modForItem = resourceLocation.substring(0, resourceLocation.indexOf(58));
      String configPath = String.format("permissions.%s.%s", modId, modForItem);
      String comment = String.format("Allow mod '%s' to set and or remove values for mod '%s'. Options: [both, set, remove, none]", modId, modForItem);
      String permission = (String)EMCMappingHandler.getOrSetDefault(config, configPath, comment, "both");
      if (permission.equals("both")) {
         return true;
      } else {
         return value == 0L ? permission.equals("remove") : permission.equals("set");
      }
   }
}
