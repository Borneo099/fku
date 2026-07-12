package moze_intel.projecte.api.mapper;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;

public interface IEMCMapper {
   String getName();

   String getDescription();

   default boolean isAvailable() {
      return true;
   }

   void addMappings(IMappingCollector var1, CommentedFileConfig var2, ReloadableServerResources var3, RegistryAccess var4, ResourceManager var5);
}
