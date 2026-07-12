package moze_intel.projecte.emc.mappers.recipe;

import java.util.Collection;
import java.util.List;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

@RecipeTypeMapper(
   priority = Integer.MIN_VALUE
)
public class FallbackRecipeTypeMapper extends BaseRecipeTypeMapper {
   public String getName() {
      return "FallbackRecipeType";
   }

   public String getDescription() {
      return "Fallback for default handling of recipes that extend ICraftingRecipe, AbstractCookingRecipe, SingleItemRecipe, or SmithingRecipe. This will catch modded extensions of the vanilla recipe classes, and if the VanillaRecipeTypes mapper is disabled, this mapper will still catch the vanilla recipes.";
   }

   public boolean canHandle(RecipeType recipeType) {
      return true;
   }

   public boolean handleRecipe(IMappingCollector mapper, Recipe recipe, RegistryAccess registryAccess, INSSFakeGroupManager fakeGroupManager) {
      return !(recipe instanceof CraftingRecipe) && !(recipe instanceof AbstractCookingRecipe) && !(recipe instanceof SingleItemRecipe) && !(recipe instanceof SmithingTransformRecipe) && !(recipe instanceof SmithingTrimRecipe) ? false : super.handleRecipe(mapper, recipe, registryAccess, fakeGroupManager);
   }

   protected Collection getIngredients(Recipe recipe) {
      Collection ingredients = super.getIngredients(recipe);
      if (ingredients.isEmpty()) {
         if (recipe instanceof SmithingTransformRecipe) {
            SmithingTransformRecipe transformRecipe = (SmithingTransformRecipe)recipe;
            return List.of(transformRecipe.f_265888_, transformRecipe.f_265907_, transformRecipe.f_265949_);
         }

         if (recipe instanceof SmithingTrimRecipe) {
            SmithingTrimRecipe trimRecipe = (SmithingTrimRecipe)recipe;
            return List.of(trimRecipe.f_266040_, trimRecipe.f_266053_, trimRecipe.f_265958_);
         }
      }

      return ingredients;
   }
}
