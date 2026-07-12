package moze_intel.projecte.gameObjs.block_entities;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

public class WrappedItemHandler implements IItemHandlerModifiable {
   private final IItemHandlerModifiable compose;
   private final WriteMode mode;

   public WrappedItemHandler(IItemHandlerModifiable compose, WriteMode mode) {
      this.compose = compose;
      this.mode = mode;
   }

   public int getSlots() {
      return this.compose.getSlots();
   }

   public @NotNull ItemStack getStackInSlot(int slot) {
      return this.compose.getStackInSlot(slot);
   }

   public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
      return this.mode != WrappedItemHandler.WriteMode.IN && this.mode != WrappedItemHandler.WriteMode.IN_OUT ? stack : this.compose.insertItem(slot, stack, simulate);
   }

   public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
      return this.mode != WrappedItemHandler.WriteMode.OUT && this.mode != WrappedItemHandler.WriteMode.IN_OUT ? ItemStack.f_41583_ : this.compose.extractItem(slot, amount, simulate);
   }

   public int getSlotLimit(int slot) {
      return this.compose.getSlotLimit(slot);
   }

   public boolean isItemValid(int slot, @NotNull ItemStack stack) {
      return this.compose.isItemValid(slot, stack);
   }

   public void setStackInSlot(int slot, @NotNull ItemStack stack) {
      this.compose.setStackInSlot(slot, stack);
   }

   public static enum WriteMode {
      IN,
      OUT,
      IN_OUT,
      NONE;

      // $FF: synthetic method
      private static WriteMode[] $values() {
         return new WriteMode[]{IN, OUT, IN_OUT, NONE};
      }
   }
}
