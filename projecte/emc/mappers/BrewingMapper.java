package moze_intel.projecte.emc.mappers;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.utils.ItemInfoHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

@EMCMapper
public class BrewingMapper implements IEMCMapper {
   private static final Set allReagents = new HashSet();
   private static final Set allInputs = new HashSet();
   private static int totalConversions;
   private static int totalPotionItems;

   private static boolean mapAllReagents() {
      int conversionCount = PotionBrewing.f_43495_.size() + PotionBrewing.f_43494_.size();
      if (totalConversions == conversionCount) {
         return true;
      } else {
         allReagents.clear();
         addReagents(PotionBrewing.f_43495_);
         addReagents(PotionBrewing.f_43494_);
         totalConversions = conversionCount;
         return true;
      }
   }

   private static void addReagents(List conversions) {
      Iterator var1 = conversions.iterator();

      while(var1.hasNext()) {
         PotionBrewing.Mix conversion = (PotionBrewing.Mix)var1.next();
         ItemStack[] var3 = conversion.f_43533_.m_43908_();
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            ItemStack r = var3[var5];
            allReagents.add(ItemInfo.fromStack(r));
         }
      }

   }

   private static void mapAllInputs() {
      int count = PotionBrewing.f_43496_.size();
      if (totalPotionItems != count) {
         allInputs.clear();
         Set inputs = new HashSet();
         Iterator var2 = PotionBrewing.f_43496_.iterator();

         while(true) {
            ItemStack[] matchingStacks;
            do {
               if (!var2.hasNext()) {
                  var2 = ForgeRegistries.POTIONS.getValues().iterator();

                  while(var2.hasNext()) {
                     Potion potion = (Potion)var2.next();
                     Iterator var10 = inputs.iterator();

                     while(var10.hasNext()) {
                        ItemInfo input = (ItemInfo)var10.next();
                        allInputs.add(ItemInfoHelper.makeWithPotion(input, potion));
                     }
                  }

                  totalPotionItems = count;
                  return;
               }

               Ingredient potionItem = (Ingredient)var2.next();
               matchingStacks = getMatchingStacks(potionItem);
            } while(matchingStacks == null);

            ItemStack[] var5 = matchingStacks;
            int var6 = matchingStacks.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               ItemStack input = var5[var7];
               inputs.add(ItemInfo.fromStack(input));
            }
         }
      }
   }

   public void addMappings(IMappingCollector mapper, CommentedFileConfig config, ReloadableServerResources serverResources, RegistryAccess registryAccess, ResourceManager resourceManager) {
      boolean vanillaRetrieved = mapAllReagents();
      if (vanillaRetrieved) {
         mapAllInputs();
      }

      Map waterIngredients = new HashMap();
      waterIngredients.put(NSSItem.createItem((ItemLike)Items.f_42590_), 1);
      waterIngredients.put(NSSFluid.createTag(FluidTags.f_13131_), 333);
      mapper.addConversion(1, NSSItem.createItem(PotionUtils.m_43549_(new ItemStack(Items.f_42589_), Potions.f_43599_)), (Map)waterIngredients);
      Set canNotMap = new HashSet();
      int recipeCount = 0;
      List recipes = BrewingRecipeRegistry.getRecipes();
      Iterator var11 = recipes.iterator();

      while(true) {
         BrewingRecipe brewingRecipe;
         ItemStack output;
         NSSItem nssInput;
         ItemStack output;
         ItemStack[] validInputs;
         ItemStack[] validReagents;
         do {
            label68:
            do {
               while(var11.hasNext()) {
                  IBrewingRecipe recipe = (IBrewingRecipe)var11.next();
                  if (recipe instanceof BrewingRecipe brewingRecipe) {
                     validInputs = getMatchingStacks(brewingRecipe.getInput());
                     validReagents = getMatchingStacks(brewingRecipe.getIngredient());
                     continue label68;
                  }

                  if (!(recipe instanceof VanillaBrewingRecipe)) {
                     canNotMap.add(recipe.getClass());
                  } else if (!vanillaRetrieved) {
                     canNotMap.add(recipe.getClass());
                  } else {
                     Iterator var14 = allInputs.iterator();

                     while(var14.hasNext()) {
                        ItemInfo inputInfo = (ItemInfo)var14.next();
                        output = inputInfo.createStack();
                        nssInput = NSSItem.createItem(output);
                        Iterator var18 = allReagents.iterator();

                        while(var18.hasNext()) {
                           ItemInfo reagentInfo = (ItemInfo)var18.next();
                           ItemStack validReagent = reagentInfo.createStack();
                           output = recipe.getOutput(output, validReagent);
                           if (!output.m_41619_()) {
                              Map ingredientsWithAmount = new HashMap();
                              ingredientsWithAmount.put(nssInput, 3);
                              ingredientsWithAmount.put(NSSItem.createItem(validReagent), 1);
                              mapper.addConversion(3 * output.m_41613_(), NSSItem.createItem(output), (Map)ingredientsWithAmount);
                              ++recipeCount;
                           }
                        }
                     }
                  }
               }

               PECore.debugLog("BrewingMapper Statistics:");
               PECore.debugLog("Found {} Brewing Recipes", recipeCount);
               var11 = canNotMap.iterator();

               while(var11.hasNext()) {
                  Class c = (Class)var11.next();
                  PECore.debugLog("Could not map Brewing Recipes with Type: {}", c.getName());
               }

               return;
            } while(validInputs == null);
         } while(validReagents == null);

         output = brewingRecipe.getOutput();
         nssInput = NSSItem.createItem(output);
         ItemStack[] var31 = validInputs;
         int var32 = validInputs.length;

         for(int var33 = 0; var33 < var32; ++var33) {
            output = var31[var33];
            NormalizedSimpleStack nssInput = NSSItem.createItem(output);
            ItemStack[] var23 = validReagents;
            int var24 = validReagents.length;

            for(int var25 = 0; var25 < var24; ++var25) {
               ItemStack validReagent = var23[var25];
               Map ingredientsWithAmount = new HashMap();
               ingredientsWithAmount.put(nssInput, 3);
               ingredientsWithAmount.put(NSSItem.createItem(validReagent), validReagent.m_41613_());
               mapper.addConversion(3 * output.m_41613_(), nssInput, (Map)ingredientsWithAmount);
               ++recipeCount;
            }
         }
      }
   }

   public String getName() {
      return "BrewingMapper";
   }

   public String getDescription() {
      return "Add Conversions for Brewing Recipes";
   }

   private static @Nullable ItemStack[] getMatchingStacks(Ingredient ingredient) {
      try {
         return ingredient.m_43908_();
      } catch (Exception var2) {
         return null;
      }
   }
}
