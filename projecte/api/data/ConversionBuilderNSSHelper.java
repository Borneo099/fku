package moze_intel.projecte.api.data;

import javax.annotation.ParametersAreNonnullByDefault;
import moze_intel.projecte.api.nss.NSSFake;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
interface ConversionBuilderNSSHelper {
   ConversionBuilder ingredient(NormalizedSimpleStack var1, int var2);

   default ConversionBuilder ingredient(NormalizedSimpleStack input) {
      return this.ingredient((NormalizedSimpleStack)input, 1);
   }

   default ConversionBuilder ingredient(ItemStack input) {
      return this.ingredient((NormalizedSimpleStack)NSSItem.createItem(input), input.m_41613_());
   }

   default ConversionBuilder ingredient(ItemLike input) {
      return this.ingredient((ItemLike)input, 1);
   }

   default ConversionBuilder ingredient(ItemLike input, int amount) {
      return this.ingredient((NormalizedSimpleStack)NSSItem.createItem(input), amount);
   }

   default ConversionBuilder ingredient(TagKey input) {
      return this.ingredient((TagKey)input, 1);
   }

   default ConversionBuilder ingredient(TagKey input, int amount) {
      return this.ingredient((NormalizedSimpleStack)NSSItem.createTag(input), amount);
   }

   default ConversionBuilder ingredient(FluidStack input) {
      return this.ingredient((NormalizedSimpleStack)NSSFluid.createFluid(input), input.getAmount());
   }

   default ConversionBuilder ingredient(Fluid input) {
      return this.ingredient((Fluid)input, 1);
   }

   default ConversionBuilder ingredient(Fluid input, int amount) {
      return this.ingredient((NormalizedSimpleStack)NSSFluid.createFluid(input), amount);
   }

   default ConversionBuilder ingredientFluid(TagKey input) {
      return this.ingredientFluid(input, 1);
   }

   default ConversionBuilder ingredientFluid(TagKey input, int amount) {
      return this.ingredient((NormalizedSimpleStack)NSSFluid.createTag(input), amount);
   }

   default ConversionBuilder ingredient(String fake) {
      return this.ingredient((String)fake, 1);
   }

   default ConversionBuilder ingredient(String fake, int amount) {
      return this.ingredient(NSSFake.create(fake), amount);
   }
}
