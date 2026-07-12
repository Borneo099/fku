package moze_intel.projecte.emc.mappers;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.util.HashMap;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

@EMCMapper
public class TippedArrowMapper implements IEMCMapper {
   public void addMappings(IMappingCollector mapper, CommentedFileConfig config, ReloadableServerResources serverResources, RegistryAccess registryAccess, ResourceManager resourceManager) {
      int recipeCount = 0;
      NSSItem nssArrow = NSSItem.createItem((ItemLike)Items.f_42412_);

      for(Iterator var8 = ForgeRegistries.POTIONS.getValues().iterator(); var8.hasNext(); ++recipeCount) {
         Potion potionType = (Potion)var8.next();
         Map ingredientsWithAmount = new HashMap();
         ingredientsWithAmount.put(nssArrow, 8);
         ingredientsWithAmount.put(NSSItem.createItem(PotionUtils.m_43549_(new ItemStack(Items.f_42739_), potionType)), 1);
         mapper.addConversion(8, NSSItem.createItem(PotionUtils.m_43549_(new ItemStack(Items.f_42738_), potionType)), (Map)ingredientsWithAmount);
      }

      PECore.debugLog("TippedArrowMapper Statistics:");
      PECore.debugLog("Found {} Tipped Arrow Recipes", recipeCount);
   }

   public String getName() {
      return "TippedArrowMapper";
   }

   public String getDescription() {
      return "Add Conversions for all lingering potions to arrow recipes";
   }
}
