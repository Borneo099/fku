package moze_intel.projecte.emc.mappers;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.util.Collections;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;

public class TagMapper implements IEMCMapper {
   public void addMappings(IMappingCollector mapper, CommentedFileConfig config, ReloadableServerResources serverResources, RegistryAccess registryAccess, ResourceManager resourceManager) {
      AbstractNSSTag.getAllCreatedTags().forEach((stack) -> {
         stack.forEachElement((normalizedSimpleStack) -> {
            mapper.addConversion(1, stack, (Iterable)Collections.singletonList(normalizedSimpleStack));
            mapper.addConversion(1, normalizedSimpleStack, (Iterable)Collections.singletonList(stack));
         });
      });
   }

   public String getName() {
      return "TagMapper";
   }

   public String getDescription() {
      return "Adds back and forth conversions of objects and their Tag variant. (EMC values assigned to tags will not behave properly if this mapper is disabled)";
   }
}
