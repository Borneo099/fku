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
interface CustomConversionNSSHelper {
   ConversionBuilder conversion(NormalizedSimpleStack var1, int var2);

   default ConversionBuilder conversion(NormalizedSimpleStack output) {
      return this.conversion((NormalizedSimpleStack)output, 1);
   }

   default ConversionBuilder conversion(ItemStack output) {
      return this.conversion((NormalizedSimpleStack)NSSItem.createItem(output), output.m_41613_());
   }

   default ConversionBuilder conversion(ItemLike output) {
      return this.conversion((ItemLike)output, 1);
   }

   default ConversionBuilder conversion(ItemLike output, int amount) {
      return this.conversion((NormalizedSimpleStack)NSSItem.createItem(output), amount);
   }

   default ConversionBuilder conversion(TagKey output) {
      return this.conversion((TagKey)output, 1);
   }

   default ConversionBuilder conversion(TagKey output, int amount) {
      return this.conversion((NormalizedSimpleStack)NSSItem.createTag(output), amount);
   }

   default ConversionBuilder conversion(FluidStack output) {
      return this.conversion((NormalizedSimpleStack)NSSFluid.createFluid(output), output.getAmount());
   }

   default ConversionBuilder conversion(Fluid output) {
      return this.conversion((Fluid)output, 1);
   }

   default ConversionBuilder conversion(Fluid output, int amount) {
      return this.conversion((NormalizedSimpleStack)NSSFluid.createFluid(output), amount);
   }

   default ConversionBuilder conversionFluid(TagKey output) {
      return this.conversionFluid(output, 1);
   }

   default ConversionBuilder conversionFluid(TagKey output, int amount) {
      return this.conversion((NormalizedSimpleStack)NSSFluid.createTag(output), amount);
   }

   default ConversionBuilder conversion(String fake) {
      return this.conversion((String)fake, 1);
   }

   default ConversionBuilder conversion(String fake, int amount) {
      return this.conversion(NSSFake.create(fake), amount);
   }
}
