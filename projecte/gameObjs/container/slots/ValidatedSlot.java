package moze_intel.projecte.gameObjs.container.slots;

import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class ValidatedSlot extends InventoryContainerSlot {
   private final Predicate validator;

   public ValidatedSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, Predicate validator) {
      super(itemHandler, index, xPosition, yPosition);
      this.validator = validator;
   }

   public boolean m_5857_(@NotNull ItemStack stack) {
      return super.m_5857_(stack) && this.validator.test(stack);
   }
}
