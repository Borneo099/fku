package moze_intel.projecte.emc.mappers.recipe;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.emc.IngredientMap;
import moze_intel.projecte.utils.RegistryUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

public abstract class BaseRecipeTypeMapper implements IRecipeTypeMapper {
   public boolean handleRecipe(IMappingCollector mapper, Recipe recipe, RegistryAccess registryAccess, INSSFakeGroupManager fakeGroupManager) {
      ItemStack recipeOutput = recipe.m_8043_(registryAccess);
      if (recipeOutput.m_41619_()) {
         return false;
      } else {
         Collection ingredientsChecked = this.getIngredientsChecked(recipe);
         if (ingredientsChecked == null) {
            return true;
         } else {
            ResourceLocation recipeID = recipe.m_6423_();
            List dummyGroupInfos = new ArrayList();
            IngredientMap ingredientMap = new IngredientMap();
            Iterator var10 = ingredientsChecked.iterator();

            ItemStack[] matches;
            label78:
            do {
               while(var10.hasNext()) {
                  Ingredient recipeItem = (Ingredient)var10.next();
                  matches = this.getMatchingStacks(recipeItem, recipeID);
                  if (matches == null) {
                     return this.addConversionsAndReturn(mapper, dummyGroupInfos, true);
                  }

                  if (matches.length == 1) {
                     if (matches[0].m_41619_()) {
                        return this.addConversionsAndReturn(mapper, dummyGroupInfos, false);
                     }
                     continue label78;
                  }

                  if (matches.length > 0) {
                     Set rawNSSMatches = new HashSet();
                     List stacks = new ArrayList();
                     ItemStack[] var15 = matches;
                     int var16 = matches.length;

                     for(int var17 = 0; var17 < var16; ++var17) {
                        ItemStack match = var15[var17];
                        if (!match.m_41619_()) {
                           rawNSSMatches.add(NSSItem.createItem(match));
                           stacks.add(match);
                        }
                     }

                     int count = stacks.size();
                     if (count == 0) {
                        return this.addConversionsAndReturn(mapper, dummyGroupInfos, false);
                     }

                     if (count == 1) {
                        if (this.addIngredient(ingredientMap, ((ItemStack)stacks.get(0)).m_41777_(), recipeID)) {
                           return this.addConversionsAndReturn(mapper, dummyGroupInfos, true);
                        }
                     } else {
                        Tuple group = fakeGroupManager.getOrCreateFakeGroup(rawNSSMatches);
                        NormalizedSimpleStack dummy = (NormalizedSimpleStack)group.m_14418_();
                        ingredientMap.addIngredient(dummy, 1);
                        if ((Boolean)group.m_14419_()) {
                           List groupIngredientMaps = new ArrayList();
                           Iterator var19 = stacks.iterator();

                           while(var19.hasNext()) {
                              ItemStack stack = (ItemStack)var19.next();
                              IngredientMap groupIngredientMap = new IngredientMap();
                              if (this.addIngredient(groupIngredientMap, stack.m_41777_(), recipeID)) {
                                 return this.addConversionsAndReturn(mapper, dummyGroupInfos, true);
                              }

                              groupIngredientMaps.add(groupIngredientMap);
                           }

                           dummyGroupInfos.add(new Tuple(dummy, groupIngredientMaps));
                        }
                     }
                  }
               }

               mapper.addConversion(recipeOutput.m_41613_(), NSSItem.createItem(recipeOutput), (Map)ingredientMap.getMap());
               return this.addConversionsAndReturn(mapper, dummyGroupInfos, true);
            } while(!this.addIngredient(ingredientMap, matches[0].m_41777_(), recipeID));

            return this.addConversionsAndReturn(mapper, dummyGroupInfos, true);
         }
      }
   }

   private boolean addConversionsAndReturn(IMappingCollector mapper, List dummyGroupInfos, boolean returnValue) {
      Iterator var4 = dummyGroupInfos.iterator();

      while(var4.hasNext()) {
         Tuple dummyGroupInfo = (Tuple)var4.next();
         Iterator var6 = ((List)dummyGroupInfo.m_14419_()).iterator();

         while(var6.hasNext()) {
            IngredientMap groupIngredientMap = (IngredientMap)var6.next();
            mapper.addConversion(1, (NormalizedSimpleStack)dummyGroupInfo.m_14418_(), (Map)groupIngredientMap.getMap());
         }
      }

      return returnValue;
   }

   private @Nullable ItemStack[] getMatchingStacks(Ingredient ingredient, ResourceLocation recipeID) {
      try {
         return ingredient.m_43908_();
      } catch (Exception var4) {
         if (this.isTagException(var4)) {
            PECore.LOGGER.error(LogUtils.FATAL_MARKER, "Error mapping recipe {}. Ingredient of type: {} crashed when getting the matching stacks due to not properly deserializing and handling tags. Please report this to the ingredient's creator.", new Object[]{recipeID, ingredient.getClass().getName(), var4});
         } else {
            PECore.LOGGER.error(LogUtils.FATAL_MARKER, "Error mapping recipe {}. Ingredient of type: {} crashed when getting the matching stacks. Please report this to the ingredient's creator.", new Object[]{recipeID, ingredient.getClass().getName(), var4});
         }

         return null;
      }
   }

   private boolean addIngredient(IngredientMap ingredientMap, ItemStack stack, ResourceLocation recipeID) {
      Item item = stack.m_41720_();
      boolean hasContainerItem = false;

      try {
         hasContainerItem = item.hasCraftingRemainingItem(stack);
         if (hasContainerItem) {
            ingredientMap.addIngredient(NSSItem.createItem(item.getCraftingRemainingItem(stack)), -1);
         }
      } catch (Exception var8) {
         ResourceLocation itemName = RegistryUtils.getName(item);
         if (hasContainerItem) {
            if (this.isTagException(var8)) {
               PECore.LOGGER.error(LogUtils.FATAL_MARKER, "Error mapping recipe {}. Item: {} reported that it has a container item, but errors when trying to get the container item due to not properly deserializing and handling tags. Please report this to {}.", new Object[]{recipeID, itemName, itemName.m_135827_(), var8});
            } else {
               PECore.LOGGER.error(LogUtils.FATAL_MARKER, "Error mapping recipe {}. Item: {} reported that it has a container item, but errors when trying to get the container item based on the stack in the recipe. Please report this to {}.", new Object[]{recipeID, itemName, itemName.m_135827_(), var8});
            }
         } else if (this.isTagException(var8)) {
            PECore.LOGGER.error(LogUtils.FATAL_MARKER, "Error mapping recipe {}. Item: {} crashed when checking if the stack has a container item, due to not properly deserializing and handling tags. Please report this to {}.", new Object[]{recipeID, itemName, itemName.m_135827_(), var8});
         } else {
            PECore.LOGGER.error(LogUtils.FATAL_MARKER, "Error mapping recipe {}. Item: {} crashed when checking if the stack in the recipe has a container item. Please report this to {}.", new Object[]{recipeID, itemName, itemName.m_135827_(), var8});
         }

         return true;
      }

      ingredientMap.addIngredient(NSSItem.createItem(stack), 1);
      return false;
   }

   private boolean isTagException(Exception e) {
      return e instanceof IllegalStateException && e.getMessage().matches("Tag \\S*:\\S* used before it was bound");
   }

   private @Nullable Collection getIngredientsChecked(Recipe recipe) {
      try {
         return this.getIngredients(recipe);
      } catch (Exception var4) {
         ResourceLocation recipeID = recipe.m_6423_();
         if (this.isTagException(var4)) {
            PECore.LOGGER.error(LogUtils.FATAL_MARKER, "Error mapping recipe {}. Failed to get ingredients due to the recipe not properly deserializing and handling tags. Please report this to {}.", new Object[]{recipeID, recipeID.m_135827_(), var4});
         } else {
            PECore.LOGGER.error(LogUtils.FATAL_MARKER, "Error mapping recipe {}. Failed to get ingredients. Please report this to {}.", new Object[]{recipeID, recipeID.m_135827_(), var4});
         }

         return null;
      }
   }

   protected Collection getIngredients(Recipe recipe) {
      return recipe.m_7527_();
   }
}
