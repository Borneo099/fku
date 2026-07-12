package moze_intel.projecte.gameObjs.customRecipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import moze_intel.projecte.gameObjs.items.PhilosophersStone;
import moze_intel.projecte.gameObjs.registries.PERecipeSerializers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public class PhiloStoneSmeltingRecipe extends CustomRecipe {
   public PhiloStoneSmeltingRecipe(ResourceLocation id, CraftingBookCategory category) {
      super(id, category);
   }

   public boolean matches(@NotNull CraftingContainer inv, @NotNull Level level) {
      return !this.getMatchingRecipes(inv, level).isEmpty();
   }

   public @NotNull ItemStack assemble(@NotNull CraftingContainer inv, @NotNull RegistryAccess registryAccess) {
      Set matchingRecipes = this.getMatchingRecipes(inv, ServerLifecycleHooks.getCurrentServer().m_129783_());
      if (matchingRecipes.isEmpty()) {
         return ItemStack.f_41583_;
      } else {
         ItemStack output = ((SmeltingRecipe)matchingRecipes.stream().findFirst().get()).m_8043_(registryAccess).m_41777_();
         output.m_41764_(output.m_41613_() * 7);
         return output;
      }
   }

   private Set getMatchingRecipes(CraftingContainer inv, @NotNull Level level) {
      List philoStones = new ArrayList();
      List coals = new ArrayList();
      List allItems = new ArrayList();

      ItemStack philoStone;
      for(int i = 0; i < inv.m_6643_(); ++i) {
         philoStone = inv.m_8020_(i);
         if (!philoStone.m_41619_()) {
            Item item = philoStone.m_41720_();
            allItems.add(philoStone);
            if (allItems.size() > 9) {
               return Collections.emptySet();
            }

            if (item instanceof PhilosophersStone) {
               philoStones.add(philoStone);
            }

            if (philoStone.m_204117_(ItemTags.f_13160_)) {
               coals.add(philoStone);
            }
         }
      }

      if (allItems.size() == 9) {
         Iterator var14 = philoStones.iterator();

         label68:
         while(var14.hasNext()) {
            philoStone = (ItemStack)var14.next();
            Iterator var15 = coals.iterator();

            HashSet matchingRecipes;
            do {
               ItemStack coal;
               do {
                  if (!var15.hasNext()) {
                     continue label68;
                  }

                  coal = (ItemStack)var15.next();
               } while(philoStone == coal);

               matchingRecipes = new HashSet();
               Iterator var11 = allItems.iterator();

               while(var11.hasNext()) {
                  ItemStack stack = (ItemStack)var11.next();
                  if (stack != philoStone && stack != coal) {
                     SimpleContainer furnaceInput = new SimpleContainer(new ItemStack[]{stack});
                     if (matchingRecipes.isEmpty()) {
                        if (!matchingRecipes.addAll(level.m_7465_().m_44056_(RecipeType.f_44108_, furnaceInput, level))) {
                           return Collections.emptySet();
                        }
                     } else if (matchingRecipes.removeIf((recipe) -> {
                        return !recipe.m_5818_(furnaceInput, level);
                     }) && matchingRecipes.isEmpty()) {
                        return Collections.emptySet();
                     }
                  }
               }
            } while(matchingRecipes.isEmpty());

            return matchingRecipes;
         }
      }

      return Collections.emptySet();
   }

   public boolean m_8004_(int width, int height) {
      return width * height >= 9;
   }

   public @NotNull RecipeSerializer m_7707_() {
      return (RecipeSerializer)PERecipeSerializers.PHILO_STONE_SMELTING.get();
   }
}
