package moze_intel.projecte.gameObjs.container.slots;

import java.util.function.Predicate;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class SlotGhost extends SlotItemHandler {
   private final Predicate validator;

   public SlotGhost(IItemHandler inv, int slotIndex, int xPos, int yPos, Predicate validator) {
      super(inv, slotIndex, xPos, yPos);
      this.validator = validator;
   }

   public boolean isValid(@NotNull ItemStack stack) {
      return this.validator.test(stack);
   }

   public boolean m_5857_(@NotNull ItemStack stack) {
      if (!stack.m_41619_() && this.isValid(stack)) {
         this.m_5852_(stack);
      }

      return false;
   }

   public void initialize(@NotNull ItemStack stack) {
      super.initialize(ItemHelper.getNormalizedStack(stack));
   }

   public void m_5852_(@NotNull ItemStack stack) {
      super.m_5852_(ItemHelper.getNormalizedStack(stack));
   }

   public boolean m_8010_(Player player) {
      return false;
   }

   public int m_6641_() {
      return 1;
   }

   public int m_5866_(@NotNull ItemStack stack) {
      return 1;
   }
}
