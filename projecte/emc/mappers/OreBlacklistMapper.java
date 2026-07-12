package moze_intel.projecte.emc.mappers;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.util.Iterator;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.gameObjs.PETags;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

@EMCMapper
public class OreBlacklistMapper implements IEMCMapper {
   public void addMappings(IMappingCollector mapper, CommentedFileConfig config, ReloadableServerResources serverResources, RegistryAccess registryAccess, ResourceManager resourceManager) {
      Iterator var6 = PETags.Items.ORES_LOOKUP.tag().iterator();

      while(var6.hasNext()) {
         Item ore = (Item)var6.next();
         NSSItem nssOre = NSSItem.createItem((ItemLike)ore);
         mapper.setValueBefore(nssOre, 0L);
         mapper.setValueAfter(nssOre, 0L);
      }

   }

   public String getName() {
      return "OresBlacklistMapper";
   }

   public String getDescription() {
      return "Set EMC=0 for everything in the forge:ores tag";
   }
}
