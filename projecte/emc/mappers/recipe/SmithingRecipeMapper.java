package moze_intel.projecte.emc.mappers.recipe;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

@RecipeTypeMapper
public class SmithingRecipeMapper extends BaseRecipeTypeMapper {
   public String getName() {
      return "Smithing";
   }

   public String getDescription() {
      return "Maps smithing recipes.";
   }

   public boolean canHandle(RecipeType recipeType) {
      return recipeType == RecipeType.f_44113_;
   }

   protected Collection getIngredients(Recipe recipe) {
      if (recipe instanceof SmithingTransformRecipe transformRecipe) {
         return List.of(transformRecipe.f_265888_, transformRecipe.f_265907_, transformRecipe.f_265949_);
      } else if (recipe instanceof SmithingTrimRecipe trimRecipe) {
         return List.of(trimRecipe.f_266040_, trimRecipe.f_266053_, trimRecipe.f_265958_);
      } else {
         return Collections.emptyList();
      }
   }
}
