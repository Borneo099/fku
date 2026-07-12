package moze_intel.projecte.gameObjs.items;

import moze_intel.projecte.gameObjs.EnumFuelType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlchemicalFuel extends ItemPE {
   private final @NotNull EnumFuelType fuelType;

   public AlchemicalFuel(Item.Properties props, @NotNull EnumFuelType type) {
      super(props);
      this.fuelType = type;
   }

   public int getBurnTime(ItemStack stack, @Nullable RecipeType recipeType) {
      return this.fuelType.getBurnTime();
   }
}
