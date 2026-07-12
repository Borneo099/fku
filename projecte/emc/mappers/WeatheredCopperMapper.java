package moze_intel.projecte.emc.mappers;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.collect.BiMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.WeatheringCopper;

@EMCMapper
public class WeatheredCopperMapper implements IEMCMapper {
   public void addMappings(IMappingCollector mapper, CommentedFileConfig config, ReloadableServerResources serverResources, RegistryAccess registryAccess, ResourceManager resourceManager) {
      int recipeCount = 0;

      for(Iterator var7 = ((BiMap)WeatheringCopper.f_154886_.get()).entrySet().iterator(); var7.hasNext(); recipeCount += 2) {
         Map.Entry entry = (Map.Entry)var7.next();
         NSSItem unweathered = NSSItem.createItem((ItemLike)entry.getKey());
         NSSItem weathered = NSSItem.createItem((ItemLike)entry.getValue());
         mapper.addConversion(1, weathered, (Iterable)Collections.singleton(unweathered));
         mapper.addConversion(1, unweathered, (Iterable)Collections.singleton(weathered));
      }

      PECore.debugLog("WeatheredCopperMapper Statistics:");
      PECore.debugLog("Found {} Weathered Copper Conversions", recipeCount);
   }

   public String getName() {
      return "WeatheredCopperMapper";
   }

   public String getDescription() {
      return "Add Conversions for all weathered copper variants";
   }
}
