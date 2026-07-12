package moze_intel.projecte.api.mapper.recipe;

import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public interface IRecipeTypeMapper {
   String getName();

   String getDescription();

   default boolean isAvailable() {
      return true;
   }

   boolean canHandle(RecipeType var1);

   boolean handleRecipe(IMappingCollector var1, Recipe var2, RegistryAccess var3, INSSFakeGroupManager var4);
}
