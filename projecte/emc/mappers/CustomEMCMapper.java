package moze_intel.projecte.emc.mappers;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.util.Iterator;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.config.CustomEMCParser;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;

@EMCMapper
public class CustomEMCMapper implements IEMCMapper {
   public void addMappings(IMappingCollector mapper, CommentedFileConfig config, ReloadableServerResources serverResources, RegistryAccess registryAccess, ResourceManager resourceManager) {
      Iterator var6 = CustomEMCParser.currentEntries.entries.iterator();

      while(var6.hasNext()) {
         CustomEMCParser.CustomEMCEntry entry = (CustomEMCParser.CustomEMCEntry)var6.next();
         PECore.debugLog("Adding custom EMC value for {}: {}", entry.item, entry.emc);
         mapper.setValueBefore(entry.item, entry.emc);
         NormalizedSimpleStack var9 = entry.item;
         if (var9 instanceof NSSTag nssTag) {
            nssTag.forEachElement((normalizedSimpleStack) -> {
               mapper.setValueBefore(normalizedSimpleStack, entry.emc);
            });
         }
      }

   }

   public String getName() {
      return "CustomEMCMapper";
   }

   public String getDescription() {
      return "Uses the `custom_emc.json` File to add EMC values.";
   }
}
