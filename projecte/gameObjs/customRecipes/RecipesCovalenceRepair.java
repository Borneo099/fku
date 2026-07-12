package moze_intel.projecte.gameObjs.customRecipes;

import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.registries.PERecipeSerializers;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecipesCovalenceRepair extends CustomRecipe {
   public RecipesCovalenceRepair(ResourceLocation id, CraftingBookCategory category) {
      super(id, category);
   }

   @Nullable
   private @Nullable RepairTargetInfo findIngredients(CraftingContainer inv) {
      List dust = new ArrayList();
      ItemStack tool = ItemStack.f_41583_;

      for(int i = 0; i < inv.m_6643_(); ++i) {
         ItemStack input = inv.m_8020_(i);
         if (!input.m_41619_()) {
            if (input.m_204117_(PETags.Items.COVALENCE_DUST)) {
               dust.add(input);
            } else {
               if (!tool.m_41619_() || !ItemHelper.isRepairableDamagedItem(input)) {
                  return null;
               }

               tool = input;
            }
         }
      }

      if (!tool.m_41619_() && !dust.isEmpty()) {
         return new RepairTargetInfo(tool, dust.stream().mapToLong(EMCHelper::getEmcValue).sum());
      } else {
         return null;
      }
   }

   public boolean matches(@NotNull CraftingContainer inv, @NotNull Level level) {
      RepairTargetInfo targetInfo = this.findIngredients(inv);
      return targetInfo != null && targetInfo.emcPerDurability <= targetInfo.dustEmc;
   }

   public @NotNull ItemStack assemble(@NotNull CraftingContainer inv, @NotNull RegistryAccess registryAccess) {
      RepairTargetInfo targetInfo = this.findIngredients(inv);
      if (targetInfo == null) {
         return ItemStack.f_41583_;
      } else {
         ItemStack output = targetInfo.tool.m_41777_();
         output.m_41721_((int)Math.max((long)output.m_41773_() - targetInfo.dustEmc / targetInfo.emcPerDurability, 0L));
         return output;
      }
   }

   public boolean m_8004_(int width, int height) {
      return width > 1 || height > 1;
   }

   public @NotNull RecipeSerializer m_7707_() {
      return (RecipeSerializer)PERecipeSerializers.COVALENCE_REPAIR.get();
   }

   private static class RepairTargetInfo {
      private final ItemStack tool;
      private final long emcPerDurability;
      private final long dustEmc;

      public RepairTargetInfo(ItemStack tool, long dustEmc) {
         this.tool = tool;
         this.dustEmc = dustEmc;
         this.emcPerDurability = EMCHelper.getEMCPerDurability(tool);
      }
   }
}
