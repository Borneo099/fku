package moze_intel.projecte.gameObjs.customRecipes;

import com.google.gson.JsonObject;
import moze_intel.projecte.gameObjs.items.KleinStar;
import moze_intel.projecte.gameObjs.registries.PERecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class RecipeShapelessKleinStar implements CraftingRecipe {
   private final ShapelessRecipe compose;

   public RecipeShapelessKleinStar(ShapelessRecipe compose) {
      this.compose = compose;
   }

   public @NotNull ResourceLocation m_6423_() {
      return this.compose.m_6423_();
   }

   public @NotNull RecipeSerializer m_7707_() {
      return (RecipeSerializer)PERecipeSerializers.KLEIN.get();
   }

   public boolean matches(@NotNull CraftingContainer inv, @NotNull Level worldIn) {
      return this.compose.m_5818_(inv, worldIn);
   }

   public @NotNull ItemStack assemble(@NotNull CraftingContainer inv, @NotNull RegistryAccess registryAccess) {
      ItemStack result = this.compose.m_5874_(inv, registryAccess);
      long storedEMC = 0L;

      for(int i = 0; i < inv.m_6643_(); ++i) {
         ItemStack stack = inv.m_8020_(i);
         if (!stack.m_41619_() && stack.m_41720_() instanceof KleinStar) {
            storedEMC += KleinStar.getEmc(stack);
         }
      }

      if (storedEMC != 0L && result.m_41720_() instanceof KleinStar) {
         KleinStar.setEmc(result, storedEMC);
      }

      return result;
   }

   public boolean m_8004_(int width, int height) {
      return this.compose.m_8004_(width, height);
   }

   public @NotNull ItemStack m_8043_(@NotNull RegistryAccess registryAccess) {
      return this.compose.m_8043_(registryAccess);
   }

   public @NotNull NonNullList getRemainingItems(@NotNull CraftingContainer inv) {
      return this.compose.m_7457_(inv);
   }

   public @NotNull NonNullList m_7527_() {
      return this.compose.m_7527_();
   }

   public boolean m_5598_() {
      return false;
   }

   public @NotNull String m_6076_() {
      return this.compose.m_6076_();
   }

   public @NotNull ItemStack m_8042_() {
      return this.compose.m_8042_();
   }

   public boolean m_142505_() {
      return this.compose.m_142505_();
   }

   public @NotNull CraftingBookCategory m_245232_() {
      return this.compose.m_245232_();
   }

   public static class Serializer implements RecipeSerializer {
      public @NotNull RecipeShapelessKleinStar fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
         return new RecipeShapelessKleinStar((ShapelessRecipe)RecipeSerializer.f_44077_.m_6729_(recipeId, json));
      }

      public @NotNull RecipeShapelessKleinStar fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
         return new RecipeShapelessKleinStar((ShapelessRecipe)RecipeSerializer.f_44077_.m_8005_(recipeId, buffer));
      }

      public void toNetwork(@NotNull FriendlyByteBuf buffer, RecipeShapelessKleinStar recipe) {
         RecipeSerializer.f_44077_.m_6178_(buffer, recipe.compose);
      }
   }
}
