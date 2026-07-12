package moze_intel.projecte.emc.mappers.recipe;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSFake;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.utils.AnnotationHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

@EMCMapper
public class CraftingMapper implements IEMCMapper {
   private static final List recipeMappers = new ArrayList();

   public static void loadMappers() {
      if (recipeMappers.isEmpty()) {
         recipeMappers.addAll(AnnotationHelper.getRecipeTypeMappers());
      }

   }

   public void addMappings(IMappingCollector mapper, CommentedFileConfig config, ReloadableServerResources serverResources, RegistryAccess registryAccess, ResourceManager resourceManager) {
      NSSFake.setCurrentNamespace("craftingMapper");
      Map recipeCount = new HashMap();
      Set canNotMap = new HashSet();
      RecipeManager recipeManager = serverResources.m_206887_();
      NSSFakeGroupManager fakeGroupManager = new NSSFakeGroupManager();
      Iterator var10 = ForgeRegistries.RECIPE_TYPES.getEntries().iterator();

      Map.Entry entry;
      ResourceLocation typeRegistryName;
      while(var10.hasNext()) {
         entry = (Map.Entry)var10.next();
         typeRegistryName = ((ResourceKey)entry.getKey()).m_135782_();
         RecipeType recipeType = (RecipeType)entry.getValue();
         boolean wasHandled = false;
         List recipes = null;
         List unhandled = new ArrayList();
         Iterator var17 = recipeMappers.iterator();

         label100:
         while(true) {
            int numHandled;
            do {
               IRecipeTypeMapper recipeMapper;
               String configKey;
               do {
                  do {
                     if (!var17.hasNext()) {
                        break label100;
                     }

                     recipeMapper = (IRecipeTypeMapper)var17.next();
                     String var10000 = this.getName();
                     configKey = var10000 + "." + recipeMapper.getName() + ".enabled";
                  } while(!(Boolean)EMCMappingHandler.getOrSetDefault(config, configKey, recipeMapper.getDescription(), recipeMapper.isAvailable()));
               } while(!recipeMapper.canHandle(recipeType));

               if (recipes == null) {
                  recipes = recipeManager.m_44013_(recipeType);
               }

               numHandled = 0;
               Iterator var21 = ((List)recipes).iterator();

               while(var21.hasNext()) {
                  Recipe recipe = (Recipe)var21.next();

                  try {
                     if (recipeMapper.handleRecipe(mapper, recipe, registryAccess, fakeGroupManager)) {
                        ++numHandled;
                     } else {
                        unhandled.add(recipe);
                     }
                  } catch (Exception var24) {
                     PECore.LOGGER.error(LogUtils.FATAL_MARKER, "A fatal error occurred while trying to map the recipe: {}", recipe.m_6423_());
                     throw var24;
                  }
               }
            } while(numHandled <= 0 && !((List)recipes).isEmpty());

            if (recipeCount.containsKey(typeRegistryName)) {
               ((RecipeCountInfo)recipeCount.get(typeRegistryName)).setUnhandled(unhandled);
            } else {
               recipeCount.put(typeRegistryName, new RecipeCountInfo(((List)recipes).size(), unhandled));
            }

            wasHandled = true;
            if (unhandled.isEmpty()) {
               break;
            }

            recipes = unhandled;
            unhandled = new ArrayList();
         }

         if (!wasHandled) {
            canNotMap.add(typeRegistryName);
         }
      }

      PECore.debugLog("CraftingMapper Statistics:");
      var10 = recipeCount.entrySet().iterator();

      while(true) {
         List unhandled;
         do {
            if (!var10.hasNext()) {
               var10 = canNotMap.iterator();

               while(var10.hasNext()) {
                  ResourceLocation typeRegistryName = (ResourceLocation)var10.next();
                  PECore.debugLog("Could not map any Recipes of Type: {}", typeRegistryName);
               }

               NSSFake.resetNamespace();
               return;
            }

            entry = (Map.Entry)var10.next();
            typeRegistryName = (ResourceLocation)entry.getKey();
            RecipeCountInfo countInfo = (RecipeCountInfo)entry.getValue();
            int total = countInfo.getTotalRecipes();
            unhandled = countInfo.getUnhandled();
            PECore.debugLog("Found and handled {} of {} Recipes of Type {}", total - unhandled.size(), total, typeRegistryName);
         } while(unhandled.isEmpty());

         PECore.debugLog("Unhandled Recipes of Type {}:", typeRegistryName);
         Iterator var29 = unhandled.iterator();

         while(var29.hasNext()) {
            Recipe recipe = (Recipe)var29.next();
            PECore.debugLog("Name: {}, Recipe class: {}", recipe.m_6423_(), recipe.getClass().getName());
         }
      }
   }

   public String getName() {
      return "CraftingMapper";
   }

   public String getDescription() {
      return "Add Conversions for Crafting Recipes gathered from net.minecraft.item.crafting.RecipeManager";
   }

   private static class NSSFakeGroupManager implements INSSFakeGroupManager {
      private final Map groups = new HashMap();
      private int fakeIndex;

      public Tuple getOrCreateFakeGroup(Set normalizedSimpleStacks) {
         NormalizedSimpleStack stack = (NormalizedSimpleStack)this.groups.get(normalizedSimpleStacks);
         if (stack == null) {
            stack = NSSFake.create(Integer.toString(this.fakeIndex++));
            this.groups.put(new HashSet(normalizedSimpleStacks), stack);
            return new Tuple(stack, true);
         } else {
            return new Tuple(stack, false);
         }
      }
   }

   private static class RecipeCountInfo {
      private final int totalRecipes;
      private List unhandled;

      private RecipeCountInfo(int totalRecipes, List unhandled) {
         this.totalRecipes = totalRecipes;
         this.unhandled = unhandled;
      }

      public int getTotalRecipes() {
         return this.totalRecipes;
      }

      public void setUnhandled(List unhandled) {
         this.unhandled = unhandled;
      }

      public List getUnhandled() {
         return this.unhandled;
      }
   }
}
