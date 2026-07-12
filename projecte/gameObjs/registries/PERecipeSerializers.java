package moze_intel.projecte.gameObjs.registries;

import moze_intel.projecte.gameObjs.customRecipes.PhiloStoneSmeltingRecipe;
import moze_intel.projecte.gameObjs.customRecipes.RecipeShapelessKleinStar;
import moze_intel.projecte.gameObjs.customRecipes.RecipesCovalenceRepair;
import moze_intel.projecte.gameObjs.registration.impl.IRecipeSerializerDeferredRegister;
import moze_intel.projecte.gameObjs.registration.impl.IRecipeSerializerRegistryObject;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public class PERecipeSerializers {
   public static final IRecipeSerializerDeferredRegister RECIPE_SERIALIZERS = new IRecipeSerializerDeferredRegister("projecte");
   public static final IRecipeSerializerRegistryObject COVALENCE_REPAIR;
   public static final IRecipeSerializerRegistryObject KLEIN;
   public static final IRecipeSerializerRegistryObject PHILO_STONE_SMELTING;

   static {
      COVALENCE_REPAIR = RECIPE_SERIALIZERS.register("covalence_repair", () -> {
         return new SimpleCraftingRecipeSerializer(RecipesCovalenceRepair::new);
      });
      KLEIN = RECIPE_SERIALIZERS.register("crafting_shapeless_kleinstar", RecipeShapelessKleinStar.Serializer::new);
      PHILO_STONE_SMELTING = RECIPE_SERIALIZERS.register("philo_stone_smelting", () -> {
         return new SimpleCraftingRecipeSerializer(PhiloStoneSmeltingRecipe::new);
      });
   }
}
