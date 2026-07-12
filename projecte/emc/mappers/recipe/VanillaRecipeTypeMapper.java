package moze_intel.projecte.emc.mappers.recipe;

import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import net.minecraft.world.item.crafting.RecipeType;

@RecipeTypeMapper
public class VanillaRecipeTypeMapper extends BaseRecipeTypeMapper {
   public String getName() {
      return "VanillaRecipeTypes";
   }

   public String getDescription() {
      return "Maps the different vanilla recipe types.";
   }

   public boolean canHandle(RecipeType recipeType) {
      return recipeType == RecipeType.f_44107_ || recipeType == RecipeType.f_44108_ || recipeType == RecipeType.f_44109_ || recipeType == RecipeType.f_44110_ || recipeType == RecipeType.f_44111_ || recipeType == RecipeType.f_44112_;
   }
}
